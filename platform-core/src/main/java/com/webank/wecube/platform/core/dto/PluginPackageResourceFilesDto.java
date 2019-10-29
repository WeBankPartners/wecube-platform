package com.webank.wecube.platform.core.dto;

import com.webank.wecube.platform.core.domain.plugin.PluginPackageResourceFile;

import java.util.Set;

public class PluginPackageResourceFilesDto {
    private Integer pluginPackageId;
    private String pluginPackageName;
    private String pluginPackageVersion;
    private Set<PluginPackageResourceFile> pluginPackageResourceFiles;

    public PluginPackageResourceFilesDto() {
    }

    public PluginPackageResourceFilesDto(Integer pluginPackageId, String pluginPackageName, String pluginPackageVersion, Set<PluginPackageResourceFile> pluginPackageResourceFiles) {
        this.pluginPackageId = pluginPackageId;
        this.pluginPackageName = pluginPackageName;
        this.pluginPackageVersion = pluginPackageVersion;
        this.pluginPackageResourceFiles = pluginPackageResourceFiles;
    }

    public Integer getPluginPackageId() {
        return pluginPackageId;
    }

    public void setPluginPackageId(Integer pluginPackageId) {
        this.pluginPackageId = pluginPackageId;
    }

    public String getPluginPackageName() {
        return pluginPackageName;
    }

    public void setPluginPackageName(String pluginPackageName) {
        this.pluginPackageName = pluginPackageName;
    }

    public String getPluginPackageVersion() {
        return pluginPackageVersion;
    }

    public void setPluginPackageVersion(String pluginPackageVersion) {
        this.pluginPackageVersion = pluginPackageVersion;
    }

    public Set<PluginPackageResourceFile> getPluginPackageResourceFiles() {
        return pluginPackageResourceFiles;
    }

    public void setPluginPackageResourceFiles(Set<PluginPackageResourceFile> pluginPackageResourceFiles) {
        this.pluginPackageResourceFiles = pluginPackageResourceFiles;
    }
}
