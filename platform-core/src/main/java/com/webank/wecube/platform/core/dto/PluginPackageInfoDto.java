package com.webank.wecube.platform.core.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.webank.wecube.platform.core.domain.plugin.PluginPackage;
import com.webank.wecube.platform.core.lazyDomain.plugin.LazyPluginPackage;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class PluginPackageInfoDto {
    private String id;

    private String name;

    private String version;

    private Timestamp uploadTimestamp;

    private boolean uiPackageIncluded;

    private String status;

    //TODO: to be removed.
    private List pluginConfigs = new ArrayList();

    public static PluginPackageInfoDto fromDomain(LazyPluginPackage pluginPackage){
        PluginPackageInfoDto dto = new PluginPackageInfoDto();
        dto.setId(pluginPackage.getId());
        dto.setName(pluginPackage.getName());
        dto.setUiPackageIncluded(pluginPackage.isUiPackageIncluded());
        dto.setVersion(pluginPackage.getVersion());
        dto.setUploadTimestamp(pluginPackage.getUploadTimestamp());
        dto.setStatus(pluginPackage.getStatus().toString());
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
