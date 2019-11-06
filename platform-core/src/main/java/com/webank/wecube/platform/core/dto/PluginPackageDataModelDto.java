package com.webank.wecube.platform.core.dto;

import com.webank.wecube.platform.core.domain.plugin.PluginPackageDataModel;

import java.util.Set;

public class PluginPackageDataModelDto {

    public static enum Source {
        PLUGIN_PACKAGE, DATA_MODEL_ENDPOINT
    }
    private Integer id;

    private String packageName;

    private String packageVersion;

    private boolean isDynamic;

    private String updatePath;

    private String updateMethod;

    private String updateSource;

    private String updateTimestamp;

    private Set<PluginPackageEntityDto> pluginPackageEntities;

    public PluginPackageDataModelDto() {
    }

    public PluginPackageDataModelDto(Integer id, String packageName, String packageVersion, boolean isDynamic, String updatePath, String updateMethod, String updateSource, String updateTimestamp, Set<PluginPackageEntityDto> pluginPackageEntities) {
        this.id = id;
        this.packageName = packageName;
        this.packageVersion = packageVersion;
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

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageVersion() {
        return packageVersion;
    }

    public void setPackageVersion(String packageVersion) {
        this.packageVersion = packageVersion;
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

    public static PluginPackageDataModel toDomain(PluginPackageDataModelDto pluginPackageDataModelDto) {
        return null;
    }
}
