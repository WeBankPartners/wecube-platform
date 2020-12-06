package com.webank.wecube.platform.core.dto.workflow;

public class WorkflowNodeDefInfoDto {
    private String nodeId;
    private String nodeName;
    private String nodeType;

    private String nodeDefId;

    private String taskCategory;
    private String routineExp;

    private String serviceId;
    private String serviceName;

    private RegisteredEntityDefDto boundEntity;

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

    public String getNodeDefId() {
        return nodeDefId;
    }

    public void setNodeDefId(String nodeDefId) {
        this.nodeDefId = nodeDefId;
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

    public String getTaskCategory() {
        return taskCategory;
    }

    public void setTaskCategory(String taskCategory) {
        this.taskCategory = taskCategory;
    }

    public String getRoutineExp() {
        return routineExp;
    }

    public void setRoutineExp(String routineExp) {
        this.routineExp = routineExp;
    }

    public RegisteredEntityDefDto getBoundEntity() {
        return boundEntity;
    }

    public void setBoundEntity(RegisteredEntityDefDto boundEntity) {
        this.boundEntity = boundEntity;
    }

}
