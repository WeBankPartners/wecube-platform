package com.webank.wecube.platform.core.support.plugin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PluginReleasedPackageListDirsRequestParameter {
    @JsonProperty(value = "endpoint")
    private String endpoint;
    @JsonProperty(value = "line_number")
    private Integer lineNumber;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

}
