package com.webank.wecube.platform.core.dto.plugin;

public class UploadPackageResultDto {
    private String id;

    private String name;

    private String version;

    private String status;

    private String uploadTimestamp;

    private Boolean uiPackageIncluded;

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUploadTimestamp() {
        return uploadTimestamp;
    }

    public void setUploadTimestamp(String uploadTimestamp) {
        this.uploadTimestamp = uploadTimestamp;
    }

    public Boolean getUiPackageIncluded() {
        return uiPackageIncluded;
    }

    public void setUiPackageIncluded(Boolean uiPackageIncluded) {
        this.uiPackageIncluded = uiPackageIncluded;
    }

}
