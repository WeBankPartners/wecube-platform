package com.webank.wecube.platform.core.dto.workflow;

import java.util.ArrayList;
import java.util.List;

public class TaskNodeDefInfoDto extends BaseNodeDefDto{
    private String nodeDefId;
    private String procDefKey;
    private String procDefId;

    private String serviceId;
    private String serviceName;

    private String routineExpression;
    private String routineRaw;

    private String description;

    private String timeoutExpression;

    private String status;
    
    private String orderedNo;
    
    private String taskCategory; //SUTN-user task,SSTN-service task,SDTN-data operation task
    
    private String preCheck;
    
    private String dynamicBind;

    private List<TaskNodeDefParamDto> paramInfos = new ArrayList<>();

    public String getNodeDefId() {
        return nodeDefId;
    }

    public void setNodeDefId(String id) {
        this.nodeDefId = id;
    }

    public String getProcDefKey() {
        return procDefKey;
    }

    public void setProcDefKey(String processDefKey) {
        this.procDefKey = processDefKey;
    }

    public String getProcDefId() {
        return procDefId;
    }

    public void setProcDefId(String processDefId) {
        this.procDefId = processDefId;
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
    
    public String getTaskCategory() {
        return taskCategory;
    }

    public void setTaskCategory(String taskCategory) {
        this.taskCategory = taskCategory;
    }

    public List<TaskNodeDefParamDto> getParamInfos() {
        return paramInfos;
    }

    public void setParamInfos(List<TaskNodeDefParamDto> paramInfos) {
        this.paramInfos = paramInfos;
    }

   
    public void addParamInfos(TaskNodeDefParamDto...paramInfos){
        for(TaskNodeDefParamDto d : paramInfos){
            if(d == null){
                continue;
            }
            
            this.paramInfos.add(d);
        }
    }
    

    public String getOrderedNo() {
        return orderedNo;
    }

    public void setOrderedNo(String orderedNo) {
        this.orderedNo = orderedNo;
    }
    
    
    
    @Override
    public String toString() {
        return "TaskNodeDefInfoDto [nodeDefId=" + nodeDefId + ", processDefKey=" + procDefKey + ", processDefId="
                + procDefId + ", serviceId=" + serviceId + ", serviceName=" + serviceName + ", routineExpression="
                + routineExpression + ", routineRaw=" + routineRaw + ", description=" + description
                + ", timeoutExpression=" + timeoutExpression + ", status=" + status + ", orderedNo=" + orderedNo
                + ", paramInfos=" + paramInfos + ", toString()=" + super.toString() + "]";
    }

    public String getPreCheck() {
        return preCheck;
    }

    public void setPreCheck(String preCheck) {
        this.preCheck = preCheck;
    }

    public String getDynamicBind() {
        return dynamicBind;
    }

    public void setDynamicBind(String dynamicBind) {
        this.dynamicBind = dynamicBind;
    }
}
