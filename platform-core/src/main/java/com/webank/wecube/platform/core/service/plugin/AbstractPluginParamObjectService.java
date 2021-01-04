package com.webank.wecube.platform.core.service.plugin;

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
        return CoreObjectPropertyMeta.DATA_TYPE_STRING.equals(dataType);
    }

    protected boolean isNumberDataType(String dataType) {
        return CoreObjectPropertyMeta.DATA_TYPE_NUMBER.equals(dataType);
    }

    protected boolean isListDataType(String dataType) {
        return CoreObjectPropertyMeta.DATA_TYPE_LIST.equals(dataType);
    }

    protected boolean isObjectDataType(String dataType) {
        return CoreObjectPropertyMeta.DATA_TYPE_OBJECT.equals(dataType);
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
