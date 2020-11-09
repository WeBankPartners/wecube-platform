package com.webank.wecube.platform.core.entity.plugin;

public class PluginPackageEntities {
    private String id;

    private String dataModelId;

    private Integer dataModelVersion;

    private String packageName;

    private String name;

    private String displayName;

    private String description;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getDataModelId() {
        return dataModelId;
    }

    public void setDataModelId(String dataModelId) {
        this.dataModelId = dataModelId == null ? null : dataModelId.trim();
    }

    public Integer getDataModelVersion() {
        return dataModelVersion;
    }

    public void setDataModelVersion(Integer dataModelVersion) {
        this.dataModelVersion = dataModelVersion;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName == null ? null : packageName.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName == null ? null : displayName.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }
}