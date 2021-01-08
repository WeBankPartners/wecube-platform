package com.webank.wecube.platform.core.parser;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.service.plugin.xml.register.AttributeType;
import com.webank.wecube.platform.core.service.plugin.xml.register.DataModelType;
import com.webank.wecube.platform.core.service.plugin.xml.register.EntityType;

public class PluginPackageDataModelValidator {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    public void validateDataModel(DataModelType xmlDataModel){
        if(xmlDataModel == null){
            return;
        }
        
        List<EntityType> xmlEntityList = xmlDataModel.getEntity();
        if(xmlEntityList == null || xmlEntityList.isEmpty()){
            return;
        }
        
        for(EntityType xmlEntity : xmlEntityList){
            validateEntity(xmlEntity);
        }
    }
    
    private void validateEntity(EntityType xmlEntity){
        List<AttributeType> xmlAttributeList = xmlEntity.getAttribute();
        if(xmlAttributeList == null || xmlAttributeList.isEmpty()){
            return;
        }
        
        for(AttributeType xmlAttribute : xmlAttributeList){
            if (StringUtils.isEmpty(xmlAttribute.getDatatype())) {
                String msg = String.format(
                        "The Datatype should not be blank while registering entity [%s][%s]",
                        xmlEntity.getName(), xmlEntity.getDisplayName());
                logger.error(msg);
                throw new WecubeCoreException(msg);
            }
            
            if ("ref".equals(xmlAttribute.getDatatype()) && StringUtils.isEmpty(xmlAttribute.getRef())) {
                String msg = "Field [ref] should be specified when [dataType] is set to [\"ref\"]";
                logger.error(msg);
                throw new WecubeCoreException("3280",msg);
            }
        }
    }
}
