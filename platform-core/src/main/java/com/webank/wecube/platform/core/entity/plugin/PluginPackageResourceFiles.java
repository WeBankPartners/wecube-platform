package com.webank.wecube.platform.core.entity.plugin;

public class PluginPackageResourceFiles {
    private String id;

    private String pluginPackageId;

    private String packageName;

    private String packageVersion;

    private String source;

    private String relatedPath;

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

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName == null ? null : packageName.trim();
    }

    public String getPackageVersion() {
        return packageVersion;
    }

    public void setPackageVersion(String packageVersion) {
        this.packageVersion = packageVersion == null ? null : packageVersion.trim();
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source == null ? null : source.trim();
    }

    public String getRelatedPath() {
        return relatedPath;
    }

    public void setRelatedPath(String relatedPath) {
        this.relatedPath = relatedPath == null ? null : relatedPath.trim();
    }
}