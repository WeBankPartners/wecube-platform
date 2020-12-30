package com.webank.wecube.platform.core.service.plugin;

import com.webank.wecube.platform.core.entity.plugin.CoreObjectPropertyMeta;

public abstract class AbstractPluginParamObjectService {
    
    protected boolean isStringDataType(String dataType){
        return CoreObjectPropertyMeta.DATA_TYPE_STRING.equals(dataType);
    }
    
    protected boolean isNumberDataType(String dataType){
        return CoreObjectPropertyMeta.DATA_TYPE_NUMBER.equals(dataType);
    }
    
    protected boolean isListDataType(String dataType){
        return CoreObjectPropertyMeta.DATA_TYPE_LIST.equals(dataType);
    }
    
    protected boolean isObjectDataType(String dataType){
        return CoreObjectPropertyMeta.DATA_TYPE_OBJECT.equals(dataType);
    }

}
