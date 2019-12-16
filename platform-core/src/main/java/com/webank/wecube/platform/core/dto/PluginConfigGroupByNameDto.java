package com.webank.wecube.platform.core.dto;

import java.util.List;

public class PluginConfigGroupByNameDto {
    private String pluginConfigName;
    private List<PluginConfigDto> pluginConfigDtoList;

    public String getPluginConfigName() {
        return pluginConfigName;
    }

    public void setPluginConfigName(String pluginConfigName) {
        this.pluginConfigName = pluginConfigName;
    }

    public List<PluginConfigDto> getPluginConfigDtoList() {
        return pluginConfigDtoList;
    }

    public void setPluginConfigDtoList(List<PluginConfigDto> pluginConfigDtoList) {
        this.pluginConfigDtoList = pluginConfigDtoList;
    }

    public PluginConfigGroupByNameDto(String pluginConfigName, List<PluginConfigDto> pluginConfigDtoList) {
        super();
        this.pluginConfigName = pluginConfigName;
        this.pluginConfigDtoList = pluginConfigDtoList;
    }

}
