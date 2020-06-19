package com.webank.wecube.platform.core.service.plugin.xmltype;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "pluginsType", propOrder = {
    "plugin"
})
public class PluginConfigsType {

    protected List<PluginConfigType> plugin;
    
    public List<PluginConfigType> getPlugin() {
        if (plugin == null) {
            plugin = new ArrayList<PluginConfigType>();
        }
        return this.plugin;
    }
}
