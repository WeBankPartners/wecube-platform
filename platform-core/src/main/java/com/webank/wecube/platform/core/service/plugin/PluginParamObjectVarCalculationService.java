package com.webank.wecube.platform.core.service.plugin;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.entity.plugin.CoreObjectListVar;
import com.webank.wecube.platform.core.entity.plugin.CoreObjectMeta;
import com.webank.wecube.platform.core.entity.plugin.CoreObjectPropertyMeta;
import com.webank.wecube.platform.core.entity.plugin.CoreObjectPropertyVar;
import com.webank.wecube.platform.core.entity.plugin.CoreObjectVar;
import com.webank.wecube.platform.core.service.dme.StandardEntityOperationService;
import com.webank.wecube.platform.workflow.WorkflowConstants;
import com.webank.wecube.platform.workflow.commons.LocalIdGenerator;

@Service
public class PluginParamObjectVarCalculationService extends AbstractPluginParamObjectService {
    private static final Logger log = LoggerFactory.getLogger(PluginParamObjectVarCalculationService.class);

    public static final String PREFIX_OBJECT_VAR_ID = "objvar";

    public static final String PREFIX_PROPERTY_VAR_ID = "attrvar";

    public static final String PREFIX_LIST_VAR_ID = "listvar";

    @Autowired
    private StandardEntityOperationService standardEntityOperationService;

    public CoreObjectVar calculateCoreObjectVar(CoreObjectMeta objectMeta, CoreObjectVarCalculationContext ctx) {

        CoreObjectVar rootObjectVar = doCalculateCoreObjectVar(objectMeta, ctx);
        // TODO store object var
        
        storeCoreObjectVar(rootObjectVar);
        return rootObjectVar;
    }

    public PluginParamObject assemblePluginParamObject(CoreObjectVar rootObjectVar,
            CoreObjectVarCalculationContext ctx) {
        PluginParamObject rootParamObject = doAssemblePluginParamObject(rootObjectVar, ctx);
        return rootParamObject;
    }

    private void storeCoreObjectVar(CoreObjectVar objectVar) {
        objectVar.setCreatedBy(WorkflowConstants.DEFAULT_USER);
        objectVar.setCreatedTime(new Date());

        coreObjectVarMapper.insert(objectVar);

        List<CoreObjectPropertyVar> propertyVars = objectVar.getPropertyVars();

        if (propertyVars == null || propertyVars.isEmpty()) {
            log.debug("there is not properties to store for {}", objectVar.getId());
            return;
        }

        for (CoreObjectPropertyVar propertyVar : propertyVars) {
            storeCorePropertyVar(propertyVar, objectVar);
        }
    }

    private void storeCorePropertyVar(CoreObjectPropertyVar propertyVar, CoreObjectVar objectVar) {
        if (isBasicDataType(propertyVar.getDataType())) {
            storeBasicCorePropertyVar(propertyVar, objectVar);
            return;
        }

        if (isListDataType(propertyVar.getDataType())) {
            storeListCorePropertyVar(propertyVar, objectVar);
        }

        if (isObjectDataType(propertyVar.getDataType())) {
            storeObjectCorePropertyVar(propertyVar, objectVar);
        }
    }

    @SuppressWarnings("unchecked")
    private void storeListCorePropertyVar(CoreObjectPropertyVar propertyVar, CoreObjectVar parentObjectVar) {
        propertyVar.setCreatedBy(WorkflowConstants.DEFAULT_USER);
        propertyVar.setCreatedTime(new Date());
        
        coreObjectPropertyVarMapper.insert(propertyVar);
        
        List<CoreObjectListVar> listVars = (List<CoreObjectListVar>)propertyVar.getDataValueObject();
        
        if(listVars == null || listVars.isEmpty()){
            log.debug("there is not list vars to store for property {}", propertyVar.getId());
            return;
        }
        
        for(CoreObjectListVar listVar : listVars){
            storeCoreObjectListVar(listVar, propertyVar);
        }
    }
    
    private void storeCoreObjectListVar(CoreObjectListVar listVar, CoreObjectPropertyVar propertyVar){
        listVar.setCreatedBy(WorkflowConstants.DEFAULT_USER);
        listVar.setCreatedTime(new Date());
        coreObjectListVarMapper.insert(listVar);
        
        if(isBasicDataType(listVar.getDataType())){
            return;
        }
        
        if(isListDataType(listVar.getDataType())){
            //
            log.debug("such data type {} is not currently supported.", listVar.getDataType());
            return;
        }
        
        if(isObjectDataType(listVar.getDataType())){
            CoreObjectVar objectVar = (CoreObjectVar)listVar.getRawObjectValue();
            storeCoreObjectVar(objectVar);
        }
    }

