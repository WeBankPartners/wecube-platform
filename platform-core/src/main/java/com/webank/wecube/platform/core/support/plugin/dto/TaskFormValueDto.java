package com.webank.wecube.platform.core.support.plugin.dto;

import java.util.ArrayList;
import java.util.List;

public class TaskFormValueDto {

    private String formItemMetaId;

    private Integer procInstId;
    private String procInstKey;
    private String taskNodeDefId;
    private Integer taskNodeInstId;

    private List<TaskFormDataRecordDto> formDataRecords = new ArrayList<>();

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

    public List<TaskFormDataRecordDto> getFormDataRecords() {
        return formDataRecords;
    }

    public void setFormDataRecords(List<TaskFormDataRecordDto> formDataRecords) {
        this.formDataRecords = formDataRecords;
    }

    public void addFormDataRecord(TaskFormDataRecordDto formDataRecord) {
        if (formDataRecord == null) {
            return;
        }
        this.formDataRecords.add(formDataRecord);
    }
}
