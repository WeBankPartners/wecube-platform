package com.webank.wecube.platform.core.service.plugin.xmltype;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


@XmlRootElement(name = "package", namespace = "")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "packageType", propOrder = { "name", "version", "paramObjects", "plugins", "systemParameters" })
public class PluginPackageType {
    @XmlAttribute(name = "name")
    protected String name;
    @XmlAttribute(name = "version")
    protected String version;
    @XmlElement(name = "paramObjects", required = false)
    protected ParamObjectsType paramObjects;
    @XmlElement(name = "plugins", required = false)
    protected PluginConfigsType plugins;
    @XmlElement(name = "systemParameters", required = false)
    protected SystemParametersType systemParameters;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public PluginConfigsType getPlugins() {
        return plugins;
    }

    public void setPlugins(PluginConfigsType plugins) {
        this.plugins = plugins;
    }

    public SystemParametersType getSystemParameters() {
        return systemParameters;
    }

    public void setSystemParameters(SystemParametersType systemParameters) {
        this.systemParameters = systemParameters;
    }

    public ParamObjectsType getParamObjects() {
        return paramObjects;
    }

    public void setParamObjects(ParamObjectsType paramObjects) {
        this.paramObjects = paramObjects;
    }

    
}
