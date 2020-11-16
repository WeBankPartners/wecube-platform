package com.webank.wecube.platform.core.entity.plugin;

public class RichPluginConfigInterfaces extends PluginConfigInterfaces {
    private String pluginConfigId;
    private String pluginConfigStatus;
    private String pluginPackageId;
    private String pluginPackageStatus;
    private String pluginPackageVersion;

    public String getPluginConfigId() {
        return pluginConfigId;
    }

    public void setPluginConfigId(String pluginConfigId) {
        this.pluginConfigId = pluginConfigId;
    }

    public String getPluginConfigStatus() {
        return pluginConfigStatus;
    }

    public void setPluginConfigStatus(String pluginConfigStatus) {
        this.pluginConfigStatus = pluginConfigStatus;
    }

    public String getPluginPackageId() {
        return pluginPackageId;
    }

    public void setPluginPackageId(String pluginPackageId) {
        this.pluginPackageId = pluginPackageId;
    }

    public String getPluginPackageStatus() {
        return pluginPackageStatus;
    }

    public void setPluginPackageStatus(String pluginPackageStatus) {
        this.pluginPackageStatus = pluginPackageStatus;
    }

    public String getPluginPackageVersion() {
        return pluginPackageVersion;
    }

    public void setPluginPackageVersion(String pluginPackageVersion) {
        this.pluginPackageVersion = pluginPackageVersion;
    }

}