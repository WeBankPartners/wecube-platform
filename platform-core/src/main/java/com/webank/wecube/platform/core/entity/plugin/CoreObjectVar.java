package com.webank.wecube.platform.core.entity.plugin;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class CoreObjectVar {
    private String id;

    private String objectMetaId;

    private String name;

    private String packageName;

    private String createdBy;

    private Date createdTime;

    private String updatedBy;

    private Date updatedTime;

    private String parentObjectVarId;

    private String parentObjectName;

    @JsonIgnore
    private transient CoreObjectMeta objectMeta;
    // @JsonIgnore
    private transient List<CoreObjectPropertyVar> propertyVars = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getObjectMetaId() {
        return objectMetaId;
    }

    public void setObjectMetaId(String objectMetaId) {
        this.objectMetaId = objectMetaId == null ? null : objectMetaId.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName == null ? null : packageName.trim();
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

    public List<CoreObjectPropertyVar> getPropertyVars() {
        return propertyVars;
    }

    public void setPropertyVars(List<CoreObjectPropertyVar> propertyVars) {
        this.propertyVars = propertyVars;
    }

    public CoreObjectMeta getObjectMeta() {
        return objectMeta;
    }

    public void setObjectMeta(CoreObjectMeta objectMeta) {
        this.objectMeta = objectMeta;
    }

    public void addPropertyVar(CoreObjectPropertyVar propertyVar) {
        if (propertyVar == null) {
            return;
        }

        if (propertyVar.getObjectVar() == null) {
            propertyVar.setObjectVar(this);
        }

        this.getPropertyVars().add(propertyVar);
    }

    public String getParentObjectVarId() {
        return parentObjectVarId;
    }

    public void setParentObjectVarId(String parentObjectVarId) {
        this.parentObjectVarId = parentObjectVarId;
    }

    public String getParentObjectName() {
        return parentObjectName;
    }

    public void setParentObjectName(String parentObjectName) {
        this.parentObjectName = parentObjectName;
    }

}