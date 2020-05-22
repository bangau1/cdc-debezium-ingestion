package tech.agung.cdc;

import io.debezium.embedded.EmbeddedEngine;
import io.debezium.config.Configuration;
import io.dropwizard.lifecycle.Managed;
import org.apache.kafka.connect.source.SourceRecord;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.data.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.fluentd.logger.FluentLogger;

import static io.debezium.data.Envelope.FieldName.*;
import static java.util.stream.Collectors.toMap;

public class FluentdProducer implements Managed {
  private static final Logger LOGGER = LoggerFactory.getLogger(FluentdProducer.class);
  private final EmbeddedEngine engine;
  private final ExecutorService executor = Executors.newSingleThreadExecutor();
  private final FluentLogger fluentLogger;
  private final boolean shouldReadInitialSnapshot;
  private final String id;

  public FluentdProducer(CDCConfig cdcConfig, FluentLogger fluentLogger) {
    Configuration.Builder builder = Configuration.create();
    this.id = cdcConfig.id;
    Configuration config = builder
            .with("connector.class", "io.debezium.connector.postgresql.PostgresConnector")
            .with("offset.storage",  "org.apache.kafka.connect.storage.FileOffsetBackingStore")
            .with("offset.storage.file.filename", "/etc/rds2fluentd/"+id+".dat")
            .with("offset.flush.interval.ms", 1000)
            .with("plugin.name", "decoderbufs")
            .with("slot.name", id)
            .with("name", id+"-connector")
            .with("database.server.name", id)
            .with("database.hostname", cdcConfig.dbHost)
            .with("database.port", cdcConfig.dbPort)
            .with("database.user", cdcConfig.dbUser)
            .with("database.password", cdcConfig.dbPassword)
            .with("database.dbname", cdcConfig.dbName)
            .build();
    this.fluentLogger = fluentLogger;
    this.shouldReadInitialSnapshot = cdcConfig.readInitialSnapshot;

    this.engine = EmbeddedEngine
            .create()
            .using(config)
            .notifying(this::handleEvent).build();
  }

  @Override
  public void start(){
    executor.execute(engine);
  }

  @Override
  public void stop(){
    if (this.engine != null){
      this.engine.stop();
    }
  }

  private void handleEvent(SourceRecord sourceRecord){
    Struct sourceRecordValue = (Struct) sourceRecord.value();
    //the struct schema example:
    // Struct{
    //  after=Struct{id=345,address=XXX, TX,email=jack@gmail.com,name=Agung}, --> the data being update/insert. before=struct{} in the case of data deletion
    //  source=Struct{version=1.1.1.Final,connector=postgresql,name=localhost-studentdb,ts_ms=1590160305974,db=studentdb,schema=public,table=student,txId=580,lsn=23753032}, --> the source
    //  op=c,
    //  ts_ms=1590160306414
    // }
    if(sourceRecordValue != null) {
      Operation operation = Operation.forCode((String) sourceRecordValue.get(OPERATION));

      //Only if this is a transactional operation.
      if(shouldReadInitialSnapshot || operation != Operation.READ) {

        Map<String, Object> message;
        String record = AFTER; //For Update & Insert operations.

        if (operation == Operation.DELETE) {
          record = BEFORE; //For Delete operations.
        }

        //Build a map with all row data received.
        Struct struct = (Struct) sourceRecordValue.get(record);
        message = struct.schema().fields().stream()
                .map(Field::name)
                .filter(fieldName -> struct.get(fieldName) != null)
                .map(fieldName -> Pair.of(fieldName, struct.get(fieldName)))
                .collect(toMap(Pair::getKey, Pair::getValue));

        Struct source = (Struct)sourceRecordValue.get(SOURCE);
        String db = source.getString("db");
        String schema = source.getString("schema");
        String table = source.getString("table");

        //Call the service to handle the data change.
        if (operation != Operation.DELETE){
            //send to fluentd
            Map<String, Object> data = new HashMap<>(message);
            data.put("_db", db);
            data.put("_schema", schema);
            data.put("_table", table);
            this.fluentLogger.log("", data);
        }
        LOGGER.debug("ID: {} Schema_DB_Table: {}_{}_{} Data Changed: {} with Operation: {}", this.id, schema, db, table, message, operation.name());
      }
    }
  }
}
