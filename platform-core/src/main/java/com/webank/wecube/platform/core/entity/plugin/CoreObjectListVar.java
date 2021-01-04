package com.webank.wecube.platform.core.entity.plugin;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class CoreObjectListVar {
    private String id;

    private String dataType;

    private String dataValue;

    private String createdBy;

    private Date createdTime;

    private Boolean sensitive;

    @JsonIgnore
    private transient Object rawObjectValue;
    @JsonIgnore
    private transient CoreObjectPropertyMeta objectPropertyMeta;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType == null ? null : dataType.trim();
    }

    public String getDataValue() {
        return dataValue;
    }

    public void setDataValue(String dataValue) {
        this.dataValue = dataValue == null ? null : dataValue.trim();
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

    public Boolean getSensitive() {
        return sensitive;
    }

    public void setSensitive(Boolean sensitive) {
        this.sensitive = sensitive;
    }

    public Object getRawObjectValue() {
        return rawObjectValue;
    }

    public void setRawObjectValue(Object rawObjectValue) {
        this.rawObjectValue = rawObjectValue;
    }

    public CoreObjectPropertyMeta getObjectPropertyMeta() {
        return objectPropertyMeta;
    }

    public void setObjectPropertyMeta(CoreObjectPropertyMeta objectPropertyMeta) {
        this.objectPropertyMeta = objectPropertyMeta;
    }

}