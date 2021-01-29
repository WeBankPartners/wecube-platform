package com.webank.wecube.platform.core.entity.plugin;

public class PluginPackageRuntimeResourcesDocker {
    private String id;

    private String pluginPackageId;

    private String imageName;

    private String containerName;

    private String portBindings;

    private String volumeBindings;

    private String envVariables;

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

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName == null ? null : imageName.trim();
    }

    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName == null ? null : containerName.trim();
    }

    public String getPortBindings() {
        return portBindings;
    }

    public void setPortBindings(String portBindings) {
        this.portBindings = portBindings == null ? null : portBindings.trim();
    }

    public String getVolumeBindings() {
        return volumeBindings;
    }

    public void setVolumeBindings(String volumeBindings) {
        this.volumeBindings = volumeBindings == null ? null : volumeBindings.trim();
    }

    public String getEnvVariables() {
        return envVariables;
    }

    public void setEnvVariables(String envVariables) {
        this.envVariables = envVariables == null ? null : envVariables.trim();
    }
}