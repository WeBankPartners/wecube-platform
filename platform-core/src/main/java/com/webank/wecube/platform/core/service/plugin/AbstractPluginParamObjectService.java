package com.webank.wecube.platform.core.service.plugin;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.webank.wecube.platform.core.entity.plugin.CoreObjectListVar;
import com.webank.wecube.platform.core.entity.plugin.CoreObjectPropertyMeta;
import com.webank.wecube.platform.core.entity.plugin.CoreObjectVar;
import com.webank.wecube.platform.core.repository.plugin.CoreObjectListVarMapper;
import com.webank.wecube.platform.core.repository.plugin.CoreObjectMetaMapper;
import com.webank.wecube.platform.core.repository.plugin.CoreObjectPropertyMetaMapper;
import com.webank.wecube.platform.core.repository.plugin.CoreObjectPropertyVarMapper;
import com.webank.wecube.platform.core.repository.plugin.CoreObjectVarMapper;
import com.webank.wecube.platform.core.utils.Constants;

public abstract class AbstractPluginParamObjectService {

    public static final String PREFIX_OBJECT_VAR_ID = "OV";

    public static final String PREFIX_PROPERTY_VAR_ID = "PV";

    public static final String PREFIX_LIST_VAR_ID = "EV";
    

    @Autowired
    protected CoreObjectMetaMapper coreObjectMetaMapper;

    @Autowired
    protected CoreObjectPropertyMetaMapper coreObjectPropertyMetaMapper;
    
    @Autowired
    protected CoreObjectVarMapper coreObjectVarMapper;
    
    @Autowired
    protected CoreObjectPropertyVarMapper coreObjectPropertyVarMapper;
    
    @Autowired
    protected CoreObjectListVarMapper coreObjectListVarMapper;

    protected boolean isStringDataType(String dataType) {
        return Constants.DATA_TYPE_STRING.equals(dataType);
    }

    protected boolean isNumberDataType(String dataType) {
        return Constants.DATA_TYPE_NUMBER.equals(dataType);
    }

    protected boolean isListDataType(String dataType) {
        return Constants.DATA_TYPE_LIST.equals(dataType);
    }

    protected boolean isObjectDataType(String dataType) {
        return Constants.DATA_TYPE_OBJECT.equals(dataType);
    }

    protected boolean isBasicDataType(String dataType) {
        return (isStringDataType(dataType) || isNumberDataType(dataType));
    }

    protected String convertCoreObjectListVarsToString(List<CoreObjectListVar> listVars) {
        StringBuilder sb = new StringBuilder();
        for (CoreObjectListVar v : listVars) {
            sb.append(v.getId()).append(",");
        }

        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    protected List<Integer> unmarshalNumbers(Object propertyValueObject) {
        List<Integer> numbers = new ArrayList<>();

        if (propertyValueObject == null) {
            return numbers;
        }

        if (propertyValueObject instanceof List) {
            List<Object> objs = (List<Object>) propertyValueObject;
            for (Object obj : objs) {
                Integer number = unmarshalNumber(obj);
                numbers.add(number);
            }

        } else {
            Integer number = unmarshalNumber(propertyValueObject);
            numbers.add(number);
        }

        return numbers;
    }

    protected Integer unmarshalNumber(Object obj) {
        if (obj instanceof Integer) {
            return (Integer) obj;
        }

        if (obj instanceof String) {
            return Integer.parseInt((String) obj);
        }

        return Integer.parseInt(obj.toString());
    }
    
    protected Object convertStringToBasicPropertyValue(String dataType, Object dataValueObject){
        if(isStringDataType(dataType)){
            return dataValueObject;
        }
        
        if(isNumberDataType(dataType)){
            Integer number = unmarshalNumber(dataValueObject);
            return number;
        }
        
        return null;
    }

    @SuppressWarnings("unchecked")
    protected String convertPropertyValueToString(CoreObjectPropertyMeta propertyMeta, Object dataValueObject) {
        if (dataValueObject == null) {
            return null;
        }

        String dataType = propertyMeta.getDataType();
        if (isStringDataType(dataType)) {
            return dataValueObject.toString();
        }

        if (isNumberDataType(dataType)) {
            return dataValueObject.toString();
        }

        if (isObjectDataType(dataType)) {
            CoreObjectVar objVar = (CoreObjectVar) dataValueObject;
            return objVar.getId();
        }

        if (isListDataType(dataType)) {
            List<CoreObjectListVar> listVars = (List<CoreObjectListVar>) dataValueObject;
            StringBuilder sb = new StringBuilder();
            for (CoreObjectListVar v : listVars) {
                sb.append(v.getId()).append(",");
            }

            return sb.toString();
        }

        return null;
    }

}
