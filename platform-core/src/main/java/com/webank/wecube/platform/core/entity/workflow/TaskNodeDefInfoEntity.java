package com.webank.wecube.platform.core.entity.workflow;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "CORE_RE_TASK_NODE_DEF_INFO")
public class TaskNodeDefInfoEntity extends BaseStatusFeaturedEntity {

    public static final String DRAFT_STATUS = "draft";
    public static final String DEPLOYED_STATUS = "deployed";
    public static final String PREDEPLOY_STATUS = "predeploy";

    public static final String NODE_TYPE_SERVICE_TASK = "serviceTask";
    public static final String NODE_TYPE_SUBPROCESS = "subProcess";
    public static final String NODE_TYPE_START_EVENT = "startEvent";

    @Id
    @Column(name = "ID")
    private String id;
    @Column(name = "NODE_ID")
    private String nodeId;
    @Column(name = "NODE_NAME")
    private String nodeName;

    @Column(name = "PROC_DEF_ID")
    private String procDefId;

    @Column(name = "PROC_DEF_KEY")
    private String procDefKey;

    @Column(name = "PROC_DEF_VER")
    private Integer procDefVersion;

    @Column(name = "PROC_DEF_KERNEL_ID")
    private String procDefKernelId;

    @Column(name = "SERVICE_ID")
    private String serviceId;

    @Column(name = "SERVICE_NAME")
    private String serviceName;

    @Column(name = "ROUTINE_EXP")
    private String routineExpression;

    @Column(name = "ROUTINE_RAW")
    private String routineRaw;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "TIMEOUT_EXP")
    private String timeoutExpression;

    @Column(name = "NODE_TYPE")
    private String nodeType;

    @Column(name = "ORDERED_NO")
    private String orderedNo;
    
    @Column(name = "PREV_NODE_IDS")
    private String previousNodeIds;
    
    @Column(name="SUCCEED_NODE_IDS")
    private String succeedingNodeIds;
    
    @Column(name="TASK_CATEGORY")
    private String taskCategory;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Integer getProcDefVersion() {
        return procDefVersion;
    }

    public void setProcDefVersion(Integer procDefVersion) {
        this.procDefVersion = procDefVersion;
    }

    public String getProcDefKernelId() {
        return procDefKernelId;
    }

    public void setProcDefKernelId(String procDefKernelId) {
        this.procDefKernelId = procDefKernelId;
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

    public String getRoutineExpression() {
        return routineExpression;
    }

    public void setRoutineExpression(String routineExpression) {
        this.routineExpression = routineExpression;
    }

    public String getRoutineRaw() {
        return routineRaw;
    }

    public void setRoutineRaw(String routineRaw) {
        this.routineRaw = routineRaw;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTimeoutExpression() {
        return timeoutExpression;
    }

    public void setTimeoutExpression(String timeoutExpression) {
        this.timeoutExpression = timeoutExpression;
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

    public String getPreviousNodeIds() {
        return previousNodeIds;
    }

    public void setPreviousNodeIds(String previousNodeIds) {
        this.previousNodeIds = previousNodeIds;
    }

    public String getSucceedingNodeIds() {
        return succeedingNodeIds;
    }

    public void setSucceedingNodeIds(String succeedingNodeIds) {
        this.succeedingNodeIds = succeedingNodeIds;
    }

    public String getTaskCategory() {
        return taskCategory;
    }

    public void setTaskCategory(String taskCategory) {
        this.taskCategory = taskCategory;
    }
}
