package com.webank.wecube.platform.core.service.plugin.xmltype;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "pluginType", propOrder = { "name", "paramObject", "pluginInterface", "roleBinds" })
public class PluginConfigType {
    @XmlAttribute(name = "name")
    protected String name;
    @XmlAttribute(name = "targetPackage")
    protected String targetPackage;
    @XmlAttribute(name = "targetEntity")
    protected String targetEntity;
    @XmlAttribute(name = "targetEntityFilterRule")
    protected String targetEntityFilterRule;
    @XmlAttribute(name = "registerName")
    protected String registerName;
    @XmlAttribute(name = "status")
    protected String status;

    @XmlElement(name = "paramObject", required = false)
    protected List<ParamObjectType> paramObject;

    @XmlElement(name = "interface", required = true)
    protected List<PluginConfigInterfaceType> pluginInterface;

    @XmlElement(name = "roleBinds", required = false)
    protected PluginRoleBindingsType roleBinds;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTargetPackage() {
        return targetPackage;
    }

    public void setTargetPackage(String targetPackage) {
        this.targetPackage = targetPackage;
    }

    public String getTargetEntity() {
        return targetEntity;
    }

    public void setTargetEntity(String targetEntity) {
        this.targetEntity = targetEntity;
    }

    public String getTargetEntityFilterRule() {
        return targetEntityFilterRule;
    }

    public void setTargetEntityFilterRule(String targetEntityFilterRule) {
        this.targetEntityFilterRule = targetEntityFilterRule;
    }

    public String getRegisterName() {
        return registerName;
    }

    public void setRegisterName(String registerName) {
        this.registerName = registerName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<PluginConfigInterfaceType> getPluginInterface() {
        if (pluginInterface == null) {
            pluginInterface = new ArrayList<PluginConfigInterfaceType>();
        }
        return this.pluginInterface;
    }

    public PluginRoleBindingsType getRoleBinds() {
        return roleBinds;
    }

    public void setRoleBinds(PluginRoleBindingsType roleBinds) {
        this.roleBinds = roleBinds;
    }

    public List<ParamObjectType> getParamObject() {
        return paramObject;
    }

    public void setParamObject(List<ParamObjectType> paramObject) {
        this.paramObject = paramObject;
    }
}
