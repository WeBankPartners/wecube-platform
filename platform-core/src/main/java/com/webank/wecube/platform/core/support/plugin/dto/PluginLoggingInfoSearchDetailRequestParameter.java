package com.webank.wecube.platform.core.support.plugin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PluginLoggingInfoSearchDetailRequestParameter {
    @JsonProperty(value = "file_name")
    private String fileName;
    @JsonProperty(value = "line_number")
    private String lineNumber;
    @JsonProperty(value = "relate_line_count")
    private Integer relateLineNumber;
}
