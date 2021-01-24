package com.webank.wecube.platform.core.entity.workflow;

import java.util.Date;

public class ExternalTaskEntity {
    public static final String STATUS_NEW = "New";
    public static final String STATUS_PREPROCESS = "Preprocess";
    public static final String STATUS_IN_PROGRESS = "InProgress";
    public static final String STATUS_COMPLETED = "Completed";
    public static final String STATUS_FAULTED = "Faulted";
    public static final String STATUS_FAILED = "Failed";
    
    public static final String TASK_TYPE_DYNAMIC_BIND_TASK_NODE_RETRY = "dynamicBindRetry";
    
    private Long id;

    private Date endTime;

    private String eventSeqNo;

    private String taskType;
    
    private String status;
    
    private int rev = 0;
    
    private Date startTime;
    
    private Date createdTime;

    private Date updatedTime;
    
    private String taskDescription;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getEventSeqNo() {
        return eventSeqNo;
    }

    public void setEventSeqNo(String eventSeqNo) {
        this.eventSeqNo = eventSeqNo;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getRev() {
        return rev;
    }

    public void setRev(int rev) {
        this.rev = rev;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }
}
