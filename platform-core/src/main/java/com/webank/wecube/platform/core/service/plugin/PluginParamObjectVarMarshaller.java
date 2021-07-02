package com.webank.wecube.platform.core.service.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.entity.plugin.CoreObjectListVar;
import com.webank.wecube.platform.core.entity.plugin.CoreObjectMeta;
import com.webank.wecube.platform.core.entity.plugin.CoreObjectPropertyMeta;
import com.webank.wecube.platform.core.entity.plugin.CoreObjectPropertyVar;
import com.webank.wecube.platform.core.entity.plugin.CoreObjectVar;
import com.webank.wecube.platform.workflow.commons.LocalIdGenerator;

@Service
public class PluginParamObjectVarMarshaller extends AbstractPluginParamObjectService {

    private static final Logger log = LoggerFactory.getLogger(PluginParamObjectVarMarshaller.class);

    /**
     * 
     * @param rootObjectVar
     * @param ctx
     * @return
     */
    public PluginParamObject marshalPluginParamObject(CoreObjectVar rootObjectVar,
            CoreObjectVarCalculationContext ctx) {
        PluginParamObject rootParamObject = doAssemblePluginParamObject(rootObjectVar, ctx);
        return rootParamObject;
    }

    /**
     * 
     * @param paramObject
     * @param objectMeta
     * @param ctx
     * @return
     */
    public CoreObjectVar unmarshalPluginParamObject(Map<String, Object> paramObject, CoreObjectMeta objectMeta,
            CoreObjectVarCalculationContext ctx) {
        if (paramObject == null) {
            log.debug("param object to unmarshal is null.");
            return null;
        }

        if (objectMeta == null) {
            log.debug("object meta was not provided and unknow how to unmarshal.");
            return null;
        }

        CoreObjectVar objectVar = doUnmarshalPluginParamObject(paramObject, objectMeta, ctx);

        if (log.isInfoEnabled()) {
            log.info("unmarshalled param object:{}", paramObject);
        }

        return objectVar;

    }

    public CoreObjectVar doUnmarshalPluginParamObject(Map<String, Object> paramObject, CoreObjectMeta objectMeta,
            CoreObjectVarCalculationContext ctx) {
        CoreObjectVar rootObjectVar = new CoreObjectVar();
        rootObjectVar.setId(LocalIdGenerator.generateId(PREFIX_OBJECT_VAR_ID));
        rootObjectVar.setName(objectMeta.getName());
        rootObjectVar.setObjectMeta(objectMeta);
        rootObjectVar.setObjectMetaId(objectMeta.getId());
        rootObjectVar.setPackageName(objectMeta.getPackageName());

        List<CoreObjectPropertyMeta> propertyMetas = objectMeta.getPropertyMetas();
        if (propertyMetas == null || propertyMetas.isEmpty()) {
            return rootObjectVar;
        }

        for (CoreObjectPropertyMeta propertyMeta : propertyMetas) {
            Object propertyValueObject = paramObject.get(propertyMeta.getName());

            CoreObjectPropertyVar propertyVar = unmarshalPropertyVar(propertyValueObject, propertyMeta, ctx);
            propertyVar.setId(LocalIdGenerator.generateId(PREFIX_PROPERTY_VAR_ID));
            propertyVar.setObjectMetaId(rootObjectVar.getObjectMetaId());

            propertyVar.setObjectPropertyMetaId(propertyMeta.getId());
            propertyVar.setPropertyMeta(propertyMeta);
            propertyVar.setObjectVar(rootObjectVar);
            propertyVar.setObjectVarId(rootObjectVar.getId());
            propertyVar.setObjectName(objectMeta.getName());
            propertyVar.setPackageName(objectMeta.getPackageName());

            rootObjectVar.addPropertyVar(propertyVar);
        }

        return rootObjectVar;
    }

