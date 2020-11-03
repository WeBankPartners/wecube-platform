package com.webank.wecube.platform.core.entity.workflow;

import java.util.Date;

public class CoreOperationEvent {
    private Long id;

    private String createdBy;

    private Date createdTime;

    private String updatedBy;

    private Date updatedTime;

    private Date endTime;

    private String eventSeqNo;

    private String eventType;

    private Boolean isNotified;

    private String notifyEndpoint;

    private Boolean isNotifyRequired;

    private String operData;

    private String operKey;

    private String operUser;

    private Integer priority;

    private String procDefId;

    private String procInstId;

    private Integer rev;

    private Boolean isSensitive;

    private String srcSubSystem;

    private Date startTime;

    private String status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy == null ? null : createdBy.trim();
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy == null ? null : updatedBy.trim();
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
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
        this.eventSeqNo = eventSeqNo == null ? null : eventSeqNo.trim();
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType == null ? null : eventType.trim();
    }

    public Boolean getIsNotified() {
        return isNotified;
    }

    public void setIsNotified(Boolean isNotified) {
        this.isNotified = isNotified;
    }

    public String getNotifyEndpoint() {
        return notifyEndpoint;
    }

    public void setNotifyEndpoint(String notifyEndpoint) {
        this.notifyEndpoint = notifyEndpoint == null ? null : notifyEndpoint.trim();
    }

    public Boolean getIsNotifyRequired() {
        return isNotifyRequired;
    }

    public void setIsNotifyRequired(Boolean isNotifyRequired) {
        this.isNotifyRequired = isNotifyRequired;
    }

    public String getOperData() {
        return operData;
    }

    public void setOperData(String operData) {
        this.operData = operData == null ? null : operData.trim();
    }

    public String getOperKey() {
        return operKey;
    }

    public void setOperKey(String operKey) {
        this.operKey = operKey == null ? null : operKey.trim();
    }

    public String getOperUser() {
        return operUser;
    }

    public void setOperUser(String operUser) {
        this.operUser = operUser == null ? null : operUser.trim();
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getProcDefId() {
        return procDefId;
    }

    public void setProcDefId(String procDefId) {
        this.procDefId = procDefId == null ? null : procDefId.trim();
    }

    public String getProcInstId() {
        return procInstId;
    }

    public void setProcInstId(String procInstId) {
        this.procInstId = procInstId == null ? null : procInstId.trim();
    }

    public Integer getRev() {
        return rev;
    }

    public void setRev(Integer rev) {
        this.rev = rev;
    }

    public Boolean getIsSensitive() {
        return isSensitive;
    }

    public void setIsSensitive(Boolean isSensitive) {
        this.isSensitive = isSensitive;
    }

    public String getSrcSubSystem() {
        return srcSubSystem;
    }

    public void setSrcSubSystem(String srcSubSystem) {
        this.srcSubSystem = srcSubSystem == null ? null : srcSubSystem.trim();
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }
}