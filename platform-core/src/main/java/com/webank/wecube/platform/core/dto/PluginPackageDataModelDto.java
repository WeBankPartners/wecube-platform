package com.webank.wecube.platform.core.dto;


import java.util.Set;
import java.util.stream.Collectors;

import com.webank.wecube.platform.core.entity.plugin.PluginPackageDataModel;
import com.webank.wecube.platform.core.entity.plugin.PluginPackageEntities;

public class PluginPackageDataModelDto {

    public static enum Source {
        PLUGIN_PACKAGE, DATA_MODEL_ENDPOINT
    }
    private String id;

    private Integer version;

    private String packageName;

    private boolean isDynamic;

    private String updatePath;

    private String updateMethod;

    private String updateSource;

    private Long updateTime;

    private Set<PluginPackageEntityDto> pluginPackageEntities;

    public PluginPackageDataModelDto() {
    }

    public PluginPackageDataModelDto(String id, Integer version, String packageName, boolean isDynamic, String updatePath, String updateMethod, String updateSource, Long updateTime, Set<PluginPackageEntityDto> pluginPackageEntities) {
        this.id = id;
        this.version = version;
        this.packageName = packageName;
        this.isDynamic = isDynamic;
        this.updatePath = updatePath;
        this.updateMethod = updateMethod;
        this.updateSource = updateSource;
        this.updateTime = updateTime;
        this.pluginPackageEntities = pluginPackageEntities;
    }

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

    public Set<PluginPackageEntityDto> getPluginPackageEntities() {
        return pluginPackageEntities;
    }

    public void setPluginPackageEntities(Set<PluginPackageEntityDto> pluginPackageEntities) {
        this.pluginPackageEntities = pluginPackageEntities;
    }

    public static PluginPackageDataModel toDomain(PluginPackageDataModelDto dataModelDto) {
        PluginPackageDataModel dataModel = new PluginPackageDataModel();
        if (null != dataModelDto.getId()) {
            dataModel.setId(dataModelDto.getId());
        }
        if (null != dataModelDto.getVersion()) {
            dataModel.setVersion(dataModelDto.getVersion());
        }
        dataModel.setPackageName(dataModelDto.getPackageName());
        if (dataModelDto.isDynamic()) {
            dataModel.setIsDynamic(true);
            dataModel.setUpdatePath(dataModelDto.getUpdatePath());
            dataModel.setUpdateMethod(dataModelDto.getUpdateMethod());
        } else {
            dataModel.setIsDynamic(false);
        }
        dataModel.setUpdateSource(dataModelDto.getUpdateSource());
        dataModel.setUpdateTime(dataModelDto.getUpdateTime());
        if (dataModelDto.getPluginPackageEntities() != null && dataModelDto.getPluginPackageEntities().size() > 0) {
            Set<PluginPackageEntities> pluginPackageEntities = dataModelDto.getPluginPackageEntities().stream().map(entityDto -> PluginPackageEntityDto.toDomain(entityDto, dataModel)).collect(Collectors.toSet());
            dataModel.getPluginPackageEntities().addAll(pluginPackageEntities);
        }

        return dataModel;
    }

    
}
