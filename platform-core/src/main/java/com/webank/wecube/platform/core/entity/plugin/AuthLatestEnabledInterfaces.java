package com.webank.wecube.platform.core.entity.plugin;

import static com.webank.wecube.platform.core.utils.Constants.LEFT_BRACKET_STRING;
import static com.webank.wecube.platform.core.utils.Constants.RIGHT_BRACKET_STRING;
import static com.webank.wecube.platform.core.utils.Constants.SEPARATOR_OF_NAMES;

import java.util.Date;

public class AuthLatestEnabledInterfaces {

    private String id;

    private String pluginConfigId;

    private String action;

    private String serviceName;

    private String serviceDisplayName;

    private String path;

    private String httpMethod;

    private String isAsyncProcessing;

    private String type;

    private String filterRule;

    private String pluginConfigName;
    private String pluginConfigRegisterName;
    private String pluginConfigTargetEntity;
    private String pluginConfigStatus;
    private String pluginPackageId;
    private String pluginPackageName;
    private String pluginPackageStatus;
    private String pluginPackageVersion;

    private Date uploadTimestamp;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPluginConfigId() {
        return pluginConfigId;
    }

    public void setPluginConfigId(String pluginConfigId) {
        this.pluginConfigId = pluginConfigId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceDisplayName() {
        return serviceDisplayName;
    }

    public void setServiceDisplayName(String serviceDisplayName) {
        this.serviceDisplayName = serviceDisplayName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getIsAsyncProcessing() {
        return isAsyncProcessing;
    }

    public void setIsAsyncProcessing(String isAsyncProcessing) {
        this.isAsyncProcessing = isAsyncProcessing;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFilterRule() {
        return filterRule;
    }

    public void setFilterRule(String filterRule) {
        this.filterRule = filterRule;
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

    public Date getUploadTimestamp() {
        return uploadTimestamp;
    }

    public void setUploadTimestamp(Date uploadTimestamp) {
        this.uploadTimestamp = uploadTimestamp;
    }

    public String getPluginConfigName() {
        return pluginConfigName;
    }

    public void setPluginConfigName(String pluginConfigName) {
        this.pluginConfigName = pluginConfigName;
    }

    public String getPluginConfigRegisterName() {
        return pluginConfigRegisterName;
    }

    public void setPluginConfigRegisterName(String pluginConfigRegisterName) {
        this.pluginConfigRegisterName = pluginConfigRegisterName;
    }

    public String getPluginConfigTargetEntity() {
        return pluginConfigTargetEntity;
    }

    public void setPluginConfigTargetEntity(String pluginConfigTargetEntity) {
        this.pluginConfigTargetEntity = pluginConfigTargetEntity;
    }

    public String getPluginPackageName() {
        return pluginPackageName;
    }

    public void setPluginPackageName(String pluginPackageName) {
        this.pluginPackageName = pluginPackageName;
    }
    
    public String generateServiceName() {
        return pluginPackageName + SEPARATOR_OF_NAMES + pluginConfigName
                + (pluginConfigRegisterName != null
                        ? LEFT_BRACKET_STRING + pluginConfigRegisterName + RIGHT_BRACKET_STRING : "")
                + SEPARATOR_OF_NAMES + action;
    }

}
