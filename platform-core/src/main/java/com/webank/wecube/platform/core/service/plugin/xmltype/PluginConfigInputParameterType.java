package com.webank.wecube.platform.core.service.plugin.xmltype;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "inputParameterType", propOrder = { "value" })
public class PluginConfigInputParameterType {

    @XmlValue
    protected String value;
    @XmlAttribute(name = "datatype", required = true)
    protected String datatype;
    @XmlAttribute(name = "mappingType")
    protected String mappingType;
    @XmlAttribute(name = "mappingEntityExpression")
    protected String mappingEntityExpression;
    @XmlAttribute(name = "mappingSystemVariableName")
    protected String mappingSystemVariableName;
    @XmlAttribute(name = "required", required = true)
    protected String required;
    @XmlAttribute(name = "sensitiveData")
    protected String sensitiveData;

    @XmlAttribute(name = "description")
    protected String description;
    
    @XmlAttribute(name = "mappingValue")
    protected String mappingValue;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDatatype() {
        return datatype;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
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

    public String getSensitiveData() {
        return sensitiveData;
    }

    public void setSensitiveData(String sensitiveData) {
        this.sensitiveData = sensitiveData;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMappingValue() {
        return mappingValue;
    }

    public void setMappingValue(String mappingValue) {
        this.mappingValue = mappingValue;
    }
    
    
}
