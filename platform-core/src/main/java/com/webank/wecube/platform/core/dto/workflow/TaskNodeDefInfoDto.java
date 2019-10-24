package com.webank.wecube.platform.core.dto.workflow;

import java.util.ArrayList;
import java.util.List;

public class TaskNodeDefInfoDto extends BaseNodeDefDto{
    private String nodeDefId;
    private String processDefKey;
    private String processDefId;

    private String serviceId;
    private String serviceName;

    private String routineExpression;
    private String routineRaw;

    private String description;

    private String timeoutExpression;

    private String status;
    
    private int orderedNo;

    private List<TaskNodeDefParamDto> paramInfos = new ArrayList<>();

    public String getNodeDefId() {
        return nodeDefId;
    }

    public void setNodeDefId(String id) {
        this.nodeDefId = id;
    }

    public String getProcessDefKey() {
        return processDefKey;
    }

    public void setProcessDefKey(String processDefKey) {
        this.processDefKey = processDefKey;
    }

    public String getProcessDefId() {
        return processDefId;
    }

    public void setProcessDefId(String processDefId) {
        this.processDefId = processDefId;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<TaskNodeDefParamDto> getParamInfos() {
        return paramInfos;
    }

    public void setParamInfos(List<TaskNodeDefParamDto> paramInfos) {
        this.paramInfos = paramInfos;
    }

   
    
    

    public int getOrderedNo() {
        return orderedNo;
    }

    public void setOrderedNo(int orderedNo) {
        this.orderedNo = orderedNo;
    }

    @Override
    public String toString() {
        return "TaskNodeDefInfoDto [nodeDefId=" + nodeDefId + ", processDefKey=" + processDefKey + ", processDefId="
                + processDefId + ", serviceId=" + serviceId + ", serviceName=" + serviceName + ", routineExpression="
                + routineExpression + ", routineRaw=" + routineRaw + ", description=" + description
                + ", timeoutExpression=" + timeoutExpression + ", status=" + status + ", orderedNo=" + orderedNo
                + ", paramInfos=" + paramInfos + ", toString()=" + super.toString() + "]";
    }

   

}
