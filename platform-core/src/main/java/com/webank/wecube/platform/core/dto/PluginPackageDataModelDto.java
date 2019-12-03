package com.webank.wecube.platform.core.dto;

import com.webank.wecube.platform.core.domain.plugin.PluginPackageDataModel;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageEntity;

import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.collect.Sets.newLinkedHashSet;

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
            dataModel.setDynamic(true);
            dataModel.setUpdatePath(dataModelDto.getUpdatePath());
            dataModel.setUpdateMethod(dataModelDto.getUpdateMethod());
        } else {
            dataModel.setDynamic(false);
        }
        dataModel.setUpdateSource(dataModelDto.getUpdateSource());
        dataModel.setUpdateTime(dataModelDto.getUpdateTime());
        if (null != dataModelDto.getPluginPackageEntities() && dataModelDto.getPluginPackageEntities().size() > 0) {
            Set<PluginPackageEntity> pluginPackageEntities = dataModelDto.getPluginPackageEntities().stream().map(entityDto -> PluginPackageEntityDto.toDomain(entityDto, dataModel)).collect(Collectors.toSet());
            dataModel.setPluginPackageEntities(pluginPackageEntities);
        }

        return dataModel;
    }

    public static PluginPackageDataModelDto fromDomain(PluginPackageDataModel savedPluginPackageDataModel) {
        PluginPackageDataModelDto dataModelDto = new PluginPackageDataModelDto();
        dataModelDto.setId(savedPluginPackageDataModel.getId());
        dataModelDto.setVersion(savedPluginPackageDataModel.getVersion());
        dataModelDto.setPackageName(savedPluginPackageDataModel.getPackageName());
        dataModelDto.setUpdateSource(savedPluginPackageDataModel.getUpdateSource());
        dataModelDto.setUpdateTime(savedPluginPackageDataModel.getUpdateTime());
        dataModelDto.setDynamic(savedPluginPackageDataModel.isDynamic());
        if (savedPluginPackageDataModel.isDynamic()) {
            dataModelDto.setUpdatePath(savedPluginPackageDataModel.getUpdatePath());
            dataModelDto.setUpdateMethod(savedPluginPackageDataModel.getUpdateMethod());
        }
        if (null != savedPluginPackageDataModel.getPluginPackageEntities() && savedPluginPackageDataModel.getPluginPackageEntities().size() > 0) {
            Set<PluginPackageEntityDto> pluginPackageEntities = newLinkedHashSet();
            savedPluginPackageDataModel.getPluginPackageEntities().forEach(entity->pluginPackageEntities.add(PluginPackageEntityDto.fromDomain(entity)));
            dataModelDto.setPluginPackageEntities(pluginPackageEntities);
        }

        return dataModelDto;
    }
}
