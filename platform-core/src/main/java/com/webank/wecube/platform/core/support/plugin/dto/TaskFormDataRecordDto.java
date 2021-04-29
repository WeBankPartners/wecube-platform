package com.webank.wecube.platform.core.support.plugin.dto;

import java.util.ArrayList;
import java.util.List;

public class TaskFormDataRecordDto {
    private String formItemMetaId;

    private String packageName;
    private String entityName;

    private String oid;
    private String entityDataId;
    private String fullEntityDataId;

    private String entityDataState;// NotCreated,Created,Deleted
    private String entityDataOp;// create,update,delete

    private List<TaskFormItemValueDto> formItemValues = new ArrayList<>();

    public List<TaskFormItemValueDto> getFormItemValues() {
        return formItemValues;
    }

    public void setFormItemValues(List<TaskFormItemValueDto> formItemValues) {
        this.formItemValues = formItemValues;
    }

    public void addFormItemValue(TaskFormItemValueDto formItemValue) {
        if (formItemValue == null) {
            return;
        }

        this.formItemValues.add(formItemValue);
    }

    public String getFormItemMetaId() {
        return formItemMetaId;
    }

    public void setFormItemMetaId(String formItemMetaId) {
        this.formItemMetaId = formItemMetaId;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getEntityDataId() {
        return entityDataId;
    }

    public void setEntityDataId(String entityDataId) {
        this.entityDataId = entityDataId;
    }

    public String getFullEntityDataId() {
        return fullEntityDataId;
    }

    public void setFullEntityDataId(String fullEntityDataId) {
        this.fullEntityDataId = fullEntityDataId;
    }

    public String getEntityDataState() {
        return entityDataState;
    }

    public void setEntityDataState(String entityDataState) {
        this.entityDataState = entityDataState;
    }

    public String getEntityDataOp() {
        return entityDataOp;
    }

    public void setEntityDataOp(String entityDataOp) {
        this.entityDataOp = entityDataOp;
    }

}
