package com.webank.wecube.platform.core.entity.plugin;

public class PluginConfigInterfaceParameters {
    public static final String TYPE_INPUT = "INPUT";
    public static final String TYPE_OUTPUT = "OUTPUT";
    public static final String DATA_TYPE_OBJECT = "object";
    public static final String DATA_TYPE_STRING = "string";
    public static final String DATA_TYPE_NUMBER = "number";
    public static final String DATA_TYPE_LIST = "list";

    public static final String MAPPING_TYPE_NOT_AVAILABLE = "N/A";
    public static final String MAPPING_TYPE_CMDB_CI_TYPE = "CMDB_CI_TYPE";

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

    private String description;

    private String refType;

    private String refName;

    private transient PluginConfigInterfaces pluginConfigInterface;

    private transient CoreObjectMeta objectMeta;

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

    public PluginConfigInterfaces getPluginConfigInterface() {
        return pluginConfigInterface;
    }

    public void setPluginConfigInterface(PluginConfigInterfaces pluginConfigInterface) {
        this.pluginConfigInterface = pluginConfigInterface;
    }

    public CoreObjectMeta getObjectMeta() {
        return objectMeta;
    }

    public void setObjectMeta(CoreObjectMeta objectMeta) {
        this.objectMeta = objectMeta;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRefType() {
        return refType;
    }

    public void setRefType(String refType) {
        this.refType = refType;
    }

    public String getRefName() {
        return refName;
    }

    public void setRefName(String refName) {
        this.refName = refName;
    }

}