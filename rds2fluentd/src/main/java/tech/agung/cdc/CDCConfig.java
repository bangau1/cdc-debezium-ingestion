package tech.agung.cdc;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

public class CDCConfig {
    @NotEmpty
    @JsonProperty
    protected String id; //a unique identifier

    @NotEmpty
    @JsonProperty
    protected String dbHost; //host can be changed during its lifetime

    @Min(1)
    @Max(65535)
    @JsonProperty
    protected int dbPort = 5432;

    @NotEmpty
    @JsonProperty
    protected String dbName;

    @NotEmpty
    @JsonProperty
    protected String dbUser; //todo: we need to move this to another configuration file

    @NotEmpty
    @JsonProperty
    protected String dbPassword; //todo: we need to move this to another configuration file

    @Valid
    @JsonProperty
    protected boolean readInitialSnapshot; //todo: we need to move this to another configuration file
}
