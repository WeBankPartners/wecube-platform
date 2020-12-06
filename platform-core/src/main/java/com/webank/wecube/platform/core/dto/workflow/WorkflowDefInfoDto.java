package com.webank.wecube.platform.core.dto.workflow;

public class WorkflowDefInfoDto {
    private String procDefId;
    private String procDefKey;
    private String procDefName;
    private String status;

    private RegisteredEntityDefDto rootEntity;

    private String createdTime;

    public String getProcDefId() {
        return procDefId;
    }

    public void setProcDefId(String procDefId) {
        this.procDefId = procDefId;
    }

    public String getProcDefKey() {
        return procDefKey;
    }

    public void setProcDefKey(String procDefKey) {
        this.procDefKey = procDefKey;
    }

    public String getProcDefName() {
        return procDefName;
    }

    public void setProcDefName(String procDefName) {
        this.procDefName = procDefName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public RegisteredEntityDefDto getRootEntity() {
        return rootEntity;
    }

    public void setRootEntity(RegisteredEntityDefDto rootEntity) {
        this.rootEntity = rootEntity;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }
    
    

}
