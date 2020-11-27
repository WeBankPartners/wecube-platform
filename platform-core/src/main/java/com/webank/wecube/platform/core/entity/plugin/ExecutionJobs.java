package com.webank.wecube.platform.core.entity.plugin;

import java.util.Date;

public class ExecutionJobs {
    private Integer id;

    private String batchExecutionJobId;

    private String packageName;

    private String entityName;

    private String businessKey;

    private String rootEntityId;

    private Date executeTime;

    private Date completeTime;

    private String errorCode;

    private String pluginConfigInterfaceId;

    private String errorMessage;

    private String returnJson;

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage == null ? null : errorMessage.trim();
    }

    public String getReturnJson() {
        return returnJson;
    }

    public void setReturnJson(String returnJson) {
        this.returnJson = returnJson == null ? null : returnJson.trim();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getBatchExecutionJobId() {
        return batchExecutionJobId;
    }

    public void setBatchExecutionJobId(String batchExecutionJobId) {
        this.batchExecutionJobId = batchExecutionJobId == null ? null : batchExecutionJobId.trim();
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName == null ? null : packageName.trim();
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName == null ? null : entityName.trim();
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public void setBusinessKey(String businessKey) {
        this.businessKey = businessKey == null ? null : businessKey.trim();
    }

    public String getRootEntityId() {
        return rootEntityId;
    }

    public void setRootEntityId(String rootEntityId) {
        this.rootEntityId = rootEntityId == null ? null : rootEntityId.trim();
    }

    public Date getExecuteTime() {
        return executeTime;
    }

    public void setExecuteTime(Date executeTime) {
        this.executeTime = executeTime;
    }

    public Date getCompleteTime() {
        return completeTime;
    }

    public void setCompleteTime(Date completeTime) {
        this.completeTime = completeTime;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode == null ? null : errorCode.trim();
    }

    public String getPluginConfigInterfaceId() {
        return pluginConfigInterfaceId;
    }

    public void setPluginConfigInterfaceId(String pluginConfigInterfaceId) {
        this.pluginConfigInterfaceId = pluginConfigInterfaceId == null ? null : pluginConfigInterfaceId.trim();
    }
}