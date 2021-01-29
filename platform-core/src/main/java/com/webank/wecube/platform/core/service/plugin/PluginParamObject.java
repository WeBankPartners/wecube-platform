package com.webank.wecube.platform.core.service.plugin;

import java.util.HashMap;
import java.util.Set;

public class PluginParamObject extends HashMap<String, Object> {

    /**
     * 
     */
    private static final long serialVersionUID = -2061663547512169756L;
    
    public PluginParamObject(){
        super();
    }
    
    public void setProperty(String propertyName, Object propertyValue){
        this.put(propertyName, propertyValue);
    }
    
    public Object getProperty(String propertyName){
        Object propertyValue = this.get(propertyName);
        return propertyValue;
    }
    
    public Set<String> getPropertyNames(){
        return this.keySet();
    }

}
