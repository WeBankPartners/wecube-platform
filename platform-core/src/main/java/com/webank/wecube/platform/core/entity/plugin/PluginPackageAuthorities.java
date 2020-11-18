package com.webank.wecube.platform.core.entity.plugin;

public class PluginPackageAuthorities {
    private String id;

    private String pluginPackageId;

    private String roleName;

    private String menuCode;

    private transient PluginPackages pluginPackge;

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

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName == null ? null : roleName.trim();
    }

    public String getMenuCode() {
        return menuCode;
    }

    public void setMenuCode(String menuCode) {
        this.menuCode = menuCode == null ? null : menuCode.trim();
    }

    public PluginPackages getPluginPackge() {
        return pluginPackge;
    }

    public void setPluginPackge(PluginPackages pluginPackge) {
        this.pluginPackge = pluginPackge;
    }

}