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

    public static final String TASK_NODE_TYPE_SERVICE_TASK = "serviceTask";
    public static final String TASK_NODE_TYPE_SUBPROCESS = "subprocess";

    @Id
    @Column(name = "ID")
    private String id;
    @Column(name = "TASK_NODE_ID")
    private String taskNodeId;
    @Column(name = "TASK_NODE_NAME")
    private String taksNodeName;
    
    @Column(name = "PROC_DEF_ID")
    private String procDefId;
    
    @Column(name = "PROC_DEF_VER")
    private Integer procDefVersion;
    
    @Column(name = "PROC_ID")
    private String procId;
    

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

    @Column(name = "TASK_NODE_TYPE")
    private String taskNodeType;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProcDefId() {
        return procDefId;
    }

    public void setProcDefId(String procDefId) {
        this.procDefId = procDefId;
    }

    public Integer getProcDefVersion() {
        return procDefVersion;
    }

    public void setProcDefVersion(Integer procDefVersion) {
        this.procDefVersion = procDefVersion;
    }

    public String getProcId() {
        return procId;
    }

    public void setProcId(String procId) {
        this.procId = procId;
    }

    public String getTaskNodeId() {
        return taskNodeId;
    }

    public void setTaskNodeId(String taskNodeId) {
        this.taskNodeId = taskNodeId;
    }

    public String getTaksNodeName() {
        return taksNodeName;
    }

    public void setTaksNodeName(String taksNodeName) {
        this.taksNodeName = taksNodeName;
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

    public String getTaskNodeType() {
        return taskNodeType;
    }

    public void setTaskNodeType(String taskNodeType) {
        this.taskNodeType = taskNodeType;
    }

}
