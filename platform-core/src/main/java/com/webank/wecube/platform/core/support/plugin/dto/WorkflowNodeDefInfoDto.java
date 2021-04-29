package com.webank.wecube.platform.core.support.plugin.dto;

import java.util.ArrayList;
import java.util.List;

public class WorkflowNodeDefInfoDto {
    private String nodeId;
    private String nodeName;
    private String nodeType;

    private String nodeDefId;

    private String taskCategory;
    private String routineExp;

    private String serviceId;
    private String serviceName;

    private List<RegisteredEntityDefDto> boundEntities;

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



    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("WorkflowNodeDefInfoDto [nodeId=");
        builder.append(nodeId);
        builder.append(", nodeName=");
        builder.append(nodeName);
        builder.append(", nodeType=");
        builder.append(nodeType);
        builder.append(", nodeDefId=");
        builder.append(nodeDefId);
        builder.append(", taskCategory=");
        builder.append(taskCategory);
        builder.append(", routineExp=");
        builder.append(routineExp);
        builder.append(", serviceId=");
        builder.append(serviceId);
        builder.append(", serviceName=");
        builder.append(serviceName);
        builder.append("]");
        return builder.toString();
    }

    public List<RegisteredEntityDefDto> getBoundEntities() {
        return boundEntities;
    }

    public void setBoundEntities(List<RegisteredEntityDefDto> boundEntities) {
        this.boundEntities = boundEntities;
    }
    
    public void addBoundEntities(RegisteredEntityDefDto boundEntity) {
        if(boundEntity == null){
            return;
        }
        
        if(this.boundEntities == null){
            this.boundEntities = new ArrayList<>();
        }
        this.boundEntities.add(boundEntity);
    }


}
