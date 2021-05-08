package com.webank.wecube.platform.core.support.plugin.dto;

import java.util.ArrayList;
import java.util.List;

public class UserTaskFormTemplateValueDto {

    private String formItemTemplateId;

    private Integer procInstId;
    private String procInstKey;
    private String taskNodeDefId;
    private Integer taskNodeInstId;

    private List<UserTaskFormItemValueDto> formItemValues = new ArrayList<>();

    public String getFormItemTemplateId() {
        return formItemTemplateId;
    }

    public void setFormItemTemplateId(String formItemTemplateId) {
        this.formItemTemplateId = formItemTemplateId;
    }

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

    public List<UserTaskFormItemValueDto> getFormItemValues() {
        return formItemValues;
    }

    public void setFormItemValues(List<UserTaskFormItemValueDto> formItemValues) {
        this.formItemValues = formItemValues;
    }

    public void addFormItemValue(UserTaskFormItemValueDto formItemValue) {
        if (formItemValue == null) {
            return;
        }

        this.formItemValues.add(formItemValue);
    }

}
