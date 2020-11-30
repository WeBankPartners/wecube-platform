package com.webank.wecube.platform.core.dto.plugin;

public class PluginPackageResourceFileDto {
    private String id;

//    private PluginPackage pluginPackage;

    private String packageName;

    private String packageVersion;

    private String source;

    private String relatedPath;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

//    public PluginPackage getPluginPackage() {
//        return pluginPackage;
//    }
//
//    public void setPluginPackage(PluginPackage pluginPackage) {
//        this.pluginPackage = pluginPackage;
//    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageVersion() {
        return packageVersion;
    }

    public void setPackageVersion(String packageVersion) {
        this.packageVersion = packageVersion;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getRelatedPath() {
        return relatedPath;
    }

    public void setRelatedPath(String relatedPath) {
        this.relatedPath = relatedPath;
    }

}
