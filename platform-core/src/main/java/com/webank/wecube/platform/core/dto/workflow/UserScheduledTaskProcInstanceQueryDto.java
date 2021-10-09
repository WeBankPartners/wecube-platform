package com.webank.wecube.platform.core.dto.workflow;

public class UserScheduledTaskProcInstanceQueryDto {
    private String startTime;
    private String endTime;

    private String procDefName;
    private String entityDataId;
    private String entityDataName;

    private String owner;
    private String procInstanceStatus;
    private String userTaskId;
    

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getProcDefName() {
        return procDefName;
    }

    public void setProcDefName(String procDefName) {
        this.procDefName = procDefName;
    }

    public String getEntityDataId() {
        return entityDataId;
    }

    public void setEntityDataId(String entityDataId) {
        this.entityDataId = entityDataId;
    }

    public String getEntityDataName() {
        return entityDataName;
    }

    public void setEntityDataName(String entityDataName) {
        this.entityDataName = entityDataName;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getProcInstanceStatus() {
        return procInstanceStatus;
    }

    public void setProcInstanceStatus(String procInstanceStatus) {
        this.procInstanceStatus = procInstanceStatus;
    }

    public String getUserTaskId() {
        return userTaskId;
    }

    public void setUserTaskId(String userTaskId) {
        this.userTaskId = userTaskId;
    }

}
