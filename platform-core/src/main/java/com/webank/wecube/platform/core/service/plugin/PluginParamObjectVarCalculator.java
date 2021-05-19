package com.webank.wecube.platform.core.service.plugin;

import static com.webank.wecube.platform.core.utils.Constants.FIELD_REQUIRED;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.entity.plugin.CoreObjectListVar;
import com.webank.wecube.platform.core.entity.plugin.CoreObjectMeta;
import com.webank.wecube.platform.core.entity.plugin.CoreObjectPropertyMeta;
import com.webank.wecube.platform.core.entity.plugin.CoreObjectPropertyVar;
import com.webank.wecube.platform.core.entity.plugin.CoreObjectVar;
import com.webank.wecube.platform.core.entity.plugin.SystemVariables;
import com.webank.wecube.platform.core.service.dme.EntityOperationRootCondition;
import com.webank.wecube.platform.core.service.dme.StandardEntityOperationService;
import com.webank.wecube.platform.workflow.commons.LocalIdGenerator;

@Service
public class PluginParamObjectVarCalculator extends AbstractPluginParamObjectService {
    private static final Logger log = LoggerFactory.getLogger(PluginParamObjectVarCalculator.class);

    @Autowired
    protected PluginParamObjectVarStorage pluginParamObjectVarStorageService;
    
    @Autowired
    protected SystemVariableService systemVariableService;
    
    @Autowired
    protected StandardEntityOperationService entityOperationService;

    /**
     * 
     * @param objectMeta
     * @param ctx
     * @return
     */
    public CoreObjectVar calculateCoreObjectVar(CoreObjectMeta objectMeta, CoreObjectVarCalculationContext ctx) {

        CoreObjectVar rootObjectVar = doCalculateCoreObjectVar(objectMeta, null, ctx);
        // TODO store object var

        pluginParamObjectVarStorageService.storeCoreObjectVar(rootObjectVar);
        return rootObjectVar;
    }

    private CoreObjectVar doCalculateCoreObjectVar(CoreObjectMeta objectMeta, CoreObjectVar parentObjectVar,
            CoreObjectVarCalculationContext ctx) {
        if (objectMeta == null) {
            return null;
        }
        CoreObjectVar objectVar = new CoreObjectVar();
        objectVar.setId(LocalIdGenerator.generateId(PREFIX_OBJECT_VAR_ID));
        objectVar.setName(objectMeta.getName());
        objectVar.setObjectMeta(objectMeta);
        objectVar.setObjectMetaId(objectMeta.getId());
        objectVar.setPackageName(objectMeta.getPackageName());

        if (parentObjectVar != null) {
            objectVar.setParentObjectVarId(parentObjectVar.getId());
            objectVar.setParentObjectName(parentObjectVar.getName());
        }

        List<CoreObjectPropertyMeta> propertyMetas = objectMeta.getPropertyMetas();
        for (CoreObjectPropertyMeta propertyMeta : propertyMetas) {
            CoreObjectPropertyVar propertyVar = calculatePropertyVar(propertyMeta, objectVar, ctx);
            propertyVar.setId(LocalIdGenerator.generateId(PREFIX_PROPERTY_VAR_ID));
            propertyVar.setObjectMetaId(objectVar.getObjectMetaId());

            propertyVar.setObjectPropertyMetaId(propertyMeta.getId());
            propertyVar.setPropertyMeta(propertyMeta);
            propertyVar.setObjectVar(objectVar);
            propertyVar.setObjectVarId(objectVar.getId());
            propertyVar.setObjectName(objectMeta.getName());
            propertyVar.setPackageName(objectMeta.getPackageName());

            objectVar.addPropertyVar(propertyVar);
        }

        return objectVar;
    }

