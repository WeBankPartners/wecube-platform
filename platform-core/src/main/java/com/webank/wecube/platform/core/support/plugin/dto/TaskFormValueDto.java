package com.webank.wecube.platform.core.support.plugin.dto;

import java.util.ArrayList;
import java.util.List;

public class TaskFormValueDto {

    private String formItemMetaId;

    private Integer procInstId;
    private String procInstKey;
    private String taskNodeDefId;
    private Integer taskNodeInstId;

    private List<TaskFormDataEntityDto> formDataEntities = new ArrayList<>();

    public Integer getProcInstId() {
        return procInstId;
    }

    public void setProcInstId(Integer procInstId) {
        this.procInstId = procInstId;
    }

    public String getProcInstKey() {
        return procInstKey;
    }

    public void setProcInstKey(String procInstKey) {
        this.procInstKey = procInstKey;
    }

    public String getTaskNodeDefId() {
        return taskNodeDefId;
    }

    public void setTaskNodeDefId(String taskNodeDefId) {
        this.taskNodeDefId = taskNodeDefId;
    }

    public Integer getTaskNodeInstId() {
        return taskNodeInstId;
    }

    public void setTaskNodeInstId(Integer taskNodeInstId) {
        this.taskNodeInstId = taskNodeInstId;
    }

    public String getFormItemMetaId() {
        return formItemMetaId;
    }

    public void setFormItemMetaId(String formItemMetaId) {
        this.formItemMetaId = formItemMetaId;
    }

    

    public void addFormDataEntities(TaskFormDataEntityDto formDataEntity) {
        if (formDataEntity == null) {
            return;
        }
        this.formDataEntities.add(formDataEntity);
    }

    public List<TaskFormDataEntityDto> getFormDataEntities() {
        return formDataEntities;
    }

    public void setFormDataEntities(List<TaskFormDataEntityDto> formDataEntities) {
        this.formDataEntities = formDataEntities;
    }
}
