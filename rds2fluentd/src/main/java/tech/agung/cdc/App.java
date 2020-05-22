package tech.agung.cdc;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import org.fluentd.logger.FluentLogger;

/**
 * App
 */
public class App extends Application<AppConfiguration>{
    public static void main(String[] args) throws Exception {
        new App().run(args);
    }

    @Override
    public String getName() {
        return "postgres-cdc";
    }

    @Override
    public void run(AppConfiguration appConfiguration, Environment environment) throws Exception {
        FluentLogger fluentLogger = appConfiguration.getFluentd().build(environment);

        for (CDCConfig config: appConfiguration.getIngestionList()){
            FluentdProducer fluentdProducer = new FluentdProducer(config, fluentLogger);
            environment.lifecycle().manage(fluentdProducer);
        }

    }
}
