package com.webank.wecube.platform.core.dto.workflow;

public abstract class BaseProcDefDto {
    private String procDefId;
    private String procDefKey;
    private String procDefName;
    private String procDefVersion;
    private String status;

    private String procDefData;
    private String rootEntity;

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

    public String getProcDefVersion() {
        return procDefVersion;
    }

    public void setProcDefVersion(String procDefVersion) {
        this.procDefVersion = procDefVersion;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProcDefData() {
        return procDefData;
    }

    public void setProcDefData(String procDefData) {
        this.procDefData = procDefData;
    }

    public String getRootEntity() {
        return rootEntity;
    }

    public void setRootEntity(String rootEntity) {
        this.rootEntity = rootEntity;
    }

}
