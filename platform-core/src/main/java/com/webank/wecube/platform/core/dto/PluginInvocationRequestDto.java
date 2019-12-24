package com.webank.wecube.platform.core.dto;

import java.util.List;

import com.webank.wecube.platform.core.domain.plugin.PluginConfigInterface;
import com.webank.wecube.platform.core.model.workflow.InputParamAttr;

public class PluginInvocationRequestDto {
    private String packageName;
    private String entityName;
    private String rootEntityId;
    private List<InputParameterDefinition> inputParameterDefinitions;

    public PluginInvocationRequestDto() {
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getRootEntityId() {
        return rootEntityId;
    }

    public void setRootEntityId(String rootEntityId) {
        this.rootEntityId = rootEntityId;
    }

    public List<InputParameterDefinition> getInputParameterDefinitions() {
        return inputParameterDefinitions;
    }

    public void setInputParameterDefinitions(List<InputParameterDefinition> inputParameterDefinitions) {
        this.inputParameterDefinitions = inputParameterDefinitions;
    }

    public PluginInvocationRequestDto(String packageName, String entityName, String rootEntityId,
            List<InputParameterDefinition> inputParameterDefinitions) {
        super();
        this.packageName = packageName;
        this.entityName = entityName;
        this.rootEntityId = rootEntityId;
        this.inputParameterDefinitions = inputParameterDefinitions;
    }
}