    private CoreObjectPropertyVar calculatePropertyVar(CoreObjectPropertyMeta propertyMeta,
            CoreObjectVar parentObjectVar, CoreObjectVarCalculationContext ctx) {
        CoreObjectPropertyVar propertyVar = new CoreObjectPropertyVar();
        propertyVar.setId(LocalIdGenerator.generateId(PREFIX_PROPERTY_VAR_ID));
        propertyVar.setName(propertyMeta.getName());
        propertyVar.setDataType(propertyMeta.getDataType());

        Object dataValueObject = calculatePropertyDataValueObject(propertyMeta, parentObjectVar, ctx);

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

    private Object calculatePropertyDataValueObject(CoreObjectPropertyMeta propertyMeta, CoreObjectVar parentObjectVar,
            CoreObjectVarCalculationContext ctx) {
        String dataType = propertyMeta.getDataType();
        Object dataObjectValue = null;

        if (isBasicDataType(dataType)) {
            dataObjectValue = calculateBasicTypePropertyValue(propertyMeta, parentObjectVar, ctx);
        } else if (isObjectDataType(dataType)) {
            dataObjectValue = calculateObjectTypePropertyValue(propertyMeta, parentObjectVar, ctx);
        } else if (isListDataType(dataType)) {
            dataObjectValue = calculateListTypePropertyValue(propertyMeta, parentObjectVar, ctx);
        }

        return dataObjectValue;
    }

    private Object calculateBasicTypePropertyValue(CoreObjectPropertyMeta propertyMeta, CoreObjectVar parentObjectVar,
            CoreObjectVarCalculationContext ctx) {
        Object dataObjectValue = null;

        if (propertyMeta.isEntityMapping()) {
            dataObjectValue = calculateBasicTypePropertyValueFromEntity(propertyMeta, parentObjectVar, ctx);
        } else if (propertyMeta.isConstantMapping()) {
            dataObjectValue = calculateBasicTypePropertyValueFromConstant(propertyMeta, parentObjectVar, ctx);
        } else if (propertyMeta.isSystemVariableMapping()) {
            dataObjectValue = calculateBasicTypePropertyValueFromSystemVariable(propertyMeta, parentObjectVar, ctx);
        } else if (propertyMeta.isContextMapping()) {
            dataObjectValue = calculateBasicTypePropertyValueFromContext(propertyMeta, parentObjectVar, ctx);
        } else {
            // TODO
            dataObjectValue = null;
        }
        return dataObjectValue;
    }

    private Object calculateBasicTypePropertyValueFromEntity(CoreObjectPropertyMeta propertyMeta,
            CoreObjectVar parentObjectVar, CoreObjectVarCalculationContext ctx) {
        String mappingEntityExpression = propertyMeta.getMapExpr();
        if (log.isDebugEnabled()) {
            log.debug("expression:{}", mappingEntityExpression);
        }

        EntityOperationRootCondition condition = new EntityOperationRootCondition(mappingEntityExpression,
                ctx.getRootEntityDataId());

        List<Object> attrValsPerExpr = entityOperationService.queryAttributeValues(condition, ctx.getExternalCacheMap());

        if (attrValsPerExpr == null || attrValsPerExpr.isEmpty()) {
            log.info("returned null while fetch data with expression:{}", mappingEntityExpression);
            return null;
        }

        if (log.isDebugEnabled()) {
            log.debug("retrieved objects with expression,size={},values={}", attrValsPerExpr.size(), attrValsPerExpr);
        }
        
        //
        Object  dataObjectValue = attrValsPerExpr.get(0);
        return dataObjectValue;
    }

    private Object calculateBasicTypePropertyValueFromConstant(CoreObjectPropertyMeta propertyMeta,
            CoreObjectVar parentObjectVar, CoreObjectVarCalculationContext ctx) {
        // TODO
        return null;
    }

    private Object calculateBasicTypePropertyValueFromSystemVariable(CoreObjectPropertyMeta propertyMeta,
            CoreObjectVar parentObjectVar, CoreObjectVarCalculationContext ctx) {
        String systemVariableName = propertyMeta.getMapExpr();
        SystemVariables sVariable = systemVariableService.getSystemVariableByPackageNameAndName(
                propertyMeta.getPackageName(), systemVariableName);
        
        if(sVariable == null){
            return null;
        }

        String sVal = null;
        if (sVariable != null) {
            sVal = sVariable.getValue();
            if (StringUtils.isBlank(sVal)) {
                sVal = sVariable.getDefaultValue();
            }
        }

        return sVal;
    }

    private Object calculateBasicTypePropertyValueFromContext(CoreObjectPropertyMeta propertyMeta,
            CoreObjectVar parentObjectVar, CoreObjectVarCalculationContext ctx) {
        // TODO
        return null;
    }

    private CoreObjectVar calculateObjectTypePropertyValue(CoreObjectPropertyMeta propertyMeta,
            CoreObjectVar parentObjectVar, CoreObjectVarCalculationContext ctx) {
        CoreObjectMeta refObjectMeta = propertyMeta.getRefObjectMeta();
        CoreObjectVar refObjectVar = doCalculateCoreObjectVar(refObjectMeta, parentObjectVar, ctx);
        return refObjectVar;
    }

    private List<String> calListStringVars(CoreObjectPropertyMeta propertyMeta, CoreObjectVarCalculationContext ctx) {
        // TODO
        List<String> rawObjectValues = new ArrayList<>();
        String rawObjectValue = String.valueOf(System.currentTimeMillis());
        rawObjectValues.add(rawObjectValue);
        rawObjectValues.add("222");
        rawObjectValues.add("333");

        
        //TODO
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

    private List<CoreObjectListVar> calculateListTypePropertyValue(CoreObjectPropertyMeta propertyMeta,
            CoreObjectVar parentObjectVar, CoreObjectVarCalculationContext ctx) {
        if (!isListDataType(propertyMeta.getDataType())) {
            return null;// throw exception here?
        }
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
        } else if (isNumberDataType(propertyMeta.getRefType())) {
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
        } else if (isObjectDataType(propertyMeta.getRefType())) {
            List<CoreObjectVar> rawObjectValues = calObjectMetaPropertyAsListResult(propertyMeta, parentObjectVar, ctx);

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
        } else {
            return null;
        }
    }

    private List<CoreObjectPropertyVar> calBasicPropertyMetaAsListPropertyVars(CoreObjectPropertyMeta propertyMeta,
            CoreObjectVarCalculationContext ctx) {
        List<CoreObjectPropertyVar> listPropertyVars = new ArrayList<>();

        List<Object> listObjects = calculateDmeExprAsListResult(propertyMeta, ctx);
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
            propertyVar.setSensitive(propertyMeta.getSensitive());

            listPropertyVars.add(propertyVar);
        }

        // TODO
        return listPropertyVars;
    }

