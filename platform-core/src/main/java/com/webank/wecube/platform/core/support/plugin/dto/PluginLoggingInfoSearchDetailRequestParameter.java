package com.webank.wecube.platform.core.support.plugin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PluginLoggingInfoSearchDetailRequestParameter {
    @JsonProperty(value = "file_name")
    private String fileName;
    @JsonProperty(value = "line_number")
    private String lineNumber;
    @JsonProperty(value = "relate_line_count")
    private Integer relateLineNumber;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(String lineNumber) {
        this.lineNumber = lineNumber;
    }

    public Integer getRelateLineNumber() {
        return relateLineNumber;
    }

    public void setRelateLineNumber(Integer relateLineNumber) {
        this.relateLineNumber = relateLineNumber;
    }

}
