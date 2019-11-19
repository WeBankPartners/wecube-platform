package com.webank.wecube.platform.core.dto.workflow;

import java.util.ArrayList;
import java.util.List;

public class StartProcInstRequestDto {
    private String procDefId;
    private String entityTypeId;
    private String entityDataId;
    private List<TaskNodeDefObjectBindInfoDto> taskNodeBinds = new ArrayList<>();

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
}
