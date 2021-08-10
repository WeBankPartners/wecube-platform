package com.webank.wecube.platform.core.entity.plugin;

import com.webank.wecube.platform.core.dto.plugin.PluginConfigInterfaceParameterDto;

public class ExecutionJobParameters {
    private Integer id;

    private Integer executionJobId;

    private String name;

    private String dataType;

    private String mappingType;

    private String mappingEntityExpression;

    private String mappingSystemVariableName;

    private String required;

    private String constantValue;

    private String value;
    
    //#2233
    private String multiple;
    
    //#2233
    private String refObjectName;
    
    private transient Object rawValue;

    private transient ExecutionJobs executionJob;

    private transient PluginConfigInterfaceParameterDto parameterDefinition;

    public ExecutionJobParameters() {

    }

    public ExecutionJobParameters(String name, String dataType, String mappingType, String mappingEntityExpression,
            String mappingSystemVariableName, String required, String value) {
        super();
        this.name = name;
        this.dataType = dataType;
        this.mappingType = mappingType;
        this.mappingEntityExpression = mappingEntityExpression;
        this.mappingSystemVariableName = mappingSystemVariableName;
        this.required = required;
        this.value = value;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getExecutionJobId() {
        return executionJobId;
    }

    public void setExecutionJobId(Integer executionJobId) {
        this.executionJobId = executionJobId;
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

    public String getConstantValue() {
        return constantValue;
    }

    public void setConstantValue(String constantValue) {
        this.constantValue = constantValue == null ? null : constantValue.trim();
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value == null ? null : value.trim();
    }

    public ExecutionJobs getExecutionJob() {
        return executionJob;
    }

    public void setExecutionJob(ExecutionJobs executionJob) {
        this.executionJob = executionJob;
    }

    public PluginConfigInterfaceParameterDto getParameterDefinition() {
        return parameterDefinition;
    }

    public void setParameterDefinition(PluginConfigInterfaceParameterDto parameterDefinition) {
        this.parameterDefinition = parameterDefinition;
    }

    public String getMultiple() {
        return multiple;
    }

    public void setMultiple(String multiple) {
        this.multiple = multiple;
    }

    public String getRefObjectName() {
        return refObjectName;
    }

    public void setRefObjectName(String refObjectName) {
        this.refObjectName = refObjectName;
    }

    public Object getRawValue() {
        return rawValue;
    }

    public void setRawValue(Object rawValue) {
        this.rawValue = rawValue;
    }
    
    

}