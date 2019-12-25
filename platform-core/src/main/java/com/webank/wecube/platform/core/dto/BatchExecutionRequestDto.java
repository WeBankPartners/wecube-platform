package com.webank.wecube.platform.core.dto;

import java.util.List;

import com.webank.wecube.platform.core.domain.plugin.PluginConfigInterface;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageAttribute;

public class BatchExecutionRequestDto {

    private PluginConfigInterface pluginConfigInterface;
    private String packageName;
    private String entityName;
    private List<InputParameterDefinition> inputParameterDefinitions;
    private BusinessKeyAttributeDto businessKeyAttribute;
    private List<ResourceDataDto> resourceDatas;

    public BatchExecutionRequestDto() {
    }

    public PluginConfigInterface getPluginConfigInterface() {
        return pluginConfigInterface;
    }

    public void setPluginConfigInterface(PluginConfigInterface pluginConfigInterface) {
        this.pluginConfigInterface = pluginConfigInterface;
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

    public List<InputParameterDefinition> getInputParameterDefinitions() {
        return inputParameterDefinitions;
    }

    public void setInputParameterDefinitions(List<InputParameterDefinition> inputParameterDefinitions) {
        this.inputParameterDefinitions = inputParameterDefinitions;
    }

    public BusinessKeyAttributeDto getBusinessKeyAttribute() {
        return businessKeyAttribute;
    }

    public void setBusinessKeyAttribute(BusinessKeyAttributeDto businessKeyAttribute) {
        this.businessKeyAttribute = businessKeyAttribute;
    }

    public List<ResourceDataDto> getResourceDatas() {
        return resourceDatas;
    }

    public void setResourceDatas(List<ResourceDataDto> resourceDatas) {
        this.resourceDatas = resourceDatas;
    }

}
