package com.webank.wecube.platform.core.dto.workflow;

import java.util.ArrayList;
import java.util.List;

public class ProcInstInfoDto {
    private Integer id;
    private String procInstKey;
    private String procInstName;
    private String createdTime;
    private String operator;
    private String status;
    private String procDefId;

    private String rootObjectId;

    private List<TaskNodeInstDto> taskNodeInstances = new ArrayList<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public String getRootObjectId() {
        return rootObjectId;
    }

    public void setRootObjectId(String rootObjectId) {
        this.rootObjectId = rootObjectId;
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

}
