package com.webank.wecube.platform.core.dto.plugin;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

public class PluginConfigInterfaceDto {
    private String id;
    private String pluginConfigId;

    private String action;
    private String serviceName;
    private String serviceDisplayName;
    private String path;
    private String httpMethod;
    private String isAsyncProcessing;
    private String filterRule;
    private List<PluginConfigInterfaceParameterDto> inputParameters;
    private List<PluginConfigInterfaceParameterDto> outputParameters;
    
    private List<PluginConfigInterfaceParameterDto> configurableInputParameters = new ArrayList<>();

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

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

    public String getIsAsyncProcessing() {
        return isAsyncProcessing;
    }

    public void setIsAsyncProcessing(String isAsyncProcessing) {
        this.isAsyncProcessing = isAsyncProcessing;
    }

    public String getFilterRule() {
        return filterRule;
    }

    public void setFilterRule(String filterRule) {
        this.filterRule = filterRule;
    }

    public List<PluginConfigInterfaceParameterDto> getConfigurableInputParameters() {
        return configurableInputParameters;
    }

    public void setConfigurableInputParameters(List<PluginConfigInterfaceParameterDto> configurableInputParameters) {
        this.configurableInputParameters = configurableInputParameters;
    }
    
    public void addConfigurableInputParameter(PluginConfigInterfaceParameterDto configurableInputParameter) {
        if(configurableInputParameter == null){
            return;
        }
        for(PluginConfigInterfaceParameterDto p : this.configurableInputParameters){
            if(p.getName().equals(configurableInputParameter.getName())){
                return;
            }
        }
        
        this.configurableInputParameters.add(configurableInputParameter);
    }

}
