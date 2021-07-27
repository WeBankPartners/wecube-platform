package com.webank.wecube.platform.core.service.plugin;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
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

    protected boolean isMultipleData(String multipleFlag) {
        return Constants.DATA_MULTIPLE.equals(multipleFlag);
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

    protected Object convertStringToBasicPropertyValue(String dataType, Object dataValueObject) {
        if (isStringDataType(dataType)) {
            return dataValueObject;
        }

        if (isNumberDataType(dataType)) {
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

        String multipleFlag = propertyMeta.getMultiple();

        String dataType = propertyMeta.getDataType();
        if (isMultipleData(multipleFlag)) {
            if (isBasicDataType(dataType)) {
                StringBuilder sb = new StringBuilder();
                List<Object> objects = (List<Object>) dataValueObject;
                for (Object obj : objects) {
                    sb.append(String.valueOf(obj)).append(",");
                }

                return sb.toString();
            }

            if (isObjectDataType(dataType)) {
                List<CoreObjectVar> listVars = (List<CoreObjectVar>) dataValueObject;
                StringBuilder sb = new StringBuilder();
                for (CoreObjectVar v : listVars) {
                    sb.append(v.getId()).append(",");
                }

                return sb.toString();
            }
        } else {
            if (isBasicDataType(dataType)) {
                return String.valueOf(dataValueObject);
            }

            if (isObjectDataType(dataType)) {
                CoreObjectVar objVar = (CoreObjectVar) dataValueObject;
                return objVar.getId();
            }
        }

        throw new WecubeCoreException("Unsupported data type :" + dataType);
    }

    protected String assembleValueList(List<Object> retDataValues) {
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        sb.append("[");

        for (Object dv : retDataValues) {
            if (!isFirst) {
                sb.append(",");
            } else {
                isFirst = false;
            }

            sb.append(dv == null ? "" : dv);
        }

        sb.append("]");

        return sb.toString();
    }

}
