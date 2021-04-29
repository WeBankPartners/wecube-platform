package com.webank.wecube.platform.core.support.plugin.dto;

import java.util.ArrayList;
import java.util.List;

public class DynamicWorkflowInstCreationInfoDto {
    private String procDefId;
    private String procDefKey;

    private DynamicEntityValueDto rootEntityValue;

    private List<DynamicTaskNodeBindInfoDto> taskNodeBindInfos = new ArrayList<>();

    public String getProcDefId() {
        return procDefId;
    }

    public void setProcDefId(String procDefId) {
        this.procDefId = procDefId;
    }

    public String getProcDefKey() {
        return procDefKey;
    }

    public void setProcDefKey(String procDefKey) {
        this.procDefKey = procDefKey;
    }

    public DynamicEntityValueDto getRootEntityValue() {
        return rootEntityValue;
    }

    public void setRootEntityValue(DynamicEntityValueDto rootEntityValue) {
        this.rootEntityValue = rootEntityValue;
    }

    public List<DynamicTaskNodeBindInfoDto> getTaskNodeBindInfos() {
        return taskNodeBindInfos;
    }

    public void setTaskNodeBindInfos(List<DynamicTaskNodeBindInfoDto> taskNodeBindInfos) {
        this.taskNodeBindInfos = taskNodeBindInfos;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DynamicWorkflowInstCreationInfoDto [procDefId=");
        builder.append(procDefId);
        builder.append(", procDefKey=");
        builder.append(procDefKey);
        builder.append(", rootEntityValue=");
        builder.append(rootEntityValue);
        builder.append(", taskNodeBindInfos=");
        builder.append(taskNodeBindInfos);
        builder.append("]");
        return builder.toString();
    }

}
