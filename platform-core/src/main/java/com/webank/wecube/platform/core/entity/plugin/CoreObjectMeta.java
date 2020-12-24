package com.webank.wecube.platform.core.entity.plugin;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CoreObjectMeta {
    private String id;

    private String name;

    private String packageName;

    private String source;

    private String latestSource;

    private String createdBy;

    private Date createdTime;

    private String updatedBy;

    private Date updatedTime;
    
    private transient List<CoreObjectPropertyMeta> propertyMetas = new ArrayList<>();

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

    public String getLatestSource() {
        return latestSource;
    }

    public void setLatestSource(String latestSource) {
        this.latestSource = latestSource == null ? null : latestSource.trim();
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

    public List<CoreObjectPropertyMeta> getPropertyMetas() {
        return propertyMetas;
    }

    public void setPropertyMetas(List<CoreObjectPropertyMeta> propertyMetas) {
        this.propertyMetas = propertyMetas;
    }
    
    public void addPropertyMeta(CoreObjectPropertyMeta propertyMeta) {
        this.propertyMetas.add(propertyMeta);
    }
}