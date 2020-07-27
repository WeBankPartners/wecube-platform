package com.webank.wecube.platform.core.service.plugin.xmltype;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "systemParameterType", propOrder = { "name", "scopeType", "defaultValue", "value", "status", "source",
        "packageName" })
public class SystemParameterType {

    @XmlAttribute(name = "name")
    protected String name;
    @XmlAttribute(name = "scopeType")
    protected String scopeType;
    @XmlAttribute(name = "defaultValue")
    protected String defaultValue;
    @XmlAttribute(name = "value")
    protected String value;
    @XmlAttribute(name = "status")
    protected String status;
    @XmlAttribute(name = "source")
    protected String source;
    @XmlAttribute(name = "packageName")
    protected String packageName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScopeType() {
        return scopeType;
    }

    public void setScopeType(String scopeType) {
        this.scopeType = scopeType;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SystemParameterType [name=");
        builder.append(name);
        builder.append(", scopeType=");
        builder.append(scopeType);
        builder.append(", defaultValue=");
        builder.append(defaultValue);
        builder.append(", value=");
        builder.append(value);
        builder.append(", status=");
        builder.append(status);
        builder.append(", source=");
        builder.append(source);
        builder.append(", packageName=");
        builder.append(packageName);
        builder.append("]");
        return builder.toString();
    }

    
}
