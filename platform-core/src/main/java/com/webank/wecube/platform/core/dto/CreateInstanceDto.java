package com.webank.wecube.platform.core.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateInstanceDto {
    private String portBindingParameters;
    private String volumeBindingParameters;
    private String envVariableParameters;
}
