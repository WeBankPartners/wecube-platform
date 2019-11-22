package com.webank.wecube.platform.core.dto;

import com.webank.wecube.platform.core.domain.plugin.PluginConfig;
import com.webank.wecube.platform.core.domain.plugin.PluginConfigInterface;
import com.webank.wecube.platform.core.domain.plugin.PluginConfigInterfaceParameter;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import java.util.List;
import java.util.Set;


import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newLinkedHashSet;

public class PluginConfigInterfaceDto {
    private String id;
    private String pluginConfigId;
    private String action;
    private String serviceName;
    private String serviceDisplayName;
    private String path;
    private String httpMethod;
    private List<PluginConfigInterfaceParameterDto> inputParameters;
    private List<PluginConfigInterfaceParameterDto> outputParameters;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPluginConfigId() {
        return pluginConfigId;
    }

    public void setPluginConfigId(String pluginConfig) {
        this.pluginConfigId = pluginConfig;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceDisplayName() {
        return serviceDisplayName;
    }

    public void setServiceDisplayName(String serviceDisplayName) {
        this.serviceDisplayName = serviceDisplayName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public List<PluginConfigInterfaceParameterDto> getInputParameters() {
        return inputParameters;
    }

    public void setInputParameters(List<PluginConfigInterfaceParameterDto> inputParameters) {
        this.inputParameters = inputParameters;
    }

    public List<PluginConfigInterfaceParameterDto> getOutputParameters() {
        return outputParameters;
    }

    public void setOutputParameters(List<PluginConfigInterfaceParameterDto> outputParameters) {
        this.outputParameters = outputParameters;
    }

    public PluginConfigInterfaceDto() {
    }

    public PluginConfigInterfaceDto(String id, String pluginConfigId, String action, String serviceName, String serviceDisplayName, String path, String httpMethod, List<PluginConfigInterfaceParameterDto> inputParameters, List<PluginConfigInterfaceParameterDto> outputParameters) {
        this.id = id;
        this.pluginConfigId = pluginConfigId;
        this.action = action;
        this.serviceName = serviceName;
        this.serviceDisplayName = serviceDisplayName;
        this.path = path;
        this.httpMethod = httpMethod;
        this.inputParameters = inputParameters;
        this.outputParameters = outputParameters;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

    public PluginConfigInterface toDomain(PluginConfig pluginConfig) {
        PluginConfigInterface pluginConfigInterface = new PluginConfigInterface();
        pluginConfigInterface.setId(getId());
        pluginConfigInterface.setPluginConfig(pluginConfig);
        pluginConfigInterface.setAction(getAction());
        pluginConfigInterface.setServiceName(getServiceName());
        pluginConfigInterface.setServiceDisplayName(getServiceDisplayName());
        pluginConfigInterface.setPath(getPath());
        pluginConfigInterface.setHttpMethod(getHttpMethod());
        Set<PluginConfigInterfaceParameter> pluginConfigInterfaceInputParameters = newLinkedHashSet();
        if (null != getInputParameters() && getInputParameters().size() > 0) {
            getInputParameters().forEach(inputParameter ->pluginConfigInterfaceInputParameters.add(inputParameter.toDomain(pluginConfigInterface, PluginConfigInterfaceParameter.TYPE_INPUT)));
        }
        pluginConfigInterface.setInputParameters(pluginConfigInterfaceInputParameters);

        Set<PluginConfigInterfaceParameter> pluginConfigInterfaceOutputParameters = newLinkedHashSet();
        if (null != getOutputParameters() && getOutputParameters().size() > 0) {
            getOutputParameters().forEach(outputParameter ->pluginConfigInterfaceOutputParameters.add(outputParameter.toDomain(pluginConfigInterface, PluginConfigInterfaceParameter.TYPE_OUTPUT)));
        }
        pluginConfigInterface.setOutputParameters(pluginConfigInterfaceOutputParameters);

        return pluginConfigInterface;
    }

    public static PluginConfigInterfaceDto fromDomain(PluginConfigInterface pluginConfigInterface) {
        PluginConfigInterfaceDto pluginConfigInterfaceDto = new PluginConfigInterfaceDto();
        pluginConfigInterfaceDto.setId(pluginConfigInterface.getId());
        pluginConfigInterfaceDto.setPluginConfigId(pluginConfigInterface.getPluginConfig().getId());

        pluginConfigInterfaceDto.setPath(pluginConfigInterface.getPath());
        pluginConfigInterfaceDto.setServiceName(pluginConfigInterface.getServiceName());
        pluginConfigInterfaceDto.setServiceDisplayName(pluginConfigInterface.getServiceDisplayName());
        pluginConfigInterfaceDto.setAction(pluginConfigInterface.getAction());
        pluginConfigInterfaceDto.setHttpMethod(pluginConfigInterface.getHttpMethod());

        List<PluginConfigInterfaceParameterDto> interfaceInputParameterDtos = newArrayList();
        if (null != pluginConfigInterface.getInputParameters() && pluginConfigInterface.getInputParameters().size() > 0) {
            pluginConfigInterface.getInputParameters().forEach(pluginConfigInterfaceParameter -> interfaceInputParameterDtos.add(PluginConfigInterfaceParameterDto.fromDomain(pluginConfigInterfaceParameter)));
        }
        pluginConfigInterfaceDto.setInputParameters(interfaceInputParameterDtos);
        List<PluginConfigInterfaceParameterDto> interfaceOutputParameterDtos = newArrayList();
        if (null != pluginConfigInterface.getOutputParameters() && pluginConfigInterface.getOutputParameters().size() > 0) {
            pluginConfigInterface.getOutputParameters().forEach(pluginConfigInterfaceParameter -> interfaceOutputParameterDtos.add(PluginConfigInterfaceParameterDto.fromDomain(pluginConfigInterfaceParameter)));
        }
        pluginConfigInterfaceDto.setOutputParameters(interfaceOutputParameterDtos);
        return pluginConfigInterfaceDto;
    }

}
