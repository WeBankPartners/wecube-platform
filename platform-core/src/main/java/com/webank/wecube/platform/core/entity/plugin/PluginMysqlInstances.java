package com.webank.wecube.platform.core.entity.plugin;

import java.util.Date;

public class PluginMysqlInstances {
    private String id;

    private String password;

    private String plugunPackageId;

    private String resourceItemId;

    private String schemaName;

    private String status;

    private String username;

    private Date createdTime;

    private String preVersion;

    private Date updatedTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password == null ? null : password.trim();
    }

    public String getPlugunPackageId() {
        return plugunPackageId;
    }

    public void setPlugunPackageId(String plugunPackageId) {
        this.plugunPackageId = plugunPackageId == null ? null : plugunPackageId.trim();
    }

    public String getResourceItemId() {
        return resourceItemId;
    }

    public void setResourceItemId(String resourceItemId) {
        this.resourceItemId = resourceItemId == null ? null : resourceItemId.trim();
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName == null ? null : schemaName.trim();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username == null ? null : username.trim();
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public String getPreVersion() {
        return preVersion;
    }

    public void setPreVersion(String preVersion) {
        this.preVersion = preVersion == null ? null : preVersion.trim();
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }
}