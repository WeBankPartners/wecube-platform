package com.webank.wecube.platform.core.entity.plugin;

public class PluginPackageRuntimeResourcesS3 {
    private String id;

    private String pluginPackageId;

    private String bucketName;

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

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName == null ? null : bucketName.trim();
    }
}