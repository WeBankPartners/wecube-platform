package com.webank.wecube.platform.core.service.plugin;

import static com.webank.wecube.platform.core.utils.Constants.MAPPING_TYPE_CONSTANT;

import java.util.ArrayList;
import java.util.Collections;
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
import com.webank.wecube.platform.core.entity.workflow.ProcInstInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeDefInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeExecParamEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeExecRequestEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeInstInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeParamEntity;
import com.webank.wecube.platform.core.repository.workflow.TaskNodeDefInfoMapper;
import com.webank.wecube.platform.core.repository.workflow.TaskNodeExecParamMapper;
import com.webank.wecube.platform.core.repository.workflow.TaskNodeExecRequestMapper;
import com.webank.wecube.platform.core.repository.workflow.TaskNodeInstInfoMapper;
import com.webank.wecube.platform.core.repository.workflow.TaskNodeParamMapper;
import com.webank.wecube.platform.core.service.dme.EntityOperationRootCondition;
import com.webank.wecube.platform.core.service.dme.StandardEntityOperationService;
import com.webank.wecube.platform.core.utils.Constants;
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

    @Autowired
    protected TaskNodeParamMapper taskNodeParamRepository;

    @Autowired
    protected TaskNodeInstInfoMapper taskNodeInstInfoRepository;

    @Autowired
    protected TaskNodeExecRequestMapper taskNodeExecRequestRepository;

    @Autowired
    protected TaskNodeDefInfoMapper taskNodeDefInfoRepository;

    @Autowired
    protected TaskNodeExecParamMapper taskNodeExecParamRepository;

    /**
     * 
     * @param objectMeta
     * @param ctx
     * @return
     */
    public List<CoreObjectVar> calculateCoreObjectVarList(CoreObjectMeta objectMeta,
            CoreObjectVarCalculationContext ctx) {

        List<CoreObjectVar> rootObjectVars = doCalculateCoreObjectVarList(objectMeta, null, ctx, null);

        if (rootObjectVars != null) {
            for (CoreObjectVar rootObjectVar : rootObjectVars) {
                pluginParamObjectVarStorageService.storeCoreObjectVar(rootObjectVar);
            }

        }

        return rootObjectVars;

    }

    /**
     * 
     * @param objectMeta
     * @param ctx
     * @return
     */
    public CoreObjectVar calculateCoreObjectVar(CoreObjectMeta objectMeta, CoreObjectVarCalculationContext ctx) {

        CoreObjectVar rootObjectVar = doCalculateCoreObjectVar(objectMeta, null, ctx);

        pluginParamObjectVarStorageService.storeCoreObjectVar(rootObjectVar);
        return rootObjectVar;
    }

    protected List<CoreObjectVar> doCalculateCoreObjectVarList(CoreObjectMeta objectMeta, CoreObjectVar parentObjectVar,
            CoreObjectVarCalculationContext ctx, String rootEntityDataId) {
        String objectMetaExpr = objectMeta.getMapExpr();
        if (StringUtils.isBlank(objectMetaExpr)) {
            if (checkIfHasEntityMapping(objectMeta)) {
                String errMsg = "The object meta expression is blank but has to calculate entity mapping data.";
                log.error(errMsg);
                throw new WecubeCoreException(errMsg);
            } else {
                return tryCalculateCoreObjectVarListFromNoneEntityMapping(objectMeta, parentObjectVar, ctx,
                        rootEntityDataId);
            }
        }

        return tryCalculateCoreObjectVarListFromEntityMapping(objectMeta, parentObjectVar, ctx, rootEntityDataId);
    }

    private List<CoreObjectVar> tryCalculateCoreObjectVarListFromEntityMapping(CoreObjectMeta objectMeta,
            CoreObjectVar parentObjectVar, CoreObjectVarCalculationContext ctx, String rootEntityDataId) {
        String objectMetaExpr = objectMeta.getMapExpr();
        EntityOperationRootCondition condition = new EntityOperationRootCondition(objectMetaExpr, rootEntityDataId);

        List<Map<String, Object>> objectMetaExprQueryResultDataMaps = entityOperationService
                .queryAttributeValuesOfLeafNode(condition, ctx.getExternalCacheMap());

        if (objectMetaExprQueryResultDataMaps == null || objectMetaExprQueryResultDataMaps.isEmpty()) {
            return Collections.emptyList();
        }
        List<CoreObjectVar> coreObjectVars = new ArrayList<>();

        List<String> objectMetaBoundDataIds = new ArrayList<>();
        for (Map<String, Object> boundDataMap : objectMetaExprQueryResultDataMaps) {
            String boundDataId = (String) boundDataMap.get(Constants.UNIQUE_IDENTIFIER);
            if (StringUtils.isBlank(boundDataId)) {
                log.info("Cannot get data id from entity data:{}", boundDataMap);
                continue;
            }

            objectMetaBoundDataIds.add(boundDataId);
        }

        for (String boundDataId : objectMetaBoundDataIds) {
            CoreObjectVar objectVar = new CoreObjectVar();
            objectVar.setName(objectMeta.getName());
            objectVar.setObjectMeta(objectMeta);
            objectVar.setObjectMetaId(objectMeta.getId());
            objectVar.setPackageName(objectMeta.getPackageName());
            
            if(parentObjectVar != null){
                objectVar.setParentObjectName(parentObjectVar.getName());
                objectVar.setParentObjectVarId(parentObjectVar.getId());
                
            }
            coreObjectVars.add(objectVar);
            
            List<CoreObjectPropertyMeta> propertyMetas = objectMeta.getPropertyMetas();
            for (CoreObjectPropertyMeta propertyMeta : propertyMetas) {
                //TODO
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
            
        }
        return coreObjectVars;
    }

    private List<CoreObjectVar> tryCalculateCoreObjectVarListFromNoneEntityMapping(CoreObjectMeta objectMeta,
            CoreObjectVar parentObjectVar, CoreObjectVarCalculationContext ctx, String rootEntityDataId) {
        // TODO
        return null;
    }

    private boolean checkIfHasEntityMapping(CoreObjectMeta objectMeta) {
        // TODO
        return true;
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
            // need throw exception here
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

        List<Object> attrValsPerExpr = entityOperationService.queryAttributeValues(condition,
                ctx.getExternalCacheMap());

        if (attrValsPerExpr == null || attrValsPerExpr.isEmpty()) {
            log.info("returned null while fetch data with expression:{}", mappingEntityExpression);
            return null;
        }

        if (log.isDebugEnabled()) {
            log.debug("retrieved objects with expression,size={},values={}", attrValsPerExpr.size(), attrValsPerExpr);
        }

        //
        Object dataObjectValue = attrValsPerExpr.get(0);
        return dataObjectValue;
    }

    private List<Object> calculateBasicTypePropertyValueListFromEntity(CoreObjectPropertyMeta propertyMeta,
            CoreObjectVar parentObjectVar, CoreObjectVarCalculationContext ctx) {
        String mappingEntityExpression = propertyMeta.getMapExpr();
        if (log.isDebugEnabled()) {
            log.debug("expression:{}", mappingEntityExpression);
        }

        EntityOperationRootCondition condition = new EntityOperationRootCondition(mappingEntityExpression,
                ctx.getRootEntityDataId());

        List<Object> attrValsPerExpr = entityOperationService.queryAttributeValues(condition,
                ctx.getExternalCacheMap());

        if (attrValsPerExpr == null || attrValsPerExpr.isEmpty()) {
            log.info("returned null while fetch data with expression:{}", mappingEntityExpression);
            return Collections.emptyList();
        }

        if (log.isDebugEnabled()) {
            log.debug("retrieved objects with expression,size={},values={}", attrValsPerExpr.size(), attrValsPerExpr);
        }

        //
        return attrValsPerExpr;
    }

    private Object calculateBasicTypePropertyValueFromConstant(CoreObjectPropertyMeta propertyMeta,
            CoreObjectVar parentObjectVar, CoreObjectVarCalculationContext ctx) {
        //
        TaskNodeDefInfoEntity currTaskNodeDefInfo = ctx.getTaskNodeDefInfo();
        String curTaskNodeDefId = currTaskNodeDefInfo.getId();

        String paramName = propertyMeta.getName();
        TaskNodeParamEntity nodeParamEntity = taskNodeParamRepository
                .selectOneByTaskNodeDefIdAndParamName(curTaskNodeDefId, paramName);

        if (nodeParamEntity == null) {
            return null;
        }

        Object val = null;

        if (MAPPING_TYPE_CONSTANT.equalsIgnoreCase(nodeParamEntity.getBindType())) {
            val = nodeParamEntity.getBindVal();
        }

        return val;
    }

    private Object calculateBasicTypePropertyValueFromSystemVariable(CoreObjectPropertyMeta propertyMeta,
            CoreObjectVar parentObjectVar, CoreObjectVarCalculationContext ctx) {
        String systemVariableName = propertyMeta.getMapExpr();
        SystemVariables sVariable = systemVariableService
                .getSystemVariableByPackageNameAndName(propertyMeta.getPackageName(), systemVariableName);

        if (sVariable == null) {
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

    private List<Object> calculateBasicTypePropertyValueListFromContext(CoreObjectPropertyMeta propertyMeta,
            CoreObjectVar parentObjectVar, CoreObjectVarCalculationContext ctx) {
        TaskNodeDefInfoEntity currTaskNodeDefInfo = ctx.getTaskNodeDefInfo();
        String curTaskNodeDefId = currTaskNodeDefInfo.getId();
        String paramName = propertyMeta.getName();
        TaskNodeParamEntity nodeParamEntity = taskNodeParamRepository
                .selectOneByTaskNodeDefIdAndParamName(curTaskNodeDefId, paramName);

        if (nodeParamEntity == null) {
            return null;
        }

        String boundNodeId = nodeParamEntity.getBindNodeId();
        String boundParamType = nodeParamEntity.getBindParamType();
        String boundParamName = nodeParamEntity.getBindParamName();

        ProcInstInfoEntity procInstInfo = ctx.getProcInstInfo();

        // get by procInstId and nodeId
        TaskNodeInstInfoEntity boundNodeInstEntity = taskNodeInstInfoRepository
                .selectOneByProcInstIdAndNodeId(procInstInfo.getId(), boundNodeId);

        if (boundNodeInstEntity == null) {
            log.error("Bound node instance entity does not exist for {} {}", procInstInfo.getId(), boundNodeId);
            throw new WecubeCoreException("3171", "Bound node instance entity does not exist.");
        }

        List<TaskNodeExecRequestEntity> requestEntities = taskNodeExecRequestRepository
                .selectCurrentEntityByNodeInstId(boundNodeInstEntity.getId());

        if (requestEntities == null || requestEntities.isEmpty()) {
            log.error("cannot find request entity for {}", boundNodeInstEntity.getId());
            throw new WecubeCoreException("3172", "Bound request entity does not exist.");
        }

        if (requestEntities.size() > 1) {
            log.warn("duplicated request entity found for {} ", boundNodeInstEntity.getId());
            // throw new WecubeCoreException("3173", "Duplicated request entity
            // found.");
        }

        TaskNodeExecRequestEntity requestEntity = requestEntities.get(0);

        // TaskNodeDefInfoEntity boundNodeDefInfoEntity =
        // taskNodeDefInfoRepository
        // .selectByPrimaryKey(boundNodeInstEntity.getNodeDefId());

        List<TaskNodeExecParamEntity> execParamEntities = taskNodeExecParamRepository
                .selectAllByRequestIdAndParamNameAndParamType(requestEntity.getReqId(), boundParamName, boundParamType);

        if (execParamEntities == null || execParamEntities.isEmpty()) {
            return null;
        }

        List<Object> vals = new ArrayList<>();

        for (TaskNodeExecParamEntity execParam : execParamEntities) {
            vals.add(execParam.getParamDataValue());
        }

        return vals;
    }

    private Object calculateBasicTypePropertyValueFromContext(CoreObjectPropertyMeta propertyMeta,
            CoreObjectVar parentObjectVar, CoreObjectVarCalculationContext ctx) {
        TaskNodeDefInfoEntity currTaskNodeDefInfo = ctx.getTaskNodeDefInfo();
        String curTaskNodeDefId = currTaskNodeDefInfo.getId();
        String paramName = propertyMeta.getName();
        TaskNodeParamEntity nodeParamEntity = taskNodeParamRepository
                .selectOneByTaskNodeDefIdAndParamName(curTaskNodeDefId, paramName);

        if (nodeParamEntity == null) {
            return null;
        }

        String boundNodeId = nodeParamEntity.getBindNodeId();
        String boundParamType = nodeParamEntity.getBindParamType();
        String boundParamName = nodeParamEntity.getBindParamName();

        ProcInstInfoEntity procInstInfo = ctx.getProcInstInfo();

        // get by procInstId and nodeId
        TaskNodeInstInfoEntity boundNodeInstEntity = taskNodeInstInfoRepository
                .selectOneByProcInstIdAndNodeId(procInstInfo.getId(), boundNodeId);

        if (boundNodeInstEntity == null) {
            log.error("Bound node instance entity does not exist for {} {}", procInstInfo.getId(), boundNodeId);
            throw new WecubeCoreException("3171", "Bound node instance entity does not exist.");
        }

        List<TaskNodeExecRequestEntity> requestEntities = taskNodeExecRequestRepository
                .selectCurrentEntityByNodeInstId(boundNodeInstEntity.getId());

        if (requestEntities == null || requestEntities.isEmpty()) {
            log.error("cannot find request entity for {}", boundNodeInstEntity.getId());
            throw new WecubeCoreException("3172", "Bound request entity does not exist.");
        }

        if (requestEntities.size() > 1) {
            log.warn("duplicated request entity found for {} ", boundNodeInstEntity.getId());
            // throw new WecubeCoreException("3173", "Duplicated request entity
            // found.");
        }

        TaskNodeExecRequestEntity requestEntity = requestEntities.get(0);

        // TaskNodeDefInfoEntity boundNodeDefInfoEntity =
        // taskNodeDefInfoRepository
        // .selectByPrimaryKey(boundNodeInstEntity.getNodeDefId());

        List<TaskNodeExecParamEntity> execParamEntities = taskNodeExecParamRepository
                .selectAllByRequestIdAndParamNameAndParamType(requestEntity.getReqId(), boundParamName, boundParamType);

        if (execParamEntities == null || execParamEntities.isEmpty()) {
            return null;
        }

        TaskNodeExecParamEntity execParam = execParamEntities.get(0);

        return execParam.getParamDataValue();
    }

    private CoreObjectVar calculateObjectTypePropertyValue(CoreObjectPropertyMeta propertyMeta,
            CoreObjectVar parentObjectVar, CoreObjectVarCalculationContext ctx) {
        CoreObjectMeta refObjectMeta = propertyMeta.getRefObjectMeta();
        CoreObjectVar refObjectVar = doCalculateCoreObjectVar(refObjectMeta, parentObjectVar, ctx);
        return refObjectVar;
    }

    private List<String> calListStringVars(CoreObjectPropertyMeta propertyMeta, CoreObjectVarCalculationContext ctx) {
        List<String> rawObjectValues = new ArrayList<>();

        if (propertyMeta.isEntityMapping()) {
            List<Object> valueObjects = calculateBasicTypePropertyValueListFromEntity(propertyMeta, null, ctx);
            if (valueObjects != null) {
                for (Object valueObject : valueObjects) {
                    String valueObjectStr = String.valueOf(valueObject);
                    rawObjectValues.add(valueObjectStr);
                }
            }
        } else if (propertyMeta.isConstantMapping()) {
            Object constantVal = calculateBasicTypePropertyValueFromConstant(propertyMeta, null, ctx);
            if (constantVal != null) {
                rawObjectValues.add(String.valueOf(constantVal));
            }
        } else if (propertyMeta.isContextMapping()) {

            List<Object> contextVals = calculateBasicTypePropertyValueListFromContext(propertyMeta, null, ctx);
            if (contextVals != null) {
                for (Object valueObject : contextVals) {
                    String valueObjectStr = String.valueOf(valueObject);
                    rawObjectValues.add(valueObjectStr);
                }
            }
        } else if (propertyMeta.isSystemVariableMapping()) {
            Object sysVal = calculateBasicTypePropertyValueFromSystemVariable(propertyMeta, null, ctx);
            if (sysVal != null) {
                String sysValStr = String.valueOf(sysVal);
                rawObjectValues.add(sysValStr);
            }
        } else {
            // do nothing
        }

        return rawObjectValues;
    }

    protected Integer convertObjectToInteger(Object val) {
        if (val == null) {
            return null;
        }

        if (val instanceof Integer) {
            return (Integer) val;
        }

        if (val instanceof String) {
            return Integer.parseInt((String) val);
        }

        throw new UnsupportedOperationException();
    }

    private List<Integer> calculateListNumberVars(CoreObjectPropertyMeta propertyMeta,
            CoreObjectVarCalculationContext ctx) {
        List<Integer> rawObjectValues = new ArrayList<>();

        if (propertyMeta.isEntityMapping()) {
            List<Object> valueObjects = calculateBasicTypePropertyValueListFromEntity(propertyMeta, null, ctx);
            if (valueObjects != null) {
                for (Object valueObject : valueObjects) {
                    Integer valueObjectInt = convertObjectToInteger(valueObject);
                    rawObjectValues.add(valueObjectInt);
                }
            }
        } else if (propertyMeta.isConstantMapping()) {

            Object constantVal = calculateBasicTypePropertyValueFromConstant(propertyMeta, null, ctx);
            if (constantVal != null) {
                rawObjectValues.add(convertObjectToInteger(constantVal));
            }
        } else if (propertyMeta.isContextMapping()) {
            List<Object> contextVals = calculateBasicTypePropertyValueListFromContext(propertyMeta, null, ctx);
            if (contextVals != null) {
                for (Object valueObject : contextVals) {
                    String valueObjectStr = String.valueOf(valueObject);
                    rawObjectValues.add(convertObjectToInteger(valueObjectStr));
                }
            }
        } else if (propertyMeta.isSystemVariableMapping()) {
            Object sysVal = calculateBasicTypePropertyValueFromSystemVariable(propertyMeta, null, ctx);
            if (sysVal != null) {
                Integer sysValInt = convertObjectToInteger(sysVal);
                rawObjectValues.add(sysValInt);
            }
        } else {
            // do nothing
        }

        return rawObjectValues;
    }

    /**
     * data type: list ref type:string, int,object
     * 
     * @param propertyMeta
     * @param parentObjectVar
     * @param ctx
     * @return List<CoreObjectListVar>
     */
    private List<CoreObjectListVar> calculateListTypePropertyValue(CoreObjectPropertyMeta propertyMeta,
            CoreObjectVar parentObjectVar, CoreObjectVarCalculationContext ctx) {
        if (!isListDataType(propertyMeta.getDataType())) {
            return null;// throw exception here?
        }

        if (isStringDataType(propertyMeta.getRefType())) {
            List<String> rawObjectValues = calListStringVars(propertyMeta, ctx);

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
        } else if (isNumberDataType(propertyMeta.getRefType())) {
            List<Integer> rawObjectValues = calculateListNumberVars(propertyMeta, ctx);

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
        } else if (isObjectDataType(propertyMeta.getRefType())) {
            List<CoreObjectVar> rawObjectValues = calObjectTypePropertyAsListResult(propertyMeta, parentObjectVar, ctx);

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
        } else {
            return null;
        }
    }

    private List<CoreObjectPropertyVar> calBasicTypeAsListPropertyVars(CoreObjectPropertyMeta propertyMeta,
            CoreObjectVarCalculationContext ctx) {
        List<CoreObjectPropertyVar> listPropertyVars = new ArrayList<>();

        List<Object> listObjects = calculateBasicTypePropertyValueAsListResult(propertyMeta, null, ctx);
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

        return listPropertyVars;
    }

    /**
     * data type: list ref type: string, int, object
     * 
     * @param propertyMeta
     * @param parentObjectVar
     * @param ctx
     * @return
     */
    private List<CoreObjectPropertyVar> calculateListPropertyMetaAsListPropertyVarResult(
            CoreObjectPropertyMeta propertyMeta, CoreObjectVar parentObjectVar, CoreObjectVarCalculationContext ctx) {
        log.debug("calculate list property meta as list property vars for :{}", propertyMeta.getName());
        String refType = propertyMeta.getRefType();

        // refType = string, int
        if (isBasicDataType(refType)) {
            List<Object> objectValues = calculateBasicTypePropertyValueAsListResult(propertyMeta, parentObjectVar, ctx);

            List<CoreObjectPropertyVar> listPropertyVars = new ArrayList<>();

            List<CoreObjectListVar> basicListVars = new ArrayList<>();
            for (Object rawObject : objectValues) {
                CoreObjectListVar basicListVar = new CoreObjectListVar();
                basicListVar.setId(LocalIdGenerator.generateId(PREFIX_LIST_VAR_ID));
                basicListVar.setDataType(propertyMeta.getRefType());
                String dataValue = String.valueOf(rawObject);

                basicListVar.setDataValue(dataValue);
                basicListVar.setRawObjectValue(rawObject);
                basicListVar.setSensitive(propertyMeta.getSensitive());
                basicListVar.setObjectPropertyMeta(propertyMeta);

                basicListVars.add(basicListVar);
            }

            for (CoreObjectListVar basicListVar : basicListVars) {
                CoreObjectPropertyVar propertyVar = new CoreObjectPropertyVar();
                propertyVar.setId(LocalIdGenerator.generateId(PREFIX_PROPERTY_VAR_ID));
                propertyVar.setDataType(propertyMeta.getDataType());
                propertyVar.setDataValue(basicListVar.getId());
                propertyVar.setName(propertyMeta.getName());
                propertyVar.setPropertyMeta(propertyMeta);
                propertyVar.setDataValueObject(basicListVars);
                propertyVar.setObjectName(propertyMeta.getObjectName());
                propertyVar.setPackageName(propertyMeta.getPackageName());
                propertyVar.setObjectPropertyMetaId(propertyMeta.getId());
                propertyVar.setSensitive(propertyMeta.getSensitive());
                listPropertyVars.add(propertyVar);
            }

            return listPropertyVars;

        }

        if (isObjectDataType(refType)) {
            CoreObjectMeta refObjectMeta = propertyMeta.getRefObjectMeta();
            List<CoreObjectVar> objectVars = calculateObjectMetaAsObjectVarListResult(refObjectMeta, parentObjectVar,
                    ctx);

            List<CoreObjectPropertyVar> listPropertyVars = new ArrayList<>();

            List<CoreObjectListVar> basicListVars = new ArrayList<>();
            for (CoreObjectVar rawObject : objectVars) {
                CoreObjectListVar basicListVar = new CoreObjectListVar();
                basicListVar.setId(LocalIdGenerator.generateId(PREFIX_LIST_VAR_ID));
                basicListVar.setDataType(propertyMeta.getRefType());
                String dataValue = rawObject.getId();

                basicListVar.setDataValue(dataValue);
                basicListVar.setRawObjectValue(rawObject);
                basicListVar.setSensitive(propertyMeta.getSensitive());
                basicListVar.setObjectPropertyMeta(propertyMeta);

                basicListVars.add(basicListVar);
            }

            for (CoreObjectListVar basicListVar : basicListVars) {
                CoreObjectPropertyVar propertyVar = new CoreObjectPropertyVar();
                propertyVar.setId(LocalIdGenerator.generateId(PREFIX_PROPERTY_VAR_ID));
                propertyVar.setDataType(propertyMeta.getDataType());
                // propertyVar.setDataValue(convertCoreObjectListVarsToString(basicListVars));
                propertyVar.setDataValue(basicListVar.getId());
                propertyVar.setName(propertyMeta.getName());
                propertyVar.setPropertyMeta(propertyMeta);
                propertyVar.setDataValueObject(basicListVars);
                propertyVar.setObjectName(propertyMeta.getObjectName());
                propertyVar.setPackageName(propertyMeta.getPackageName());
                propertyVar.setObjectPropertyMetaId(propertyMeta.getId());
                propertyVar.setSensitive(propertyMeta.getSensitive());
                listPropertyVars.add(propertyVar);
            }

            return listPropertyVars;
        }
        throw new UnsupportedOperationException();
    }

    private List<CoreObjectPropertyVar> calObjectPropertyMetaAsListPropertyVars(CoreObjectPropertyMeta propertyMeta,
            CoreObjectVar parentObjectVar, CoreObjectVarCalculationContext ctx) {
        throw new UnsupportedOperationException();
    }

    /**
     * 
     * @param propertyMeta
     * @param parentObjectVar
     * @param ctx
     * @return
     */
    private List<CoreObjectPropertyVar> calPropertyMetaAsListPropertyVarResult(CoreObjectPropertyMeta propertyMeta,
            CoreObjectVar parentObjectVar, CoreObjectVarCalculationContext ctx) {

        log.debug("to calculate list property vars for objectName={}, propName={}, dataType={}, refType={}",
                propertyMeta.getObjectMeta().getName(), propertyMeta.getName(), propertyMeta.getDataType(),
                propertyMeta.getRefType());

        String dataType = propertyMeta.getDataType();
        if (isBasicDataType(dataType)) {
            return calBasicTypeAsListPropertyVars(propertyMeta, ctx);
        } else if (isListDataType(dataType)) {
            return calculateListPropertyMetaAsListPropertyVarResult(propertyMeta, parentObjectVar, ctx);
        } else if (isObjectDataType(dataType)) {
            return calObjectPropertyMetaAsListPropertyVars(propertyMeta, parentObjectVar, ctx);
        } else {
            return null;
        }
    }

    private List<CoreObjectVar> calculateObjectMetaAsObjectVarListResult(CoreObjectMeta objectMeta,
            CoreObjectVar parentObjectVar, CoreObjectVarCalculationContext ctx) {
        List<CoreObjectVar> rawObjectValues = new ArrayList<>();

        Map<String, List<CoreObjectPropertyVar>> propertyMetaVarsMap = new HashMap<String, List<CoreObjectPropertyVar>>();

        List<CoreObjectPropertyMeta> propertyMetas = objectMeta.getPropertyMetas();

        for (CoreObjectPropertyMeta propertyMeta : propertyMetas) {
            List<CoreObjectPropertyVar> listPropertyVars = calPropertyMetaAsListPropertyVarResult(propertyMeta,
                    parentObjectVar, ctx);
            propertyMetaVarsMap.put(propertyMeta.getId(), listPropertyVars);
        }

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

    /**
     * data type: list ref type: object
     * 
     * @param propertyMeta
     * @param parentObjectVar
     * @param ctx
     * @return List<CoreObjectVar>
     */
    private List<CoreObjectVar> calObjectTypePropertyAsListResult(CoreObjectPropertyMeta propertyMeta,
            CoreObjectVar parentObjectVar, CoreObjectVarCalculationContext ctx) {
        CoreObjectMeta refObjectMeta = propertyMeta.getRefObjectMeta();
        log.debug("calculate list object vars for objectName={},propertyName={}, refObject={} ",
                propertyMeta.getObjectMeta().getName(), propertyMeta.getName(), refObjectMeta.getName());
        List<CoreObjectVar> rawObjectValues = calculateObjectMetaAsObjectVarListResult(refObjectMeta, parentObjectVar,
                ctx);

        return rawObjectValues;

    }

    private List<Object> calculateBasicTypePropertyValueAsListResult(CoreObjectPropertyMeta propertyMeta,
            CoreObjectVar parentObjectVar, CoreObjectVarCalculationContext ctx) {
        List<Object> dataObjectValues = new ArrayList<>();

        if (propertyMeta.isEntityMapping()) {
            List<Object> vals = calculateBasicTypePropertyValueListFromEntity(propertyMeta, parentObjectVar, ctx);
            if (vals != null) {
                dataObjectValues.addAll(vals);
            }
        } else if (propertyMeta.isConstantMapping()) {
            Object val = calculateBasicTypePropertyValueFromConstant(propertyMeta, parentObjectVar, ctx);
            if (val != null) {
                dataObjectValues.add(val);
            }
        } else if (propertyMeta.isSystemVariableMapping()) {
            Object val = calculateBasicTypePropertyValueFromSystemVariable(propertyMeta, parentObjectVar, ctx);
            if (val != null) {
                dataObjectValues.add(val);
            }
        } else if (propertyMeta.isContextMapping()) {
            List<Object> vals = calculateBasicTypePropertyValueListFromContext(propertyMeta, parentObjectVar, ctx);
            if (vals != null) {
                dataObjectValues.addAll(vals);
            }
        } else {
            // do nothing
        }
        return dataObjectValues;
    }

}
