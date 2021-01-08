package com.webank.wecube.platform.core.entity.plugin;

public class RolePluginInterface {
    private String id;

    private String roleName;

    private String pluginInterfaceId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName == null ? null : roleName.trim();
    }

    public String getPluginInterfaceId() {
        return pluginInterfaceId;
    }

    public void setPluginInterfaceId(String pluginInterfaceId) {
        this.pluginInterfaceId = pluginInterfaceId == null ? null : pluginInterfaceId.trim();
    }
}