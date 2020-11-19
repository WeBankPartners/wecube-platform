package com.webank.wecube.platform.core.entity.plugin;

import java.util.ArrayList;
import java.util.List;

public class PluginPackageDataModel {
    public static final String PLUGIN_PACKAGE = "PLUGIN_PACKAGE";
    public static final String DATA_MODEL_ENDPOINT = "DATA_MODEL_ENDPOINT";
    private String id;

    private Integer version;

    private String packageName;

    private Boolean isDynamic;

    private String updatePath;

    private String updateMethod;

    private String updateSource;

    private Long updateTime;
    
    private transient List<PluginPackageEntities> pluginPackageEntities = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName == null ? null : packageName.trim();
    }

    public Boolean getIsDynamic() {
        return isDynamic;
    }

    public void setIsDynamic(Boolean isDynamic) {
        this.isDynamic = isDynamic;
    }

    public String getUpdatePath() {
        return updatePath;
    }

    public void setUpdatePath(String updatePath) {
        this.updatePath = updatePath == null ? null : updatePath.trim();
    }

    public String getUpdateMethod() {
        return updateMethod;
    }

    public void setUpdateMethod(String updateMethod) {
        this.updateMethod = updateMethod == null ? null : updateMethod.trim();
    }

    public String getUpdateSource() {
        return updateSource;
    }

    public void setUpdateSource(String updateSource) {
        this.updateSource = updateSource == null ? null : updateSource.trim();
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public List<PluginPackageEntities> getPluginPackageEntities() {
        return pluginPackageEntities;
    }

    public void setPluginPackageEntities(List<PluginPackageEntities> pluginPackageEntities) {
        this.pluginPackageEntities = pluginPackageEntities;
    }
    
    
}