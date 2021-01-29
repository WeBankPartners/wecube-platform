package com.webank.wecube.platform.core.dto.plugin;

public class PluginPackageRuntimeResourcesMysqlDto {
    private String id;

    private String pluginPackageId;

    private String schemaName;

    private String initFileName;

    private String upgradeFileName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPluginPackageId() {
        return pluginPackageId;
    }

    public void setPluginPackageId(String pluginPackageId) {
        this.pluginPackageId = pluginPackageId;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getInitFileName() {
        return initFileName;
    }

    public void setInitFileName(String initFileName) {
        this.initFileName = initFileName;
    }

    public String getUpgradeFileName() {
        return upgradeFileName;
    }

    public void setUpgradeFileName(String upgradeFileName) {
        this.upgradeFileName = upgradeFileName;
    }

}
