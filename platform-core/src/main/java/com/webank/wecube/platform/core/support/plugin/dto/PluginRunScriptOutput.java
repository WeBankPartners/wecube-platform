package com.webank.wecube.platform.core.support.plugin.dto;

import lombok.Data;

@Data
public class PluginRunScriptOutput {
    private Integer retCode;
    private String  detail;
    private String target;
}