    private List<CoreObjectPropertyVar> calculateListPropertyMetaAsListPropertyVars(CoreObjectPropertyMeta propertyMeta,
            CoreObjectVar parentObjectVar, CoreObjectVarCalculationContext ctx) {
        log.debug("calculate list property meta as list property vars for :{}", propertyMeta.getName());
        String refType = propertyMeta.getRefType();
        if (isBasicDataType(refType)) {
            List<Object> listObjects1 = calculateDmeExprAsListResult(propertyMeta, ctx);
            List<Object> listObjects2 = calculateDmeExprAsListResult(propertyMeta, ctx);

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
            propertyVar1.setSensitive(propertyMeta.getSensitive());
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
            propertyVar2.setSensitive(propertyMeta.getSensitive());

            listPropertyVars.add(propertyVar2);

            return listPropertyVars;

        }
        throw new UnsupportedOperationException();
    }

    private List<CoreObjectPropertyVar> calObjectPropertyMetaAsListPropertyVars(CoreObjectPropertyMeta propertyMeta,
            CoreObjectVar parentObjectVar, CoreObjectVarCalculationContext ctx) {
        throw new UnsupportedOperationException();
    }

    private List<CoreObjectPropertyVar> calPropertyMetaAsListPropertyVars(CoreObjectPropertyMeta propertyMeta,
            CoreObjectVar parentObjectVar, CoreObjectVarCalculationContext ctx) {

        log.debug("to calculate list property vars for objectName={}, propName={}, dataType={}, refType={}",
                propertyMeta.getObjectMeta().getName(), propertyMeta.getName(), propertyMeta.getDataType(),
                propertyMeta.getRefType());

        String dataType = propertyMeta.getDataType();
        if (isBasicDataType(dataType)) {
            return calBasicPropertyMetaAsListPropertyVars(propertyMeta, ctx);
        }

        if (isListDataType(dataType)) {
            return calculateListPropertyMetaAsListPropertyVars(propertyMeta, parentObjectVar, ctx);
        }

        if (isObjectDataType(dataType)) {
            return calObjectPropertyMetaAsListPropertyVars(propertyMeta, parentObjectVar, ctx);
        }

        // unsupported data type
        return null;
    }

    private List<CoreObjectVar> calculateObjectMetaAsListResult(CoreObjectMeta objectMeta,
            CoreObjectVar parentObjectVar, CoreObjectVarCalculationContext ctx) {
        List<CoreObjectVar> rawObjectValues = new ArrayList<>();

        Map<String, List<CoreObjectPropertyVar>> propertyMetaVarsMap = new HashMap<String, List<CoreObjectPropertyVar>>();

        List<CoreObjectPropertyMeta> propertyMetas = objectMeta.getPropertyMetas();

        for (CoreObjectPropertyMeta propertyMeta : propertyMetas) {
            List<CoreObjectPropertyVar> listPropertyVars = calPropertyMetaAsListPropertyVars(propertyMeta,
                    parentObjectVar, ctx);
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

            if (parentObjectVar != null) {
                objectVar.setParentObjectVarId(parentObjectVar.getId());
                objectVar.setParentObjectName(parentObjectVar.getName());
            }

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
            CoreObjectVar parentObjectVar, CoreObjectVarCalculationContext ctx) {
        // TODO
        CoreObjectMeta refObjectMeta = propertyMeta.getRefObjectMeta();
        log.debug("calculate list object vars for objectName={},propertyName={}, refObject={} ",
                propertyMeta.getObjectMeta().getName(), propertyMeta.getName(), refObjectMeta.getName());
        List<CoreObjectVar> rawObjectValues = calculateObjectMetaAsListResult(refObjectMeta, parentObjectVar, ctx);

        return rawObjectValues;

    }

    // mock data
    private List<Object> calculateDmeExprAsListResult(CoreObjectPropertyMeta propertyMeta,
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
