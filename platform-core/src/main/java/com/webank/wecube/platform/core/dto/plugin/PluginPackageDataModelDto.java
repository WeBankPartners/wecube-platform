package com.webank.wecube.platform.core.dto.plugin;

import java.util.ArrayList;
import java.util.List;

public class PluginPackageDataModelDto {

    private String id;

    private Integer version;

    private String packageName;

    private boolean isDynamic;

    private String updatePath;

    private String updateMethod;

    private String updateSource;

    private Long updateTime;

    private List<PluginPackageEntityDto> entities = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
        this.packageName = packageName;
    }

    public boolean isDynamic() {
        return isDynamic;
    }

    public void setDynamic(boolean dynamic) {
        isDynamic = dynamic;
    }

    public String getUpdatePath() {
        return updatePath;
    }

    public void setUpdatePath(String updatePath) {
        this.updatePath = updatePath;
    }

    public String getUpdateMethod() {
        return updateMethod;
    }

    public void setUpdateMethod(String updateMethod) {
        this.updateMethod = updateMethod;
    }

    public String getUpdateSource() {
        return updateSource;
    }

    public void setUpdateSource(String updateSource) {
        this.updateSource = updateSource;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public List<PluginPackageEntityDto> getEntities() {
        return entities;
    }

    public void setPluginPackageEntities(List<PluginPackageEntityDto> pluginPackageEntities) {
        this.entities = pluginPackageEntities;
    }

    public void addPluginPackageEntity(PluginPackageEntityDto pluginPackageEntityDto) {
        if (pluginPackageEntityDto == null) {
            return;
        }

        if (this.entities == null) {
            this.entities = new ArrayList<>();
        }

        this.entities.add(pluginPackageEntityDto);
    }

}
