package com.webank.wecube.platform.core.service.plugin;

import static com.webank.wecube.platform.core.utils.Constants.MAPPING_TYPE_CONSTANT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
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
            CoreObjectVarCalculationContext ctx, String objectMetaExpr) {

        List<CoreObjectVar> rootObjectVars = doCalculateCoreObjectVarList(objectMeta, null, ctx,
                ctx.getRootEntityDataId(), objectMetaExpr);

        return rootObjectVars;

    }

    protected List<CoreObjectVar> doCalculateCoreObjectVarList(CoreObjectMeta objectMeta, CoreObjectVar parentObjectVar,
            CoreObjectVarCalculationContext ctx, String rootEntityDataId, String objectMetaExpr) {
        log.info("start to calculate object values for:{} {} {}", objectMeta, rootEntityDataId, objectMetaExpr);
        // String objectMetaExpr = objectMeta.getMapExpr();
        if (StringUtils.isBlank(objectMetaExpr)) {
            if (checkIfHasEntityMappingProperty(objectMeta)) {
                String errMsg = "The object meta expression is blank but has to calculate entity mapping data.";
                log.error(errMsg);
                throw new WecubeCoreException(errMsg);
            } else {
                log.info(
                        "The expression of object {} is blank and try to calculate object value from none entity strategy.",
                        objectMeta.getName());
                return tryCalculateCoreObjectVarListFromNoneEntityMapping(objectMeta, parentObjectVar, ctx,
                        rootEntityDataId);
            }
        } else {
            log.info("To calculate object: {} with expression: {} and root data id: {}", objectMeta.getName(),
                    objectMetaExpr, rootEntityDataId);
            return tryCalculateCoreObjectVarListFromEntityMapping(objectMeta, parentObjectVar, ctx, rootEntityDataId,
                    objectMetaExpr);
        }
    }

    private List<CoreObjectVar> tryCalculateCoreObjectVarListFromEntityMapping(CoreObjectMeta objectMeta,
            CoreObjectVar parentObjectVar, CoreObjectVarCalculationContext ctx, String rootEntityDataId,
            String objectMetaExpr) {
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
                log.info("Cannot get entity data id from entity data:{}", boundDataMap);
                continue;
            }

            objectMetaBoundDataIds.add(boundDataId);
        }

        for (String boundDataId : objectMetaBoundDataIds) {
            CoreObjectVar objectVar = new CoreObjectVar();
            objectVar.setId(LocalIdGenerator.generateId(PREFIX_OBJECT_VAR_ID));
            objectVar.setName(objectMeta.getName());
            objectVar.setObjectMeta(objectMeta);
            objectVar.setObjectMetaId(objectMeta.getId());
            objectVar.setPackageName(objectMeta.getPackageName());

            if (parentObjectVar != null) {
                objectVar.setParentObjectName(parentObjectVar.getName());
                objectVar.setParentObjectVarId(parentObjectVar.getId());

            }
            coreObjectVars.add(objectVar);

            List<CoreObjectPropertyMeta> propertyMetas = objectMeta.getPropertyMetas();
            for (CoreObjectPropertyMeta propertyMeta : propertyMetas) {
                CoreObjectPropertyVar propertyVar = tryCalculatePropertyValue(propertyMeta, objectVar, ctx,
                        boundDataId);
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

    private List<Object> tryCalculatePropertyDataValueObject(CoreObjectPropertyMeta propertyMeta,
            CoreObjectVar parentObjectVar, CoreObjectVarCalculationContext ctx, String rootDataId) {
        log.info("start to calculate property data value for:{}  {}", propertyMeta, rootDataId);
        String dataType = propertyMeta.getDataType();
        List<Object> dataObjectValues = new ArrayList<>();

        //TODO
        if (propertyMeta.isMultipleData()) {
            List<Object> propertyResultValues = tryCalculateListTypePropertyValue(propertyMeta, parentObjectVar, ctx,
                    rootDataId);
            if (propertyResultValues != null) {
                dataObjectValues.addAll(propertyResultValues);
            }
        } else {
            //TODO
            if (isBasicDataType(dataType)) {
                List<Object> propertyResultValues = tryCalculateBasicTypePropertyValue(propertyMeta, parentObjectVar,
                        ctx, rootDataId);
                
                if(propertyResultValues == null || propertyResultValues.isEmpty()) {
                    
                }else {
                    if(propertyResultValues.size() > 1) {
                        String errMsg = String.format(
                                "object [%s] property [%s] required [%s] but total [%s] objects got.",propertyMeta.getObjectName(), propertyMeta.getName(), dataType, propertyResultValues.size());
                        
                        log.error(errMsg);
                        
                        throw new WecubeCoreException(errMsg);
                    }
                    dataObjectValues.add(propertyResultValues.get(0));
                }
            } else if (isObjectDataType(dataType)) {
                CoreObjectMeta refObjectMeta = propertyMeta.getRefObjectMeta();
                if (refObjectMeta == null) {
                    String errMsg = String.format(
                            "Cannot get reference object meta for [%s]:[%s] but object type is [%s]",
                            propertyMeta.getObjectName(), propertyMeta.getName(), dataType);
                    log.error(errMsg);
                    throw new WecubeCoreException(errMsg);
                }
                List<CoreObjectVar> refObjectVars = doCalculateCoreObjectVarList(refObjectMeta, parentObjectVar, ctx,
                        rootDataId, propertyMeta.getMapExpr());

                if (refObjectVars == null || refObjectVars.isEmpty()) {
                    // do nothing
                } else {
                    if (refObjectVars.size() > 1) {
                        String errMsg = String.format(
                                "object [%s] property [%s] required [%s] but total [%s] objects got.",
                                propertyMeta.getObjectName(), propertyMeta.getName(), dataType, refObjectVars.size());
                        throw new WecubeCoreException(errMsg);
                    }

                    dataObjectValues.add(refObjectVars.get(0));
                }
            }
        }

        return dataObjectValues;
    }

    private List<CoreObjectVar> tryCalculateCoreObjectVarListFromNoneEntityMapping(CoreObjectMeta objectMeta,
            CoreObjectVar parentObjectVar, CoreObjectVarCalculationContext ctx, String rootEntityDataId) {
        List<CoreObjectVar> coreObjectVars = new ArrayList<>();
        CoreObjectVar objectVar = new CoreObjectVar();
        objectVar.setId(LocalIdGenerator.generateId(PREFIX_OBJECT_VAR_ID));
        objectVar.setName(objectMeta.getName());
        objectVar.setObjectMeta(objectMeta);
        objectVar.setObjectMetaId(objectMeta.getId());
        objectVar.setPackageName(objectMeta.getPackageName());

        if (parentObjectVar != null) {
            objectVar.setParentObjectName(parentObjectVar.getName());
            objectVar.setParentObjectVarId(parentObjectVar.getId());

        }
        coreObjectVars.add(objectVar);

        List<CoreObjectPropertyMeta> propertyMetas = objectMeta.getPropertyMetas();
        for (CoreObjectPropertyMeta propertyMeta : propertyMetas) {
            CoreObjectPropertyVar propertyVar = tryCalculatePropertyValue(propertyMeta, objectVar, ctx, null);
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

        return coreObjectVars;
    }

    private boolean checkIfHasEntityMappingProperty(CoreObjectMeta objectMeta) {
        List<CoreObjectPropertyMeta> propertyMetas = objectMeta.getPropertyMetas();
        if (propertyMetas == null || propertyMetas.isEmpty()) {
            return false;
        }

        for (CoreObjectPropertyMeta propertyMeta : propertyMetas) {
            if (propertyMeta.isEntityMapping()) {
                return true;
            }
        }

        return false;
    }

    private CoreObjectPropertyVar tryCalculatePropertyValue(CoreObjectPropertyMeta propertyMeta,
            CoreObjectVar parentObjectVar, CoreObjectVarCalculationContext ctx, String rootDataId) {

        CoreObjectPropertyVar propertyVar = new CoreObjectPropertyVar();
        propertyVar.setId(LocalIdGenerator.generateId(PREFIX_PROPERTY_VAR_ID));
        propertyVar.setName(propertyMeta.getName());
        propertyVar.setDataType(propertyMeta.getDataType());

        List<Object> rawDataValueObjects = tryCalculatePropertyDataValueObject(propertyMeta, parentObjectVar, ctx,
                rootDataId);

        Object dataValueObject = determineObjectDataValue(propertyMeta, rawDataValueObjects);

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

    private Object determineObjectDataValue(CoreObjectPropertyMeta propertyMeta, List<Object> dataValueObjects) {
        if (dataValueObjects == null) {
            return null;
        }

        if (dataValueObjects.isEmpty()) {
            return null;
        }
        String dataType = propertyMeta.getDataType();
        if (propertyMeta.isMultipleData()) {
            return dataValueObjects;
        } else {
            if (isObjectDataType(dataType)) {
                return dataValueObjects.get(0);
            }

            if (isBasicDataType(dataType)) {
                if (dataValueObjects.size() == 1) {
                    return dataValueObjects.get(0);
                } else {
                    if (isStringDataType(dataType)) {
                        return assembleValueList(dataValueObjects);
                    } else {
                        return dataValueObjects;
                    }
                }
            }

            return dataValueObjects;
        }

    }

    private List<Object> tryCalculateBasicTypePropertyValue(CoreObjectPropertyMeta propertyMeta,
            CoreObjectVar parentObjectVar, CoreObjectVarCalculationContext ctx, String rootDataId) {
        List<Object> dataObjectValues = null;

        if (propertyMeta.isEntityMapping()) {
            dataObjectValues = calculateBasicTypePropertyValueFromEntity(propertyMeta, parentObjectVar, ctx,
                    rootDataId);
        } else if (propertyMeta.isConstantMapping()) {
            dataObjectValues = calculateBasicTypePropertyValueFromConstant(propertyMeta, parentObjectVar, ctx);
        } else if (propertyMeta.isSystemVariableMapping()) {
            dataObjectValues = calculateBasicTypePropertyValueFromSystemVariable(propertyMeta, parentObjectVar, ctx);
        } else if (propertyMeta.isContextMapping()) {
            dataObjectValues = calculateBasicTypePropertyValueFromContext(propertyMeta, parentObjectVar, ctx,
                    rootDataId);
        } else {
            // need throw exception here
            dataObjectValues = null;
        }
        return dataObjectValues;
    }

    private List<Object> calculateBasicTypePropertyValueFromEntity(CoreObjectPropertyMeta propertyMeta,
            CoreObjectVar parentObjectVar, CoreObjectVarCalculationContext ctx, String rootDataId) {
        String mappingEntityExpression = propertyMeta.getMapExpr();
        if (log.isDebugEnabled()) {
            log.debug("expression:{}", mappingEntityExpression);
        }

        EntityOperationRootCondition condition = new EntityOperationRootCondition(mappingEntityExpression, rootDataId);

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
        return attrValsPerExpr;
    }

    private List<Object> calculateBasicTypePropertyValueFromConstant(CoreObjectPropertyMeta propertyMeta,
            CoreObjectVar parentObjectVar, CoreObjectVarCalculationContext ctx) {
        List<Object> resultObjectValues = new ArrayList<>();
        if (StringUtils.isNoneBlank(propertyMeta.getMapExpr())) {
            resultObjectValues.add(propertyMeta.getMapExpr());
            return resultObjectValues;
        }
        //
        TaskNodeDefInfoEntity currTaskNodeDefInfo = ctx.getTaskNodeDefInfo();
        String curTaskNodeDefId = currTaskNodeDefInfo.getId();

        String paramName = propertyMeta.getName();
        TaskNodeParamEntity nodeParamEntity = taskNodeParamRepository
                .selectOneByTaskNodeDefIdAndParamName(curTaskNodeDefId, paramName);

        if (nodeParamEntity == null) {
            return resultObjectValues;
        }

        String val = null;

        if (MAPPING_TYPE_CONSTANT.equalsIgnoreCase(nodeParamEntity.getBindType())) {
            val = nodeParamEntity.getBindVal();
        }

        if (StringUtils.isNoneBlank(val)) {
            resultObjectValues.add(val);
        }

        return resultObjectValues;
    }

    private List<Object> calculateBasicTypePropertyValueFromSystemVariable(CoreObjectPropertyMeta propertyMeta,
            CoreObjectVar parentObjectVar, CoreObjectVarCalculationContext ctx) {
        List<Object> resultObjectValues = new ArrayList<>();
        String systemVariableName = propertyMeta.getMapExpr();
        SystemVariables sVariable = systemVariableService
                .getSystemVariableByPackageNameAndName(propertyMeta.getPackageName(), systemVariableName);

        if (sVariable == null) {
            return resultObjectValues;
        }

        String sVal = null;
        if (sVariable != null) {
            sVal = sVariable.getValue();
            if (StringUtils.isBlank(sVal)) {
                sVal = sVariable.getDefaultValue();
            }
        }

        if (StringUtils.isNoneBlank(sVal)) {
            resultObjectValues.add(sVal);
        }

        return resultObjectValues;
    }

    private List<Object> calculateBasicTypePropertyValueFromContext(CoreObjectPropertyMeta propertyMeta,
            CoreObjectVar parentObjectVar, CoreObjectVarCalculationContext ctx, String rootDataId) {
        List<Object> resultObjectValues = new ArrayList<>();

        TaskNodeDefInfoEntity currTaskNodeDefInfo = ctx.getTaskNodeDefInfo();
        String curTaskNodeDefId = currTaskNodeDefInfo.getId();
        String paramName = propertyMeta.getName();
        TaskNodeParamEntity nodeParamEntity = taskNodeParamRepository
                .selectOneByTaskNodeDefIdAndParamName(curTaskNodeDefId, paramName);

        if (nodeParamEntity == null) {
            return resultObjectValues;
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

        List<TaskNodeExecParamEntity> execParamEntities = taskNodeExecParamRepository
                .selectAllByRequestIdAndParamNameAndParamType(requestEntity.getReqId(), boundParamName, boundParamType);

        if (execParamEntities == null || execParamEntities.isEmpty()) {
            return resultObjectValues;
        }

        for (TaskNodeExecParamEntity param : execParamEntities) {

            resultObjectValues.add(param.getParamDataValue());
        }

        return resultObjectValues;
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

    /**
     * data type: list ref type:string, int, object
     * 
     * @param propertyMeta
     * @param parentObjectVar
     * @param ctx
     * @return List<CoreObjectListVar>
     */
    private List<Object> tryCalculateListTypePropertyValue(CoreObjectPropertyMeta propertyMeta,
            CoreObjectVar parentObjectVar, CoreObjectVarCalculationContext ctx, String rootDataId) {
        if (!propertyMeta.isMultipleData()) {
            return null;// throw exception here?
        }

        List<Object> dataObjectValues = new ArrayList<>();

        if (isBasicDataType(propertyMeta.getDataType())) {
            List<Object> propertyResultValues = tryCalculateBasicTypePropertyValue(propertyMeta, parentObjectVar, ctx,
                    rootDataId);
            if (propertyResultValues != null) {
                dataObjectValues.addAll(propertyResultValues);
            }
        }

        if (isObjectDataType(propertyMeta.getDataType())) {
            CoreObjectMeta refObjectMeta = propertyMeta.getRefObjectMeta();
            List<CoreObjectVar> refObjectVars = doCalculateCoreObjectVarList(refObjectMeta, parentObjectVar, ctx,
                    rootDataId, propertyMeta.getMapExpr());

            if (dataObjectValues != null) {
                for (CoreObjectVar refObjectVar : refObjectVars) {
                    dataObjectValues.add(refObjectVar);
                }
            }
        }

        return dataObjectValues;
    }

}
