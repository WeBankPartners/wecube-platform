package com.webank.wecube.platform.core.dto.plugin;

public class PluginPackageRuntimeResourcesDockerDto {
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
        this.id = id;
    }

    public String getPluginPackageId() {
        return pluginPackageId;
    }

    public void setPluginPackageId(String pluginPackageId) {
        this.pluginPackageId = pluginPackageId;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public String getPortBindings() {
        return portBindings;
    }

    public void setPortBindings(String portBindings) {
        this.portBindings = portBindings;
    }

    public String getVolumeBindings() {
        return volumeBindings;
    }

    public void setVolumeBindings(String volumeBindings) {
        this.volumeBindings = volumeBindings;
    }

    public String getEnvVariables() {
        return envVariables;
    }

    public void setEnvVariables(String envVariables) {
        this.envVariables = envVariables;
    }

}
