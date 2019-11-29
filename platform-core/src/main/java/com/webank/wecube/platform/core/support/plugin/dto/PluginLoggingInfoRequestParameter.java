package com.webank.wecube.platform.core.support.plugin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PluginLoggingInfoRequestParameter {
    @JsonProperty(value="key_word")
    private String keyWord;
//    @JsonProperty(value="line_number")
//    private Integer lineNumber;
}
