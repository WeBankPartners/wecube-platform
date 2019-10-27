package com.webank.wecube.platform.core.dto.workflow;

public class ProcInstOutlineDto {
    private Integer id;
    private String procInstKey;
    private String procInstName;
    private String createdTime;
    private String operator;
    private String status;
    private String procDefId;
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public String getProcInstKey() {
        return procInstKey;
    }
    public void setProcInstKey(String procInstKey) {
        this.procInstKey = procInstKey;
    }
    public String getProcInstName() {
        return procInstName;
    }
    public void setProcInstName(String procInstName) {
        this.procInstName = procInstName;
    }
    public String getCreatedTime() {
        return createdTime;
    }
    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }
    public String getOperator() {
        return operator;
    }
    public void setOperator(String operator) {
        this.operator = operator;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getProcDefId() {
        return procDefId;
    }
    public void setProcDefId(String procDefId) {
        this.procDefId = procDefId;
    }
    
    
}
