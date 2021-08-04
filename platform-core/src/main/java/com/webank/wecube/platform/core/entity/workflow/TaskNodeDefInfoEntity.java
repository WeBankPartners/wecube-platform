package com.webank.wecube.platform.core.entity.workflow;

import java.util.Date;

public class TaskNodeDefInfoEntity {

    public static final String DRAFT_STATUS = "draft";
    public static final String DEPLOYED_STATUS = "deployed";
    public static final String PREDEPLOY_STATUS = "predeploy";

    public static final String NODE_TYPE_SERVICE_TASK = "serviceTask";
    public static final String NODE_TYPE_SUBPROCESS = "subProcess";
    public static final String NODE_TYPE_START_EVENT = "startEvent";

    public static final String DYNAMIC_BIND_YES = "Y";
    public static final String DYNAMIC_BIND_NO = "N";
    
    public static final String PRE_CHECK_YES = "Y";
    public static final String PRE_CHECK_NO = "N";

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

    private String dynamicBind;

    private String preCheck;
    
    private String prevCtxNodeIds;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
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
        this.updatedBy = updatedBy;
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
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getOrderedNo() {
        return orderedNo;
    }

    public void setOrderedNo(String orderedNo) {
        this.orderedNo = orderedNo;
    }

    public String getPrevNodeIds() {
        return prevNodeIds;
    }

    public void setPrevNodeIds(String prevNodeIds) {
        this.prevNodeIds = prevNodeIds;
    }

    public String getProcDefId() {
        return procDefId;
    }

    public void setProcDefId(String procDefId) {
        this.procDefId = procDefId;
    }

    public String getProcDefKernelId() {
        return procDefKernelId;
    }

    public void setProcDefKernelId(String procDefKernelId) {
        this.procDefKernelId = procDefKernelId;
    }

    public String getProcDefKey() {
        return procDefKey;
    }

    public void setProcDefKey(String procDefKey) {
        this.procDefKey = procDefKey;
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
        this.routineExp = routineExp;
    }

    public String getRoutineRaw() {
        return routineRaw;
    }

    public void setRoutineRaw(String routineRaw) {
        this.routineRaw = routineRaw;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getSucceedNodeIds() {
        return succeedNodeIds;
    }

    public void setSucceedNodeIds(String succeedNodeIds) {
        this.succeedNodeIds = succeedNodeIds;
    }

    public String getTimeoutExp() {
        return timeoutExp;
    }

    public void setTimeoutExp(String timeoutExp) {
        this.timeoutExp = timeoutExp;
    }

    public String getTaskCategory() {
        return taskCategory;
    }

    public void setTaskCategory(String taskCategory) {
        this.taskCategory = taskCategory;
    }

    public String getDynamicBind() {
        return dynamicBind;
    }

    public void setDynamicBind(String dynamicBind) {
        this.dynamicBind = dynamicBind;
    }

    public String getPreCheck() {
        return preCheck;
    }

    public void setPreCheck(String preCheck) {
        this.preCheck = preCheck;
    }

    public String getPrevCtxNodeIds() {
        return prevCtxNodeIds;
    }

    public void setPrevCtxNodeIds(String prevCtxNodeIds) {
        this.prevCtxNodeIds = prevCtxNodeIds;
    }
    
    

}