    private CoreObjectPropertyVar unmarshalPropertyVar(Object propertyValueObject, CoreObjectPropertyMeta propertyMeta,
            CoreObjectVarCalculationContext ctx) {
        CoreObjectPropertyVar propertyVar = new CoreObjectPropertyVar();
        propertyVar.setId(LocalIdGenerator.generateId(PREFIX_PROPERTY_VAR_ID));
        propertyVar.setName(propertyMeta.getName());
        propertyVar.setDataType(propertyMeta.getDataType());

        Object dataValueObject = unmarshalDataValueObject(propertyValueObject, propertyMeta, ctx);

        log.info("data value object for {} : {}", propertyMeta.getName(), dataValueObject);

        String dataValue = convertPropertyValueToString(propertyMeta, dataValueObject);
        propertyVar.setDataValueObject(dataValueObject);
        propertyVar.setDataValue(dataValue);
        propertyVar.setPropertyMeta(propertyMeta);
        propertyVar.setSensitive(propertyMeta.getSensitive());
        propertyVar.setObjectName(propertyMeta.getObjectName());
        propertyVar.setPackageName(propertyMeta.getPackageName());
        propertyVar.setObjectPropertyMetaId(propertyMeta.getId());

        return propertyVar;
    }

    private Object unmarshalDataValueObject(Object propertyValueObject, CoreObjectPropertyMeta propertyMeta,
            CoreObjectVarCalculationContext ctx) {
        if (propertyValueObject == null) {
            return null;
        }
        String dataType = propertyMeta.getDataType();
        Object dataObjectValue = null;

        if (isStringDataType(dataType)) {
            dataObjectValue = unmarshalStringPropertyValue(propertyValueObject, propertyMeta, ctx);
        } else if (isNumberDataType(dataType)) {
            dataObjectValue = unmarshalNumberPropertyValue(propertyValueObject, propertyMeta, ctx);
        } else if (isObjectDataType(dataType)) {
            dataObjectValue = unmarshalObjectPropertyValue(propertyValueObject, propertyMeta, ctx);
        } else if (isListDataType(dataType)) {
            dataObjectValue = unmarshalListPropertyValue(propertyValueObject, propertyMeta, ctx);
        }

        return dataObjectValue;
    }

    @SuppressWarnings("unchecked")
    private List<CoreObjectListVar> unmarshalListPropertyValue(Object propertyValueObject,
            CoreObjectPropertyMeta propertyMeta, CoreObjectVarCalculationContext ctx) {
        if (propertyValueObject == null) {
            return null;
        }
        if (isStringDataType(propertyMeta.getRefType())) {
            List<String> rawObjectValues = (List<String>) propertyValueObject;

            List<CoreObjectListVar> stringListVars = new ArrayList<>();
            for (String rawObject : rawObjectValues) {
                CoreObjectListVar stringListVar = new CoreObjectListVar();
                stringListVar.setId(LocalIdGenerator.generateId(PREFIX_LIST_VAR_ID));
                stringListVar.setDataType(propertyMeta.getRefType());
                String dataValue = rawObject;

                stringListVar.setDataValue(dataValue);
                stringListVar.setRawObjectValue(rawObject);
                stringListVar.setSensitive(propertyMeta.getSensitive());
                stringListVar.setObjectPropertyMeta(propertyMeta);

                stringListVars.add(stringListVar);
            }

            return stringListVars;
        }

        if (isNumberDataType(propertyMeta.getRefType())) {
            List<Integer> rawObjectValues = unmarshalNumbers(propertyValueObject);

            List<CoreObjectListVar> numberListVars = new ArrayList<>();
            for (Integer rawObject : rawObjectValues) {
                CoreObjectListVar numberListVar = new CoreObjectListVar();
                numberListVar.setId(LocalIdGenerator.generateId(PREFIX_LIST_VAR_ID));
                numberListVar.setDataType(propertyMeta.getRefType());
                String dataValue = String.valueOf(rawObject);

                numberListVar.setDataValue(dataValue);
                numberListVar.setRawObjectValue(rawObject);
                numberListVar.setSensitive(propertyMeta.getSensitive());
                numberListVar.setObjectPropertyMeta(propertyMeta);

                numberListVars.add(numberListVar);
            }

            return numberListVars;
        }

        if (isObjectDataType(propertyMeta.getRefType())) {
            List<CoreObjectVar> rawObjectValues = unmarshalObjectMetaPropertyAsListResult(propertyValueObject,
                    propertyMeta, ctx);

            List<CoreObjectListVar> objectListVars = new ArrayList<>();
            for (CoreObjectVar objectVar : rawObjectValues) {
                CoreObjectListVar objectListVar = new CoreObjectListVar();
                objectListVar.setId(LocalIdGenerator.generateId(PREFIX_LIST_VAR_ID));
                objectListVar.setDataType(propertyMeta.getRefType());

                String dataValue = objectVar.getId();

                objectListVar.setDataValue(dataValue);
                objectListVar.setRawObjectValue(objectVar);
                objectListVar.setSensitive(propertyMeta.getSensitive());
                objectListVar.setObjectPropertyMeta(propertyMeta);

                objectListVars.add(objectListVar);
            }

            return objectListVars;
        }
        return null;
    }

