package com.webank.wecube.platform.core.dto;

import com.webank.wecube.platform.core.domain.plugin.PluginPackageDataModel;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageEntity;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.collect.Sets.newLinkedHashSet;

public class PluginPackageDataModelDto {

    public static enum Source {
        PLUGIN_PACKAGE, DATA_MODEL_ENDPOINT
    }
    private Integer id;

    private Integer version;

    private String packageName;

    private boolean isDynamic;

    private String updatePath;

    private String updateMethod;

    private String updateSource;

    private String updateTimestamp;

    private Set<PluginPackageEntityDto> pluginPackageEntities;

    public PluginPackageDataModelDto() {
    }

    public PluginPackageDataModelDto(Integer id, Integer version, String packageName, boolean isDynamic, String updatePath, String updateMethod, String updateSource, String updateTimestamp, Set<PluginPackageEntityDto> pluginPackageEntities) {
        this.id = id;
        this.version = version;
        this.packageName = packageName;
        this.isDynamic = isDynamic;
        this.updatePath = updatePath;
        this.updateMethod = updateMethod;
        this.updateSource = updateSource;
        this.updateTimestamp = updateTimestamp;
        this.pluginPackageEntities = pluginPackageEntities;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    public String getUpdateTimestamp() {
        return updateTimestamp;
    }

    public void setUpdateTimestamp(String updateTimestamp) {
        this.updateTimestamp = updateTimestamp;
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
        if (!StringUtils.isEmpty(dataModelDto.getPackageName())) {
            dataModel.setPackageName(dataModelDto.getPackageName());
        }
        if (dataModelDto.isDynamic()) {
            dataModel.setDynamic(true);
            dataModel.setUpdatePath(dataModelDto.getUpdatePath());
            dataModel.setUpdateMethod(dataModelDto.getUpdateMethod());
        } else {
            dataModel.setDynamic(false);
        }
        dataModel.setUpdateTime(Long.valueOf(dataModelDto.getUpdateTimestamp()));
        if (null != dataModelDto.getPluginPackageEntities() && dataModelDto.getPluginPackageEntities().size() > 0) {
            dataModel.setPluginPackageEntities(dataModelDto.getPluginPackageEntities().stream().map(entityDto -> PluginPackageEntityDto.toDomain(entityDto)).collect(Collectors.toSet()));
        }

        return dataModel;
    }
}
