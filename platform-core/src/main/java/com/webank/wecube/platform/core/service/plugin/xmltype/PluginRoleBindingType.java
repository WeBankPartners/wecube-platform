package com.webank.wecube.platform.core.service.plugin.xmltype;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "roleBindType", propOrder = { "permission" , "roleName"})
public class PluginRoleBindingType {
    
    @XmlAttribute(name = "permission")
    protected String permission;
    @XmlAttribute(name = "roleName")
    protected String roleName;

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

}
