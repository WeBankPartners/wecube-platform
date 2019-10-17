package com.webank.wecube.platform.core.dto.workflow;

import java.util.ArrayList;
import java.util.List;

public class TaskNodeInfoDto {
    private String id;
    private String processId;

    private String nodeId;
    private String nodeName;

    private String serviceId;
    private String serviceName;

    private String routineExpression;
    private String routineRaw;

    private String description;

    private String timeoutExpression;
    
    private String status;

    private List<TaskNodeParamInfoDto> paramInfos = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
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

    public List<TaskNodeParamInfoDto> getParamInfos() {
        return paramInfos;
    }

    public void setParamInfos(List<TaskNodeParamInfoDto> paramInfos) {
        this.paramInfos = paramInfos;
    }
    
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "TaskNodeInfoDto [id=" + id + ", processId=" + processId + ", nodeId=" + nodeId + ", nodeName="
                + nodeName + ", serviceId=" + serviceId + ", serviceName=" + serviceName + ", routineExpression="
                + routineExpression + ", routineRaw=" + routineRaw + ", description=" + description
                + ", timeoutExpression=" + timeoutExpression + ", status=" + status + ", paramInfos=" + paramInfos
                + "]";
    }

}
