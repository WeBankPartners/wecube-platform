package com.webank.wecube.platform.core.service.plugin.xmltype;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "inputParametersType", propOrder = {
    "parameter"
})
public class PluginConfigInputParametersType {
    @XmlElement(required = true)
    protected List<PluginConfigInputParameterType> parameter;

    public List<PluginConfigInputParameterType> getParameter() {
        if (parameter == null) {
            parameter = new ArrayList<PluginConfigInputParameterType>();
        }
        return this.parameter;
    }
}
