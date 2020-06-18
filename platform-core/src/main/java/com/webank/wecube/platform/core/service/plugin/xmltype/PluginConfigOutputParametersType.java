package com.webank.wecube.platform.core.service.plugin.xmltype;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "outputParametersType", propOrder = {
    "parameter"
})
public class PluginConfigOutputParametersType {
    protected List<PluginConfigOutputParameterType> parameter;
    
    public List<PluginConfigOutputParameterType> getParameter() {
        if (parameter == null) {
            parameter = new ArrayList<PluginConfigOutputParameterType>();
        }
        return this.parameter;
    }

}
