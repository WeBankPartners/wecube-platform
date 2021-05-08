package com.webank.wecube.platform.core.support.plugin.dto;

public class UserTaskFormItemValueDto {

    private String formItemTemplateId;

    private String packageName;
    private String entityName;
    private String attrName;

    private String entityDataId;
    private String fullEntityDataId;
    private Object attrValue;

    public String getFormItemTemplateId() {
        return formItemTemplateId;
    }

    public void setFormItemTemplateId(String formItemTemplateId) {
        this.formItemTemplateId = formItemTemplateId;
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

    public String getAttrName() {
        return attrName;
    }

    public void setAttrName(String attrName) {
        this.attrName = attrName;
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

    public Object getAttrValue() {
        return attrValue;
    }

    public void setAttrValue(Object attrValue) {
        this.attrValue = attrValue;
    }

}
