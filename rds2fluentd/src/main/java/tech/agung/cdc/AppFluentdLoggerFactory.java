package tech.agung.cdc;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Environment;
import org.fluentd.logger.FluentLogger;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

public class AppFluentdLoggerFactory {
    @NotEmpty
    @JsonProperty
    private String tag;

    @NotEmpty
    @JsonProperty
    private String host;

    @Min(1)
    @Max(65535)
    @JsonProperty
    private int port = 5672;

    public FluentLogger build(Environment environment) {
        FluentLogger logger = FluentLogger.getLogger(this.tag, this.host, this.port);
        environment.lifecycle().manage(new Managed() {
            @Override
            public void start() {
            }

            @Override
            public void stop() {
                logger.close();
            }
        });
        return logger;
    }
}
