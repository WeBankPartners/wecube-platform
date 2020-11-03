package com.webank.wecube.platform.core.entity.workflow;

import java.util.Date;

public class CoreReTaskNodeDefInfo {
    private String id;

    private String createdBy;

    private Date createdTime;

    private String updatedBy;

    private Date updatedTime;

    private Boolean active;

    private Integer rev;

    private String status;

    private String description;

    private String nodeId;

    private String nodeName;

    private String nodeType;

    private String orderedNo;

    private String prevNodeIds;

    private String procDefId;

    private String procDefKernelId;

    private String procDefKey;

    private Integer procDefVer;

    private String routineExp;

    private String routineRaw;

    private String serviceId;

    private String serviceName;

    private String succeedNodeIds;

    private String timeoutExp;

    private String taskCategory;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Integer getRev() {
        return rev;
    }

    public void setRev(Integer rev) {
        this.rev = rev;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId == null ? null : nodeId.trim();
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName == null ? null : nodeName.trim();
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType == null ? null : nodeType.trim();
    }

    public String getOrderedNo() {
        return orderedNo;
    }

    public void setOrderedNo(String orderedNo) {
        this.orderedNo = orderedNo == null ? null : orderedNo.trim();
    }

    public String getPrevNodeIds() {
        return prevNodeIds;
    }

    public void setPrevNodeIds(String prevNodeIds) {
        this.prevNodeIds = prevNodeIds == null ? null : prevNodeIds.trim();
    }

    public String getProcDefId() {
        return procDefId;
    }

    public void setProcDefId(String procDefId) {
        this.procDefId = procDefId == null ? null : procDefId.trim();
    }

    public String getProcDefKernelId() {
        return procDefKernelId;
    }

    public void setProcDefKernelId(String procDefKernelId) {
        this.procDefKernelId = procDefKernelId == null ? null : procDefKernelId.trim();
    }

    public String getProcDefKey() {
        return procDefKey;
    }

    public void setProcDefKey(String procDefKey) {
        this.procDefKey = procDefKey == null ? null : procDefKey.trim();
    }

    public Integer getProcDefVer() {
        return procDefVer;
    }

    public void setProcDefVer(Integer procDefVer) {
        this.procDefVer = procDefVer;
    }

    public String getRoutineExp() {
        return routineExp;
    }

    public void setRoutineExp(String routineExp) {
        this.routineExp = routineExp == null ? null : routineExp.trim();
    }

    public String getRoutineRaw() {
        return routineRaw;
    }

    public void setRoutineRaw(String routineRaw) {
        this.routineRaw = routineRaw == null ? null : routineRaw.trim();
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId == null ? null : serviceId.trim();
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName == null ? null : serviceName.trim();
    }

    public String getSucceedNodeIds() {
        return succeedNodeIds;
    }

    public void setSucceedNodeIds(String succeedNodeIds) {
        this.succeedNodeIds = succeedNodeIds == null ? null : succeedNodeIds.trim();
    }

    public String getTimeoutExp() {
        return timeoutExp;
    }

    public void setTimeoutExp(String timeoutExp) {
        this.timeoutExp = timeoutExp == null ? null : timeoutExp.trim();
    }

    public String getTaskCategory() {
        return taskCategory;
    }

    public void setTaskCategory(String taskCategory) {
        this.taskCategory = taskCategory == null ? null : taskCategory.trim();
    }
}