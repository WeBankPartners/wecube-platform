package com.webank.wecube.platform.core.service.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.entity.plugin.CoreObjectMeta;
import com.webank.wecube.platform.core.entity.plugin.CoreObjectPropertyMeta;
import com.webank.wecube.platform.core.entity.plugin.CoreObjectPropertyVar;
import com.webank.wecube.platform.core.entity.plugin.CoreObjectVar;
import com.webank.wecube.platform.workflow.commons.LocalIdGenerator;

@Service
public class PluginParamObjectVarCalculationService extends AbstractPluginParamObjectService{
    
    public CoreObjectVar calculateCoreObjectVar(CoreObjectMeta objectMeta, CoreObjectVarCalculationContext ctx) {
        Map<String,CoreObjectVar> cachedObjectVars = new HashMap<String,CoreObjectVar>();
        CoreObjectVar rootObjectVar = doCalculateCoreObjectVar(objectMeta, ctx, cachedObjectVars);
        return rootObjectVar;
    }
    
    private CoreObjectVar doCalculateCoreObjectVar(CoreObjectMeta objectMeta, CoreObjectVarCalculationContext ctx, Map<String,CoreObjectVar> cachedObjectVars){
        if(objectMeta == null ){
            return null;
        }
        CoreObjectVar rootObjectVar = new CoreObjectVar();
        rootObjectVar.setId(LocalIdGenerator.generateId());
        rootObjectVar.setName(objectMeta.getName());
        rootObjectVar.setObjectMeta(objectMeta);
        rootObjectVar.setObjectMetaId(objectMeta.getId());
        rootObjectVar.setPackageName(objectMeta.getPackageName());
        
        List<CoreObjectPropertyMeta> propertyMetas = objectMeta.getPropertyMetas();
        for(CoreObjectPropertyMeta propertyMeta : propertyMetas){
            CoreObjectPropertyVar propertyVar = new CoreObjectPropertyVar();
            propertyVar.setId(LocalIdGenerator.generateId());
            propertyVar.setName(propertyMeta.getName());
            propertyVar.setObjectMetaId(rootObjectVar.getObjectMetaId());
            propertyVar.setDataType(propertyMeta.getDataType());
            
            Object dataValueObject = calculateDataValueObject(propertyMeta);
            String dataValue = convertPropertyValueToString(propertyMeta, dataValueObject);
            propertyVar.setDataValueObject(dataValueObject);
            propertyVar.setDataValue(dataValue);
            propertyVar.setPropertyMeta(propertyMeta);
            propertyVar.setSensitive(propertyMeta.getSensitive());
            propertyVar.setObjectVar(rootObjectVar);
            propertyVar.setObjectVarId(rootObjectVar.getId());
            
            
            rootObjectVar.addPropertyVar(propertyVar);
        }
        
        return rootObjectVar;
    }
    
    private String convertPropertyValueToString(CoreObjectPropertyMeta propertyMeta, Object dataValueObject){
        if(dataValueObject == null ){
            return null;
        }
        
        
        return null;
    }
    
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
    
    private Object calculateDataValueObject(CoreObjectPropertyMeta propertyMeta){
        String dataType = propertyMeta.getDataType();
        Object dataObjectValue = null;
        
        if(CoreObjectPropertyMeta.DATA_TYPE_STRING.equals(dataType)){
            dataObjectValue = calculateStringPropertyValue(propertyMeta);
        }else if(CoreObjectPropertyMeta.DATA_TYPE_NUMBER.equals(dataType)){
            dataObjectValue = calculateNumberPropertyValue(propertyMeta);
        }else if(CoreObjectPropertyMeta.DATA_TYPE_OBJECT.equals(dataType)){
            dataObjectValue = calculateObjectPropertyValue(propertyMeta);
        }else if(CoreObjectPropertyMeta.DATA_TYPE_LIST.equals(dataType)){
            dataObjectValue = calculateListPropertyValue(propertyMeta);
        }
        
        return dataObjectValue;
    }
    
    private Object calculateStringPropertyValue(CoreObjectPropertyMeta propertyMeta){
        if(CoreObjectPropertyMeta.DATA_TYPE_STRING.equals(propertyMeta.getDataType())){
            //TODO
            return String.valueOf(System.currentTimeMillis());
        }
        return null;
    }
    
    private Object calculateNumberPropertyValue(CoreObjectPropertyMeta propertyMeta){
        if(CoreObjectPropertyMeta.DATA_TYPE_NUMBER.equals(propertyMeta.getDataType())){
            //TODO
            return System.currentTimeMillis();
        }
        return null;
    }
    
    private Object calculateObjectPropertyValue(CoreObjectPropertyMeta propertyMeta){
        if(CoreObjectPropertyMeta.DATA_TYPE_OBJECT.equals(propertyMeta.getDataType())){
            CoreObjectMeta refObjectMeta = propertyMeta.getRefObjectMeta();
            //TODO
            CoreObjectVar refObjectVar = calculateCoreObjectVar(refObjectMeta, null);
            return refObjectVar;
        }
        return null;
    }
    
    private Object calculateListPropertyValue(CoreObjectPropertyMeta propertyMeta){
        if(CoreObjectPropertyMeta.DATA_TYPE_LIST.equals(propertyMeta.getDataType())){
            if(CoreObjectPropertyMeta.DATA_TYPE_STRING.equals(propertyMeta.getRefType())){
                List<String> refValues = new ArrayList<>();
                return refValues;
            }
            
            if(CoreObjectPropertyMeta.DATA_TYPE_NUMBER.equals(propertyMeta.getRefType())){
                List<Integer> refValues = new ArrayList<>();
                return refValues;
            }
            
            if(CoreObjectPropertyMeta.DATA_TYPE_OBJECT.equals(propertyMeta.getRefType())){
                List<CoreObjectVar> refValues = new ArrayList<>();
                
                
                return refValues;
            }
        }
        return null;
    }

}
