//package com.webank.wecube.platform.core.domain.plugin;
//
//import com.fasterxml.jackson.annotation.JsonBackReference;
//import com.fasterxml.jackson.annotation.JsonInclude;
//import com.webank.wecube.platform.core.support.DomainIdBuilder;
//import org.apache.commons.lang.builder.ReflectionToStringBuilder;
//
//import javax.persistence.*;
//
//@Entity
//@Table(name = "plugin_config_interface_parameters")
//public class PluginConfigInterfaceParameter {
//
//    public static final String TYPE_INPUT = "INPUT";
//    public static final String TYPE_OUTPUT = "OUTPUT";
//
//    public static final String MAPPING_TYPE_NOT_AVAILABLE = "N/A";
//    public static final String MAPPING_TYPE_CMDB_CI_TYPE = "CMDB_CI_TYPE";
//
//    @Id
//    private String id;
//
//    @JsonBackReference
//    @ManyToOne
//    @JoinColumn(name = "plugin_config_interface_id")
//    private PluginConfigInterface pluginConfigInterface;
//
//    @Column
//    private String type;
//    @Column
//    private String name;
//    @Column
//    private String dataType;
//    @Column
//    private String mappingType;
//    @Column
//    private String mappingEntityExpression;
//    @Column
//    private String mappingSystemVariableName;
//    @Column
//    private String required;
//    
//    @Column
//    private String sensitiveData;
//
//    @JsonInclude
//    public String getInterfaceId() {
//        return pluginConfigInterface == null ? null : pluginConfigInterface.getId();
//    }
//
//    public PluginConfigInterfaceParameter() {
//    }
//
//    public PluginConfigInterfaceParameter(String id, PluginConfigInterface pluginConfigInterface, String type, String name, String dataType, String mappingType, String mappingEntityExpression, String mappingSystemVariableName, String required) {
//        this.id = id;
//        this.pluginConfigInterface = pluginConfigInterface;
//        this.type = type;
//        this.name = name;
//        this.dataType = dataType;
//        this.mappingType = mappingType;
//        this.mappingEntityExpression = mappingEntityExpression;
//        this.mappingSystemVariableName = mappingSystemVariableName;
//        this.required = required;
//    }
//
//
//    public String getId() {
//        return id;
//    }
//
//    @PrePersist
//    public void initId() {
//        this.id = DomainIdBuilder.buildDomainId(this);
//    }
//
//    public void setId(String id) {
//        this.id = id;
//    }
//
//    public PluginConfigInterface getPluginConfigInterface() {
//        return pluginConfigInterface;
//    }
//
//    public void setPluginConfigInterface(PluginConfigInterface pluginConfigInterface) {
//        this.pluginConfigInterface = pluginConfigInterface;
//    }
//
//    public String getType() {
//        return type;
//    }
//
//    public void setType(String type) {
//        this.type = type;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public String getDataType() {
//        return dataType;
//    }
//
//    public void setDataType(String dataType) {
//        this.dataType = dataType;
//    }
//
//    public String getMappingType() {
//        return mappingType;
//    }
//
//    public void setMappingType(String mappingType) {
//        this.mappingType = mappingType;
//    }
//
//    public String getMappingEntityExpression() {
//        return mappingEntityExpression;
//    }
//
//    public void setMappingEntityExpression(String mappingEntityExpression) {
//        this.mappingEntityExpression = mappingEntityExpression;
//    }
//
//    public String getMappingSystemVariableName() {
//        return mappingSystemVariableName;
//    }
//
//    public void setMappingSystemVariableName(String mappingSystemVariableName) {
//        this.mappingSystemVariableName = mappingSystemVariableName;
//    }
//
//    public String getRequired() {
//        return required;
//    }
//
//    public void setRequired(String required) {
//        this.required = required;
//    }
//
//    @Override
//    public String toString() {
//        return ReflectionToStringBuilder.toStringExclude(this, "pluginConfigInterface");
//    }
//
//    public String getSensitiveData() {
//        return sensitiveData;
//    }
//
//    public void setSensitiveData(String sensitiveData) {
//        this.sensitiveData = sensitiveData;
//    }
//}
