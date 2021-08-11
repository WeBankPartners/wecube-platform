package com.webank.wecube.platform.core.dto.plugin;

import java.util.List;

public class BatchExecutionRequestDto {

    private PluginConfigInterfaceDto pluginConfigInterface;
    private String packageName;
    private String entityName;
    private List<InputParameterDefinitionDto> inputParameterDefinitions;
    private BusinessKeyAttributeDto businessKeyAttribute;
    private List<ResourceDataDto> resourceDatas;

    public BatchExecutionRequestDto() {
    }

    public PluginConfigInterfaceDto getPluginConfigInterface() {
        return pluginConfigInterface;
    }

    public void setPluginConfigInterface(PluginConfigInterfaceDto pluginConfigInterface) {
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

    public List<InputParameterDefinitionDto> getInputParameterDefinitions() {
        return inputParameterDefinitions;
    }

    public void setInputParameterDefinitions(List<InputParameterDefinitionDto> inputParameterDefinitions) {
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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("BatchExecutionRequestDto [pluginConfigInterface=");
		builder.append(pluginConfigInterface);
		builder.append(", packageName=");
		builder.append(packageName);
		builder.append(", entityName=");
		builder.append(entityName);
		builder.append(", inputParameterDefinitions=");
		builder.append(inputParameterDefinitions);
		builder.append(", businessKeyAttribute=");
		builder.append(businessKeyAttribute);
		builder.append(", resourceDatas=");
		builder.append(resourceDatas);
		builder.append("]");
		return builder.toString();
	}

    
}
