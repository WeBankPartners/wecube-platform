package com.webank.wecube.platform.core.service.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.webank.wecube.platform.core.utils.Constants;

public class PluginParamObject extends HashMap<String, Object> {

    /**
     * 
     */
    private static final long serialVersionUID = -2061663547512169756L;

    public PluginParamObject() {
        super();
    }

    public void setProperty(String propertyName, Object propertyValue) {
        this.put(propertyName, propertyValue);
    }

    public Object getProperty(String propertyName) {
        Object propertyValue = this.get(propertyName);
        return propertyValue;
    }

    public Set<String> getPropertyNames() {
        return this.keySet();
    }

    public static PluginParamObject wipeOffObjectIdAndClone(PluginParamObject obj) {
        if (obj == null) {
            return null;
        }

        PluginParamObject clonedObj = new PluginParamObject();
        for (String propertyName : obj.getPropertyNames()) {
            if (Constants.CORE_OBJECT_ID_KEY.equals(propertyName)) {
                continue;
            }

            if (Constants.CORE_OBJECT_NAME_KEY.equals(propertyName)) {
                continue;
            }
            Object propertyValue = obj.getProperty(propertyName);

            if (propertyValue instanceof PluginParamObject) {
                PluginParamObject objPropertyValue = (PluginParamObject) propertyValue;
                PluginParamObject clonedObjPropertyValue = wipeOffObjectIdAndClone(objPropertyValue);
                clonedObj.setProperty(propertyName, clonedObjPropertyValue);
            } else if (propertyValue instanceof List) {
                List<?> listPropertyValues = (List<?>) propertyValue;
                if (listPropertyValues == null || listPropertyValues.isEmpty()) {
                    clonedObj.setProperty(propertyName, listPropertyValues);
                } else {
                    List<Object> cloneListPropertyValues = new ArrayList<>(listPropertyValues.size());
                    for (Object listPropertyValue : listPropertyValues) {
                        if (listPropertyValue instanceof PluginParamObject) {
                            PluginParamObject objValue = (PluginParamObject) listPropertyValue;
                            PluginParamObject clonedObjValue = wipeOffObjectIdAndClone(objValue);
                            cloneListPropertyValues.add(clonedObjValue);
                        } else {
                            cloneListPropertyValues.add(listPropertyValue);
                        }
                    }
                }
            } else {
                clonedObj.setProperty(propertyName, propertyValue);
            }
        }

        return clonedObj;
    }

}
