package com.webank.wecube.platform.core.dto.plugin;

public class PluginPackageInfoDto {
    private String id;

    private String name;

    private String version;

    private String uploadTimestamp;

    private boolean uiPackageIncluded;

    private String status;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUploadTimestamp() {
        return uploadTimestamp;
    }

    public void setUploadTimestamp(String uploadTimestamp) {
        this.uploadTimestamp = uploadTimestamp;
    }

    public boolean isUiPackageIncluded() {
        return uiPackageIncluded;
    }

    public void setUiPackageIncluded(boolean uiPackageIncluded) {
        this.uiPackageIncluded = uiPackageIncluded;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
