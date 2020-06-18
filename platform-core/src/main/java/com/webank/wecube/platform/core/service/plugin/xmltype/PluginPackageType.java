package com.webank.wecube.platform.core.service.plugin.xmltype;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="package", namespace="")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "packageType", propOrder = { "name", "version", "plugins" })
public class PluginPackageType {
    @XmlAttribute(name = "name")
    protected String name;
    @XmlAttribute(name = "version")
    protected String version;

    protected PluginConfigsType plugins;

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

}
