package com.webank.wecube.platform.core.dto.plugin;

public class PluginPackageRuntimeResourcesS3Dto {
    private String id;

    private String pluginPackageId;

    private String bucketName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPluginPackageId() {
        return pluginPackageId;
    }

    public void setPluginPackageId(String pluginPackageId) {
        this.pluginPackageId = pluginPackageId;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

}
