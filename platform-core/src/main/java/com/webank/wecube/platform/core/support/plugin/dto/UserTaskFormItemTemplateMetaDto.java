package com.webank.wecube.platform.core.support.plugin.dto;

public class UserTaskFormItemTemplateMetaDto {
    private String formItemTemplateId;

    private String packageName;
    private String entityName;
    private String attrName;

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

}
