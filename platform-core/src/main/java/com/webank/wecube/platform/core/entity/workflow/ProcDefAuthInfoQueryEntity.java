package com.webank.wecube.platform.core.entity.workflow;

public class ProcDefAuthInfoQueryEntity {
    private String id;
    private String procDefKey;
    private String procDefName;
    private String rootEntity;
    private String status;
    private Integer procDefVersion;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getRootEntity() {
        return rootEntity;
    }

    public void setRootEntity(String rootEntity) {
        this.rootEntity = rootEntity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getProcDefVersion() {
        return procDefVersion;
    }

    public void setProcDefVersion(Integer procDefVersion) {
        this.procDefVersion = procDefVersion;
    }

}
