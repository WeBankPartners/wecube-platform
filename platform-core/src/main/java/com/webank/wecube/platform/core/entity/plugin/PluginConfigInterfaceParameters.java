package com.webank.wecube.platform.core.entity.plugin;

public class PluginConfigInterfaceParameters {
    private String id;

    private String pluginConfigInterfaceId;

    private String type;

    private String name;

    private String dataType;

    private String mappingType;

    private String mappingEntityExpression;

    private String mappingSystemVariableName;

    private String required;

    private String sensitiveData;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getPluginConfigInterfaceId() {
        return pluginConfigInterfaceId;
    }

    public void setPluginConfigInterfaceId(String pluginConfigInterfaceId) {
        this.pluginConfigInterfaceId = pluginConfigInterfaceId == null ? null : pluginConfigInterfaceId.trim();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type == null ? null : type.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType == null ? null : dataType.trim();
    }

    public String getMappingType() {
        return mappingType;
    }

    public void setMappingType(String mappingType) {
        this.mappingType = mappingType == null ? null : mappingType.trim();
    }

    public String getMappingEntityExpression() {
        return mappingEntityExpression;
    }

    public void setMappingEntityExpression(String mappingEntityExpression) {
        this.mappingEntityExpression = mappingEntityExpression == null ? null : mappingEntityExpression.trim();
    }

    public String getMappingSystemVariableName() {
        return mappingSystemVariableName;
    }

    public void setMappingSystemVariableName(String mappingSystemVariableName) {
        this.mappingSystemVariableName = mappingSystemVariableName == null ? null : mappingSystemVariableName.trim();
    }

    public String getRequired() {
        return required;
    }

    public void setRequired(String required) {
        this.required = required == null ? null : required.trim();
    }

    public String getSensitiveData() {
        return sensitiveData;
    }

    public void setSensitiveData(String sensitiveData) {
        this.sensitiveData = sensitiveData == null ? null : sensitiveData.trim();
    }
}