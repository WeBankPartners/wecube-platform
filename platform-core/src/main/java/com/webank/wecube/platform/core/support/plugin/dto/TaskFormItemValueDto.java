package com.webank.wecube.platform.core.support.plugin.dto;

public class TaskFormItemValueDto {

    private String formItemMetaId;

    private String packageName;
    private String entityName;
    private String attrName;

    private String oid;
    private String entityDataId;
    private String fullEntityDataId;
    private Object attrValue;

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

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

}