    private List<CoreObjectVar> unmarshalObjectMetaPropertyAsListResult(Object propertyValueObject,
            CoreObjectPropertyMeta propertyMeta, CoreObjectVarCalculationContext ctx) {
        if (propertyValueObject == null) {
            return null;
        }
        CoreObjectMeta refObjectMeta = propertyMeta.getRefObjectMeta();
        log.debug("unmarshal list object vars for objectName={},propertyName={}, refObject={} ",
                propertyMeta.getObjectMeta().getName(), propertyMeta.getName(), refObjectMeta.getName());
        List<CoreObjectVar> rawObjectValues = unmarshalObjectMetaAsListResult(propertyValueObject, refObjectMeta, ctx);

        return rawObjectValues;

    }

    @SuppressWarnings("unchecked")
    private List<CoreObjectVar> unmarshalObjectMetaAsListResult(Object propertyValueObject, CoreObjectMeta objectMeta,
            CoreObjectVarCalculationContext ctx) {
        if (!(propertyValueObject instanceof List)) {
            log.debug("property value type is {} and expect {}", propertyValueObject.getClass().getName(),
                    List.class.getName());
            return null;
        }

        List<Object> objs = (List<Object>) propertyValueObject;

        List<CoreObjectVar> rawObjectValues = new ArrayList<>();

        for (Object obj : objs) {
            Map<String, Object> paramObject = (Map<String, Object>) obj;
            CoreObjectVar objVar = doUnmarshalPluginParamObject(paramObject, objectMeta, ctx);

            rawObjectValues.add(objVar);
        }

        return rawObjectValues;
    }

    @SuppressWarnings("unchecked")
    private CoreObjectVar unmarshalObjectPropertyValue(Object propertyValueObject, CoreObjectPropertyMeta propertyMeta,
            CoreObjectVarCalculationContext ctx) {
        if (propertyValueObject == null) {
            return null;
        }

        Map<String, Object> paramObject = (Map<String, Object>) propertyValueObject;
        if (isObjectDataType(propertyMeta.getDataType())) {
            CoreObjectMeta refObjectMeta = propertyMeta.getRefObjectMeta();
            CoreObjectVar refObjectVar = doUnmarshalPluginParamObject(paramObject, refObjectMeta, ctx);
            return refObjectVar;
        }
        return null;
    }

    private Integer unmarshalNumberPropertyValue(Object propertyValueObject, CoreObjectPropertyMeta propertyMeta,
            CoreObjectVarCalculationContext ctx) {
        if (propertyValueObject == null) {
            return null;
        }
        if (isNumberDataType(propertyMeta.getDataType())) {
            if (propertyValueObject instanceof Integer) {
                return (int) propertyValueObject;
            }

            if (propertyValueObject instanceof String) {
                return Integer.parseInt((String) propertyValueObject);
            }

            return Integer.parseInt(propertyValueObject.toString());
        }
        return null;
    }

