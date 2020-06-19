package com.webank.wecube.platform.core.service.plugin;

public class PluginRegistryInfo {

    private String pluginPackageName;
    private String pluginPackageVersion;
    private String pluginPackageData;

    public String getPluginPackageName() {
        return pluginPackageName;
    }

    public void setPluginPackageName(String pluginPackageName) {
        this.pluginPackageName = pluginPackageName;
    }

    public String getPluginPackageVersion() {
        return pluginPackageVersion;
    }

    public void setPluginPackageVersion(String pluginPackageVersion) {
        this.pluginPackageVersion = pluginPackageVersion;
    }

    public String getPluginPackageData() {
        return pluginPackageData;
    }

    public void setPluginPackageData(String pluginPackageData) {
        this.pluginPackageData = pluginPackageData;
    }

}
