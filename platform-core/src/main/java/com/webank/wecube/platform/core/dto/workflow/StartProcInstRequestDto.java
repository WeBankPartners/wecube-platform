package com.webank.wecube.platform.core.dto.workflow;

import java.util.ArrayList;
import java.util.List;

public class StartProcInstRequestDto {
    private String procDefId;
    private String entityTypeId;
    private String entityDataId;
    private String entityDisplayName;
    private String processSessionId;
    private String procBatchKey;
    private String procInitUser;
    
    private List<TaskNodeDefObjectBindInfoDto> taskNodeBinds = new ArrayList<>();

    public String getEntityDisplayName() {
        return entityDisplayName;
    }

    public void setEntityDisplayName(String entityDisplayName) {
        this.entityDisplayName = entityDisplayName;
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

    public List<TaskNodeDefObjectBindInfoDto> getTaskNodeBinds() {
        return taskNodeBinds;
    }

    public void setTaskNodeBinds(List<TaskNodeDefObjectBindInfoDto> taskNodeBinds) {
        this.taskNodeBinds = taskNodeBinds;
    }

    public StartProcInstRequestDto addAllTaskNodeDefObjectBindInfos(List<TaskNodeDefObjectBindInfoDto> dtos) {
        if (dtos == null) {
            return this;
        }

        for (TaskNodeDefObjectBindInfoDto dto : dtos) {
            if (dto != null) {
                this.taskNodeBinds.add(dto);
            }
        }

        return this;
    }

    public StartProcInstRequestDto addTaskNodeDefObjectBindInfos(TaskNodeDefObjectBindInfoDto... bindInfoDtos) {
        for (TaskNodeDefObjectBindInfoDto dto : bindInfoDtos) {
            if (dto != null) {
                this.taskNodeBinds.add(dto);
            }
        }

        return this;
    }

    public String getProcessSessionId() {
        return processSessionId;
    }

    public void setProcessSessionId(String processSessionId) {
        this.processSessionId = processSessionId;
    }

    public String getProcBatchKey() {
        return procBatchKey;
    }

    public void setProcBatchKey(String procBatchKey) {
        this.procBatchKey = procBatchKey;
    }

    public String getProcInitUser() {
        return procInitUser;
    }

    public void setProcInitUser(String procInitUser) {
        this.procInitUser = procInitUser;
    }

}
