package com.webank.wecube.platform.core.service.plugin.xmltype;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "systemParametersType", propOrder = { "systemParameter"})
public class SystemParametersType {

    protected List<SystemParameterType> systemParameter;
    
    public List<SystemParameterType> getSystemParameter(){
        if(systemParameter == null){
            systemParameter = new ArrayList<SystemParameterType>();
        }
        
        return systemParameter;
    }
}
