package com.webank.wecube.platform.core.dto.workflow;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProcInstInfoDto {
    private Integer id;
    private String procInstKey;
    private String procInstName;
    private String createdTime;
    private String operator;
    private String status;
    private String procDefId;

    private String entityTypeId;
    private String entityDataId;
    private String entityDisplayName;

    private List<TaskNodeInstDto> taskNodeInstances = new ArrayList<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getEntityDisplayName() {
		return entityDisplayName;
	}

	public void setEntityDisplayName(String entityDisplayName) {
		this.entityDisplayName = entityDisplayName;
	}

	public String getProcInstKey() {
        return procInstKey;
    }

    public void setProcInstKey(String procInstKey) {
        this.procInstKey = procInstKey;
    }

    public String getProcInstName() {
        return procInstName;
    }

    public void setProcInstName(String procInstName) {
        this.procInstName = procInstName;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProcDefId() {
        return procDefId;
    }

    public void setProcDefId(String procDefId) {
        this.procDefId = procDefId;
    }

    public String getEntityTypeId() {
        return entityTypeId;
    }

    public void setEntityTypeId(String entityTypeId) {
        this.entityTypeId = entityTypeId;
    }

    public String getEntityDataId() {
        return entityDataId;
    }

    public void setEntityDataId(String entityDataId) {
        this.entityDataId = entityDataId;
    }

    public List<TaskNodeInstDto> getTaskNodeInstances() {
        return taskNodeInstances;
    }

    public void setTaskNodeInstances(List<TaskNodeInstDto> taskNodeInstances) {
        this.taskNodeInstances = taskNodeInstances;
    }

    public ProcInstInfoDto addTaskNodeInstances(TaskNodeInstDto... taskNodeInstances) {
        if (this.taskNodeInstances == null) {
            this.taskNodeInstances = new ArrayList<>();
        }

        for (TaskNodeInstDto t : taskNodeInstances) {
            this.taskNodeInstances.add(t);
        }

        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ProcInstInfoDto [id=");
        builder.append(id);
        builder.append(", procInstKey=");
        builder.append(procInstKey);
        builder.append(", procInstName=");
        builder.append(procInstName);
        builder.append(", createdTime=");
        builder.append(createdTime);
        builder.append(", operator=");
        builder.append(operator);
        builder.append(", status=");
        builder.append(status);
        builder.append(", procDefId=");
        builder.append(procDefId);
        builder.append(", entityTypeId=");
        builder.append(entityTypeId);
        builder.append(", entityDataId=");
        builder.append(entityDataId);
        builder.append(", entityDisplayName=");
        builder.append(entityDisplayName);
        builder.append(", taskNodeInstances=");
        builder.append(taskNodeInstances);
        builder.append("]");
        return builder.toString();
    }

    
}
