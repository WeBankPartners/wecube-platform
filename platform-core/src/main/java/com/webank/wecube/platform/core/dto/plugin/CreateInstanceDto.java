package com.webank.wecube.platform.core.dto.plugin;

public class CreateInstanceDto {
    private String imageName;
    private String containerName;
    private String portBindingParameters;
    private String volumeBindingParameters;
    private String envVariableParameters;

    public CreateInstanceDto(String imageName, String containerName, String portBindingParameters,
            String volumeBindingParameters) {
        super();
        this.imageName = imageName;
        this.containerName = containerName;
        this.portBindingParameters = portBindingParameters;
        this.volumeBindingParameters = volumeBindingParameters;
    }

    public CreateInstanceDto() {
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

    public String getPortBindingParameters() {
        return portBindingParameters;
    }

    public void setPortBindingParameters(String portBindingParameters) {
        this.portBindingParameters = portBindingParameters;
    }

    public String getVolumeBindingParameters() {
        return volumeBindingParameters;
    }

    public void setVolumeBindingParameters(String volumeBindingParameters) {
        this.volumeBindingParameters = volumeBindingParameters;
    }

    public String getEnvVariableParameters() {
        return envVariableParameters;
    }

    public void setEnvVariableParameters(String envVariableParameters) {
        this.envVariableParameters = envVariableParameters;
    }

}
