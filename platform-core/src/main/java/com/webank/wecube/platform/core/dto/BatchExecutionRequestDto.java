package com.webank.wecube.platform.core.dto;

import java.util.List;

import com.webank.wecube.platform.core.domain.plugin.PluginConfigInterface;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageAttribute;

public class BatchExecutionRequestDto {

    private PluginConfigInterface pluginConfigInterface;
    private String packageName;
    private String entityName;
    private List<InputParameterDefinition> inputParameterDefinitions;
//    private List<String> rootEntityIds;
    private PluginPackageAttribute businessKeyAttribute;
    private List<ResourceData> resourceDatas;

    public class ResourceData {
        private Object businessKeyValue;
        private String id;

        public Object getBusinessKeyValue() {
            return businessKeyValue;
        }

        public void setBusinessKeyValue(Object businessKeyValue) {
            this.businessKeyValue = businessKeyValue;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

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

    public PluginPackageAttribute getBusinessKeyAttribute() {
        return businessKeyAttribute;
    }

    public void setBusinessKeyAttribute(PluginPackageAttribute businessKeyAttribute) {
        this.businessKeyAttribute = businessKeyAttribute;
    }

    public List<ResourceData> getResourceDatas() {
        return resourceDatas;
    }

    public void setResourceDatas(List<ResourceData> resourceDatas) {
        this.resourceDatas = resourceDatas;
    }

}
