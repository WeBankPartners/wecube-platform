package com.webank.wecube.platform.core.dto.workflow;

import java.util.ArrayList;
import java.util.List;

public class ProcessDefinitionDeployRequestDto {

    private String processId;
    private String processName;
    private String processData;

    private String rootEntity;

    private List<TaskNodeInfoDto> taskNodeInfos = new ArrayList<>();

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getProcessData() {
        return processData;
    }

    public void setProcessData(String processData) {
        this.processData = processData;
    }

    public String getRootEntity() {
        return rootEntity;
    }

    public void setRootEntity(String rootEntity) {
        this.rootEntity = rootEntity;
    }

    public List<TaskNodeInfoDto> getTaskNodeInfos() {
        return taskNodeInfos;
    }

    public void setTaskNodeInfos(List<TaskNodeInfoDto> taskNodeInfos) {
        this.taskNodeInfos = taskNodeInfos;
    }

    @Override
    public String toString() {
        return "ProcessDefinitionDeployRequestDto [processId=" + processId + ", processName=" + processName
                + ", processData=" + processData + ", rootEntity=" + rootEntity + ", taskNodeInfos=" + taskNodeInfos
                + "]";
    }

}
