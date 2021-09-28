package com.webank.wecube.platform.core.dto.workflow;

public class UserScheduledTaskProcessInstanceDto {

    private int procInstId;
    private String procDefId;
    private String procDefName;
    private String status;
    private String execTime;
    public int getProcInstId() {
        return procInstId;
    }
    public void setProcInstId(int procInstId) {
        this.procInstId = procInstId;
    }
    public String getProcDefId() {
        return procDefId;
    }
    public void setProcDefId(String procDefId) {
        this.procDefId = procDefId;
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
    public String getExecTime() {
        return execTime;
    }
    public void setExecTime(String execTime) {
        this.execTime = execTime;
    }
    
    
}
