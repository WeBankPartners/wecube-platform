package com.webank.wecube.platform.core.dto;

import com.webank.wecube.platform.core.domain.plugin.PluginConfigInterface;
import com.webank.wecube.platform.core.domain.plugin.PluginConfigInterfaceParameter;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

public class PluginConfigInterfaceParameterDto {
    private String id;
    private String pluginConfigInterfaceId;
    public static enum MappingType {
        context, entity, system_variable
    }
    private String type;
    private String name;
    private String dataType;
    private String mappingType;
    private String mappingEntityExpression;
    private String mappingSystemVariableId;
    private String required;

    public PluginConfigInterfaceParameterDto() {
    }

    public PluginConfigInterfaceParameterDto(String id, String pluginConfigInterfaceId, String type, String name, String dataType, String mappingType, String mappingEntityExpression, String mappingSystemVariableId, String required) {
        this.id = id;
        this.pluginConfigInterfaceId = pluginConfigInterfaceId;
        this.type = type;
        this.name = name;
        this.dataType = dataType;
        this.mappingType = mappingType;
        this.mappingEntityExpression = mappingEntityExpression;
        this.mappingSystemVariableId = mappingSystemVariableId;
        this.required = required;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPluginConfigInterfaceId() {
        return pluginConfigInterfaceId;
    }

    public void setPluginConfigInterfaceId(String pluginConfigInterfaceId) {
        this.pluginConfigInterfaceId = pluginConfigInterfaceId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getMappingType() {
        return mappingType;
    }

    public void setMappingType(String mappingType) {
        this.mappingType = mappingType;
    }

    public String getMappingEntityExpression() {
        return mappingEntityExpression;
    }

    public void setMappingEntityExpression(String mappingEntityExpression) {
        this.mappingEntityExpression = mappingEntityExpression;
    }

    public String getMappingSystemVariableId() {
        return mappingSystemVariableId;
    }

    public void setMappingSystemVariableId(String mappingSystemVariableId) {
        this.mappingSystemVariableId = mappingSystemVariableId;
    }

    public String getRequired() {
        return required;
    }

    public void setRequired(String required) {
        this.required = required;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

    public PluginConfigInterfaceParameter toDomain(PluginConfigInterface pluginConfigInterface, String type) {
        PluginConfigInterfaceParameter parameter = new PluginConfigInterfaceParameter();
        parameter.setPluginConfigInterface(pluginConfigInterface);

        if (pluginConfigInterface.getPluginConfig().getId() != null) {
            parameter.setId(getId());
        }
        parameter.setName(getName());
        parameter.setType(type);
        parameter.setDataType(getDataType());
        parameter.setMappingType(getMappingType());
        parameter.setMappingEntityExpression(getMappingEntityExpression());
        parameter.setMappingSystemVariableId(getMappingSystemVariableId());
        parameter.setRequired(getRequired());

        return parameter;
    }

    public static PluginConfigInterfaceParameterDto fromDomain(PluginConfigInterfaceParameter pluginConfigInterfaceParameter) {
        PluginConfigInterfaceParameterDto pluginConfigInterfaceParameterDto = new PluginConfigInterfaceParameterDto();
        pluginConfigInterfaceParameterDto.setId(pluginConfigInterfaceParameter.getId());
        pluginConfigInterfaceParameterDto.setPluginConfigInterfaceId(pluginConfigInterfaceParameter.getPluginConfigInterface().getId());
        pluginConfigInterfaceParameterDto.setType(pluginConfigInterfaceParameter.getType());
        pluginConfigInterfaceParameterDto.setName(pluginConfigInterfaceParameter.getName());
        pluginConfigInterfaceParameterDto.setDataType(pluginConfigInterfaceParameter.getDataType());
        pluginConfigInterfaceParameterDto.setMappingType(pluginConfigInterfaceParameter.getMappingType());
        pluginConfigInterfaceParameterDto.setMappingEntityExpression(pluginConfigInterfaceParameter.getMappingEntityExpression());
        pluginConfigInterfaceParameterDto.setMappingSystemVariableId(pluginConfigInterfaceParameter.getMappingSystemVariableId());
        pluginConfigInterfaceParameterDto.setRequired(pluginConfigInterfaceParameter.getRequired());
        return pluginConfigInterfaceParameterDto;
    }
}
