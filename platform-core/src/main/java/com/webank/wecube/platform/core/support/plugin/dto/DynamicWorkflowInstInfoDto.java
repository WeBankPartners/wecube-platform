package com.webank.wecube.platform.core.support.plugin.dto;

public class DynamicWorkflowInstInfoDto {
    private int id;
    private String procInstKey;
    private String procDefId;
    private String procDefKey;
    private String status;
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getProcInstKey() {
        return procInstKey;
    }
    public void setProcInstKey(String procInstKey) {
        this.procInstKey = procInstKey;
    }
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
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    
    

}