    private String unmarshalStringPropertyValue(Object propertyValueObject, CoreObjectPropertyMeta propertyMeta,
            CoreObjectVarCalculationContext ctx) {
        if (propertyValueObject == null) {
            return null;
        }
        if (isStringDataType(propertyMeta.getDataType())) {
            return String.valueOf(propertyValueObject);
        }
        return null;
    }

    private PluginParamObject doAssemblePluginParamObject(CoreObjectVar objectVar,
            CoreObjectVarCalculationContext ctx) {
        PluginParamObject paramObject = new PluginParamObject();

        List<CoreObjectPropertyVar> propertyVars = objectVar.getPropertyVars();
        if (propertyVars == null || propertyVars.isEmpty()) {
            return paramObject;
        }

        paramObject.setProperty("coreObjectId", objectVar.getId());
        paramObject.setProperty("coreObjectName", objectVar.getName());

        for (CoreObjectPropertyVar propertyVar : propertyVars) {
            String dataType = propertyVar.getDataType();
            log.debug("assemble object={},propertyName={}", objectVar.getName(), propertyVar.getName());
            if (isStringDataType(dataType)) {
                paramObject.setProperty(propertyVar.getName(), propertyVar.getDataValue());
            }

            if (isNumberDataType(dataType)) {
                paramObject.setProperty(propertyVar.getName(), Integer.parseInt(propertyVar.getDataValue()));
            }

            if (isObjectDataType(dataType)) {
                PluginParamObject propertyParamObject = doAssemblePluginParamObject(
                        (CoreObjectVar) propertyVar.getDataValueObject(), ctx);
                paramObject.setProperty(propertyVar.getName(), propertyParamObject);
            }

            if (isListDataType(dataType)) {
                List<Object> listPropertyVars = assembleListPropertyVars(propertyVar, ctx);
                paramObject.setProperty(propertyVar.getName(), listPropertyVars);
            }
        }

        return paramObject;

    }

    @SuppressWarnings("unchecked")
    private List<Object> assembleListPropertyVars(CoreObjectPropertyVar propertyVar,
            CoreObjectVarCalculationContext ctx) {
        if (propertyVar.getDataValueObject() == null) {
            return null;
        }
        CoreObjectPropertyMeta propertyMeta = propertyVar.getPropertyMeta();
        String refType = propertyMeta.getRefType();

        log.debug("assemble {} {} {} {}", propertyMeta.getObjectMeta().getName(), propertyVar.getName(),
                propertyVar.getDataType(), refType);
        if (isStringDataType(refType)) {
            List<Object> stringListValues = new ArrayList<>();
            log.debug("assemble string: {} {}", propertyVar.getDataValueObject().getClass().getName(),
                    propertyVar.getDataValueObject());
            List<CoreObjectListVar> stringListValueObjects = (List<CoreObjectListVar>) propertyVar.getDataValueObject();
            for (CoreObjectListVar listVar : stringListValueObjects) {
                stringListValues.add(listVar.getDataValue());
            }

            return stringListValues;
        }

        if (isNumberDataType(refType)) {
            List<Object> numberListValues = new ArrayList<>();
            log.debug("assemble number: {} {}", propertyVar.getDataValueObject().getClass().getSimpleName(),
                    propertyVar.getDataValueObject());
            List<CoreObjectListVar> numberListValueObjects = (List<CoreObjectListVar>) propertyVar.getDataValueObject();
            for (CoreObjectListVar listVar : numberListValueObjects) {
                numberListValues.add(Integer.parseInt(listVar.getDataValue()));
            }

            return numberListValues;
        }

        if (isObjectDataType(refType)) {
            List<Object> objectListValues = new ArrayList<>();
            List<CoreObjectListVar> objectListValueObjects = (List<CoreObjectListVar>) propertyVar.getDataValueObject();
            for (CoreObjectListVar listVar : objectListValueObjects) {
                CoreObjectVar objectVar = (CoreObjectVar) listVar.getRawObjectValue();
                PluginParamObject paramObject = doAssemblePluginParamObject(objectVar, ctx);
                objectListValues.add(paramObject);
            }

            return objectListValues;
        }

        return null;
    }

}
