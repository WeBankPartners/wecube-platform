package com.webank.wecube.platform.core.dto;

import com.webank.wecube.platform.core.domain.plugin.PluginConfig;

import java.util.ArrayList;
import java.util.List;

public class PluginDeclarationDto {
    private String pluginPackageId;
    private String name;
    private String targetEntityWithFilterRule;
    private String registerName;
    private String status;

    private List<PluginConfigDto> pluginConfigs = new ArrayList<PluginConfigDto>();

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

    public List<PluginConfigDto> getPluginConfigs() {
        return pluginConfigs;
    }

    public void setPluginConfigs(List<PluginConfigDto> pluginConfigs) {
        this.pluginConfigs = pluginConfigs;
    }

    public void addPluginConfig(PluginConfigDto inputPluginConfig) {
        if (inputPluginConfig == null) {
            return;
        }

        if (this.pluginConfigs == null) {
            this.pluginConfigs = new ArrayList<PluginConfigDto>();
        }

        this.pluginConfigs.add(inputPluginConfig);
    }

    public static PluginDeclarationDto fromDomain(PluginConfig pluginConfig) {
        PluginDeclarationDto pluginDeclarationDto = new PluginDeclarationDto();
        pluginDeclarationDto.setPluginPackageId(pluginConfig.getPluginPackage().getId());
        pluginDeclarationDto.setName(pluginConfig.getName());
        pluginDeclarationDto.setTargetEntityWithFilterRule(pluginConfig.getTargetEntityWithFilterRule());
        pluginDeclarationDto.setRegisterName(pluginConfig.getRegisterName());
        pluginDeclarationDto.setStatus(pluginConfig.getStatus().name());
        return pluginDeclarationDto;
    }


}
