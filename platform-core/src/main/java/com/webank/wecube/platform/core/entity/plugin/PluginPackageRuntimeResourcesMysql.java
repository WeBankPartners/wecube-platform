package com.webank.wecube.platform.core.entity.plugin;

public class PluginPackageRuntimeResourcesMysql {
    private String id;

    private String pluginPackageId;

    private String schemaName;

    private String initFileName;

    private String upgradeFileName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getPluginPackageId() {
        return pluginPackageId;
    }

    public void setPluginPackageId(String pluginPackageId) {
        this.pluginPackageId = pluginPackageId == null ? null : pluginPackageId.trim();
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName == null ? null : schemaName.trim();
    }

    public String getInitFileName() {
        return initFileName;
    }

    public void setInitFileName(String initFileName) {
        this.initFileName = initFileName == null ? null : initFileName.trim();
    }

    public String getUpgradeFileName() {
        return upgradeFileName;
    }

    public void setUpgradeFileName(String upgradeFileName) {
        this.upgradeFileName = upgradeFileName == null ? null : upgradeFileName.trim();
    }
}