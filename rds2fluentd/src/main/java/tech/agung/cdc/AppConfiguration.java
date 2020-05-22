package tech.agung.cdc;

import io.dropwizard.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

public class AppConfiguration extends Configuration {
    @Valid
    @NotNull
    @JsonProperty
    private AppFluentdLoggerFactory fluentd;

    @Valid
    @NotNull
    @JsonProperty
    private List<CDCConfig> ingestionList;

    public AppFluentdLoggerFactory getFluentd(){
        return this.fluentd;
    }

    public List<CDCConfig> getIngestionList(){
        return this.ingestionList;
    }
}
