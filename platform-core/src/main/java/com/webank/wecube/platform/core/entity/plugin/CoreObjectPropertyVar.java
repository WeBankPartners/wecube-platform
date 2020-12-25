package com.webank.wecube.platform.core.entity.plugin;

import java.util.Date;

public class CoreObjectPropertyVar {
    
    private String id;

    private String name;

    private String dataType;

    private String objectPropertyMetaId;

    private String objectMetaId;

    private String objectVarId;

    private String dataValue;

    private String dataTypeId;

    private String dataId;

    private String dataName;

    private String createdBy;

    private Date createdTime;

    private String updatedBy;

    private Date updatedTime;

    private Boolean sensitive;

    private transient CoreObjectPropertyMeta propertyMeta;

    private transient CoreObjectVar objectVar;
    
    private transient Object dataValueObject;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType == null ? null : dataType.trim();
    }

    public String getObjectPropertyMetaId() {
        return objectPropertyMetaId;
    }

    public void setObjectPropertyMetaId(String objectPropertyMetaId) {
        this.objectPropertyMetaId = objectPropertyMetaId == null ? null : objectPropertyMetaId.trim();
    }

    public String getObjectMetaId() {
        return objectMetaId;
    }

    public void setObjectMetaId(String objectMetaId) {
        this.objectMetaId = objectMetaId == null ? null : objectMetaId.trim();
    }

    public String getObjectVarId() {
        return objectVarId;
    }

    public void setObjectVarId(String objectVarId) {
        this.objectVarId = objectVarId == null ? null : objectVarId.trim();
    }

    public String getDataValue() {
        return dataValue;
    }

    public void setDataValue(String dataValue) {
        this.dataValue = dataValue == null ? null : dataValue.trim();
    }

    public String getDataTypeId() {
        return dataTypeId;
    }

    public void setDataTypeId(String dataTypeId) {
        this.dataTypeId = dataTypeId == null ? null : dataTypeId.trim();
    }

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId == null ? null : dataId.trim();
    }

    public String getDataName() {
        return dataName;
    }

    public void setDataName(String dataName) {
        this.dataName = dataName == null ? null : dataName.trim();
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy == null ? null : createdBy.trim();
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy == null ? null : updatedBy.trim();
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }

    public Boolean getSensitive() {
        return sensitive;
    }

    public void setSensitive(Boolean sensitive) {
        this.sensitive = sensitive;
    }

    public CoreObjectVar getObjectVar() {
        return objectVar;
    }

    public void setObjectVar(CoreObjectVar objectVar) {
        this.objectVar = objectVar;
    }

    public CoreObjectPropertyMeta getPropertyMeta() {
        return propertyMeta;
    }

    public void setPropertyMeta(CoreObjectPropertyMeta propertyMeta) {
        this.propertyMeta = propertyMeta;
    }

    public Object getDataValueObject() {
        return dataValueObject;
    }

    public void setDataValueObject(Object dataValueObject) {
        this.dataValueObject = dataValueObject;
    }
    
    

}