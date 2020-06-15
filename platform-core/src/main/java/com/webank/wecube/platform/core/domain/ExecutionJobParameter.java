package com.webank.wecube.platform.core.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import javax.persistence.*;

@Entity
@Table(name = "execution_job_parameters")
public class ExecutionJobParameter {

    public static final String TYPE_INPUT = "INPUT";
    public static final String TYPE_OUTPUT = "OUTPUT";

    public static final String MAPPING_TYPE_NOT_AVAILABLE = "N/A";
    public static final String MAPPING_TYPE_CMDB_CI_TYPE = "CMDB_CI_TYPE";

    @Id
    @GeneratedValue
    private Integer id;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "execution_job_id")
    private ExecutionJob executionJob;

    @Column
    private String name;
    @Column
    private String dataType;
    @Column
    private String mappingType;
    @Column
    private String mappingEntityExpression;
    @Column
    private String mappingSystemVariableName;
    @Column
    private String required;
    @Column
    private String value;

    public ExecutionJobParameter() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public String getMappingSystemVariableName() {
        return mappingSystemVariableName;
    }

    public void setMappingSystemVariableName(String mappingSystemVariableName) {
        this.mappingSystemVariableName = mappingSystemVariableName;
    }

    public String getRequired() {
        return required;
    }

    public void setRequired(String required) {
        this.required = required;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toStringExclude(this, "pluginConfigInterface");
    }

    public ExecutionJob getExecutionJob() {
        return executionJob;
    }

    public void setExecutionJob(ExecutionJob executionJob) {
        this.executionJob = executionJob;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public static String getTypeInput() {
        return TYPE_INPUT;
    }

    public static String getTypeOutput() {
        return TYPE_OUTPUT;
    }

    public static String getMappingTypeNotAvailable() {
        return MAPPING_TYPE_NOT_AVAILABLE;
    }

    public static String getMappingTypeCmdbCiType() {
        return MAPPING_TYPE_CMDB_CI_TYPE;
    }

    public ExecutionJobParameter(String name, String dataType, String mappingType, String mappingEntityExpression,
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
}