    private void storeObjectCorePropertyVar(CoreObjectPropertyVar propertyVar, CoreObjectVar parentObjectVar) {
        
        propertyVar.setCreatedBy(WorkflowConstants.DEFAULT_USER);
        propertyVar.setCreatedTime(new Date());
        
        coreObjectPropertyVarMapper.insert(propertyVar);
        
        CoreObjectVar objectVar = (CoreObjectVar)propertyVar.getDataValueObject();
        
        storeCoreObjectVar(objectVar);
    }

    private void storeBasicCorePropertyVar(CoreObjectPropertyVar propertyVar, CoreObjectVar parentObjectVar) {
        propertyVar.setCreatedBy(WorkflowConstants.DEFAULT_USER);
        propertyVar.setCreatedTime(new Date());
        coreObjectPropertyVarMapper.insert(propertyVar);
    }

    private PluginParamObject doAssemblePluginParamObject(CoreObjectVar objectVar,
            CoreObjectVarCalculationContext ctx) {
        PluginParamObject paramObject = new PluginParamObject();

        // paramObject.setProperty("pluginObjectId", objectVar.getId());
        // paramObject.setProperty("pluginObjectName", objectVar.getName());
        List<CoreObjectPropertyVar> propertyVars = objectVar.getPropertyVars();
        if (propertyVars == null || propertyVars.isEmpty()) {
            return paramObject;
        }

        for (CoreObjectPropertyVar propertyVar : propertyVars) {
            String dataType = propertyVar.getDataType();
            log.info("propertyName={}", propertyVar.getName());
            System.out.println("propertyName=" + propertyVar.getName());
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

    private CoreObjectVar doCalculateCoreObjectVar(CoreObjectMeta objectMeta, CoreObjectVarCalculationContext ctx) {
        if (objectMeta == null) {
            return null;
        }
        CoreObjectVar rootObjectVar = new CoreObjectVar();
        rootObjectVar.setId(LocalIdGenerator.generateId(PREFIX_OBJECT_VAR_ID));
        rootObjectVar.setName(objectMeta.getName());
        rootObjectVar.setObjectMeta(objectMeta);
        rootObjectVar.setObjectMetaId(objectMeta.getId());
        rootObjectVar.setPackageName(objectMeta.getPackageName());

        List<CoreObjectPropertyMeta> propertyMetas = objectMeta.getPropertyMetas();
        for (CoreObjectPropertyMeta propertyMeta : propertyMetas) {
            CoreObjectPropertyVar propertyVar = calPropertyVar(propertyMeta, ctx);
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

    private CoreObjectPropertyVar calPropertyVar(CoreObjectPropertyMeta propertyMeta,
            CoreObjectVarCalculationContext ctx) {
        CoreObjectPropertyVar propertyVar = new CoreObjectPropertyVar();
        propertyVar.setId(LocalIdGenerator.generateId(PREFIX_PROPERTY_VAR_ID));
        propertyVar.setName(propertyMeta.getName());
        propertyVar.setDataType(propertyMeta.getDataType());
        Object dataValueObject = calculateDataValueObject(propertyMeta, ctx);
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

    private Object calculateDataValueObject(CoreObjectPropertyMeta propertyMeta, CoreObjectVarCalculationContext ctx) {
        String dataType = propertyMeta.getDataType();
        Object dataObjectValue = null;

        if (isStringDataType(dataType)) {
            dataObjectValue = calculateStringPropertyValue(propertyMeta, ctx);
        } else if (isNumberDataType(dataType)) {
            dataObjectValue = calculateNumberPropertyValue(propertyMeta, ctx);
        } else if (isObjectDataType(dataType)) {
            dataObjectValue = calculateObjectPropertyValue(propertyMeta, ctx);
        } else if (isListDataType(dataType)) {
            dataObjectValue = calculateListPropertyValue(propertyMeta, ctx);
        }

        return dataObjectValue;
    }

    private String calculateStringPropertyValue(CoreObjectPropertyMeta propertyMeta,
            CoreObjectVarCalculationContext ctx) {
        if (isStringDataType(propertyMeta.getDataType())) {
            // TODO
            return String.valueOf(System.currentTimeMillis());
        }
        return null;
    }

    private Integer calculateNumberPropertyValue(CoreObjectPropertyMeta propertyMeta,
            CoreObjectVarCalculationContext ctx) {
        if (isNumberDataType(propertyMeta.getDataType())) {
            // TODO
            return Integer.valueOf(1000);
        }
        return null;
    }

    private CoreObjectVar calculateObjectPropertyValue(CoreObjectPropertyMeta propertyMeta,
            CoreObjectVarCalculationContext ctx) {
        if (isObjectDataType(propertyMeta.getDataType())) {
            CoreObjectMeta refObjectMeta = propertyMeta.getRefObjectMeta();
            // TODO
            CoreObjectVar refObjectVar = doCalculateCoreObjectVar(refObjectMeta, ctx);
            return refObjectVar;
        }
        return null;
    }

    private List<String> calListStringVars(CoreObjectPropertyMeta propertyMeta, CoreObjectVarCalculationContext ctx) {
        // TODO
        List<String> rawObjectValues = new ArrayList<>();
        String rawObjectValue = String.valueOf(System.currentTimeMillis());
        rawObjectValues.add(rawObjectValue);
        rawObjectValues.add("222");
        rawObjectValues.add("333");

        return rawObjectValues;
    }

    private List<Integer> calculateListNumberVars(CoreObjectPropertyMeta propertyMeta,
            CoreObjectVarCalculationContext ctx) {
        List<Integer> rawObjectValues = new ArrayList<>();
        // TODO
        Integer rawObjectValue = 1111111;
        rawObjectValues.add(rawObjectValue);
        rawObjectValues.add(222);
        rawObjectValues.add(3333);

        return rawObjectValues;
    }

    private List<CoreObjectListVar> calculateListPropertyValue(CoreObjectPropertyMeta propertyMeta,
            CoreObjectVarCalculationContext ctx) {
        if (isListDataType(propertyMeta.getDataType())) {
            if (isStringDataType(propertyMeta.getRefType())) {
                // TODO
                List<String> rawObjectValues = calListStringVars(propertyMeta, ctx);

                List<CoreObjectListVar> stringListVars = new ArrayList<>();
                for (String rawObject : rawObjectValues) {
                    CoreObjectListVar stringListVar = new CoreObjectListVar();
                    stringListVar.setId(LocalIdGenerator.generateId(PREFIX_LIST_VAR_ID));
                    stringListVar.setDataType(propertyMeta.getRefType());
                    // TODO
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
                List<Integer> rawObjectValues = calculateListNumberVars(propertyMeta, ctx);

                List<CoreObjectListVar> numberListVars = new ArrayList<>();
                for (Integer rawObject : rawObjectValues) {
                    CoreObjectListVar numberListVar = new CoreObjectListVar();
                    numberListVar.setId(LocalIdGenerator.generateId(PREFIX_LIST_VAR_ID));
                    numberListVar.setDataType(propertyMeta.getRefType());
                    // TODO
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
                List<CoreObjectVar> rawObjectValues = calObjectMetaPropertyAsListResult(propertyMeta, ctx);

                // TODO
                List<CoreObjectListVar> objectListVars = new ArrayList<>();
                for (CoreObjectVar objectVar : rawObjectValues) {
                    CoreObjectListVar objectListVar = new CoreObjectListVar();
                    objectListVar.setId(LocalIdGenerator.generateId(PREFIX_LIST_VAR_ID));
                    objectListVar.setDataType(propertyMeta.getRefType());

                    // TODO
                    String dataValue = objectVar.getId();

                    objectListVar.setDataValue(dataValue);
                    objectListVar.setRawObjectValue(objectVar);
                    objectListVar.setSensitive(propertyMeta.getSensitive());
                    objectListVar.setObjectPropertyMeta(propertyMeta);

                    objectListVars.add(objectListVar);
                }

                return objectListVars;
            }
        }
        return null;
    }

    private List<CoreObjectPropertyVar> calBasicPropertyMetaAsListPropertyVars(CoreObjectPropertyMeta propertyMeta,
            CoreObjectVarCalculationContext ctx) {
        List<CoreObjectPropertyVar> listPropertyVars = new ArrayList<>();

        List<Object> listObjects = calDmeExprAsListResult(propertyMeta, ctx);
        for (Object listObject : listObjects) {
            CoreObjectPropertyVar propertyVar = new CoreObjectPropertyVar();
            propertyVar.setId(LocalIdGenerator.generateId(PREFIX_PROPERTY_VAR_ID));
            propertyVar.setDataType(propertyMeta.getDataType());
            propertyVar.setDataValue(String.valueOf(listObject));
            propertyVar.setName(propertyMeta.getName());
            propertyVar.setPropertyMeta(propertyMeta);
            propertyVar.setObjectPropertyMetaId(propertyMeta.getId());
            propertyVar.setDataValueObject(listObject);
            propertyVar.setObjectName(propertyMeta.getObjectName());
            propertyVar.setPackageName(propertyMeta.getPackageName());

            listPropertyVars.add(propertyVar);
        }

        // TODO
        return listPropertyVars;
    }

    private List<CoreObjectPropertyVar> calListPropertyMetaAsListPropertyVars(CoreObjectPropertyMeta propertyMeta,
            CoreObjectVarCalculationContext ctx) {
        log.debug("calculate list property meta as list property vars for :{}", propertyMeta.getName());
        String refType = propertyMeta.getRefType();
        if (isBasicDataType(refType)) {
            List<Object> listObjects1 = calDmeExprAsListResult(propertyMeta, ctx);
            List<Object> listObjects2 = calDmeExprAsListResult(propertyMeta, ctx);

            List<CoreObjectPropertyVar> listPropertyVars = new ArrayList<>();

            List<CoreObjectListVar> basicListVars1 = new ArrayList<>();
            for (Object rawObject : listObjects1) {
                CoreObjectListVar basicListVar = new CoreObjectListVar();
                basicListVar.setId(LocalIdGenerator.generateId(PREFIX_LIST_VAR_ID));
                basicListVar.setDataType(propertyMeta.getRefType());
                // TODO
                String dataValue = String.valueOf(rawObject);

                basicListVar.setDataValue(dataValue);
                basicListVar.setRawObjectValue(rawObject);
                basicListVar.setSensitive(propertyMeta.getSensitive());
                basicListVar.setObjectPropertyMeta(propertyMeta);

                basicListVars1.add(basicListVar);
            }

            CoreObjectPropertyVar propertyVar1 = new CoreObjectPropertyVar();
            propertyVar1.setId(LocalIdGenerator.generateId(PREFIX_PROPERTY_VAR_ID));
            propertyVar1.setDataType(propertyMeta.getDataType());
            propertyVar1.setDataValue(convertCoreObjectListVarsToString(basicListVars1));
            propertyVar1.setName(propertyMeta.getName());
            propertyVar1.setPropertyMeta(propertyMeta);
            propertyVar1.setDataValueObject(basicListVars1);
            propertyVar1.setObjectName(propertyMeta.getObjectName());
            propertyVar1.setPackageName(propertyMeta.getPackageName());
            propertyVar1.setObjectPropertyMetaId(propertyMeta.getId());
            listPropertyVars.add(propertyVar1);

            List<CoreObjectListVar> basicListVars2 = new ArrayList<>();
            for (Object rawObject : listObjects2) {
                CoreObjectListVar basicListVar = new CoreObjectListVar();
                basicListVar.setId(LocalIdGenerator.generateId(PREFIX_LIST_VAR_ID));
                basicListVar.setDataType(propertyMeta.getRefType());
                // TODO
                String dataValue = String.valueOf(rawObject);

                basicListVar.setDataValue(dataValue);
                basicListVar.setRawObjectValue(rawObject);
                basicListVar.setSensitive(propertyMeta.getSensitive());
                basicListVar.setObjectPropertyMeta(propertyMeta);

                basicListVars2.add(basicListVar);
            }

            CoreObjectPropertyVar propertyVar2 = new CoreObjectPropertyVar();
            propertyVar2.setId(LocalIdGenerator.generateId(PREFIX_PROPERTY_VAR_ID));
            propertyVar2.setDataType(propertyMeta.getDataType());
            propertyVar2.setDataValue(convertCoreObjectListVarsToString(basicListVars2));
            propertyVar2.setName(propertyMeta.getName());
            propertyVar2.setPropertyMeta(propertyMeta);
            propertyVar2.setDataValueObject(basicListVars2);
            propertyVar2.setObjectName(propertyMeta.getObjectName());
            propertyVar2.setPackageName(propertyMeta.getPackageName());
            propertyVar2.setObjectPropertyMetaId(propertyMeta.getId());

            listPropertyVars.add(propertyVar2);

            return listPropertyVars;

        }
        throw new UnsupportedOperationException();
    }

    private List<CoreObjectPropertyVar> calObjectPropertyMetaAsListPropertyVars(CoreObjectPropertyMeta propertyMeta,
            CoreObjectVarCalculationContext ctx) {
        throw new UnsupportedOperationException();
    }

    private List<CoreObjectPropertyVar> calPropertyMetaAsListPropertyVars(CoreObjectPropertyMeta propertyMeta,
            CoreObjectVarCalculationContext ctx) {

        log.debug("to cal list property vars for objectName={}, propName={}, dataType={}, refType={}",
                propertyMeta.getObjectMeta().getName(), propertyMeta.getName(), propertyMeta.getDataType(),
                propertyMeta.getRefType());

        String dataType = propertyMeta.getDataType();
        if (isBasicDataType(dataType)) {
            return calBasicPropertyMetaAsListPropertyVars(propertyMeta, ctx);
        }

        if (isListDataType(dataType)) {
            return calListPropertyMetaAsListPropertyVars(propertyMeta, ctx);
        }

        if (isObjectDataType(dataType)) {
            return calObjectPropertyMetaAsListPropertyVars(propertyMeta, ctx);
        }

        // unsupported data type
        return null;
    }

    private List<CoreObjectVar> calObjectMetaAsListResult(CoreObjectMeta objectMeta,
            CoreObjectVarCalculationContext ctx) {
        List<CoreObjectVar> rawObjectValues = new ArrayList<>();

        Map<String, List<CoreObjectPropertyVar>> propertyMetaVarsMap = new HashMap<String, List<CoreObjectPropertyVar>>();

        List<CoreObjectPropertyMeta> propertyMetas = objectMeta.getPropertyMetas();

        for (CoreObjectPropertyMeta propertyMeta : propertyMetas) {
            List<CoreObjectPropertyVar> listPropertyVars = calPropertyMetaAsListPropertyVars(propertyMeta, ctx);
            propertyMetaVarsMap.put(propertyMeta.getId(), listPropertyVars);
        }

        // TODO
        if (propertyMetaVarsMap.isEmpty()) {
            return rawObjectValues;
        }

        int maxSize = 0;
        for (Map.Entry<String, List<CoreObjectPropertyVar>> entry : propertyMetaVarsMap.entrySet()) {
            if (entry.getValue().size() > maxSize) {
                maxSize = entry.getValue().size();
            }
        }

        for (int index = 0; index < maxSize; index++) {
            CoreObjectVar objectVar = new CoreObjectVar();
            objectVar.setId(LocalIdGenerator.generateId(PREFIX_OBJECT_VAR_ID));
            objectVar.setName(objectMeta.getName());
            objectVar.setPackageName(objectMeta.getPackageName());
            objectVar.setObjectMeta(objectMeta);
            objectVar.setObjectMetaId(objectMeta.getId());

            for (List<CoreObjectPropertyVar> listVars : propertyMetaVarsMap.values()) {
                CoreObjectPropertyVar listVar = null;
                if (listVars.size() > index) {
                    listVar = listVars.get(index);
                }

                if (listVar != null) {
                    listVar.setObjectVarId(objectVar.getId());
                    listVar.setObjectVar(objectVar);
                    listVar.setObjectMetaId(objectMeta.getId());
                    objectVar.addPropertyVar(listVar);
                }

            }

            rawObjectValues.add(objectVar);
        }

        return rawObjectValues;
    }

    private List<CoreObjectVar> calObjectMetaPropertyAsListResult(CoreObjectPropertyMeta propertyMeta,
            CoreObjectVarCalculationContext ctx) {
        // TODO
        CoreObjectMeta refObjectMeta = propertyMeta.getRefObjectMeta();
        log.debug("cal list object vars for objectName={},propertyName={}, refObject={} ",
                propertyMeta.getObjectMeta().getName(), propertyMeta.getName(), refObjectMeta.getName());
        List<CoreObjectVar> rawObjectValues = calObjectMetaAsListResult(refObjectMeta, ctx);

        return rawObjectValues;

    }

    // mock data
    private List<Object> calDmeExprAsListResult(CoreObjectPropertyMeta propertyMeta,
            CoreObjectVarCalculationContext ctx) {
        String refType = propertyMeta.getRefType();
        if (isStringDataType(refType)) {
            List<Object> stringListObjects = new ArrayList<>();
            stringListObjects.add("1111");
            stringListObjects.add("2222");

            return stringListObjects;
        }

        if (isNumberDataType(refType)) {
            List<Object> numberListObjects = new ArrayList<>();
            numberListObjects.add(1000);
            numberListObjects.add(2000);

            return numberListObjects;
        }

        log.debug("unsupported data type {} for list", refType);
        return null;
    }

}
