package com.webank.wecube.platform.core.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.webank.wecube.platform.core.lazyDomain.plugin.LazyPluginPackage;

import java.sql.Timestamp;
import java.util.List;

public class PluginPackageInfoDto {
    private String id;

    private String name;

    private String version;

    private Timestamp uploadTimestamp;

    private boolean uiPackageIncluded;

    //TODO: to be removed.
    private List pluginConfigs;

    public static PluginPackageInfoDto fromDomain(LazyPluginPackage pluginPackage){
        PluginPackageInfoDto dto = new PluginPackageInfoDto();
        dto.setId(pluginPackage.getId());
        dto.setName(pluginPackage.getName());
        dto.setUiPackageIncluded(pluginPackage.isUiPackageIncluded());
        dto.setVersion(pluginPackage.getVersion());
        dto.setUploadTimestamp(pluginPackage.getUploadTimestamp());
        return dto;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Timestamp getUploadTimestamp() {
        return uploadTimestamp;
    }

    public void setUploadTimestamp(Timestamp uploadTimestamp) {
        this.uploadTimestamp = uploadTimestamp;
    }

    public boolean isUiPackageIncluded() {
        return uiPackageIncluded;
    }

    public void setUiPackageIncluded(boolean uiPackageIncluded) {
        this.uiPackageIncluded = uiPackageIncluded;
    }

    public List getPluginConfigs() {
        return pluginConfigs;
    }

    public void setPluginConfigs(List pluginConfigs) {
        this.pluginConfigs = pluginConfigs;
    }
}
