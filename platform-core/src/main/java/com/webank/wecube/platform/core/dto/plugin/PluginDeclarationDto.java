package com.webank.wecube.platform.core.dto.plugin;

import java.util.ArrayList;
import java.util.List;

public class PluginDeclarationDto {
    private String id;
    private String pluginPackageId;
    private String name;
    private String targetEntityWithFilterRule;
    private String registerName;
    private String status;

    private List<PluginConfigOutlineDto> pluginConfigs = new ArrayList<>();

    public String getPluginPackageId() {
        return pluginPackageId;
    }

    public void setPluginPackageId(String pluginPackageId) {
        this.pluginPackageId = pluginPackageId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTargetEntityWithFilterRule() {
        return targetEntityWithFilterRule;
    }

    public void setTargetEntityWithFilterRule(String targetEntityWithFilterRule) {
        this.targetEntityWithFilterRule = targetEntityWithFilterRule;
    }

    public String getRegisterName() {
        return registerName;
    }

    public void setRegisterName(String registerName) {
        this.registerName = registerName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<PluginConfigOutlineDto> getPluginConfigs() {
        return pluginConfigs;
    }

    public void setPluginConfigs(List<PluginConfigOutlineDto> pluginConfigs) {
        this.pluginConfigs = pluginConfigs;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


}
