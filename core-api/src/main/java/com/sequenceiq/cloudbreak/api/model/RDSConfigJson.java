package com.sequenceiq.cloudbreak.api.model;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sequenceiq.cloudbreak.doc.ModelDescriptions;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("RDSConfig")
@JsonIgnoreProperties(ignoreUnknown = true)
public class RDSConfigJson {

    @NotNull
    @ApiModelProperty(value = ModelDescriptions.RDSConfig.CONNECTION_URL, required = true)
    private String connectionURL;
    @NotNull
    @ApiModelProperty(value = ModelDescriptions.RDSConfig.DB_TYPE, required = true)
    private RDSDatabase databaseType;
    @NotNull
    @ApiModelProperty(value = ModelDescriptions.RDSConfig.USERNAME, required = true)
    private String connectionUserName;
    @NotNull
    @ApiModelProperty(value = ModelDescriptions.RDSConfig.PASSWORD, required = true)
    private String connectionPassword;

    public String getConnectionURL() {
        return connectionURL;
    }

    public void setConnectionURL(String connectionURL) {
        this.connectionURL = connectionURL;
    }

    public RDSDatabase getDatabaseType() {
        return databaseType;
    }

    public void setDatabaseType(RDSDatabase databaseType) {
        this.databaseType = databaseType;
    }

    public String getConnectionUserName() {
        return connectionUserName;
    }

    public void setConnectionUserName(String connectionUserName) {
        this.connectionUserName = connectionUserName;
    }

    public String getConnectionPassword() {
        return connectionPassword;
    }

    public void setConnectionPassword(String connectionPassword) {
        this.connectionPassword = connectionPassword;
    }
}
