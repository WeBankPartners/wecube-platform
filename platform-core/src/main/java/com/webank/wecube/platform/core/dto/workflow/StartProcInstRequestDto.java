package com.webank.wecube.platform.core.dto.workflow;

import java.util.ArrayList;
import java.util.List;

public class StartProcInstRequestDto {
    private String procDefId;
    private String rootObjectId;
    private List<TaskNodeDefObjectBindInfoDto> taskNodeBinds = new ArrayList<>();

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

    public List<TaskNodeDefObjectBindInfoDto> getTaskNodeBinds() {
        return taskNodeBinds;
    }

    public void setTaskNodeBinds(List<TaskNodeDefObjectBindInfoDto> taskNodeBinds) {
        this.taskNodeBinds = taskNodeBinds;
    }
}
