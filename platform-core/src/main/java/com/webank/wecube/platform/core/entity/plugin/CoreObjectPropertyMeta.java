package com.webank.wecube.platform.core.entity.plugin;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.webank.wecube.platform.core.utils.Constants;

public class CoreObjectPropertyMeta {

    private String id;

    private String name;

    private String dataType;

    private String refType;

    private String refName;

    private String mapType;

    private String mapExpr;// entity,systemVariable, constant

    private String objectMetaId;

    private String objectName;

    private String packageName;

    private String source;

    private String createdBy;

    private Date createdTime;

    private String updatedBy;

    private Date updatedTime;

    private Boolean sensitive;

    private String configId;

    @JsonIgnore
    private transient CoreObjectMeta objectMeta;

    @JsonIgnore
    private transient CoreObjectMeta refObjectMeta;

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

    public String getRefType() {
        return refType;
    }

    public void setRefType(String refType) {
        this.refType = refType == null ? null : refType.trim();
    }

    public String getMapType() {
        return mapType;
    }

    public void setMapType(String mapType) {
        this.mapType = mapType == null ? null : mapType.trim();
    }

    public String getMapExpr() {
        return mapExpr;
    }

    public void setMapExpr(String mapExpr) {
        this.mapExpr = mapExpr == null ? null : mapExpr.trim();
    }

    public String getObjectMetaId() {
        return objectMetaId;
    }

    public void setObjectMetaId(String objectMetaId) {
        this.objectMetaId = objectMetaId == null ? null : objectMetaId.trim();
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName == null ? null : objectName.trim();
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName == null ? null : packageName.trim();
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source == null ? null : source.trim();
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

    public String getRefName() {
        return refName;
    }

    public void setRefName(String refName) {
        this.refName = refName;
    }

    public CoreObjectMeta getObjectMeta() {
        return objectMeta;
    }

    public void setObjectMeta(CoreObjectMeta objectMeta) {
        this.objectMeta = objectMeta;
    }

    public CoreObjectMeta getRefObjectMeta() {
        return refObjectMeta;
    }

    public void setRefObjectMeta(CoreObjectMeta refObjectMeta) {
        this.refObjectMeta = refObjectMeta;
    }

    public boolean isEntityMapping() {
        return Constants.MAPPING_TYPE_ENTITY.equalsIgnoreCase(mapType);
    }

    public boolean isContextMapping() {
        return Constants.MAPPING_TYPE_CONTEXT.equalsIgnoreCase(mapType);
    }

    public boolean isConstantMapping() {
        return Constants.MAPPING_TYPE_CONSTANT.equalsIgnoreCase(mapType);
    }

    public boolean isSystemVariableMapping() {
        return Constants.MAPPING_TYPE_SYSTEM_VARIABLE.equalsIgnoreCase(mapType);
    }

    public boolean isObjectMapping() {
        return Constants.MAPPING_TYPE_OBJECT.equalsIgnoreCase(mapType);
    }

    public String getConfigId() {
        return configId;
    }

    public void setConfigId(String configId) {
        this.configId = configId;
    }

}