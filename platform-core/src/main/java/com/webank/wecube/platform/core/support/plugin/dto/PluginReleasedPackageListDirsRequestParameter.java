package com.webank.wecube.platform.core.support.plugin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PluginReleasedPackageListDirsRequestParameter {
    @JsonProperty(value="endpoint")
    private String endpoint;
    @JsonProperty(value="line_number")
    private Integer lineNumber;
}
