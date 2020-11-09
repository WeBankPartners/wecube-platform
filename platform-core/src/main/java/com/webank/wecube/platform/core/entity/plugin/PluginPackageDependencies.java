package com.webank.wecube.platform.core.entity.plugin;

public class PluginPackageDependencies {
    private String id;

    private String pluginPackageId;

    private String dependencyPackageName;

    private String dependencyPackageVersion;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getPluginPackageId() {
        return pluginPackageId;
    }

    public void setPluginPackageId(String pluginPackageId) {
        this.pluginPackageId = pluginPackageId == null ? null : pluginPackageId.trim();
    }

    public String getDependencyPackageName() {
        return dependencyPackageName;
    }

    public void setDependencyPackageName(String dependencyPackageName) {
        this.dependencyPackageName = dependencyPackageName == null ? null : dependencyPackageName.trim();
    }

    public String getDependencyPackageVersion() {
        return dependencyPackageVersion;
    }

    public void setDependencyPackageVersion(String dependencyPackageVersion) {
        this.dependencyPackageVersion = dependencyPackageVersion == null ? null : dependencyPackageVersion.trim();
    }
}