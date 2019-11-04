package com.webank.wecube.platform.core.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateInstanceDto {
    private String imageName;
    private String containerName;
    private String portBindingParameters;
    private String volumeBindingParameters;
    private String envVariableParameters;

    public CreateInstanceDto(String imageName, String containerName, String portBindingParameters,
            String volumeBindingParameters, String envVariableParameters) {
        super();
        this.imageName = imageName;
        this.containerName = containerName;
        this.portBindingParameters = portBindingParameters;
        this.volumeBindingParameters = volumeBindingParameters;
        this.envVariableParameters = envVariableParameters;
    }

    public CreateInstanceDto() {
    }
}
