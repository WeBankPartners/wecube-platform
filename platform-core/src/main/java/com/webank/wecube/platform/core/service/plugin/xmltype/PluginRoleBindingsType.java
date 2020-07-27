package com.webank.wecube.platform.core.service.plugin.xmltype;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "roleBindsType", propOrder = {
    "roleBind"
})
public class PluginRoleBindingsType {

    protected List<PluginRoleBindingType> roleBind;
    
    public List<PluginRoleBindingType> getRoleBind() {
        if (roleBind == null) {
            roleBind = new ArrayList<PluginRoleBindingType>();
        }
        return this.roleBind;
    }
}
