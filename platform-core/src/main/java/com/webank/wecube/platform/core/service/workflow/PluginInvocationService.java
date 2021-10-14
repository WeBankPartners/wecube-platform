package com.webank.wecube.platform.core.service.workflow;

import static com.webank.wecube.platform.core.utils.Constants.ASYNC_SERVICE_SYMBOL;
import static com.webank.wecube.platform.core.utils.Constants.FIELD_REQUIRED;
import static com.webank.wecube.platform.core.utils.Constants.MAPPING_TYPE_CONSTANT;
import static com.webank.wecube.platform.core.utils.Constants.MAPPING_TYPE_CONTEXT;
import static com.webank.wecube.platform.core.utils.Constants.MAPPING_TYPE_ENTITY;
import static com.webank.wecube.platform.core.utils.Constants.MAPPING_TYPE_OBJECT;
import static com.webank.wecube.platform.core.utils.Constants.MAPPING_TYPE_SYSTEM_VARIABLE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.plugin.ItsDangerConfirmResultDto;
import com.webank.wecube.platform.core.entity.plugin.CoreObjectMeta;
import com.webank.wecube.platform.core.entity.plugin.CoreObjectPropertyMeta;
import com.webank.wecube.platform.core.entity.plugin.CoreObjectVar;
import com.webank.wecube.platform.core.entity.plugin.PluginConfigInterfaceParameters;
import com.webank.wecube.platform.core.entity.plugin.PluginConfigInterfaces;
import com.webank.wecube.platform.core.entity.plugin.PluginConfigs;
import com.webank.wecube.platform.core.entity.plugin.PluginInstances;
import com.webank.wecube.platform.core.entity.plugin.PluginPackageAttributes;
import com.webank.wecube.platform.core.entity.plugin.PluginPackages;
import com.webank.wecube.platform.core.entity.plugin.SystemVariables;
import com.webank.wecube.platform.core.entity.workflow.ExtraTaskEntity;
import com.webank.wecube.platform.core.entity.workflow.ProcDefInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.ProcExecBindingEntity;
import com.webank.wecube.platform.core.entity.workflow.ProcExecContextEntity;
import com.webank.wecube.platform.core.entity.workflow.ProcInstInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeDefInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeExecParamEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeExecRequestEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeInstInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeParamEntity;
import com.webank.wecube.platform.core.model.workflow.BoundTaskNodeExecParamWrapper;
import com.webank.wecube.platform.core.model.workflow.ContextCalculationParam;
import com.webank.wecube.platform.core.model.workflow.ContextCalculationParamCollection;
import com.webank.wecube.platform.core.model.workflow.DmeOutputParamAttr;
import com.webank.wecube.platform.core.model.workflow.InputParamAttr;
import com.webank.wecube.platform.core.model.workflow.InputParamObject;
import com.webank.wecube.platform.core.model.workflow.PluginInvocationCommand;
import com.webank.wecube.platform.core.model.workflow.PluginInvocationResult;
import com.webank.wecube.platform.core.model.workflow.ProcExecBindingKey;
import com.webank.wecube.platform.core.model.workflow.ProcExecBindingKeyLink;
import com.webank.wecube.platform.core.model.workflow.WorkflowInstCreationContext;
import com.webank.wecube.platform.core.service.dme.EntityDataAttr;
import com.webank.wecube.platform.core.service.dme.EntityDataRecord;
import com.webank.wecube.platform.core.service.dme.EntityOperationRootCondition;
import com.webank.wecube.platform.core.service.dme.EntityQueryExprNodeInfo;
import com.webank.wecube.platform.core.service.dme.EntityRouteDescription;
import com.webank.wecube.platform.core.service.dme.EntityTreeNodesOverview;
import com.webank.wecube.platform.core.service.dme.StandardEntityDataNode;
import com.webank.wecube.platform.core.service.dme.StandardEntityOperationResponseDto;
import com.webank.wecube.platform.core.service.dme.StandardEntityOperationRestClient;
import com.webank.wecube.platform.core.service.plugin.CoreObjectVarCalculationContext;
import com.webank.wecube.platform.core.service.plugin.PluginPackageDataModelService;
import com.webank.wecube.platform.core.service.plugin.PluginParamObject;
import com.webank.wecube.platform.core.service.plugin.PluginParamObjectVarStorage;
import com.webank.wecube.platform.core.service.workflow.PluginInvocationProcessor.PluginInterfaceInvocationContext;
import com.webank.wecube.platform.core.service.workflow.PluginInvocationProcessor.PluginInterfaceInvocationResult;
import com.webank.wecube.platform.core.service.workflow.PluginInvocationProcessor.PluginInvocationOperation;
import com.webank.wecube.platform.core.support.plugin.dto.DynamicEntityAttrValueDto;
import com.webank.wecube.platform.core.support.plugin.dto.DynamicEntityValueDto;
import com.webank.wecube.platform.core.support.plugin.dto.TaskFormDataEntityDto;
import com.webank.wecube.platform.core.support.plugin.dto.TaskFormItemMetaDto;
import com.webank.wecube.platform.core.support.plugin.dto.TaskFormItemValueDto;
import com.webank.wecube.platform.core.support.plugin.dto.TaskFormMetaDto;
import com.webank.wecube.platform.core.support.plugin.dto.TaskFormValueDto;
import com.webank.wecube.platform.core.utils.Constants;
import com.webank.wecube.platform.core.utils.JsonUtils;
import com.webank.wecube.platform.workflow.WorkflowConstants;
import com.webank.wecube.platform.workflow.commons.LocalIdGenerator;

/**
 * 
 * @author gavin
 *
 */
@Service
public class PluginInvocationService extends AbstractPluginInvocationService {

    @Autowired
    protected PluginParamObjectVarStorage pluginParamObjectVarStorageService;

    @Autowired
    protected PluginPackageDataModelService pluginPackageDataModelService;

    /**
     * 
     * @param cmd
     */
    public void invokePluginInterface(PluginInvocationCommand cmd) {
        if (log.isInfoEnabled()) {
            log.info("invoke plugin interface with:{}", cmd);
        }

        ProcInstInfoEntity procInstEntity = null;
        TaskNodeInstInfoEntity taskNodeInstEntity = null;
        try {
            procInstEntity = retrieveProcInstInfoEntity(cmd);
            taskNodeInstEntity = retrieveTaskNodeInstInfoEntity(procInstEntity.getId(), cmd.getNodeId());
            doInvokePluginInterface(procInstEntity, taskNodeInstEntity, cmd);
        } catch (Exception e) {
            log.warn("errors while processing {} {}", cmd.getClass().getSimpleName(), cmd, e);
            pluginInvocationResultService.responsePluginInterfaceInvocation(
                    new PluginInvocationResult().parsePluginInvocationCommand(cmd).withResultCode(RESULT_CODE_ERR));

            updateTaskNodeInstInfoEntityFaulted(taskNodeInstEntity, e);
        }
    }

    /**
     * handle results of plugin interface invocation.
     * 
     * @param pluginInvocationResult
     * @param ctx
     */
    public void handlePluginInterfaceInvocationResult(PluginInterfaceInvocationResult pluginInvocationResult,
            PluginInterfaceInvocationContext ctx) {
        if (log.isDebugEnabled()) {
            log.debug("handle plugin interface invocation result");
        }

        if (!pluginInvocationResult.isSuccess() || pluginInvocationResult.hasErrors()) {
            handleErrorInvocationResult(pluginInvocationResult, ctx);

            return;
        }

        PluginConfigInterfaces pci = ctx.getPluginConfigInterface();
        if (ASYNC_SERVICE_SYMBOL.equalsIgnoreCase(pci.getIsAsyncProcessing())) {
            log.debug("such interface is asynchronous service : {} ", pci.getServiceName());
            return;
        }

        List<Object> resultData = pluginInvocationResult.getResultData();

        if (resultData == null) {
            handleNullResultData(pluginInvocationResult, ctx);
            return;
        }

        try {

            handleResultData(pluginInvocationResult, ctx, resultData);
            PluginInvocationResult result = new PluginInvocationResult()
                    .parsePluginInvocationCommand(ctx.getPluginInvocationCommand());
            result.setResultCode(RESULT_CODE_OK);
            pluginInvocationResultService.responsePluginInterfaceInvocation(result);
            handlePluginInterfaceInvocationSuccess(pluginInvocationResult, ctx);

            return;
        } catch (Exception e) {
            PluginInvocationResult result = new PluginInvocationResult()
                    .parsePluginInvocationCommand(ctx.getPluginInvocationCommand());
            String errMsg = String.format("result data handling failed :{}", ctx.getPluginInvocationCommand());
            log.warn(errMsg, e);
            result.setResultCode(RESULT_CODE_ERR);

            try {
                pluginInvocationResultService.responsePluginInterfaceInvocation(result);
                String pluginErrMsg = (e.getMessage() == null ? "error" : trimWithMaxLength(e.getMessage()));
                handlePluginInterfaceInvocationFailure(pluginInvocationResult, ctx, "5002",
                        "result data handling failed:" + pluginErrMsg);
            } catch (Exception e1) {
                log.error("errors while process plugin result.{} ", ctx.getPluginInvocationCommand(), e1);
            }
        }

        return;
    }

    private void updateTaskNodeInstInfoEntityFaulted(TaskNodeInstInfoEntity taskNodeInstEntity, Exception e) {
        if (taskNodeInstEntity == null) {
            return;
        }
        log.debug("mark task node instance {} as {}", taskNodeInstEntity.getId(),
                TaskNodeInstInfoEntity.FAULTED_STATUS);
        TaskNodeInstInfoEntity toUpdateTaskNodeInstInfoEntity = taskNodeInstInfoRepository
                .selectByPrimaryKey(taskNodeInstEntity.getId());

        toUpdateTaskNodeInstInfoEntity.setStatus(TaskNodeInstInfoEntity.FAULTED_STATUS);
        toUpdateTaskNodeInstInfoEntity.setUpdatedTime(new Date());
        toUpdateTaskNodeInstInfoEntity.setUpdatedBy(WorkflowConstants.DEFAULT_USER);
        toUpdateTaskNodeInstInfoEntity.setErrMsg(trimWithMaxLength(e == null ? "errors" : e.getMessage()));

        taskNodeInstInfoRepository.updateByPrimaryKeySelective(toUpdateTaskNodeInstInfoEntity);
    }

    private boolean verifyIfExcludeModeExecBindings(ProcDefInfoEntity procDefInfo, ProcInstInfoEntity procInst,
            TaskNodeDefInfoEntity taskNodeDef, TaskNodeInstInfoEntity taskNodeInst, PluginInvocationCommand cmd,
            List<ProcExecBindingEntity> nodeObjectBindings) {
        if (nodeObjectBindings == null || nodeObjectBindings.isEmpty()) {
            return false;
        }

        String excludeMode = procDefInfo.getExcludeMode();
        if (ProcDefInfoEntity.EXCLUDE_MODE_YES.equalsIgnoreCase(excludeMode)) {
            return tryVerifyIfAnyRunningProcInstBound(procDefInfo, procInst, taskNodeDef, taskNodeInst, cmd,
                    nodeObjectBindings);
        } else {
            return tryVerifyIfAnyExclusiveRunningProcInstBound(procDefInfo, procInst, taskNodeDef, taskNodeInst, cmd,
                    nodeObjectBindings);
        }
    }

    private boolean tryVerifyIfAnyRunningProcInstBound(ProcDefInfoEntity procDefInfo, ProcInstInfoEntity procInst,
            TaskNodeDefInfoEntity taskNodeDef, TaskNodeInstInfoEntity taskNodeInst, PluginInvocationCommand cmd,
            List<ProcExecBindingEntity> nodeObjectBindings) {
        if (nodeObjectBindings == null || nodeObjectBindings.isEmpty()) {
            return false;
        }

        Set<Integer> boundProcInstIds = new HashSet<>();
        for (ProcExecBindingEntity nodeObjectBinding : nodeObjectBindings) {
            if (StringUtils.isBlank(nodeObjectBinding.getEntityDataId())) {
                continue;
            }
            int boundCount = procExecBindingMapper.countAllBoundRunningProcInstancesWithoutProcInst(
                    nodeObjectBinding.getEntityDataId(), procInst.getId());
            if (boundCount <= 0) {
                continue;
            }

            List<Integer> boundProcInstIdsOfSingleEntity = procExecBindingMapper
                    .selectAllBoundRunningProcInstancesWithoutProcInst(nodeObjectBinding.getEntityDataId(),
                            procInst.getId());
            if (boundProcInstIdsOfSingleEntity == null || boundProcInstIdsOfSingleEntity.isEmpty()) {
                continue;
            }

            boundProcInstIds.addAll(boundProcInstIdsOfSingleEntity);
        }

        if (boundProcInstIds.isEmpty()) {
            return false;
        }

        log.info("Current process {}:{}:{} is exclusive but still {} processes running.", procDefInfo.getId(),
                procDefInfo.getProcDefName(), procInst.getId(), boundProcInstIds.size());
        for (Integer boundProcInstId : boundProcInstIds) {
            log.info("boundProcInstId:{}", boundProcInstId);
        }

        return true;

    }

    private boolean tryVerifyIfAnyExclusiveRunningProcInstBound(ProcDefInfoEntity procDefInfo,
            ProcInstInfoEntity procInst, TaskNodeDefInfoEntity taskNodeDef, TaskNodeInstInfoEntity taskNodeInst,
            PluginInvocationCommand cmd, List<ProcExecBindingEntity> nodeObjectBindings) {
        if (nodeObjectBindings == null || nodeObjectBindings.isEmpty()) {
            return false;
        }
        Set<Integer> boundExclusiveProcInstIds = new HashSet<>();

        for (ProcExecBindingEntity nodeObjectBinding : nodeObjectBindings) {
            if (StringUtils.isBlank(nodeObjectBinding.getEntityDataId())) {
                continue;
            }

            int exclusiveProcInstCount = procExecBindingMapper
                    .countAllExclusiveBoundRunningProcInstancesWithoutProcInst(nodeObjectBinding.getEntityDataId(),
                            procInst.getId());

            if (exclusiveProcInstCount <= 0) {
                continue;
            }

            List<Integer> boundProcInstIdsOfSingleEntity = procExecBindingMapper
                    .selectAllExclusiveBoundRunningProcInstancesWithoutProcInst(nodeObjectBinding.getEntityDataId(),
                            procInst.getId());
            if (boundProcInstIdsOfSingleEntity == null || boundProcInstIdsOfSingleEntity.isEmpty()) {
                continue;
            }

            boundExclusiveProcInstIds.addAll(boundProcInstIdsOfSingleEntity);
        }

        if (boundExclusiveProcInstIds.isEmpty()) {
            return false;
        }

        log.info("Current process {}:{}:{} is shared but there are {}  exclusive processes running.",
                procDefInfo.getId(), procDefInfo.getProcDefName(), procInst.getId(), boundExclusiveProcInstIds.size());

        for (Integer boundExclusiveProcInstId : boundExclusiveProcInstIds) {
            log.info("boundExclusiveProcInstId:{}", boundExclusiveProcInstId);
        }

        return true;
    }

    protected void doInvokePluginInterface(ProcInstInfoEntity procInstEntity, TaskNodeInstInfoEntity taskNodeInstEntity,
            PluginInvocationCommand cmd) {

        ProcDefInfoEntity procDefInfoEntity = procDefInfoMapper.selectByPrimaryKey(procInstEntity.getProcDefId());
        TaskNodeDefInfoEntity taskNodeDefEntity = retrieveTaskNodeDefInfoEntity(procInstEntity.getProcDefId(),
                cmd.getNodeId());

        // to refactor using strategy mode
        if (isSystemAutomationTaskNode(taskNodeDefEntity)) {
            doInvokeSystemAutomationPluginInterface(procInstEntity, taskNodeInstEntity, procDefInfoEntity,
                    taskNodeDefEntity, cmd);
        } else if (isUserTaskNode(taskNodeDefEntity)) {
            doInvokeUserTaskPluginInterface(procInstEntity, taskNodeInstEntity, procDefInfoEntity, taskNodeDefEntity,
                    cmd);
        } else if (isDataOperationTaskNode(taskNodeDefEntity)) {
            doInvokeDataOperationPluginInterface(procInstEntity, taskNodeInstEntity, procDefInfoEntity,
                    taskNodeDefEntity, cmd);
        }
    }

    /**
     * SDTN Handling data operation task node.
     * 
     */
    protected void doInvokeDataOperationPluginInterface(ProcInstInfoEntity procInstEntity,
            TaskNodeInstInfoEntity taskNodeInstEntity, ProcDefInfoEntity procDefInfoEntity,
            TaskNodeDefInfoEntity taskNodeDefEntity, PluginInvocationCommand cmd) {
        // TODO
        List<ProcExecBindingEntity> nodeObjectBindings = retrieveProcExecBindingEntities(taskNodeInstEntity);
        if (nodeObjectBindings == null || nodeObjectBindings.isEmpty()) {
            log.info("There are not any task node object bindings found and skipped for task node:{}",
                    taskNodeInstEntity.getId());
            return;
        }

        WorkflowInstCreationContext ctx = tryFetchWorkflowInstCreationContext(taskNodeInstEntity);
        if (ctx == null) {
            return;
        }

        for (ProcExecBindingEntity objectBinding : nodeObjectBindings) {

            String bindDataId = objectBinding.getEntityDataId();
            if (bindDataId.startsWith(TEMPORARY_ENTITY_ID_PREFIX)) {
                tryCreateNewEntityData(bindDataId, ctx, objectBinding);
            } else {
                tryUpdateExistedEntityData(bindDataId, ctx);
            }
        }

        String ctxJson = convertWorkflowInstCreationContextToJson(ctx);
        List<ProcExecContextEntity> procExecContextEntities = this.procExecContextMapper.selectAllContextByCtxType(
                procDefInfoEntity.getId(), procInstEntity.getId(), ProcExecContextEntity.CTX_TYPE_PROCESS);

        if (procExecContextEntities == null || procExecContextEntities.isEmpty()) {
            log.info("Cannot find any process creation context information for {} {}", procDefInfoEntity.getId(),
                    procInstEntity.getId());

            return;
        }

        ProcExecContextEntity procExecContextEntity = procExecContextEntities.get(0);
        procExecContextEntity.setCtxData(ctxJson);
        procExecContextEntity.setUpdatedBy(WorkflowConstants.DEFAULT_USER);
        procExecContextEntity.setUpdatedTime(new Date());
        procExecContextMapper.updateByPrimaryKeySelective(procExecContextEntity);
    }

    private void tryCreateNewEntityData(String bindDataId, WorkflowInstCreationContext ctx,
            ProcExecBindingEntity objectBinding) {
        String objectId = bindDataId.substring(TEMPORARY_ENTITY_ID_PREFIX.length());
        DynamicEntityValueDto entityValueDto = ctx.findByOid(objectId);
        if (entityValueDto == null) {
            log.info("Can not find such entity value from creation context with ID:{}", objectId);
            return;
        }

        // TODO
        String packageName = entityValueDto.getPackageName();
        String entityName = entityValueDto.getEntityName();

        List<DynamicEntityAttrValueDto> attrValues = entityValueDto.getAttrValues();
        if (attrValues == null || attrValues.isEmpty()) {
            log.info("Attributes not assigned values for object :{}", objectId);
            return;
        }

        Map<String, Object> objDataMap = new HashMap<String, Object>();
        for (DynamicEntityAttrValueDto attr : attrValues) {
            objDataMap.put(attr.getAttrName(), attr.getDataValue());
        }

        log.info("try to create entity.{} {} {}", entityValueDto.getPackageName(), entityValueDto.getEntityName(),
                objDataMap);

        Map<String, Object> resultMap = entityOperationService.create(packageName, entityName, objDataMap);
        String newEntityDataId = (String) resultMap.get(Constants.UNIQUE_IDENTIFIER);
        if (StringUtils.isBlank(newEntityDataId)) {
            log.warn("Entity created but there is not identity returned.{} {} {}", packageName, entityName, objDataMap);
            return;
        }

        String newEntityDataName = (String) resultMap.get(Constants.VISUAL_FIELD);

        // to refresh entity data id to dto
        if (StringUtils.isBlank(entityValueDto.getEntityDataId())) {
            entityValueDto.setEntityDataId(newEntityDataId);
            entityValueDto.setEntityDisplayName(newEntityDataName);
            entityValueDto.setEntityDataState("Created");
        }

        // refresh object binding
        objectBinding.setEntityDataId(newEntityDataId);
        objectBinding.setEntityDataName(newEntityDataName);
        objectBinding.setUpdatedBy(WorkflowConstants.DEFAULT_USER);
        objectBinding.setUpdatedTime(new Date());

        procExecBindingMapper.updateByPrimaryKeySelective(objectBinding);

    }

    private void tryUpdateExistedEntityData(String bindDataId, WorkflowInstCreationContext ctx) {
        DynamicEntityValueDto entityValueDto = ctx.findByEntityDataIdOrOid(bindDataId);
        if (entityValueDto == null) {
            log.info("entity data value does not exist in creation context for object id:{}", bindDataId);
            return;
        }

        // TODO
        String packageName = entityValueDto.getPackageName();
        String entityName = entityValueDto.getEntityName();
        EntityRouteDescription entityDef = entityDataRouteFactory.deduceEntityDescription(packageName, entityName);
        List<EntityDataRecord> recordsToUpdate = new ArrayList<>();
        EntityDataRecord recordToUpdate = new EntityDataRecord();
        recordToUpdate.setId(bindDataId);

        List<DynamicEntityAttrValueDto> attrValues = entityValueDto.getAttrValues();
        if (attrValues == null || attrValues.isEmpty()) {
            return;
        }

        for (DynamicEntityAttrValueDto attr : attrValues) {
            EntityDataAttr attrUpdate = new EntityDataAttr();
            attrUpdate.setAttrName(attr.getAttrName());
            attrUpdate.setAttrValue(attr.getDataValue());

            recordToUpdate.addAttrs(attrUpdate);
        }

        recordsToUpdate.add(recordToUpdate);

        StandardEntityOperationRestClient restClient = new StandardEntityOperationRestClient(jwtSsoRestTemplate);

        StandardEntityOperationResponseDto resultDto = restClient.update(entityDef, recordsToUpdate);
        if (StandardEntityOperationResponseDto.STATUS_ERROR.equals(resultDto.getStatus())) {
            log.error("errors to update entity:{}", resultDto.getMessage());
            return;
        }

        log.info("entity data updated successfully:{}", bindDataId);
    }

    /**
     * SUTN Handling user operation task node.
     * 
     */
    protected void doInvokeUserTaskPluginInterface(ProcInstInfoEntity procInstEntity,
            TaskNodeInstInfoEntity taskNodeInstEntity, ProcDefInfoEntity procDefInfoEntity,
            TaskNodeDefInfoEntity taskNodeDefEntity, PluginInvocationCommand cmd) {

        List<ProcExecBindingEntity> nodeObjectBindings = retrieveProcExecBindingEntities(taskNodeInstEntity);
        PluginConfigInterfaces pluginConfigInterface = retrievePluginConfigInterface(taskNodeDefEntity,
                cmd.getNodeId());

        List<InputParamObject> inputParamObjs = calculateInputParamObjectsForUserTask(procInstEntity,
                taskNodeInstEntity, procDefInfoEntity, taskNodeDefEntity, cmd, pluginConfigInterface,
                nodeObjectBindings);

        int reqObjectAmount = 0;
        if (inputParamObjs != null) {
            reqObjectAmount = inputParamObjs.size();
        }

        PluginInterfaceInvocationContext ctx = new PluginInterfaceInvocationContext() //
                .withNodeObjectBindings(nodeObjectBindings) //
                .withPluginConfigInterface(pluginConfigInterface) //
                .withProcInstEntity(procInstEntity) //
                .withTaskNodeInstEntity(taskNodeInstEntity)//
                .withTaskNodeDefEntity(taskNodeDefEntity)//
                .withPluginInvocationCommand(cmd)//
                .withReqObjectAmount(reqObjectAmount);

        parsePluginInstance(ctx);

        buildTaskNodeExecRequestEntity(ctx);
        List<Map<String, Object>> pluginParameters = calculateInputParameters(ctx, inputParamObjs, ctx.getRequestId(),
                procInstEntity.getOper());

        PluginInvocationOperation operation = new PluginInvocationOperation() //
                .withCallback(this::handlePluginInterfaceInvocationResult) //
                .withPluginInvocationRestClient(this.pluginInvocationRestClient) //
                .withPluginParameters(pluginParameters) //
                .withInstanceHost(ctx.getInstanceHost()) //
                .withInterfacePath(ctx.getInterfacePath()) //
                .withPluginInterfaceInvocationContext(ctx) //
                .withRequestId(ctx.getRequestId());

        pluginInvocationProcessor.process(operation);
    }

    private List<InputParamObject> calculateInputParamObjectsForUserTask(ProcInstInfoEntity procInstEntity,
            TaskNodeInstInfoEntity taskNodeInstEntity, ProcDefInfoEntity procDefInfoEntity,
            TaskNodeDefInfoEntity taskNodeDefEntity, PluginInvocationCommand cmd,
            PluginConfigInterfaces pluginConfigInterface, List<ProcExecBindingEntity> nodeObjectBindings) {

        String taskFormInputValue = "";
        if (hasTaskFormInputParameter(pluginConfigInterface)) {
            taskFormInputValue = tryCalculateTaskFormValueAsJson(procInstEntity, taskNodeInstEntity, procDefInfoEntity,
                    taskNodeDefEntity, cmd, pluginConfigInterface, nodeObjectBindings);
        }

        List<InputParamObject> inputParamObjs = new ArrayList<>();

        List<PluginConfigInterfaceParameters> intfInputParams = pluginConfigInterface.getInputParameters();
        if (intfInputParams == null || intfInputParams.isEmpty()) {
            return inputParamObjs;
        }

        InputParamObject inputObj = new InputParamObject();

        for (PluginConfigInterfaceParameters param : intfInputParams) {
            String paramName = param.getName();
            String paramType = param.getDataType();
            String mappingType = param.getMappingType();

            if (!(MAPPING_TYPE_SYSTEM_VARIABLE.equalsIgnoreCase(mappingType)
                    || MAPPING_TYPE_CONSTANT.equalsIgnoreCase(mappingType) 
                    || MAPPING_TYPE_CONTEXT.equalsIgnoreCase(mappingType))) {
                continue;
            }

            inputObj.addAttrNames(paramName);

            InputParamAttr inputAttr = new InputParamAttr();
            inputAttr.setName(paramName);
            inputAttr.setDataType(paramType);
            inputAttr.setSensitive(IS_SENSITIVE_ATTR.equalsIgnoreCase(param.getSensitiveData()));
            inputAttr.setParamDef(param);

            List<Object> objectVals = new ArrayList<Object>();
            //
            inputAttr.setMapType(mappingType);
            inputAttr.setMultiple(param.getMultiple());
            boolean isFieldRequired = isFieldRequired(param.getRequired());

            if (PARAM_NAME_TASK_FORM_INPUT.equals(param.getName())) {
                objectVals.add(taskFormInputValue);
                inputAttr.addValues(objectVals);

                inputObj.addAttrs(inputAttr);
                continue;
            }

            if (MAPPING_TYPE_SYSTEM_VARIABLE.equalsIgnoreCase(mappingType)) {
                handleSystemMapping(mappingType, param, paramName, objectVals);
            }

            if (MAPPING_TYPE_CONSTANT.equalsIgnoreCase(mappingType)) {
                handleConstantMapping(mappingType, taskNodeDefEntity, paramName, objectVals, isFieldRequired, param);
            }

            if (MAPPING_TYPE_CONTEXT.equals(mappingType)) {
                handleContextMappingForUserTask(mappingType, taskNodeDefEntity, paramName, procInstEntity, param,
                        paramType, objectVals);
            }

            inputAttr.addValues(objectVals);

            inputObj.addAttrs(inputAttr);
        }

        inputParamObjs.add(inputObj);

        return inputParamObjs;
    }

    private String tryCalculateTaskFormValueAsJson(ProcInstInfoEntity procInstEntity,
            TaskNodeInstInfoEntity taskNodeInstEntity, ProcDefInfoEntity procDefInfoEntity,
            TaskNodeDefInfoEntity taskNodeDefEntity, PluginInvocationCommand cmd,
            PluginConfigInterfaces pluginConfigInterface, List<ProcExecBindingEntity> nodeObjectBindings) {
        TaskFormMetaDto taskFormMetaDto = tryFetchUserTaskFormMeta(procInstEntity, taskNodeInstEntity,
                procDefInfoEntity, taskNodeDefEntity, pluginConfigInterface);

        TaskFormValueDto taskFormValueDto = tryCalculateUserTaskFormValue(procInstEntity, taskNodeInstEntity,
                procDefInfoEntity, taskNodeDefEntity, cmd, pluginConfigInterface, nodeObjectBindings, taskFormMetaDto);

        String taskFormInputValue = "";
        if (taskFormValueDto != null) {
            taskFormInputValue = convertTaskFormValueToJson(taskFormValueDto);
        }

        return taskFormInputValue;
    }

    private boolean hasTaskFormInputParameter(PluginConfigInterfaces pluginConfigInterface) {
        List<PluginConfigInterfaceParameters> inputParams = pluginConfigInterface.getInputParameters();
        if (inputParams == null || inputParams.isEmpty()) {
            return false;
        }

        for (PluginConfigInterfaceParameters inputParam : inputParams) {
            if (PARAM_NAME_TASK_FORM_INPUT.equals(inputParam.getName())) {
                return true;
            }
        }

        return false;
    }

    private TaskFormMetaDto tryFetchUserTaskFormMeta(ProcInstInfoEntity procInstEntity,
            TaskNodeInstInfoEntity taskNodeInstEntity, ProcDefInfoEntity procDefInfoEntity,
            TaskNodeDefInfoEntity taskNodeDefEntity, PluginConfigInterfaces pluginConfigInterface) {

        String intfPath = pluginConfigInterface.getPath();
        String procInstId = String.valueOf(procInstEntity.getId());
        String nodeDefId = taskNodeDefEntity.getId();

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("procInstId", procInstId);
        params.put("nodeDefId", nodeDefId);

        TaskFormMetaDto taskFormMetaDto = this.pluginTaskFormRestClient.getRemoteTaskFormMeta(intfPath, params);
        return taskFormMetaDto;
    }

    private String convertTaskFormValueToJson(TaskFormValueDto dto) {
        try {
            String json = objectMapper.writeValueAsString(dto);
            return json;
        } catch (JsonProcessingException e) {
            String errMsg = String.format("Failed to convert %s to JSON.", dto.getClass().getName());
            log.error(errMsg, e);
            throw new WecubeCoreException(errMsg);
        }
    }

    private TaskFormValueDto tryCalculateUserTaskFormValue(ProcInstInfoEntity procInstEntity,
            TaskNodeInstInfoEntity taskNodeInstEntity, ProcDefInfoEntity procDefInfoEntity,
            TaskNodeDefInfoEntity taskNodeDefEntity, PluginInvocationCommand cmd,
            PluginConfigInterfaces pluginConfigInterface, List<ProcExecBindingEntity> nodeObjectBindings,
            TaskFormMetaDto taskFormMetaDto) {

        TaskFormValueDto taskFormValueDto = new TaskFormValueDto();
        taskFormValueDto.setProcDefId(procDefInfoEntity.getId());
        taskFormValueDto.setProcDefKey(procDefInfoEntity.getProcDefKey());
        taskFormValueDto.setProcInstId(procInstEntity.getId());
        taskFormValueDto.setProcInstKey(procInstEntity.getProcInstKey());
        taskFormValueDto.setTaskNodeDefId(taskNodeDefEntity.getId());
        taskFormValueDto.setTaskNodeInstId(taskNodeInstEntity.getId());

        taskFormValueDto.setFormMetaId(taskFormMetaDto.getFormMetaId());

        List<TaskFormDataEntityDto> formDataEntities = calculateFormDataEntities(procInstEntity, taskNodeInstEntity,
                procDefInfoEntity, taskNodeDefEntity, cmd, pluginConfigInterface, nodeObjectBindings, taskFormMetaDto);

        taskFormValueDto.addFormDataEntities(formDataEntities);

        return taskFormValueDto;
    }

    private List<TaskFormDataEntityDto> calculateFormDataEntities(ProcInstInfoEntity procInstEntity,
            TaskNodeInstInfoEntity taskNodeInstEntity, ProcDefInfoEntity procDefInfoEntity,
            TaskNodeDefInfoEntity taskNodeDefEntity, PluginInvocationCommand cmd,
            PluginConfigInterfaces pluginConfigInterface, List<ProcExecBindingEntity> nodeObjectBindings,
            TaskFormMetaDto taskFormMetaDto) {
        List<TaskFormDataEntityDto> formDataEntities = new ArrayList<>();
        if (nodeObjectBindings == null || nodeObjectBindings.isEmpty()) {
            log.info("There are not any task node object bindings for {} {} {}", procDefInfoEntity.getId(),
                    procInstEntity.getId(), taskNodeInstEntity.getId());
            return formDataEntities;
        }

        WorkflowInstCreationContext ctx = tryFetchWorkflowInstCreationContext(taskNodeInstEntity);
        if (ctx == null) {
            return formDataEntities;
        }

        formDataEntities = calculateFormDataEntitiesWithContext(nodeObjectBindings, ctx, taskFormMetaDto);

        return formDataEntities;
    }

    private List<TaskFormDataEntityDto> calculateFormDataEntitiesWithContext(
            List<ProcExecBindingEntity> nodeObjectBindings, WorkflowInstCreationContext ctx,
            TaskFormMetaDto taskFormMetaDto) {
        Map<String, TaskFormDataEntityDto> oidAndEntities = new HashMap<String, TaskFormDataEntityDto>();

        List<TaskFormDataEntityDto> formDataEntities = new ArrayList<>();
        List<TaskFormItemMetaDto> formItemMetas = taskFormMetaDto.getFormItemMetas();
        if (formItemMetas == null || formItemMetas.isEmpty()) {
            return formDataEntities;
        }

        for (TaskFormItemMetaDto taskFormItemMeta : formItemMetas) {
            List<String> bindObjectIds = tryCalculateBindObjectIds(taskFormItemMeta, nodeObjectBindings);
            if (bindObjectIds == null || bindObjectIds.isEmpty()) {
                continue;
            }

            for (String bindObjectId : bindObjectIds) {

                TaskFormDataEntityDto taskFormDataEntityDto = oidAndEntities.get(bindObjectId);
                if (taskFormDataEntityDto == null) {
                    taskFormDataEntityDto = new TaskFormDataEntityDto();
                    taskFormDataEntityDto.setOid(bindObjectId);
                    taskFormDataEntityDto.setEntityName(taskFormItemMeta.getEntityName());
                    taskFormDataEntityDto.setPackageName(taskFormItemMeta.getPackageName());

                    oidAndEntities.put(bindObjectId, taskFormDataEntityDto);
                }

                TaskFormItemValueDto taskFormItemValueDto = new TaskFormItemValueDto();
                taskFormItemValueDto.setOid(bindObjectId);
                taskFormItemValueDto.setAttrName(taskFormItemMeta.getAttrName());
                taskFormItemValueDto.setPackageName(taskFormItemMeta.getPackageName());
                taskFormItemValueDto.setEntityName(taskFormItemMeta.getEntityName());
                taskFormItemValueDto.setFormItemMetaId(taskFormItemMeta.getFormItemMetaId());

                DynamicEntityValueDto dynamicEntityValueDto = ctx.findByEntityDataIdOrOid(bindObjectId);

                if (dynamicEntityValueDto != null) {
                    taskFormDataEntityDto.setEntityDataId(dynamicEntityValueDto.getEntityDataId());
                    taskFormDataEntityDto.setEntityDataState(dynamicEntityValueDto.getEntityDataState());
                    taskFormDataEntityDto.setBindFlag(dynamicEntityValueDto.getBindFlag());
                    taskFormDataEntityDto.setFullEntityDataId(dynamicEntityValueDto.getFullEntityDataId());

                    DynamicEntityAttrValueDto attrValueDto = dynamicEntityValueDto
                            .findAttrValue(taskFormItemMeta.getAttrName());
                    if (attrValueDto != null) {
                        taskFormItemValueDto.setAttrValue(attrValueDto.getDataValue());
                    } else {
                        // fetch form cmdb?
                    }
                }

                taskFormDataEntityDto.addFormItemValue(taskFormItemValueDto);

            }

        }

        formDataEntities.addAll(oidAndEntities.values());

        return formDataEntities;
    }

    private List<String> tryCalculateBindObjectIds(TaskFormItemMetaDto taskFormItemMeta,
            List<ProcExecBindingEntity> nodeObjectBindings) {
        List<String> nodeBindObjectIds = new ArrayList<>();
        for (ProcExecBindingEntity bindEntity : nodeObjectBindings) {
            String entityTypeId = bindEntity.getEntityTypeId();
            if (StringUtils.isBlank(entityTypeId)) {
                continue;
            }

            String[] entityTypeIdParts = entityTypeId.split(":");
            if (entityTypeIdParts.length != 2) {
                continue;
            }

            if (taskFormItemMeta.getPackageName().equals(entityTypeIdParts[0])
                    && taskFormItemMeta.getEntityName().equals(entityTypeIdParts[1])) {
                String bindId = bindEntity.getEntityDataId();
                if (bindId.startsWith(TEMPORARY_ENTITY_ID_PREFIX)) {
                    bindId = bindId.substring(TEMPORARY_ENTITY_ID_PREFIX.length());
                }
                nodeBindObjectIds.add(bindId);
            }
            // to tidy entity name here?
        }

        return nodeBindObjectIds;
    }

    /**
     * SSTN Handling system automation task node
     * 
     */
    protected void doInvokeSystemAutomationPluginInterface(ProcInstInfoEntity procInstEntity,
            TaskNodeInstInfoEntity taskNodeInstEntity, ProcDefInfoEntity procDefInfoEntity,
            TaskNodeDefInfoEntity taskNodeDefEntity, PluginInvocationCommand cmd) {

        Map<Object, Object> externalCacheMap = new HashMap<>();

        List<ProcExecBindingEntity> nodeObjectBindings = null;

        if (isDynamicBindTaskNode(taskNodeDefEntity) && !isBoundTaskNodeInst(taskNodeInstEntity)) {
            nodeObjectBindings = dynamicCalculateTaskNodeExecBindings(taskNodeDefEntity, procInstEntity,
                    taskNodeInstEntity, cmd, externalCacheMap);
            boolean hasExcludeModeExecBindings = verifyIfExcludeModeExecBindings(procDefInfoEntity, procInstEntity,
                    taskNodeDefEntity, taskNodeInstEntity, cmd, nodeObjectBindings);
            if (hasExcludeModeExecBindings) {

                ExtraTaskEntity extraTaskEntity = new ExtraTaskEntity();
                extraTaskEntity.setCreatedBy(WorkflowConstants.DEFAULT_USER);
                extraTaskEntity.setCreatedTime(new Date());
                extraTaskEntity.setPriority(0);
                extraTaskEntity.setRev(0);
                extraTaskEntity.setTaskType(ExtraTaskEntity.TASK_TYPE_DYNAMIC_BIND_TASK_NODE_RETRY);
                extraTaskEntity.setStatus(ExtraTaskEntity.STATUS_NEW);
                extraTaskEntity.setTaskSeqNo(LocalIdGenerator.generateId());
                String taskDef = marshalPluginInvocationCommand(cmd);
                extraTaskEntity.setTaskDef(taskDef);

                extraTaskMapper.insert(extraTaskEntity);
                return;
            } else {
                storeProcExecBindingEntities(nodeObjectBindings);
                taskNodeInstEntity.setBindStatus(TaskNodeInstInfoEntity.BIND_STATUS_BOUND);
                taskNodeInstInfoRepository.updateByPrimaryKeySelective(taskNodeInstEntity);
            }

        } else {
            nodeObjectBindings = retrieveProcExecBindingEntities(taskNodeInstEntity);
        }

        PluginConfigInterfaces pluginConfigInterface = retrievePluginConfigInterface(taskNodeDefEntity,
                cmd.getNodeId());

        List<InputParamObject> inputParamObjs = new ArrayList<>();

        if (nodeObjectBindings == null || nodeObjectBindings.isEmpty()) {
            // #2233
            inputParamObjs = tryCalculateInputParamObjectsWithoutBindings(procDefInfoEntity, procInstEntity,
                    taskNodeInstEntity, taskNodeDefEntity, pluginConfigInterface);
        } else {
            inputParamObjs = tryCalculateInputParamObjectsWithBindings(procDefInfoEntity, procInstEntity,
                    taskNodeInstEntity, taskNodeDefEntity, nodeObjectBindings, pluginConfigInterface, externalCacheMap);
        }

        int reqObjectAmount = 0;
        if (inputParamObjs != null) {
            reqObjectAmount = inputParamObjs.size();
        }

        PluginInterfaceInvocationContext ctx = new PluginInterfaceInvocationContext() //
                .withNodeObjectBindings(nodeObjectBindings) //
                .withPluginConfigInterface(pluginConfigInterface) //
                .withProcInstEntity(procInstEntity) //
                .withTaskNodeInstEntity(taskNodeInstEntity)//
                .withTaskNodeDefEntity(taskNodeDefEntity)//
                .withPluginInvocationCommand(cmd)//
                .withReqObjectAmount(reqObjectAmount);

        parsePluginInstance(ctx);

        buildTaskNodeExecRequestEntity(ctx);
        List<Map<String, Object>> pluginParameters = calculateInputParameters(ctx, inputParamObjs, ctx.getRequestId(),
                procInstEntity.getOper());

        if (riskyCommandVerifier.needPerformDangerousCommandsChecking(taskNodeInstEntity, taskNodeDefEntity)) {
            log.info("risky commands pre checking needed by task node : {}:{}", taskNodeDefEntity.getId(),
                    taskNodeInstEntity.getId());
            ItsDangerConfirmResultDto confirmResult = riskyCommandVerifier.performDangerousCommandsChecking(ctx,
                    pluginParameters);

            if (confirmResult != null) {
                postProcessRiskyVerifyingResult(ctx, cmd, taskNodeInstEntity, confirmResult);
                return;
            } else {
                postProcessNoneRiskyVerifyingResult(ctx, cmd, taskNodeInstEntity, taskNodeDefEntity);
            }

        }

        PluginInvocationOperation operation = new PluginInvocationOperation() //
                .withCallback(this::handlePluginInterfaceInvocationResult) //
                .withPluginInvocationRestClient(this.pluginInvocationRestClient) //
                .withPluginParameters(pluginParameters) //
                .withInstanceHost(ctx.getInstanceHost()) //
                .withInterfacePath(ctx.getInterfacePath()) //
                .withPluginInterfaceInvocationContext(ctx) //
                .withRequestId(ctx.getRequestId());

        pluginInvocationProcessor.process(operation);
    }

    private void postProcessNoneRiskyVerifyingResult(PluginInterfaceInvocationContext ctx, PluginInvocationCommand cmd,
            TaskNodeInstInfoEntity taskNodeInstEntity, TaskNodeDefInfoEntity taskNodeDefEntity) {
        taskNodeInstEntity.setPreCheckRet(TaskNodeInstInfoEntity.PRE_CHECK_RESULT_NONE_RISK);
        taskNodeInstEntity.setUpdatedTime(new Date());
        taskNodeInstInfoRepository.updateByPrimaryKeySelective(taskNodeInstEntity);
        log.info("RISKY commands checking performed and passed by task node: {}:{}:{}", taskNodeDefEntity.getNodeName(),
                taskNodeDefEntity.getId(), taskNodeInstEntity.getId());
    }

    private void postProcessRiskyVerifyingResult(PluginInterfaceInvocationContext ctx, PluginInvocationCommand cmd,
            TaskNodeInstInfoEntity taskNodeInstEntity, ItsDangerConfirmResultDto confirmResult) {
        taskNodeInstEntity.setStatus(TaskNodeInstInfoEntity.RISKY_STATUS);
        taskNodeInstEntity.setUpdatedBy(WorkflowConstants.DEFAULT_USER);
        taskNodeInstEntity.setUpdatedTime(new Date());
        taskNodeInstEntity.setPreCheckRet(TaskNodeInstInfoEntity.PRE_CHECK_RESULT_RISKY);
        taskNodeInstInfoRepository.updateByPrimaryKeySelective(taskNodeInstEntity);

        pluginInvocationResultService.responsePluginInterfaceInvocation(
                new PluginInvocationResult().parsePluginInvocationCommand(cmd).withResultCode(RESULT_CODE_ERR));

        TaskNodeExecRequestEntity requestEntity = ctx.getTaskNodeExecRequestEntity();
        requestEntity.setErrCode("CONFIRM");
        requestEntity.setErrMsg(confirmResult.getMessage());
        requestEntity.setUpdatedTime(new Date());

        taskNodeExecRequestRepository.updateByPrimaryKey(requestEntity);
    }

    private List<ProcExecBindingEntity> dynamicCalculateTaskNodeExecBindings(TaskNodeDefInfoEntity taskNodeDefEntity,
            ProcInstInfoEntity procInstEntity, TaskNodeInstInfoEntity taskNodeInstEntity, PluginInvocationCommand cmd,
            Map<Object, Object> cacheMap) {

        log.info("about to calculate bindings for task node {} {}", taskNodeDefEntity.getId(),
                taskNodeDefEntity.getNodeName());
        int procInstId = procInstEntity.getId();
        int nodeInstId = taskNodeInstEntity.getId();
        procExecBindingMapper.deleteAllTaskNodeBindings(procInstId, nodeInstId);

        String associatedNodeId = taskNodeDefEntity.getAssociatedNodeId();
        if (StringUtils.isBlank(associatedNodeId)) {
            return dynamicCalculateTaskNodeExecBindingsFromCmdb(taskNodeDefEntity, procInstEntity, taskNodeInstEntity,
                    cmd, cacheMap);
        } else {
            return dynamicCalculateTaskNodeExecBindingsFromPreNode(taskNodeDefEntity, procInstEntity,
                    taskNodeInstEntity, cmd, cacheMap);
        }

    }

    private List<ProcExecBindingEntity> dynamicCalculateTaskNodeExecBindingsFromPreNode(
            TaskNodeDefInfoEntity taskNodeDefEntity, ProcInstInfoEntity procInstEntity,
            TaskNodeInstInfoEntity taskNodeInstEntity, PluginInvocationCommand cmd, Map<Object, Object> cacheMap) {
        String associatedNodeId = taskNodeDefEntity.getAssociatedNodeId();
        int procInstId = procInstEntity.getId();

        TaskNodeInstInfoEntity associatedNodeInstEntity = taskNodeInstInfoRepository
                .selectOneByProcInstIdAndNodeId(procInstEntity.getId(), associatedNodeId);

        if (associatedNodeInstEntity == null) {
            String errMsg = String.format("Associated task node instance:%s does not exist.", associatedNodeId);
            throw new WecubeCoreException(errMsg);
        }

        List<ProcExecBindingEntity> bindEntities = new ArrayList<>();

        List<ProcExecBindingEntity> bindingsOfAssociatedNode = procExecBindingMapper
                .selectAllBoundTaskNodeBindings(procInstId, associatedNodeInstEntity.getId());

        if (bindingsOfAssociatedNode == null || bindingsOfAssociatedNode.isEmpty()) {
            return bindEntities;
        }

        for (ProcExecBindingEntity assBinding : bindingsOfAssociatedNode) {
            ProcExecBindingEntity taskNodeBinding = new ProcExecBindingEntity();
            taskNodeBinding.setBindType(ProcExecBindingEntity.BIND_TYPE_TASK_NODE_INSTANCE);
            taskNodeBinding.setBindFlag(ProcExecBindingEntity.BIND_FLAG_YES);
            taskNodeBinding.setProcDefId(taskNodeDefEntity.getProcDefId());
            taskNodeBinding.setProcInstId(procInstEntity.getId());
            taskNodeBinding.setEntityDataId(assBinding.getEntityDataId());
            taskNodeBinding.setFullEntityDataId(assBinding.getFullEntityDataId());
            taskNodeBinding.setEntityTypeId(assBinding.getEntityTypeId());
            taskNodeBinding.setNodeDefId(taskNodeDefEntity.getId());
            taskNodeBinding.setTaskNodeInstId(taskNodeInstEntity.getId());
            taskNodeBinding.setEntityDataName(assBinding.getEntityDataName());
            taskNodeBinding.setCreatedBy(WorkflowConstants.DEFAULT_USER);
            taskNodeBinding.setCreatedTime(new Date());

            bindEntities.add(taskNodeBinding);
        }

        return bindEntities;
    }

    private List<ProcExecBindingEntity> dynamicCalculateTaskNodeExecBindingsFromCmdb(
            TaskNodeDefInfoEntity taskNodeDefEntity, ProcInstInfoEntity procInstEntity,
            TaskNodeInstInfoEntity taskNodeInstEntity, PluginInvocationCommand cmd, Map<Object, Object> cacheMap) {
        int procInstId = procInstEntity.getId();
//        int nodeInstId = taskNodeInstEntity.getId();
        List<ProcExecBindingEntity> entities = new ArrayList<>();

        ProcExecBindingEntity procInstBinding = procExecBindingMapper.selectProcInstBindings(procInstId);
        if (procInstBinding == null) {
            log.info("cannot find process instance exec binding for {}", procInstId);
            return entities;
        }

        String rootDataId = procInstBinding.getEntityDataId();

        if (StringUtils.isBlank(rootDataId)) {
            log.info("root data id is blank for process instance {}", procInstId);
            return entities;
        }
        String routineExpr = calculateDataModelExpression(taskNodeDefEntity);

        if (StringUtils.isBlank(routineExpr)) {
            log.info("the routine expression is blank for {} {}", taskNodeDefEntity.getId(),
                    taskNodeDefEntity.getNodeName());
            return entities;
        }

        log.info("About to fetch data for node {} {} with expression {} and data id {}", taskNodeDefEntity.getId(),
                taskNodeDefEntity.getNodeName(), routineExpr, rootDataId);
        EntityOperationRootCondition condition = new EntityOperationRootCondition(routineExpr, rootDataId);
        try {
            EntityTreeNodesOverview overview = entityOperationService.generateEntityLinkOverview(condition, cacheMap);

            List<ProcExecBindingEntity> boundEntities = calDynamicLeafNodeEntityNodesBindings(taskNodeDefEntity,
                    procInstEntity, taskNodeInstEntity, overview.getLeafNodeEntityNodes());

            log.info("DYNAMIC BINDING:total {} entities bound for {}-{}-{}", boundEntities.size(),
                    taskNodeInstEntity.getNodeDefId(), taskNodeInstEntity.getNodeName(), taskNodeInstEntity.getId());
            return boundEntities;
        } catch (Exception e) {
            String errMsg = String.format("Errors while fetching data for node %s %s with expr %s and data id %s",
                    taskNodeDefEntity.getId(), taskNodeDefEntity.getNodeName(), routineExpr, rootDataId);
            log.error(errMsg, e);
            throw new WecubeCoreException("3191", errMsg, taskNodeDefEntity.getId(), taskNodeDefEntity.getNodeName(),
                    routineExpr, rootDataId);
        }
    }

    private List<ProcExecBindingEntity> calDynamicLeafNodeEntityNodesBindings(TaskNodeDefInfoEntity taskNodeDef,
            ProcInstInfoEntity procInstEntity, TaskNodeInstInfoEntity taskNodeInstEntity,
            List<StandardEntityDataNode> leafNodeEntityNodes) {
        List<ProcExecBindingEntity> entities = new ArrayList<>();
        if (leafNodeEntityNodes == null) {
            return entities;
        }

        if (log.isInfoEnabled()) {
            log.info("total {} nodes returned as default bindings for {} {} {}", leafNodeEntityNodes.size(),
                    taskNodeDef.getId(), taskNodeDef.getNodeId(), taskNodeDef.getNodeName());
        }

        for (StandardEntityDataNode tn : leafNodeEntityNodes) {

            ProcExecBindingEntity taskNodeBinding = new ProcExecBindingEntity();
            taskNodeBinding.setBindType(ProcExecBindingEntity.BIND_TYPE_TASK_NODE_INSTANCE);
            taskNodeBinding.setBindFlag(ProcExecBindingEntity.BIND_FLAG_YES);
            taskNodeBinding.setProcDefId(taskNodeDef.getProcDefId());
            taskNodeBinding.setProcInstId(procInstEntity.getId());
            taskNodeBinding.setEntityDataId(tn.getId());
            taskNodeBinding.setFullEntityDataId(tn.getFullId());
            taskNodeBinding.setEntityTypeId(String.format("%s:%s", tn.getPackageName(), tn.getEntityName()));
            taskNodeBinding.setNodeDefId(taskNodeDef.getId());
            taskNodeBinding.setTaskNodeInstId(taskNodeInstEntity.getId());
            taskNodeBinding.setEntityDataName(String.valueOf(tn.getDisplayName()));
            taskNodeBinding.setCreatedBy(WorkflowConstants.DEFAULT_USER);
            taskNodeBinding.setCreatedTime(new Date());

            entities.add(taskNodeBinding);
        }

        return entities;

    }

    private List<InputParamObject> tryCalculateContextMappingInputParamsObjects(ProcDefInfoEntity procDefEntity,
            ProcInstInfoEntity procInstEntity, TaskNodeInstInfoEntity currTaskNodeInstEntity,
            TaskNodeDefInfoEntity currTaskNodeDefEntity, PluginConfigInterfaces pluginConfigInterface,
            Map<String, PluginConfigInterfaceParameters> contextConfigInterfaceInputParams) {
        log.info("try to calculate input parameter objects from context for taskNodeInstId={}",
                currTaskNodeInstEntity.getId());
        String curTaskNodeDefId = currTaskNodeDefEntity.getId();

        ContextCalculationParamCollection contextCalculationParamCollection = new ContextCalculationParamCollection();
        contextCalculationParamCollection.setProcDefInfoEntity(procDefEntity);
        contextCalculationParamCollection.setProcInstEntity(procInstEntity);
        contextCalculationParamCollection.setCurrTaskNodeDefEntity(currTaskNodeDefEntity);
        contextCalculationParamCollection.setCurrTaskNodeInstEntity(currTaskNodeInstEntity);
        contextCalculationParamCollection.setPluginConfigInterface(pluginConfigInterface);

        for (PluginConfigInterfaceParameters param : contextConfigInterfaceInputParams.values()) {

            String paramName = param.getName();
            String paramDataType = param.getDataType();

            ContextCalculationParam contextCalculationParam = new ContextCalculationParam();
            contextCalculationParam.setParam(param);
            contextCalculationParam.setParamName(paramName);
            contextCalculationParam.setParamDataType(paramDataType);
            contextCalculationParam.setProcDefInfoEntity(procDefEntity);
            contextCalculationParam.setProcInstEntity(procInstEntity);
            contextCalculationParam.setCurrTaskNodeDefEntity(currTaskNodeDefEntity);
            contextCalculationParam.setCurrTaskNodeInstEntity(currTaskNodeInstEntity);
            contextCalculationParam.setPluginConfigInterface(pluginConfigInterface);

            contextCalculationParamCollection.addContextCalculationParam(contextCalculationParam);

            if (isObjectDataType(paramDataType)) {
                log.info("Parameter:{} is object type.", paramName);
                continue;
            }

            TaskNodeParamEntity nodeParamEntity = taskNodeParamRepository
                    .selectOneByTaskNodeDefIdAndParamName(curTaskNodeDefId, paramName);

            if (nodeParamEntity == null) {
                log.info("mapping type is {} but node parameter entity is null for {}", MAPPING_TYPE_CONTEXT,
                        curTaskNodeDefId);

                if (Constants.FIELD_REQUIRED.equalsIgnoreCase(param.getRequired())) {
                    log.info("Task node parameter entity does not exist for {} {}", curTaskNodeDefId, paramName);
                } else {
                    log.info("Task node parameter entity does not exist for {} {} but field not required.",
                            curTaskNodeDefId, paramName);
                }

                continue;
            }

            contextCalculationParam.setNodeParamEntity(nodeParamEntity);

            String bindNodeId = nodeParamEntity.getBindNodeId();
            String bindParamType = nodeParamEntity.getBindParamType();
            String bindParamName = nodeParamEntity.getBindParamName();

            // get by procInstId and nodeId
            TaskNodeInstInfoEntity boundNodeInstEntity = taskNodeInstInfoRepository
                    .selectOneByProcInstIdAndNodeId(procInstEntity.getId(), bindNodeId);

            if (boundNodeInstEntity == null) {
                log.error("Bound node instance entity does not exist for {} {}", procInstEntity.getId(), bindNodeId);
                throw new WecubeCoreException("3171", "Bound node instance entity does not exist.");
            }

            contextCalculationParam.setBoundNodeInstEntity(boundNodeInstEntity);
            TaskNodeDefInfoEntity boundNodeDefEntity = taskNodeDefInfoRepository
                    .selectByPrimaryKey(boundNodeInstEntity.getNodeDefId());
            contextCalculationParam.setBoundNodeDefEntity(boundNodeDefEntity);

            List<TaskNodeExecRequestEntity> boundRequestEntities = taskNodeExecRequestRepository
                    .selectCurrentEntityByNodeInstId(boundNodeInstEntity.getId());

            if (boundRequestEntities == null || boundRequestEntities.isEmpty()) {
                log.info("cannot find request entity for {}", boundNodeInstEntity.getId());
                continue;
            }

            if (boundRequestEntities.size() > 1) {
                log.warn("duplicated request entity found for {}, total {} requests found.",
                        boundNodeInstEntity.getId(), boundRequestEntities.size());
            }

            TaskNodeExecRequestEntity boundRequestEntity = boundRequestEntities.get(0);
            contextCalculationParam.setBoundNodeRequestEntity(boundRequestEntity);

            List<TaskNodeExecParamEntity> boundExecParamEntities = taskNodeExecParamRepository
                    .selectAllByRequestIdAndParamNameAndParamType(boundRequestEntity.getReqId(), bindParamName,
                            bindParamType);

            if (boundExecParamEntities == null || boundExecParamEntities.isEmpty()) {
                if (FIELD_REQUIRED.equals(param.getRequired())) {
                    log.info("parameter entity does not exist but such plugin parameter is mandatory for {} {}",
                            bindParamName, bindParamType);
                } else {
                    log.info("parameter entity does not exist but such plugin parameter is not mandatory for {} {}",
                            bindParamName, bindParamType);
                }
                continue;
            }

            for (TaskNodeExecParamEntity boundExecParamEntity : boundExecParamEntities) {
                BoundTaskNodeExecParamWrapper boundTaskNodeExecParamWrapper = new BoundTaskNodeExecParamWrapper();
                boundTaskNodeExecParamWrapper.setBoundTaskNodeExecParamEntity(boundExecParamEntity);
                if (StringUtils.isNoneBlank(boundExecParamEntity.getParamDefId())) {
                    PluginConfigInterfaceParameters boundParamDef = pluginConfigInterfaceParametersMapper
                            .selectByPrimaryKey(boundExecParamEntity.getParamDefId());
                    boundTaskNodeExecParamWrapper.setBoundParam(boundParamDef);
                }

                contextCalculationParam.getBoundExecParamWrappers().add(boundTaskNodeExecParamWrapper);
            }
        }

        List<InputParamObject> paramObjects = tryCalculateContextMappingInputParamsObjects(
                contextCalculationParamCollection);
        return paramObjects;

    }

    private List<InputParamObject> tryCalculateContextMappingInputParamsObjects(
            ContextCalculationParamCollection contextCalculationParamCollection) {
        List<InputParamObject> paramObjects = new ArrayList<>();

        String prevCtxNodeIdsStr = contextCalculationParamCollection.getCurrTaskNodeDefEntity().getPrevCtxNodeIds();
        if (StringUtils.isBlank(prevCtxNodeIdsStr)) {
            log.info("previous context node configuration is blank for node:{}-{}",
                    contextCalculationParamCollection.getCurrTaskNodeDefEntity().getId(),
                    contextCalculationParamCollection.getCurrTaskNodeDefEntity().getNodeName());
            if (contextCalculationParamCollection.hasMandatoryContextParam()) {
                String errMsg = String.format("Previous context node configuration is blank for node:%s-%s",
                        contextCalculationParamCollection.getCurrTaskNodeDefEntity().getId(),
                        contextCalculationParamCollection.getCurrTaskNodeDefEntity().getNodeName());
                log.error(errMsg);
                throw new WecubeCoreException(errMsg);
            } else {
                return paramObjects;
            }

        }

        String[] prevCtxNodeIds = prevCtxNodeIdsStr.trim().split(",");
        int prevCtxNodesSize = prevCtxNodeIds.length;

        if (prevCtxNodesSize == 1) {
            String prevCtxNodeId = prevCtxNodeIds[0];
            paramObjects = tryCalCtxMapInputParamsObjectsWithSinglePrevNode(contextCalculationParamCollection,
                    prevCtxNodeId);
        } else {
            paramObjects = tryCalCtxMapInputParamsObjectsWithMultiPrevNodes(contextCalculationParamCollection,
                    prevCtxNodeIds);
        }

        return paramObjects;
    }

    private List<InputParamObject> tryCalCtxMapInputParamsObjectsWithSinglePrevNode(
            ContextCalculationParamCollection contextCalculationParamCollection, String prevCtxNodeId) {
        ProcDefInfoEntity procDefInfo = contextCalculationParamCollection.getProcDefInfoEntity();
        ProcInstInfoEntity procInstInfo = contextCalculationParamCollection.getProcInstEntity();
        TaskNodeInstInfoEntity currTaskNodeInstInfo = contextCalculationParamCollection.getCurrTaskNodeInstEntity();
        TaskNodeDefInfoEntity currTaskNodeDefInfo = contextCalculationParamCollection.getCurrTaskNodeDefEntity();

        log.info(
                "Try to calculate context mapping parameter for [process:{},node:{},node instance:{}] with single previous context node:{}",
                procDefInfo.getId(), currTaskNodeDefInfo.getId(), currTaskNodeInstInfo.getId(), prevCtxNodeId);

        TaskNodeInstInfoEntity prevCtxTaskNodeInstInfo = taskNodeInstInfoRepository
                .selectOneByProcInstIdAndNodeId(procInstInfo.getId(), prevCtxNodeId);
        if (prevCtxTaskNodeInstInfo == null) {
            String errMsg = String.format("Previous context task node instance does not exist currently with ID:%s",
                    prevCtxNodeId);
            log.error(errMsg);
            throw new WecubeCoreException(errMsg);
        }

        // check parent context node here??
        List<InputParamObject> paramObjects = new ArrayList<>();
        List<ProcExecBindingEntity> prevCtxTaskNodeInstBindings = procExecBindingMapper
                .selectAllBoundTaskNodeBindings(procInstInfo.getId(), prevCtxTaskNodeInstInfo.getId());
        if (prevCtxTaskNodeInstBindings == null || prevCtxTaskNodeInstBindings.isEmpty()) {
            return paramObjects;
        }

        List<ContextCalculationParam> contextCalculationParams = contextCalculationParamCollection
                .getContextCalculationParams();
        for (ProcExecBindingEntity prevCtxTaskNodeBinding : prevCtxTaskNodeInstBindings) {
            InputParamObject paramObject = new InputParamObject();
            String newEntityDataId = String.format("%s-%s", CALLBACK_PARAMETER_SYSTEM_PREFIX,
                    prevCtxTaskNodeBinding.getEntityDataId()); // ?
            paramObject.setEntityDataId(newEntityDataId);// ?
            paramObject.setEntityTypeId(prevCtxTaskNodeBinding.getEntityTypeId());// ?
            paramObject.setFullEntityDataId(prevCtxTaskNodeBinding.getFullEntityDataId());// ?

            for (ContextCalculationParam contextCalculationParam : contextCalculationParams) {
                String attrName = contextCalculationParam.getParamName();
                String paramDataType = contextCalculationParam.getParamDataType();

                paramObject.addAttrNames(attrName);
                InputParamAttr paramAttr = null;
                if (isObjectDataType(paramDataType)) {
                    paramAttr = tryCalObjectInputParamAttrWithPrevBinding(prevCtxTaskNodeBinding,
                            contextCalculationParam);
                } else {
                    paramAttr = tryCalBasicInputParamAttrWithPrevBinding(prevCtxTaskNodeBinding,
                            contextCalculationParam);
                }

                if (paramAttr != null) {
                    paramObject.addAttrs(paramAttr);
                }

            }

            paramObjects.add(paramObject);
        }

        return paramObjects;
    }

    /**
     * 
     * @param prevCtxTaskNodeBinding
     * @param contextCalculationParam
     * @return
     */
    private InputParamAttr tryCalObjectInputParamAttrWithPrevBinding(ProcExecBindingEntity prevCtxTaskNodeBinding,
            ContextCalculationParam contextCalculationParam) {
        String attrName = contextCalculationParam.getParamName();
        String paramDataType = contextCalculationParam.getParamDataType();

        PluginConfigInterfaceParameters paramDef = contextCalculationParam.getParam();
        CoreObjectMeta refObjectMeta = paramDef.getObjectMeta();

        if (refObjectMeta == null) {
            String errMsg = String.format("Data type of parameter:%s is object but there is not object meta provided.",
                    contextCalculationParam.getParamName());
            log.error(errMsg);

            throw new WecubeCoreException(errMsg);
        }

        String multiple = paramDef.getMultiple();
        String required = paramDef.getRequired();
        InputParamAttr paramAttr = new InputParamAttr();
        paramAttr.setName(attrName);
        paramAttr.setDataType(paramDataType);
        paramAttr.setMultiple(multiple);
        paramAttr.setParamDef(paramDef);
        paramAttr.setSensitive(Constants.DATA_SENSITIVE.equalsIgnoreCase(paramDef.getSensitiveData()));

        boolean isMultiple = Constants.DATA_MULTIPLE.equalsIgnoreCase(multiple);
        boolean isMandatory = Constants.FIELD_REQUIRED.equalsIgnoreCase(required);

        List<CoreObjectVar> objectVars = pluginParamObjectVarCalculator
                .calculateCoreObjectVarsFromContext(prevCtxTaskNodeBinding, contextCalculationParam, isMultiple);

        if (objectVars == null || objectVars.isEmpty()) {
            String errMsg = String.format("Got empty object values for : %s", contextCalculationParam.getParamName());
            log.info(errMsg);
            if (isMandatory) {
                throw new WecubeCoreException(errMsg);
            }
            return paramAttr;
        }

        CoreObjectVarCalculationContext calCtx = new CoreObjectVarCalculationContext();

        if (isMultiple) {
            List<Object> objectVals = new ArrayList<>();
            for (CoreObjectVar objectVar : objectVars) {
                PluginParamObject paramObject = pluginParamObjectVarAssembleService.marshalPluginParamObject(objectVar,
                        calCtx);
                objectVals.add(paramObject);

                pluginParamObjectVarStorageService.storeCoreObjectVar(objectVar);
            }

            paramAttr.setValues(objectVals);
            return paramAttr;
        } else {

            if (objectVars.size() > 1) {
                String errMsg = String.format("Required data type %s but %s objects returned.", paramDataType,
                        objectVars.size());
                log.error(errMsg);

                throw new WecubeCoreException(errMsg);
            }

            List<Object> objectVals = new ArrayList<>();
            CoreObjectVar objectVar = objectVars.get(0);

            PluginParamObject paramObject = pluginParamObjectVarAssembleService.marshalPluginParamObject(objectVar,
                    calCtx);

            objectVals.add(paramObject);

            pluginParamObjectVarStorageService.storeCoreObjectVar(objectVar);

            paramAttr.setValues(objectVals);
            return paramAttr;
        }

    }

    private InputParamAttr tryCalBasicInputParamAttrWithPrevBinding(ProcExecBindingEntity prevCtxTaskNodeBinding,
            ContextCalculationParam contextCalculationParam) {
        String attrName = contextCalculationParam.getParamName();
        String paramDataType = contextCalculationParam.getParamDataType();
        PluginConfigInterfaceParameters paramDef = contextCalculationParam.getParam();
        String multiple = paramDef.getMultiple();
        String required = paramDef.getRequired();
        InputParamAttr paramAttr = new InputParamAttr();
        paramAttr.setName(attrName);
        paramAttr.setDataType(paramDataType);
        paramAttr.setMultiple(multiple);
        paramAttr.setParamDef(paramDef);
        paramAttr.setSensitive(Constants.DATA_SENSITIVE.equalsIgnoreCase(paramDef.getSensitiveData()));

        boolean isMultiple = Constants.DATA_MULTIPLE.equalsIgnoreCase(multiple);
        boolean isMandatory = Constants.FIELD_REQUIRED.equalsIgnoreCase(required);
        List<Object> objectValues = tryCalInputParamAttrValueWithPrevBinding(prevCtxTaskNodeBinding,
                contextCalculationParam);

        if (objectValues == null || objectValues.isEmpty()) {
            if (isMandatory) {
                String errMsg = String.format("The value is empty but field is mandatory for parameter:%s", attrName);
                log.error(errMsg);
                throw new WecubeCoreException(errMsg);
            } else {
                paramAttr.setValues(new ArrayList<Object>());
            }

            return paramAttr;
        }

        if (isMultiple) {
            paramAttr.setValues(objectValues);
            return paramAttr;
        }

        if ((objectValues.size() > 1) && (!isMultiple)) {
            String errMsg = String.format("Total:%s object values found but field:%s is not multiple.",
                    objectValues.size(), attrName);
            log.error(errMsg);
            throw new WecubeCoreException(errMsg);
        } else {
            paramAttr.setValues(objectValues);
        }

        return paramAttr;
    }

    private List<Object> tryCalInputParamAttrValueWithPrevBinding(ProcExecBindingEntity prevCtxTaskNodeBinding,
            ContextCalculationParam contextCalculationParam) {
        List<BoundTaskNodeExecParamWrapper> boundExecParamWrappers = contextCalculationParam
                .getBoundExecParamWrappers();
        if (boundExecParamWrappers == null || boundExecParamWrappers.isEmpty()) {
            return new ArrayList<>();
        }

        String fullEntityDataIdOfPrevBinding = prevCtxTaskNodeBinding.getFullEntityDataId();
        if (StringUtils.isBlank(fullEntityDataIdOfPrevBinding)) {
            String errMsg = String.format("Unknown full entity data id of binding:{}", prevCtxTaskNodeBinding.getId());
            log.error(errMsg);
            throw new WecubeCoreException(errMsg);
        }
        List<Object> objectValues = new ArrayList<>();
        for (BoundTaskNodeExecParamWrapper wrapper : boundExecParamWrappers) {
            TaskNodeExecParamEntity boundTaskNodeExecParamEntity = wrapper.getBoundTaskNodeExecParamEntity();
            if (boundTaskNodeExecParamEntity == null) {
                continue;
            }

            String targetFullEntityDataId = boundTaskNodeExecParamEntity.getFullEntityDataId();
            if (StringUtils.isBlank(targetFullEntityDataId)) {
                log.info("Unknown full entity data ID of param:{}", boundTaskNodeExecParamEntity.getId());
                continue;
            }

            if (targetFullEntityDataId.startsWith(fullEntityDataIdOfPrevBinding)) {
                Object paramDataValue = parseStringParamDataValueToObject(wrapper);
                if (paramDataValue != null) {
                    objectValues.add(paramDataValue);
                }
            }
        }

        return objectValues;
    }

    private Object parseStringParamDataValueToObject(BoundTaskNodeExecParamWrapper paramWrapper) {
        TaskNodeExecParamEntity paramInfo = paramWrapper.getBoundTaskNodeExecParamEntity();
        String paramDataType = paramInfo.getParamDataType();
        String paramDataValueStr = paramInfo.getParamDataValue();

        boolean isMultiple = Constants.DATA_MULTIPLE.equalsIgnoreCase(paramInfo.getMultiple());
        boolean isSensitive = unpackBoolean(paramInfo.getIsSensitive());

        if (StringUtils.isBlank(paramDataValueStr)) {
            return null;
        }

        if (isSensitive) {
            paramDataValueStr = tryDecodeParamDataValue(paramDataValueStr);
        }

        if (isBasicDataType(paramDataType)) {
            if (isMultiple) {
                List<Object> basicParamObjectValues = new ArrayList<>();
                String[] paramDataValueStrParts = paramDataValueStr.split(",");
                for (String paramDataValueStrPart : paramDataValueStrParts) {
                    if (Constants.DATA_TYPE_STRING.equalsIgnoreCase(paramDataType)) {
                        basicParamObjectValues.add(paramDataValueStrPart);
                    }

                    if (Constants.DATA_TYPE_NUMBER.equalsIgnoreCase(paramDataType)) {
                        Integer val = Integer.parseInt(paramDataValueStrPart);
                        basicParamObjectValues.add(val);
                    }
                }

                return basicParamObjectValues;
            } else {
                Object basicParamObjectValue = null;
                if (Constants.DATA_TYPE_STRING.equalsIgnoreCase(paramDataType)) {
                    basicParamObjectValue = paramDataValueStr;
                }

                if (Constants.DATA_TYPE_NUMBER.equalsIgnoreCase(paramDataType)) {
                    basicParamObjectValue = Integer.parseInt(paramDataValueStr);
                }

                return basicParamObjectValue;
            }
        } else if (isObjectDataType(paramDataType)) {
//            PluginConfigInterfaceParameters boundParamDef = paramWrapper.getBoundParam();
            try {
                return JsonUtils.toObject(paramDataValueStr, Object.class);
            } catch (IOException e) {
                log.error("", e);
                throw new WecubeCoreException(e.getMessage());
            }
            // todo
//            if (isMultiple) {
//
//            }
        }

        throw new WecubeCoreException("Unknown parameter data type of parameter : " + paramInfo.getParamName());
    }

    private boolean unpackBoolean(Boolean b) {
        if (b == null) {
            return false;
        }

        return b;
    }

    private boolean isObjectDataType(String dataType) {
        if (StringUtils.isBlank(dataType)) {
            throw new IllegalArgumentException();
        }

        return Constants.DATA_TYPE_OBJECT.equalsIgnoreCase(dataType);
    }

    private boolean isBasicDataType(String dataType) {
        if (StringUtils.isBlank(dataType)) {
            throw new IllegalArgumentException();
        }

        if (Constants.DATA_TYPE_STRING.equalsIgnoreCase(dataType)
                || Constants.DATA_TYPE_NUMBER.equalsIgnoreCase(dataType)) {
            return true;
        }

        return false;
    }

    private List<InputParamObject> tryCalCtxMapInputParamsObjectsWithMultiPrevNodes(
            ContextCalculationParamCollection contextCalculationParamCollection, String[] prevCtxNodeIds) {

        ProcInstInfoEntity procInstInfo = contextCalculationParamCollection.getProcInstEntity();
        List<ProcExecBindingKeyLink> procExecBindingKeyLinks = new ArrayList<>();
        for (String prevCtxNodeId : prevCtxNodeIds) {
            TaskNodeInstInfoEntity prevCtxTaskNodeInstInfo = taskNodeInstInfoRepository
                    .selectOneByProcInstIdAndNodeId(procInstInfo.getId(), prevCtxNodeId);
            List<ProcExecBindingEntity> procExecBindings = procExecBindingMapper
                    .selectAllBoundTaskNodeBindings(procInstInfo.getId(), prevCtxTaskNodeInstInfo.getId());

            procExecBindingKeyLinks = propagateProcExecBindingKeyLinks(procExecBindingKeyLinks, procExecBindings,
                    prevCtxNodeId);
        }

        ProcDefInfoEntity procDefInfo = contextCalculationParamCollection.getProcDefInfoEntity();
        List<TaskNodeDefInfoEntity> prevCtxTaskNodeDefInfos = new ArrayList<>();
        for (String prevCtxNodeId : prevCtxNodeIds) {
            TaskNodeDefInfoEntity prevCtxTaskNodeDefInfo = taskNodeDefInfoRepository
                    .selectOneWithProcessIdAndNodeIdAndStatus(procDefInfo.getId(), prevCtxNodeId,
                            TaskNodeDefInfoEntity.DEPLOYED_STATUS);
            if (prevCtxTaskNodeDefInfo != null) {
                prevCtxTaskNodeDefInfos.add(prevCtxTaskNodeDefInfo);
            }
        }

        List<InputParamObject> paramObjects = new ArrayList<>();

        List<ContextCalculationParam> contextCalculationParams = contextCalculationParamCollection
                .getContextCalculationParams();
        for (ProcExecBindingKeyLink procExecBindingKeyLink : procExecBindingKeyLinks) {
            InputParamObject paramObject = new InputParamObject();
            paramObject.setEntityTypeId("TaskNode");
            String entityDataId = String.format("%s-%s", CALLBACK_PARAMETER_SYSTEM_PREFIX,
                    LocalIdGenerator.generateId());
            paramObject.setEntityDataId(entityDataId);// ?
            paramObject.setFullEntityDataId(entityDataId);// ?

            for (ContextCalculationParam contextCalculationParam : contextCalculationParams) {
                String attrName = contextCalculationParam.getParamName();
                String paramDataType = contextCalculationParam.getParamDataType();

                paramObject.addAttrNames(attrName);

                InputParamAttr paramAttr = null;

                if (isObjectDataType(paramDataType)) {
                    paramAttr = tryCalObjectInputParamAttrWithProcExecBindingKeyLink(procExecBindingKeyLink,
                            contextCalculationParam);
                } else {
                    paramAttr = tryCalBasicInputParamAttrWithProcExecBindingKeyLink(procExecBindingKeyLink,
                            contextCalculationParam);
                }

                if (paramAttr != null) {
                    paramObject.addAttrs(paramAttr);
                }

            }

            paramObjects.add(paramObject);
        }

        return paramObjects;
    }

    private InputParamAttr tryCalObjectInputParamAttrWithProcExecBindingKeyLink(
            ProcExecBindingKeyLink procExecBindingKeyLink, ContextCalculationParam contextCalculationParam) {
        String attrName = contextCalculationParam.getParamName();
        String paramDataType = contextCalculationParam.getParamDataType();

        PluginConfigInterfaceParameters paramDef = contextCalculationParam.getParam();
        CoreObjectMeta refObjectMeta = paramDef.getObjectMeta();

        if (refObjectMeta == null) {
            String errMsg = String.format("Data type of parameter:%s is object but there is not object meta provided.",
                    contextCalculationParam.getParamName());
            log.error(errMsg);

            throw new WecubeCoreException(errMsg);
        }

        String multiple = paramDef.getMultiple();
        String required = paramDef.getRequired();
        InputParamAttr paramAttr = new InputParamAttr();
        paramAttr.setName(attrName);
        paramAttr.setDataType(paramDataType);
        paramAttr.setMultiple(multiple);
        paramAttr.setParamDef(paramDef);
        paramAttr.setSensitive(Constants.DATA_SENSITIVE.equalsIgnoreCase(paramDef.getSensitiveData()));

        boolean isMultiple = Constants.DATA_MULTIPLE.equalsIgnoreCase(multiple);
        boolean isMandatory = Constants.FIELD_REQUIRED.equalsIgnoreCase(required);

        List<CoreObjectVar> objectVars = pluginParamObjectVarCalculator
                .calculateCoreObjectVarsFromContext(procExecBindingKeyLink, contextCalculationParam, isMultiple);

        if (objectVars == null || objectVars.isEmpty()) {
            String errMsg = String.format("Got empty object values for : %s", contextCalculationParam.getParamName());
            log.info(errMsg);
            if (isMandatory) {
                throw new WecubeCoreException(errMsg);
            }
            return paramAttr;
        }

        CoreObjectVarCalculationContext calCtx = new CoreObjectVarCalculationContext();

        if (isMultiple) {
            List<Object> objectVals = new ArrayList<>();
            for (CoreObjectVar objectVar : objectVars) {
                PluginParamObject paramObject = pluginParamObjectVarAssembleService.marshalPluginParamObject(objectVar,
                        calCtx);
                objectVals.add(paramObject);

                pluginParamObjectVarStorageService.storeCoreObjectVar(objectVar);
            }

            paramAttr.setValues(objectVals);
            return paramAttr;
        } else {

            if (objectVars.size() > 1) {
                String errMsg = String.format("Required data type %s but %s objects returned.", paramDataType,
                        objectVars.size());
                log.error(errMsg);

                throw new WecubeCoreException(errMsg);
            }

            List<Object> objectVals = new ArrayList<>();
            CoreObjectVar objectVar = objectVars.get(0);

            PluginParamObject paramObject = pluginParamObjectVarAssembleService.marshalPluginParamObject(objectVar,
                    calCtx);

            objectVals.add(paramObject);

            pluginParamObjectVarStorageService.storeCoreObjectVar(objectVar);

            paramAttr.setValues(objectVals);
            return paramAttr;
        }

    }

    private InputParamAttr tryCalBasicInputParamAttrWithProcExecBindingKeyLink(
            ProcExecBindingKeyLink procExecBindingKeyLink, ContextCalculationParam contextCalculationParam) {
        String attrName = contextCalculationParam.getParamName();
        String paramDataType = contextCalculationParam.getParamDataType();
        PluginConfigInterfaceParameters paramDef = contextCalculationParam.getParam();
        String multiple = paramDef.getMultiple();
        String required = paramDef.getRequired();
        InputParamAttr paramAttr = new InputParamAttr();
        paramAttr.setName(attrName);
        paramAttr.setDataType(paramDataType);
        paramAttr.setMultiple(multiple);
        paramAttr.setParamDef(paramDef);
        paramAttr.setSensitive(Constants.DATA_SENSITIVE.equalsIgnoreCase(paramDef.getSensitiveData()));

        boolean isMultiple = Constants.DATA_MULTIPLE.equalsIgnoreCase(multiple);
        boolean isMandatory = Constants.FIELD_REQUIRED.equalsIgnoreCase(required);
        List<Object> objectValues = tryCalInputParamAttrValueWithProcExecBindingKeyLink(procExecBindingKeyLink,
                contextCalculationParam);

        if (objectValues == null || objectValues.isEmpty()) {
            if (isMandatory) {
                String errMsg = String.format("The value is empty but field is mandatory for parameter:%s", attrName);
                log.error(errMsg);
                throw new WecubeCoreException(errMsg);
            } else {
                paramAttr.setValues(new ArrayList<Object>());
            }

            return paramAttr;
        }

        if (isMultiple) {
            paramAttr.setValues(objectValues);
            return paramAttr;
        }

        if ((objectValues.size() > 1) && (!isMultiple)) {
            String errMsg = String.format("Total:%s object values found but field:%s is not multiple.",
                    objectValues.size(), attrName);
            log.error(errMsg);
            throw new WecubeCoreException(errMsg);
        } else {
            paramAttr.setValues(objectValues);
        }

        return paramAttr;
    }

    private List<Object> tryCalInputParamAttrValueWithProcExecBindingKeyLink(
            ProcExecBindingKeyLink procExecBindingKeyLink, ContextCalculationParam contextCalculationParam) {
        List<BoundTaskNodeExecParamWrapper> boundExecParamWrappers = contextCalculationParam
                .getBoundExecParamWrappers();
        if (boundExecParamWrappers == null || boundExecParamWrappers.isEmpty()) {
            return new ArrayList<>();
        }

        List<Object> objectValues = new ArrayList<>();
        for (BoundTaskNodeExecParamWrapper wrapper : boundExecParamWrappers) {
            TaskNodeExecParamEntity boundTaskNodeExecParamEntity = wrapper.getBoundTaskNodeExecParamEntity();
            if (boundTaskNodeExecParamEntity == null) {
                continue;
            }

            String targetFullEntityDataId = boundTaskNodeExecParamEntity.getFullEntityDataId();
            if (StringUtils.isBlank(targetFullEntityDataId)) {
                log.info("Unknown full entity data ID of param:{}", boundTaskNodeExecParamEntity.getId());
                continue;
            }

            if (matchBoundTaskNodeExecParamWrapper(wrapper, procExecBindingKeyLink)) {
                Object paramDataValue = parseStringParamDataValueToObject(wrapper);
                if (paramDataValue != null) {
                    objectValues.add(paramDataValue);
                }
            }
        }

        return objectValues;
    }

    private boolean matchBoundTaskNodeExecParamWrapper(BoundTaskNodeExecParamWrapper wrapper,
            ProcExecBindingKeyLink procExecBindingKeyLink) {
        List<ProcExecBindingKey> procExecBindingKeys = procExecBindingKeyLink.getProcExecBindingKeys();
        if (procExecBindingKeys == null || procExecBindingKeys.isEmpty()) {
            return false;
        }

        TaskNodeExecParamEntity boundTaskNodeExecParamEntity = wrapper.getBoundTaskNodeExecParamEntity();
        String targetFullDataId = boundTaskNodeExecParamEntity.getFullEntityDataId();
        for (ProcExecBindingKey procExecBindingKey : procExecBindingKeys) {
            ProcExecBindingEntity procExecBinding = procExecBindingKey.getProcExecBinding();
            if (procExecBinding == null) {
                continue;
            }
            String bindingFullDataId = procExecBinding.getFullEntityDataId();
            if (StringUtils.isBlank(bindingFullDataId)) {
                continue;
            }

            if (targetFullDataId.startsWith(bindingFullDataId)) {
                return true;
            }
        }
        return false;
    }

    private List<ProcExecBindingKeyLink> propagateProcExecBindingKeyLinks(
            List<ProcExecBindingKeyLink> originProcExecBindingKeyLinks,
            List<ProcExecBindingEntity> tailProcExecBindingKeys, String ctxNodeId) {
        if (tailProcExecBindingKeys == null || tailProcExecBindingKeys.isEmpty()) {

            for (ProcExecBindingKeyLink originProcExecBindingKeyLink : originProcExecBindingKeyLinks) {
                ProcExecBindingKey bindingKey = new ProcExecBindingKey();
                bindingKey.setTaskNodeId(ctxNodeId);

                originProcExecBindingKeyLink.addProcExecBindingKey(bindingKey);
            }

            return originProcExecBindingKeyLinks;
        }

        List<ProcExecBindingKeyLink> retProcExecBindingKeyLinks = new ArrayList<>();
        for (ProcExecBindingEntity tailProcExecBinding : tailProcExecBindingKeys) {
            if (originProcExecBindingKeyLinks.isEmpty()) {
                ProcExecBindingKeyLink newLink = new ProcExecBindingKeyLink();
                ProcExecBindingKey bindingKey = new ProcExecBindingKey();
                bindingKey.setTaskNodeId(ctxNodeId);
                bindingKey.setProcExecBinding(tailProcExecBinding);

                newLink.addProcExecBindingKey(bindingKey);

                retProcExecBindingKeyLinks.add(newLink);
            } else {
                for (ProcExecBindingKeyLink originLink : originProcExecBindingKeyLinks) {
                    ProcExecBindingKeyLink newLink = new ProcExecBindingKeyLink();
                    newLink.getProcExecBindingKeys().addAll(originLink.getProcExecBindingKeys());

                    ProcExecBindingKey bindingKey = new ProcExecBindingKey();
                    bindingKey.setTaskNodeId(ctxNodeId);
                    bindingKey.setProcExecBinding(tailProcExecBinding);

                    newLink.addProcExecBindingKey(bindingKey);

                    retProcExecBindingKeyLinks.add(newLink);
                }
            }
        }

        return retProcExecBindingKeyLinks;
    }

    private List<InputParamObject> tryCalculateInputParamObjectsWithoutBindings(ProcDefInfoEntity procDefEntity,
            ProcInstInfoEntity procInstEntity, TaskNodeInstInfoEntity taskNodeInstEntity,
            TaskNodeDefInfoEntity taskNodeDefEntity, PluginConfigInterfaces pluginConfigInterface) {

        log.info(
                "Did not get input parameter objects and try to calculate input parameter objects from system for taskNodeInstId={}",
                taskNodeInstEntity.getId());

        List<PluginConfigInterfaceParameters> configInterfaceInputParams = pluginConfigInterface.getInputParameters();

        if (!determineContextCalculationPossibility(configInterfaceInputParams)) {
            log.info("Such node {}:{} does not have binding and context calculation is impossible.",
                    taskNodeDefEntity.getNodeId(), taskNodeDefEntity.getNodeName());
            return new ArrayList<>();
        }

        Map<String, PluginConfigInterfaceParameters> contextConfigInterfaceInputParams = new HashMap<>();
        for (PluginConfigInterfaceParameters c : configInterfaceInputParams) {
            if (MAPPING_TYPE_CONTEXT.equalsIgnoreCase(c.getMappingType())) {
                contextConfigInterfaceInputParams.put(c.getName(), c);
            }
        }

        List<InputParamObject> inputParamObjs = null;

        if (!contextConfigInterfaceInputParams.isEmpty()) {
            // #2233
            inputParamObjs = tryCalculateContextMappingInputParamsObjects(procDefEntity, procInstEntity,
                    taskNodeInstEntity, taskNodeDefEntity, pluginConfigInterface, contextConfigInterfaceInputParams);
        }

        if (inputParamObjs == null || inputParamObjs.isEmpty()) {
            inputParamObjs = new ArrayList<InputParamObject>();
            InputParamObject inputObj = new InputParamObject();

            inputObj.setEntityTypeId("TaskNode");
            inputObj.setEntityDataId(
                    String.format("%s-%s", CALLBACK_PARAMETER_SYSTEM_PREFIX, LocalIdGenerator.generateId()));

            inputParamObjs.add(inputObj);
        }

        for (InputParamObject inputObj : inputParamObjs) {
            for (PluginConfigInterfaceParameters param : configInterfaceInputParams) {
                String paramName = param.getName();
                String paramType = param.getDataType();
                String mappingType = param.getMappingType();

                if (!(MAPPING_TYPE_SYSTEM_VARIABLE.equalsIgnoreCase(mappingType)
                        || MAPPING_TYPE_CONSTANT.equalsIgnoreCase(mappingType))) {
                    continue;
                }

                inputObj.addAttrNames(paramName);

                InputParamAttr inputAttr = new InputParamAttr();
                inputAttr.setName(paramName);
                inputAttr.setDataType(paramType);
                inputAttr.setSensitive(IS_SENSITIVE_ATTR.equalsIgnoreCase(param.getSensitiveData()));
                inputAttr.setParamDef(param);

                boolean isFieldRequired = isFieldRequired(param.getRequired());

                List<Object> objectVals = new ArrayList<Object>();
                //
                inputAttr.setMapType(mappingType);
                inputAttr.setMultiple(param.getMultiple());

                if (MAPPING_TYPE_SYSTEM_VARIABLE.equalsIgnoreCase(mappingType)) {
                    handleSystemMapping(mappingType, param, paramName, objectVals);
                }

                if (MAPPING_TYPE_CONSTANT.equalsIgnoreCase(mappingType)) {
                    handleConstantMapping(mappingType, taskNodeDefEntity, paramName, objectVals, isFieldRequired,
                            param);
                }

                inputAttr.addValues(objectVals);

                inputObj.addAttrs(inputAttr);
            }
        }

        return inputParamObjs;
    }

    private boolean determineContextCalculationPossibility(
            List<PluginConfigInterfaceParameters> configInterfaceInputParams) {
        if (configInterfaceInputParams == null || configInterfaceInputParams.isEmpty()) {
            return false;
        }

        // #2169
        // #2233
        for (PluginConfigInterfaceParameters c : configInterfaceInputParams) {
            if (MAPPING_TYPE_ENTITY.equalsIgnoreCase(c.getMappingType())) {
                return false;
            }

            CoreObjectMeta refObjectMeta = c.getObjectMeta();
            if (refObjectMeta != null) {
                log.info("Object type parameter:{}-{}", c.getName(), refObjectMeta.getName());
                if (!determineContextCalculationPossibility(refObjectMeta)) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean determineContextCalculationPossibility(CoreObjectMeta targetObjectMeta) {
        log.info("determine context calculation possibility for object meta:{}", targetObjectMeta.getName());
        List<CoreObjectPropertyMeta> propertyMetas = targetObjectMeta.getPropertyMetas();
        if (propertyMetas == null || propertyMetas.isEmpty()) {
            return true;
        }

        for (CoreObjectPropertyMeta propertyMeta : propertyMetas) {
            if (MAPPING_TYPE_ENTITY.equalsIgnoreCase(propertyMeta.getMapType())) {
                return false;
            }

            CoreObjectMeta refObjectMeta = propertyMeta.getRefObjectMeta();
            if (refObjectMeta != null) {
                log.info("Object type property:{}-{}", propertyMeta.getName(), refObjectMeta.getName());
                if (!determineContextCalculationPossibility(refObjectMeta)) {
                    return false;
                }
            }
        }

        return true;
    }

    private void buildTaskNodeExecRequestEntity(PluginInterfaceInvocationContext ctx) {

        List<TaskNodeExecRequestEntity> formerRequestEntities = taskNodeExecRequestRepository
                .selectCurrentEntityByNodeInstId(ctx.getTaskNodeInstEntity().getId());

        if (formerRequestEntities != null) {
            for (TaskNodeExecRequestEntity formerRequestEntity : formerRequestEntities) {
                formerRequestEntity.setIsCurrent(false);
                formerRequestEntity.setUpdatedTime(new Date());
                taskNodeExecRequestRepository.updateByPrimaryKeySelective(formerRequestEntity);
            }
        }

        String requestId = UUID.randomUUID().toString();

        TaskNodeInstInfoEntity taskNodeInstEntity = ctx.getTaskNodeInstEntity();

        PluginInvocationCommand cmd = ctx.getPluginInvocationCommand();
        TaskNodeExecRequestEntity requestEntity = new TaskNodeExecRequestEntity();
        requestEntity.setNodeInstId(taskNodeInstEntity.getId());
        requestEntity.setReqId(requestId);
        requestEntity.setReqUrl(ctx.getInstanceHost() + ctx.getInterfacePath());

        requestEntity.setExecutionId(cmd.getExecutionId());
        requestEntity.setNodeId(cmd.getNodeId());
        requestEntity.setNodeName(cmd.getNodeName());
        requestEntity.setProcDefKernelId(cmd.getProcDefId());
        requestEntity.setProcDefKernelKey(cmd.getProcDefKey());
        requestEntity.setProcDefVer(cmd.getProcDefVersion());
        requestEntity.setProcInstKernelId(cmd.getProcInstId());
        requestEntity.setProcInstKernelKey(cmd.getProcInstKey());
        requestEntity.setCreatedBy(WorkflowConstants.DEFAULT_USER);
        requestEntity.setCreatedTime(new Date());
        requestEntity.setIsCurrent(true);
        requestEntity.setIsCompleted(false);

        List<ProcExecBindingEntity> nodeObjectBindings = ctx.getNodeObjectBindings();
        if (nodeObjectBindings == null || nodeObjectBindings.isEmpty()) {
            requestEntity.setContextDataFlag("Y");
        } else {
            requestEntity.setContextDataFlag("N");
        }
        requestEntity.setReqObjectAmount(ctx.getReqObjectAmount());

        taskNodeExecRequestRepository.insert(requestEntity);

        ctx.withTaskNodeExecRequestEntity(requestEntity);
        ctx.setRequestId(requestId);

    }

    private void parsePluginInstance(PluginInterfaceInvocationContext ctx) {
        PluginConfigInterfaces pluginConfigInterface = ctx.getPluginConfigInterface();
        PluginInstances pluginInstance = retrieveAvailablePluginInstance(pluginConfigInterface);
        String interfacePath = pluginConfigInterface.getPath();
        if (pluginInstance == null) {
            log.warn("cannot find an available plugin instance for {}", pluginConfigInterface.getServiceName());
            throw new WecubeCoreException("3169", "Cannot find an available plugin instance.");
        }

        String instanceHostAndPort = applicationProperties.getGatewayUrl();
        ctx.setInstanceHost(instanceHostAndPort);
        ctx.setInterfacePath(interfacePath);
    }

    private List<InputParamObject> tryCalculateInputParamObjectsWithBindings(ProcDefInfoEntity procDefInfoEntity,
            ProcInstInfoEntity procInstEntity, TaskNodeInstInfoEntity taskNodeInstEntity,
            TaskNodeDefInfoEntity taskNodeDefEntity, List<ProcExecBindingEntity> nodeObjectBindings,
            PluginConfigInterfaces pluginConfigInterface, Map<Object, Object> externalCacheMap) {

        List<InputParamObject> inputParamObjs = new ArrayList<InputParamObject>();

        List<PluginConfigInterfaceParameters> configInterfaceInputParams = pluginConfigInterface.getInputParameters();
        for (ProcExecBindingEntity nodeObjectBinding : nodeObjectBindings) {
            String entityDataId = nodeObjectBinding.getEntityDataId();

            InputParamObject inputObj = new InputParamObject();
            inputObj.setEntityTypeId(nodeObjectBinding.getEntityTypeId());
            inputObj.setEntityDataId(nodeObjectBinding.getEntityDataId());
            inputObj.setFullEntityDataId(nodeObjectBinding.getFullEntityDataId());

            if (StringUtils.isNoneBlank(nodeObjectBinding.getConfirmToken())) {
                inputObj.setConfirmToken(nodeObjectBinding.getConfirmToken());
            }

            for (PluginConfigInterfaceParameters param : configInterfaceInputParams) {
                String paramName = param.getName();
                String paramType = param.getDataType();

                inputObj.addAttrNames(paramName);

                InputParamAttr inputAttr = new InputParamAttr();
                inputAttr.setName(paramName);
                inputAttr.setDataType(paramType);
                inputAttr.setSensitive(IS_SENSITIVE_ATTR.equalsIgnoreCase(param.getSensitiveData()));
                inputAttr.setParamDef(param);

                boolean isFieldRequired = isFieldRequired(param.getRequired());

                List<Object> objectVals = new ArrayList<Object>();
                //
                String mappingType = param.getMappingType();
                inputAttr.setMapType(mappingType);
                inputAttr.setMultiple(param.getMultiple());

                String dataType = param.getDataType();

                if (MAPPING_TYPE_OBJECT.equalsIgnoreCase(dataType)) {
                    handleObjectMapping(mappingType, param, entityDataId, objectVals, externalCacheMap,
                            procDefInfoEntity, procInstEntity, nodeObjectBinding.getFullEntityDataId(),
                            nodeObjectBinding.getEntityTypeId(), taskNodeDefEntity, taskNodeInstEntity);
                } else {
                    if (MAPPING_TYPE_ENTITY.equalsIgnoreCase(mappingType)) {
                        handleEntityMapping(mappingType, param, entityDataId, objectVals, externalCacheMap);
                    }

                    if (MAPPING_TYPE_CONTEXT.equalsIgnoreCase(mappingType)) {
                        handleContextMapping(mappingType, taskNodeDefEntity, paramName, procInstEntity, param,
                                paramType, nodeObjectBinding, objectVals);
                    }

                    if (MAPPING_TYPE_SYSTEM_VARIABLE.equalsIgnoreCase(mappingType)) {
                        handleSystemMapping(mappingType, param, paramName, objectVals);
                    }

                    if (MAPPING_TYPE_CONSTANT.equalsIgnoreCase(mappingType)) {
                        handleConstantMapping(mappingType, taskNodeDefEntity, paramName, objectVals, isFieldRequired,
                                param);
                    }
                }

                // #2226

                inputAttr.addValues(objectVals);

                inputObj.addAttrs(inputAttr);
            }

            inputParamObjs.add(inputObj);

        }

        return inputParamObjs;
    }

    private void handleObjectMapping(String mappingType, PluginConfigInterfaceParameters param, String entityDataId,
            List<Object> objectVals, Map<Object, Object> cacheMap, ProcDefInfoEntity procDefInfo,
            ProcInstInfoEntity procInstInfo, String rootEntityFullDataId, String rootEntityTypeId,
            TaskNodeDefInfoEntity taskNodeDefInfo, TaskNodeInstInfoEntity taskNodeInstInfo) {
        // #2226
        if (!MAPPING_TYPE_OBJECT.equals(param.getDataType())) {
            return;
        }

        CoreObjectVarCalculationContext calCtx = new CoreObjectVarCalculationContext();
        calCtx.setExternalCacheMap(cacheMap);
        calCtx.setProcDefInfo(procDefInfo);
        calCtx.setProcInstInfo(procInstInfo);
        calCtx.setRootEntityDataId(entityDataId);
        calCtx.setRootEntityFullDataId(rootEntityFullDataId);
        calCtx.setRootEntityTypeId(rootEntityTypeId);
        calCtx.setTaskNodeDefInfo(taskNodeDefInfo);
        calCtx.setTaskNodeInstInfo(taskNodeInstInfo);

        CoreObjectMeta objectMeta = param.getObjectMeta();

        if (objectMeta == null) {
            if (StringUtils.isBlank(param.getMappingEntityExpression())
                    || param.getMappingEntityExpression().endsWith(".NONE")) {
                return;
            }

            String entityAttrName = null;
            List<EntityQueryExprNodeInfo> currExprNodeInfos = this.entityQueryExpressionParser
                    .parse(param.getMappingEntityExpression());
            if (currExprNodeInfos == null || currExprNodeInfos.isEmpty()) {
                // nothing
            } else {
                EntityQueryExprNodeInfo leafNode = currExprNodeInfos.get(currExprNodeInfos.size() - 1);
                entityAttrName = leafNode.getQueryAttrName();
            }
            List<Map<String, Object>> rawObjectMapVals = pluginParamObjectVarCalculator
                    .calculateRawObjectVarList(objectMeta, calCtx, param.getMappingEntityExpression());
            if (Constants.DATA_MULTIPLE.equalsIgnoreCase(param.getMultiple())) {
                for (Map<String, Object> recordMap : rawObjectMapVals) {
                    if (StringUtils.isBlank(entityAttrName)) {
                        objectVals.add(recordMap);
                    } else {
                        Object objVal = recordMap.get(entityAttrName);
                        if (objVal != null) {
                            objectVals.add(objVal);
                        }
                    }
                }
            } else {
                if (!rawObjectMapVals.isEmpty()) {
                    Map<String, Object> recordMap = rawObjectMapVals.get(0);
                    if (StringUtils.isBlank(entityAttrName)) {
                        objectVals.add(recordMap);
                    } else {
                        Object objVal = recordMap.get(entityAttrName);
                        if (objVal != null) {
                            objectVals.add(objVal);
                        }
                    }
                }
            }

            return;
        }

        // store objects here
        List<CoreObjectVar> objectVars = pluginParamObjectVarCalculator.calculateCoreObjectVarList(objectMeta, calCtx,
                param.getMappingEntityExpression());

        if (objectVars == null || objectVars.isEmpty()) {
            log.info("Got empty object values for : {}", objectMeta.getName());
            return;
        }

        if (Constants.DATA_MULTIPLE.equalsIgnoreCase(param.getMultiple())) {
            for (CoreObjectVar objectVar : objectVars) {
                PluginParamObject paramObject = pluginParamObjectVarAssembleService.marshalPluginParamObject(objectVar,
                        calCtx);
                objectVals.add(paramObject);

                pluginParamObjectVarStorageService.storeCoreObjectVar(objectVar);
            }

            return;
        } else {

            if (objectVars.size() > 1) {
                String errMsg = String.format("Required data type %s but %s objects returned.", param.getDataType(),
                        objectVars.size());
                log.error(errMsg);

                throw new WecubeCoreException(errMsg);
            }

            CoreObjectVar objectVar = objectVars.get(0);

            PluginParamObject paramObject = pluginParamObjectVarAssembleService.marshalPluginParamObject(objectVar,
                    calCtx);

            objectVals.add(paramObject);

            pluginParamObjectVarStorageService.storeCoreObjectVar(objectVar);

            return;
        }
    }

    private void handleEntityMapping(String mappingType, PluginConfigInterfaceParameters param, String entityDataId,
            List<Object> objectVals, Map<Object, Object> cacheMap) {
        if (!MAPPING_TYPE_ENTITY.equals(mappingType)) {
            return;
        }
        String mappingEntityExpression = param.getMappingEntityExpression();

        if (log.isDebugEnabled()) {
            log.debug("expression:{}", mappingEntityExpression);
        }

        EntityOperationRootCondition condition = new EntityOperationRootCondition(mappingEntityExpression,
                entityDataId);

        List<Object> attrValsPerExpr = entityOperationService.queryAttributeValues(condition, cacheMap);

        if (attrValsPerExpr == null) {
            log.info("returned null while fetch data with expression:{}", mappingEntityExpression);
            attrValsPerExpr = new ArrayList<>();
        }

        if (log.isDebugEnabled()) {
            log.debug("retrieved objects with expression,size={},values={}", attrValsPerExpr.size(), attrValsPerExpr);
        }

        objectVals.addAll(attrValsPerExpr);

    }

    private void handleContextMappingForUserTask(String mappingType, TaskNodeDefInfoEntity taskNodeDefEntity,
            String paramName, ProcInstInfoEntity procInstEntity, PluginConfigInterfaceParameters param,
            String paramType, List<Object> objectVals) {
        if (!MAPPING_TYPE_CONTEXT.equals(mappingType)) {
            return;
        }
        // #1993
        String curTaskNodeDefId = taskNodeDefEntity.getId();
        TaskNodeParamEntity nodeParamEntity = taskNodeParamRepository
                .selectOneByTaskNodeDefIdAndParamName(curTaskNodeDefId, paramName);

        if (nodeParamEntity == null) {
            log.error("mapping type is {} but node parameter entity is null for {}", mappingType, curTaskNodeDefId);

            if (Constants.FIELD_REQUIRED.equalsIgnoreCase(param.getRequired())) {

                log.error("Task node parameter entity does not exist for {} {}", curTaskNodeDefId, paramName);
                throw new WecubeCoreException("3170", "Task node parameter entity does not exist.");
            } else {
                log.info("Task node parameter entity does not exist for {} {} but field not required.",
                        curTaskNodeDefId, paramName);
                return;
            }
        }

        String bindNodeId = nodeParamEntity.getBindNodeId();
        String bindParamType = nodeParamEntity.getBindParamType();
        String bindParamName = nodeParamEntity.getBindParamName();

        // get by procInstId and nodeId
        TaskNodeInstInfoEntity bindNodeInstEntity = taskNodeInstInfoRepository
                .selectOneByProcInstIdAndNodeId(procInstEntity.getId(), bindNodeId);

        if (bindNodeInstEntity == null) {
            log.error("Bound node instance entity does not exist for {} {}", procInstEntity.getId(), bindNodeId);
            throw new WecubeCoreException("3171", "Bound node instance entity does not exist.");
        }

        if (TaskNodeDefInfoEntity.NODE_TYPE_START_EVENT.equalsIgnoreCase(bindNodeInstEntity.getNodeType())) {
            handleContextMappingForStartEvent(mappingType, taskNodeDefEntity, paramName, procInstEntity, param,
                    paramType, objectVals, bindNodeInstEntity, bindParamName, bindParamType);
            return;
        } else {
            throw new WecubeCoreException("Unsupported context type currently.");
        }

    }

    private void handleContextMapping(String mappingType, TaskNodeDefInfoEntity taskNodeDefEntity, String paramName,
            ProcInstInfoEntity procInstEntity, PluginConfigInterfaceParameters param, String paramType,
            ProcExecBindingEntity currNodeObjectBinding, List<Object> objectVals) {
        if (!MAPPING_TYPE_CONTEXT.equals(mappingType)) {
            return;
        }
        // #1993
        String curTaskNodeDefId = taskNodeDefEntity.getId();
        TaskNodeParamEntity nodeParamEntity = taskNodeParamRepository
                .selectOneByTaskNodeDefIdAndParamName(curTaskNodeDefId, paramName);

        if (nodeParamEntity == null) {
            log.error("mapping type is {} but node parameter entity is null for {}", mappingType, curTaskNodeDefId);

            if (Constants.FIELD_REQUIRED.equalsIgnoreCase(param.getRequired())) {

                log.error("Task node parameter entity does not exist for {} {}", curTaskNodeDefId, paramName);
                throw new WecubeCoreException("3170", "Task node parameter entity does not exist.");
            } else {
                log.info("Task node parameter entity does not exist for {} {} but field not required.",
                        curTaskNodeDefId, paramName);
                return;
            }
        }

        String bindNodeId = nodeParamEntity.getBindNodeId();
        String bindParamType = nodeParamEntity.getBindParamType();
        String bindParamName = nodeParamEntity.getBindParamName();

        // get by procInstId and nodeId
        TaskNodeInstInfoEntity bindNodeInstEntity = taskNodeInstInfoRepository
                .selectOneByProcInstIdAndNodeId(procInstEntity.getId(), bindNodeId);

        if (bindNodeInstEntity == null) {
            log.error("Bound node instance entity does not exist for {} {}", procInstEntity.getId(), bindNodeId);
            throw new WecubeCoreException("3171", "Bound node instance entity does not exist.");
        }

        if (TaskNodeDefInfoEntity.NODE_TYPE_START_EVENT.equalsIgnoreCase(bindNodeInstEntity.getNodeType())) {
            handleContextMappingForStartEvent(mappingType, taskNodeDefEntity, paramName, procInstEntity, param,
                    paramType, objectVals, bindNodeInstEntity, bindParamName, bindParamType);
            return;
        } else {
            handleContextMappingForTaskNode(mappingType, taskNodeDefEntity, paramName, procInstEntity, param, paramType,
                    objectVals, bindNodeInstEntity, bindParamName, bindParamType, currNodeObjectBinding);

            return;
        }

    }

    private void handleContextMappingForStartEvent(String mappingType, TaskNodeDefInfoEntity taskNodeDefEntity,
            String paramName, ProcInstInfoEntity procInstEntity, PluginConfigInterfaceParameters param,
            String paramType, List<Object> objectVals, TaskNodeInstInfoEntity bindNodeInstEntity, String bindParamName,
            String bindParamType) {
        // #1993
        // 1
        if (LocalWorkflowConstants.CONTEXT_NAME_PROC_DEF_KEY.equals(bindParamName)) {
            String procDefKey = procInstEntity.getProcDefKey();
            objectVals.add(procDefKey);
            return;
        }

        // 2
        if (LocalWorkflowConstants.CONTEXT_NAME_PROC_DEF_NAME.equals(bindParamName)) {
            String procDefName = procInstEntity.getProcDefName();
            objectVals.add(procDefName);

            return;
        }

        // 3
        if (LocalWorkflowConstants.CONTEXT_NAME_PROC_INST_ID.equals(bindParamName)) {
            String procInstId = String.valueOf(procInstEntity.getId());
            objectVals.add(procInstId);
            return;
        }

        // 4
        if (LocalWorkflowConstants.CONTEXT_NAME_PROC_INST_KEY.equals(bindParamName)) {
            String procInstKey = procInstEntity.getProcInstKey();
            objectVals.add(procInstKey);
            return;
        }

        // 5
        if (LocalWorkflowConstants.CONTEXT_NAME_PROC_INST_NAME.equals(bindParamName)) {
            ProcExecBindingEntity procExecBindingEntity = procExecBindingMapper
                    .selectProcInstBindings(procInstEntity.getId());
            String rootEntityName = "";
            if (procExecBindingEntity != null) {
                rootEntityName = procExecBindingEntity.getEntityDataName();
            }
            String procInstName = procInstEntity.getProcDefName() + " " + rootEntityName + " "
                    + procInstEntity.getOper() + " " + formatDate(procInstEntity.getCreatedTime());
            objectVals.add(procInstName);

            return;
        }

        // 6
        if (LocalWorkflowConstants.CONTEXT_NAME_ROOT_ENTITY_NAME.equals(bindParamName)) {
            //

            ProcExecBindingEntity procExecBindingEntity = procExecBindingMapper
                    .selectProcInstBindings(procInstEntity.getId());
            String rootEntityName = null;
            if (procExecBindingEntity != null) {
                rootEntityName = procExecBindingEntity.getEntityDataName();
            }

            objectVals.add(rootEntityName);

            return;
        }

        // 7
        if (LocalWorkflowConstants.CONTEXT_NAME_ROOT_ENTITY_ID.equals(bindParamName)) {
            //

            ProcExecBindingEntity procExecBindingEntity = procExecBindingMapper
                    .selectProcInstBindings(procInstEntity.getId());
            String rootEntityId = null;
            if (procExecBindingEntity != null) {
                rootEntityId = procExecBindingEntity.getEntityDataId();
            }

            objectVals.add(rootEntityId);

            return;
        }
    }

    private void handleContextMappingForTaskNode(String mappingType, TaskNodeDefInfoEntity currTaskNodeDefEntity,
            String paramName, ProcInstInfoEntity procInstEntity, PluginConfigInterfaceParameters param,
            String paramType, List<Object> objectVals, TaskNodeInstInfoEntity bindNodeInstEntity, String bindParamName,
            String bindParamType, ProcExecBindingEntity currNodeObjectBinding) {
        List<TaskNodeExecRequestEntity> requestEntities = taskNodeExecRequestRepository
                .selectCurrentEntityByNodeInstId(bindNodeInstEntity.getId());

        if (requestEntities == null || requestEntities.isEmpty()) {
            log.error("cannot find request entity for {}", bindNodeInstEntity.getId());
            throw new WecubeCoreException("3172", "Bound request entity does not exist.");
        }

        if (requestEntities.size() > 1) {
            log.warn("duplicated request entity found for {} ", bindNodeInstEntity.getId());
            // throw new WecubeCoreException("3173", "Duplicated request entity
            // found.");
        }

        TaskNodeExecRequestEntity requestEntity = requestEntities.get(0);

        TaskNodeDefInfoEntity bindNodeDefInfoEntity = taskNodeDefInfoRepository
                .selectByPrimaryKey(bindNodeInstEntity.getNodeDefId());

        List<TaskNodeExecParamEntity> execParamEntities = taskNodeExecParamRepository
                .selectAllByRequestIdAndParamNameAndParamType(requestEntity.getReqId(), bindParamName, bindParamType);

        if (execParamEntities == null || execParamEntities.isEmpty()) {
            if (FIELD_REQUIRED.equals(param.getRequired())) {
                log.warn("parameter entity does not exist but such plugin parameter is mandatory for {} {}",
                        bindParamName, bindParamType);
                // throw new WecubeCoreException("3174",
                // String.format(
                // "parameter entity does not exist but such plugin parameter is
                // mandatory for {%s} {%s}",
                // bindParamName, bindParamType),
                // bindParamName, bindParamType);
            }
        }

        // #2169
        Object finalInputParam = calculateContextValue(paramType, execParamEntities, currTaskNodeDefEntity,
                currNodeObjectBinding, bindNodeDefInfoEntity);

        log.debug("context final input parameter {} {} {}", paramName, paramType, finalInputParam);

        objectVals.add(finalInputParam);
    }

    // #2169
    private Object calculateContextValue(String paramType, List<TaskNodeExecParamEntity> execParamEntities,
            TaskNodeDefInfoEntity currTaskNodeDefEntity, ProcExecBindingEntity currNodeObjectBinding,
            TaskNodeDefInfoEntity bindNodeDefInfoEntity) {

        // #2169
        List<Object> retDataValues = parseDataValueFromContext(paramType, execParamEntities, currTaskNodeDefEntity,
                currNodeObjectBinding, bindNodeDefInfoEntity);
        if (retDataValues == null || retDataValues.isEmpty()) {
            return null;
        }

        if (retDataValues.size() == 1) {
            return retDataValues.get(0);
        }

        if (Constants.DATA_TYPE_STRING.equalsIgnoreCase(paramType)) {
            return assembleValueList(retDataValues);
        } else {
            return retDataValues;
        }
    }

    // #2169
    private List<Object> parseDataValueFromContext(String paramType, List<TaskNodeExecParamEntity> execParamEntities,
            TaskNodeDefInfoEntity currTaskNodeDefEntity, ProcExecBindingEntity currNodeObjectBinding,
            TaskNodeDefInfoEntity bindNodeDefInfoEntity) {
        List<Object> retDataValues = new ArrayList<>();
        if (execParamEntities == null) {
            return retDataValues;
        }

        String currTaskNodeRoutineExp = currTaskNodeDefEntity.getRoutineExp();
        String bindTaskNodeRoutineExp = bindNodeDefInfoEntity.getRoutineExp();

        String currFullEntityDataId = currNodeObjectBinding.getFullEntityDataId();

        for (TaskNodeExecParamEntity e : execParamEntities) {
            String lastFullEntityDataId = e.getFullEntityDataId();

            if (!checkIfNeedPickoutFromContext(currTaskNodeRoutineExp, bindTaskNodeRoutineExp, currFullEntityDataId,
                    lastFullEntityDataId)) {
                continue;
            }

            String paramDataValue = e.getParamDataValue();
            if (e.getIsSensitive() != null && e.getIsSensitive() == true) {
                paramDataValue = tryDecodeParamDataValue(paramDataValue);
            }
            retDataValues.add(fromString(e.getParamDataValue(), e.getParamDataType()));
        }

        return retDataValues;
    }

    private boolean checkIfNeedPickoutFromContext(String currTaskNodeRoutineExp, String bindTaskNodeRoutineExp,
            String currFullEntityDataId, String lastFullEntityDataId) {

        log.debug(
                "to calculate currTaskNodeRoutineExp={}, bindTaskNodeRoutineExp={}, currFullEntityDataId={}, lastFullEntityDataId={}",
                currTaskNodeRoutineExp, bindTaskNodeRoutineExp, currFullEntityDataId, lastFullEntityDataId);

        if (StringUtils.isBlank(currTaskNodeRoutineExp) || StringUtils.isBlank(bindTaskNodeRoutineExp)
                || StringUtils.isBlank(currFullEntityDataId) || StringUtils.isBlank(lastFullEntityDataId)) {
            return true;
        }

        List<EntityQueryExprNodeInfo> currExprNodeInfos = this.entityQueryExpressionParser
                .parse(currTaskNodeRoutineExp);
        List<EntityQueryExprNodeInfo> lastExprNodeInfos = this.entityQueryExpressionParser
                .parse(bindTaskNodeRoutineExp);

        if (currExprNodeInfos == null || currExprNodeInfos.isEmpty()) {
            return true;
        }

        if (lastExprNodeInfos == null || lastExprNodeInfos.isEmpty()) {
            return true;
        }

        int currExprNodeInfoIndex = -1;
        int lastExprNodeInfoIndex = -1;

        int currExprNodeInfoSize = currExprNodeInfos.size();
        int lastExprNodeInfoSize = lastExprNodeInfos.size();

        for (int currIndex = (currExprNodeInfoSize - 1); currIndex >= 0; currIndex--) {
            EntityQueryExprNodeInfo currNode = currExprNodeInfos.get(currIndex);
            boolean match = false;
            for (int lastIndex = (lastExprNodeInfoSize - 1); lastIndex >= 0; lastIndex--) {
                EntityQueryExprNodeInfo lastNode = lastExprNodeInfos.get(lastIndex);
                if (currNode.getPackageName().equals(lastNode.getPackageName())
                        && currNode.getEntityName().equals(lastNode.getPackageName())) {
                    match = true;
                    lastExprNodeInfoIndex = lastIndex;
                    break;
                }
            }

            if (match) {
                currExprNodeInfoIndex = currIndex;
                break;
            }
        }

        if ((currExprNodeInfoIndex < 0) || (lastExprNodeInfoIndex < 0)) {
            return true;
        }

        String[] currFullEntityDataIdParts = currFullEntityDataId.split("::");
        if (currFullEntityDataIdParts.length != currExprNodeInfoSize) {
            return true;
        }

        String[] lastFullEntityDataIdParts = lastFullEntityDataId.split("::");
        if (lastFullEntityDataIdParts.length != lastExprNodeInfoSize) {
            return true;
        }

        String currEntityDataId = currFullEntityDataIdParts[currExprNodeInfoIndex];
        String lastEntityDataId = lastFullEntityDataIdParts[lastExprNodeInfoIndex];

        if (!currEntityDataId.equals(lastEntityDataId)) {
            return false;
        }

        return true;

    }

    private void handleSystemMapping(String mappingType, PluginConfigInterfaceParameters param, String paramName,
            List<Object> objectVals) {
        if (!MAPPING_TYPE_SYSTEM_VARIABLE.equals(mappingType)) {
            return;
        }
        String systemVariableName = param.getMappingSystemVariableName();
        SystemVariables sVariable = systemVariableService.getSystemVariableByPackageNameAndName(
                param.getPluginConfigInterface().getPluginConfig().getPluginPackage().getName(), systemVariableName);

        if (sVariable == null && FIELD_REQUIRED.equals(param.getRequired())) {
            log.error("variable is null but [{}] is mandatory", paramName);
            throw new WecubeCoreException("3175", String.format("Variable is absent but [%s] is mandatory.", paramName),
                    paramName);
        }

        String sVal = null;
        if (sVariable != null) {
            sVal = sVariable.getValue();
            if (StringUtils.isBlank(sVal)) {
                sVal = sVariable.getDefaultValue();
            }
        }

        if (StringUtils.isBlank(sVal) && FIELD_REQUIRED.equals(param.getRequired())) {
            log.error("variable is blank but [{}] is mandatory", paramName);
            throw new WecubeCoreException("3176",
                    String.format("Variable is absent but [%s] is mandatory.", paramName));
        }

        objectVals.add(sVal);
    }

    private void handleConstantMapping(String mappingType, TaskNodeDefInfoEntity taskNodeDefEntity, String paramName,
            List<Object> objectVals, boolean fieldRequired, PluginConfigInterfaceParameters param) {
        if (!MAPPING_TYPE_CONSTANT.equals(mappingType)) {
            return;
        }

        if (StringUtils.isNoneBlank(param.getMappingValue())) {
            objectVals.add(param.getMappingValue());
            return;
        }

        String curTaskNodeDefId = taskNodeDefEntity.getId();
        TaskNodeParamEntity nodeParamEntity = taskNodeParamRepository
                .selectOneByTaskNodeDefIdAndParamName(curTaskNodeDefId, paramName);

        if (nodeParamEntity == null) {
            if (fieldRequired) {
                log.error("mapping type is {} but node parameter entity is null for {}", mappingType, curTaskNodeDefId);
                throw new WecubeCoreException("3177",
                        String.format("Task node parameter entity does not exist for {%s}.", paramName), paramName);
            } else {
                log.info("mapping type is {} but node parameter entity is null for {}, field not madantory.",
                        mappingType, curTaskNodeDefId);
                return;
            }
        }

        Object val = null;

        if (MAPPING_TYPE_CONSTANT.equalsIgnoreCase(nodeParamEntity.getBindType())) {
            val = nodeParamEntity.getBindVal();
        }

        if (val != null) {
            objectVals.add(val);
        }
    }

    private PluginConfigInterfaces retrievePluginConfigInterface(TaskNodeDefInfoEntity taskNodeDefEntity,
            String nodeId) {
        String serviceId = retrieveServiceId(taskNodeDefEntity, nodeId);
        PluginConfigInterfaces pluginConfigInterface = pluginConfigMgmtService
                .getPluginConfigInterfaceByServiceName(serviceId);

        if (pluginConfigInterface == null) {
            log.error("Plugin config interface does not exist for {} {} {}", taskNodeDefEntity.getId(), nodeId,
                    serviceId);
            throw new WecubeCoreException("3178", "Plugin config interface does not exist.");
        }

        return pluginConfigInterface;
    }

    private List<ProcExecBindingEntity> retrieveProcExecBindingEntities(TaskNodeInstInfoEntity taskNodeInstEntity) {
        List<ProcExecBindingEntity> nodeObjectBindings = procExecBindingMapper
                .selectAllBoundTaskNodeBindings(taskNodeInstEntity.getProcInstId(), taskNodeInstEntity.getId());

        if (nodeObjectBindings == null) {
            log.info("node object bindings is empty for {} {}", taskNodeInstEntity.getProcInstId(),
                    taskNodeInstEntity.getId());
            nodeObjectBindings = new ArrayList<>();
        }

        return nodeObjectBindings;
    }

    private String retrieveServiceId(TaskNodeDefInfoEntity taskNodeDefEntity, String nodeId) {
        String serviceId = taskNodeDefEntity.getServiceId();
        if (StringUtils.isBlank(serviceId)) {
            log.error("service ID is invalid for {} {}", taskNodeDefEntity.getProcDefId(), nodeId);
            throw new WecubeCoreException("3179", "Service ID is invalid.");
        }

        if (log.isDebugEnabled()) {
            log.debug("retrieved service id {} for {},{}", serviceId, taskNodeDefEntity.getProcDefId(), nodeId);
        }
        return serviceId;
    }

    private TaskNodeInstInfoEntity retrieveTaskNodeInstInfoEntity(Integer procInstId, String nodeId) {
        Date currTime = new Date();
        TaskNodeInstInfoEntity taskNodeInstEntity = taskNodeInstInfoRepository
                .selectOneByProcInstIdAndNodeId(procInstId, nodeId);
        if (taskNodeInstEntity == null) {
            log.warn("Task node instance does not exist for {} {}", procInstId, nodeId);
            throw new WecubeCoreException("3180", "Task node instance does not exist.");
        }

        //
        String originalStatus = taskNodeInstEntity.getStatus();
        taskNodeInstEntity.setStatus(TaskNodeInstInfoEntity.IN_PROGRESS_STATUS);

        log.debug("task node instance {} update status from {} to {}", taskNodeInstEntity.getId(), originalStatus,
                taskNodeInstEntity.getStatus());

        taskNodeInstEntity.setUpdatedTime(currTime);
        taskNodeInstEntity.setUpdatedBy(WorkflowConstants.DEFAULT_USER);
        taskNodeInstEntity.setErrMsg(EMPTY_ERROR_MSG);
        taskNodeInstInfoRepository.updateByPrimaryKeySelective(taskNodeInstEntity);

        List<TaskNodeExecRequestEntity> formerRequestEntities = taskNodeExecRequestRepository
                .selectCurrentEntityByNodeInstId(taskNodeInstEntity.getId());

        if (formerRequestEntities != null) {
            for (TaskNodeExecRequestEntity formerRequestEntity : formerRequestEntities) {
                formerRequestEntity.setIsCurrent(false);
                formerRequestEntity.setUpdatedTime(currTime);
                formerRequestEntity.setUpdatedBy(WorkflowConstants.DEFAULT_USER);
                taskNodeExecRequestRepository.updateByPrimaryKeySelective(formerRequestEntity);
            }
        }

        return taskNodeInstEntity;
    }

    private TaskNodeDefInfoEntity retrieveTaskNodeDefInfoEntity(String procDefId, String nodeId) {
        TaskNodeDefInfoEntity taskNodeDefEntity = taskNodeDefInfoRepository
                .selectOneWithProcessIdAndNodeIdAndStatus(procDefId, nodeId, TaskNodeDefInfoEntity.DEPLOYED_STATUS);

        if (taskNodeDefEntity == null) {
            log.warn("Task node definition does not exist for {} {} {}", procDefId, nodeId,
                    TaskNodeDefInfoEntity.DEPLOYED_STATUS);
            throw new WecubeCoreException("3181", "Task node definition does not exist.");
        }

        return taskNodeDefEntity;
    }

    private ProcInstInfoEntity retrieveProcInstInfoEntity(PluginInvocationCommand cmd) {
        return doRetrieveProcInstInfoEntity(cmd);
    }

    private ProcInstInfoEntity doRetrieveProcInstInfoEntity(PluginInvocationCommand cmd) {
        String procInstKernelId = cmd.getProcInstId();

        ProcInstInfoEntity procInstEntity = null;
        int round = 0;
        while (round < 15) {
            procInstEntity = procInstInfoRepository.selectOneByProcInstKernelId(procInstKernelId);

            if (procInstEntity != null) {
                break;
            }

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
            }

            round++;
        }

        if (procInstEntity == null) {
            log.error("Process instance info does not exist for id:{}", procInstKernelId);
            throw new WecubeCoreException("3182", "Process instance information does not exist.");
        }

        if (!ProcInstInfoEntity.IN_PROGRESS_STATUS.equals(procInstEntity.getStatus())) {

            String orignalStatus = procInstEntity.getStatus();
            procInstEntity.setUpdatedTime(new Date());
            procInstEntity.setUpdatedBy(WorkflowConstants.DEFAULT_USER);
            procInstEntity.setStatus(ProcInstInfoEntity.IN_PROGRESS_STATUS);

            if (log.isDebugEnabled()) {
                log.debug("process instance {} update status from {} to {}", procInstEntity.getId(), orignalStatus,
                        ProcInstInfoEntity.IN_PROGRESS_STATUS);
            }

            procInstInfoRepository.updateByPrimaryKeySelective(procInstEntity);
        }

        return procInstEntity;
    }

    private List<Map<String, Object>> calculateInputParameters(PluginInterfaceInvocationContext ctx,
            List<InputParamObject> inputParamObjs, String requestId, String operator) {
        List<Map<String, Object>> pluginParameters = new ArrayList<Map<String, Object>>();

        int objectId = 0;

        for (InputParamObject ipo : inputParamObjs) {
            if (log.isDebugEnabled()) {
                log.debug("process input parameters for entity:{} {}", ipo.getEntityTypeId(), ipo.getEntityDataId());
            }

            String sObjectId = String.valueOf(objectId);
            String entityTypeId = ipo.getEntityTypeId();
            String entityDataId = ipo.getEntityDataId();
            String callbackId = entityDataId;
            // #2169
            String fullEntityDataId = ipo.getFullEntityDataId();

            Map<String, Object> inputMap = new HashMap<String, Object>();
            inputMap.put(CALLBACK_PARAMETER_KEY, callbackId);
            TaskNodeExecParamEntity p = new TaskNodeExecParamEntity();
            p.setReqId(requestId);
            p.setParamName(CALLBACK_PARAMETER_KEY);
            p.setParamType(TaskNodeExecParamEntity.PARAM_TYPE_REQUEST);
            p.setParamDataType(Constants.DATA_TYPE_STRING);
            p.setObjId(sObjectId);
            p.setParamDataValue(entityDataId);
            p.setEntityDataId(entityDataId);
            p.setEntityTypeId(entityTypeId);
            // 2169
            p.setFullEntityDataId(fullEntityDataId);
            p.setCreatedBy(WorkflowConstants.DEFAULT_USER);
            p.setCreatedTime(new Date());
            p.setIsSensitive(false);
            // #2233
            p.setCallbackId(callbackId);
            p.setMappingType(null);
            p.setMultiple(Constants.DATA_NOT_MULTIPLE);
            p.setParamDefId(null);

            taskNodeExecParamRepository.insert(p);

            if (StringUtils.isNoneBlank(ipo.getConfirmToken())) {
                inputMap.put(CONFIRM_TOKEN_KEY, ipo.getConfirmToken());
                TaskNodeExecParamEntity confirmTokenParam = new TaskNodeExecParamEntity();
                confirmTokenParam.setReqId(requestId);
                confirmTokenParam.setParamName(CONFIRM_TOKEN_KEY);
                confirmTokenParam.setParamType(TaskNodeExecParamEntity.PARAM_TYPE_REQUEST);
                confirmTokenParam.setParamDataType(Constants.DATA_TYPE_STRING);
                confirmTokenParam.setObjId(sObjectId);
                confirmTokenParam.setParamDataValue(ipo.getConfirmToken());
                confirmTokenParam.setEntityDataId(entityDataId);
                confirmTokenParam.setEntityTypeId(entityTypeId);
                // 2169
                confirmTokenParam.setFullEntityDataId(fullEntityDataId);
                confirmTokenParam.setCreatedBy(WorkflowConstants.DEFAULT_USER);
                confirmTokenParam.setCreatedTime(new Date());
                confirmTokenParam.setIsSensitive(false);

                taskNodeExecParamRepository.insert(confirmTokenParam);
            }

            inputMap.put(INPUT_PARAMETER_KEY_OPERATOR, operator);

            for (InputParamAttr attr : ipo.getAttrs()) {
                TaskNodeExecParamEntity e = new TaskNodeExecParamEntity();
                e.setReqId(requestId);
                e.setParamName(attr.getName());
                e.setParamType(TaskNodeExecParamEntity.PARAM_TYPE_REQUEST);
                e.setParamDataType(attr.getDataType());
                e.setObjId(sObjectId);
                e.setParamDataValue(tryCalculateParamDataValue(attr));
                e.setEntityDataId(entityDataId);
                e.setEntityTypeId(entityTypeId);
                e.setFullEntityDataId(fullEntityDataId);
                e.setCreatedBy(WorkflowConstants.DEFAULT_USER);
                e.setCreatedTime(new Date());

                e.setIsSensitive(attr.isSensitive());

                e.setCallbackId(callbackId);
                e.setMultiple(attr.getMultiple());
                if (attr.getParamDef() != null) {
                    e.setMappingType(attr.getParamDef().getMappingType());
                    e.setParamDefId(attr.getParamDef().getId());
                }

                taskNodeExecParamRepository.insert(e);

                inputMap.put(attr.getName(), attr.getExpectedValue());
            }

            pluginParameters.add(inputMap);

            objectId++;
        }

        return pluginParameters;
    }

    private String tryCalculateParamDataValue(InputParamAttr attr) {
        if (attr.getExpectedValue() == null) {
            return null;
        }

        // #2226
        Object dataValue = attr.getExpectedValue();
        String dataValueStr = InputParamAttr.convertToString(dataValue);
        if (attr.isSensitive()) {
            dataValueStr = tryEncodeParamDataValue(dataValueStr);
        }

        return dataValueStr;
    }

    private PluginInstances retrieveAvailablePluginInstance(PluginConfigInterfaces itf) {
        PluginConfigs config = itf.getPluginConfig();
        PluginPackages pkg = config.getPluginPackage();
        String pluginName = pkg.getName();

        List<PluginInstances> instances = pluginInstanceMgmtService.getRunningPluginInstances(pluginName);

        return instances.get(0);

    }

    private void handleErrorInvocationResult(PluginInterfaceInvocationResult pluginInvocationResult,
            PluginInterfaceInvocationContext ctx) {

        if (pluginInvocationResult.getResultData() != null && !pluginInvocationResult.getResultData().isEmpty()) {
            log.debug("plugin invocation partially succeeded.{} {}", ctx.getRequestId(), ctx.getInterfacePath());
            handleResultData(pluginInvocationResult, ctx, pluginInvocationResult.getResultData());
        }

        PluginInvocationResult result = new PluginInvocationResult()
                .parsePluginInvocationCommand(ctx.getPluginInvocationCommand());

        log.warn("system errors:{}", pluginInvocationResult.getErrMsg());
        result.setResultCode(RESULT_CODE_ERR);
        pluginInvocationResultService.responsePluginInterfaceInvocation(result);
        handlePluginInterfaceInvocationFailure(pluginInvocationResult, ctx, "5001",
                "Errors:" + trimWithMaxLength(pluginInvocationResult.getErrMsg()));

        return;
    }

    private void handleNullResultData(PluginInterfaceInvocationResult pluginInvocationResult,
            PluginInterfaceInvocationContext ctx) {
        PluginInvocationResult result = new PluginInvocationResult()
                .parsePluginInvocationCommand(ctx.getPluginInvocationCommand());
        PluginConfigInterfaces pluginConfigInterface = ctx.getPluginConfigInterface();
        List<PluginConfigInterfaceParameters> outputParameters = pluginConfigInterface.getOutputParameters();

        if (outputParameters == null || outputParameters.isEmpty()) {
            log.debug("output parameter is NOT configured for interface {}", pluginConfigInterface.getServiceName());
            result.setResultCode(RESULT_CODE_OK);
            pluginInvocationResultService.responsePluginInterfaceInvocation(result);
            handlePluginInterfaceInvocationSuccess(pluginInvocationResult, ctx);
            return;
        }

        if (outputParameters != null && !outputParameters.isEmpty()) {
            if (ctx.getPluginParameters() == null || ctx.getPluginParameters().isEmpty()) {
                log.debug("output parameter is configured but INPUT is empty for interface {}",
                        pluginConfigInterface.getServiceName());
                result.setResultCode(RESULT_CODE_OK);
                pluginInvocationResultService.responsePluginInterfaceInvocation(result);
                handlePluginInterfaceInvocationSuccess(pluginInvocationResult, ctx);
                return;
            } else {
                log.warn("output parameter is configured but result is empty for interface {}",
                        pluginConfigInterface.getServiceName());
                result.setResultCode(RESULT_CODE_ERR);
                pluginInvocationResultService.responsePluginInterfaceInvocation(result);
                handlePluginInterfaceInvocationFailure(pluginInvocationResult, ctx, "5003", "output is null");
                return;
            }
        }

        return;
    }

    private void handleResultData(PluginInterfaceInvocationResult pluginInvocationResult,
            PluginInterfaceInvocationContext ctx, List<Object> resultData) {

        List<Map<String, Object>> outputParameterMaps = validateAndCastResultData(resultData);
        storeOutputParameterMaps(ctx, outputParameterMaps);

        if (log.isDebugEnabled()) {
            log.debug("about to process output parameters for {}", ctx.getPluginConfigInterface().getServiceName());
        }

        Exception except = null;
        for (Map<String, Object> outputParameterMap : outputParameterMaps) {
            try {
                handleSingleOutputMap(pluginInvocationResult, ctx, outputParameterMap);
            } catch (Exception e) {
                String errMsg = String.format("handling output errors:%s %s :%s", outputParameterMap, e.getMessage(),
                        ctx.getPluginInvocationCommand());
                log.error(errMsg);
                except = e;
            }
        }

        if (except != null) {
            log.error("failed to process output parameters for {} :{}", ctx.getPluginConfigInterface().getServiceName(),
                    ctx.getPluginInvocationCommand());
            throw new WecubeCoreException("Handling output result errors:" + except.getMessage());
        }

        if (log.isInfoEnabled()) {
            log.info("finished processing {} output parameters for {} :{}", outputParameterMaps.size(),
                    ctx.getPluginConfigInterface().getServiceName(), ctx.getPluginInvocationCommand());
        }

        return;
    }

    private void storeOutputParameterMaps(PluginInterfaceInvocationContext ctx,
            List<Map<String, Object>> outputParameterMaps) {
        int count = 0;
        for (Map<String, Object> outputParameterMap : outputParameterMaps) {
            String objectId = String.valueOf(count);
            storeSingleOutputParameterMap(ctx, outputParameterMap, objectId);
            count++;
        }
    }

    private void storeSingleOutputParameterMap(PluginInterfaceInvocationContext ctx,
            Map<String, Object> outputParameterMap, String inputObjectId) {
        String requestId = ctx.getTaskNodeExecRequestEntity().getReqId();

        if (outputParameterMap == null || outputParameterMap.isEmpty()) {
            log.info("empty output parameters for {} {} and ignored", requestId, inputObjectId);
            return;
        }
        String entityTypeId = null;
        String entityDataId = null;
        // #2169
        String fullEntityDataId = null;

        String callbackParameter = (String) outputParameterMap.get(CALLBACK_PARAMETER_KEY);

        TaskNodeExecParamEntity callbackParameterInputEntity = null;
        if (StringUtils.isNotBlank(callbackParameter)) {
            List<TaskNodeExecParamEntity> callbackParameterInputEntities = taskNodeExecParamRepository
                    .selectOneByRequestIdAndParamTypeAndParamNameAndValue(requestId,
                            TaskNodeExecParamEntity.PARAM_TYPE_REQUEST, CALLBACK_PARAMETER_KEY, callbackParameter);
            if (callbackParameterInputEntities != null && !callbackParameterInputEntities.isEmpty()) {
                callbackParameterInputEntity = callbackParameterInputEntities.get(0);
            }
        }

        // #2169
        String objectId = inputObjectId;

        if (callbackParameterInputEntity != null) {
            // objectId = callbackParameterInputEntity.getObjId();
            entityTypeId = callbackParameterInputEntity.getEntityTypeId();
            entityDataId = callbackParameterInputEntity.getEntityDataId();
            fullEntityDataId = callbackParameterInputEntity.getFullEntityDataId();
        }

        // #2169
        if (StringUtils.isBlank(callbackParameter)) {
            callbackParameter = String.format("%s-%s", CALLBACK_PARAMETER_SYSTEM_PREFIX, LocalIdGenerator.generateId());

            TaskNodeExecParamEntity paramEntity = new TaskNodeExecParamEntity();
            paramEntity.setEntityTypeId(entityTypeId);
            paramEntity.setEntityDataId(entityDataId);
            // #2169
            paramEntity.setFullEntityDataId(fullEntityDataId);
            paramEntity.setObjId(objectId);
            paramEntity.setParamType(TaskNodeExecParamEntity.PARAM_TYPE_RESPONSE);
            paramEntity.setParamName(CALLBACK_PARAMETER_KEY);
            paramEntity.setParamDataType(Constants.DATA_TYPE_STRING);
            paramEntity.setParamDataValue(callbackParameter);
            paramEntity.setReqId(requestId);
            paramEntity.setIsSensitive(false);
            paramEntity.setCreatedBy(WorkflowConstants.DEFAULT_USER);
            paramEntity.setCreatedTime(new Date());

            // #2233
            paramEntity.setCallbackId(callbackParameter);
            paramEntity.setMappingType(null);
            paramEntity.setMultiple(Constants.DATA_NOT_MULTIPLE);
            paramEntity.setParamDefId(null);

            taskNodeExecParamRepository.insert(paramEntity);
        }

        List<PluginConfigInterfaceParameters> outputParameters = ctx.getPluginConfigInterface().getOutputParameters();

        for (Map.Entry<String, Object> entry : outputParameterMap.entrySet()) {

            PluginConfigInterfaceParameters p = findPreConfiguredPluginConfigInterfaceParameter(outputParameters,
                    entry.getKey());

            String paramDataType = null;
            boolean isSensitiveData = false;
            if (p == null) {
                paramDataType = Constants.DATA_TYPE_STRING;
            } else {
                paramDataType = p.getDataType();
                isSensitiveData = (IS_SENSITIVE_ATTR.equalsIgnoreCase(p.getSensitiveData()));
            }

            String paramDataValue = trimExceedParamValue(asString(entry.getValue(), paramDataType), MAX_PARAM_VAL_SIZE);

            if (isSensitiveData) {
                paramDataValue = tryEncodeParamDataValue(paramDataValue);
            }

            TaskNodeExecParamEntity paramEntity = new TaskNodeExecParamEntity();
            paramEntity.setEntityTypeId(entityTypeId);
            paramEntity.setEntityDataId(entityDataId);
            // 2169
            paramEntity.setFullEntityDataId(fullEntityDataId);
            paramEntity.setObjId(objectId);
            paramEntity.setParamType(TaskNodeExecParamEntity.PARAM_TYPE_RESPONSE);
            paramEntity.setParamName(entry.getKey());
            paramEntity.setParamDataType(paramDataType);
            paramEntity.setParamDataValue(paramDataValue);
            paramEntity.setReqId(requestId);
            paramEntity.setIsSensitive(isSensitiveData);
            paramEntity.setCreatedBy(WorkflowConstants.DEFAULT_USER);
            paramEntity.setCreatedTime(new Date());

            // #2233
            paramEntity.setCallbackId(callbackParameter);
            if (p != null) {
                paramEntity.setMultiple(p.getMultiple());
                paramEntity.setMappingType(p.getMappingType());
                paramEntity.setParamDefId(p.getId());
            }

            taskNodeExecParamRepository.insert(paramEntity);
        }
    }

    private boolean verifySystemCallbackParameterKeyValue(String callbackParameterValue) {
        if (StringUtils.isBlank(callbackParameterValue)) {
            return false;
        }

        return callbackParameterValue.startsWith(CALLBACK_PARAMETER_SYSTEM_PREFIX);
    }

    private boolean verifyIfHasNormalEntityMappingExcludeAssign(List<DmeOutputParamAttr> rootDemOutputParamAttrs) {
        if (rootDemOutputParamAttrs == null || rootDemOutputParamAttrs.isEmpty()) {
            return false;
        }

        for (DmeOutputParamAttr attr : rootDemOutputParamAttrs) {
            if (Constants.MAPPING_TYPE_ENTITY.equalsIgnoreCase(attr.getInterfParam().getMappingType())) {
                return true;
            }
        }

        return false;
    }

    // #2169
    private void tryHandleSingleOutputMapOnceEntityCreation(PluginInterfaceInvocationResult pluginInvocationResult,
            PluginInterfaceInvocationContext ctx, Map<String, Object> outputParameterMap) {
        // to check if there are any bindings?
        List<DmeOutputParamAttr> allDmeOutputParamAttrs = new ArrayList<>();

        List<DmeOutputParamAttr> rootDemOutputParamAttrs = new ArrayList<>();

        PluginConfigInterfaces pci = ctx.getPluginConfigInterface();
        List<PluginConfigInterfaceParameters> outputParameters = pci.getOutputParameters();
        for (PluginConfigInterfaceParameters pciParam : outputParameters) {
            String paramName = pciParam.getName();
            String paramExpr = pciParam.getMappingEntityExpression();

            if (StringUtils.isBlank(paramExpr)) {
                log.info("expression not configured for {}", paramName);
                continue;
            }

            if (!(Constants.MAPPING_TYPE_ENTITY.equalsIgnoreCase(pciParam.getMappingType())
                    || Constants.MAPPING_TYPE_ASSIGN.equalsIgnoreCase(pciParam.getMappingType()))) {
                continue;
            }

            Object retVal = null;
            if (Constants.MAPPING_TYPE_ENTITY.equalsIgnoreCase(pciParam.getMappingType())) {
                retVal = outputParameterMap.get(paramName);
            } else if (Constants.MAPPING_TYPE_ASSIGN.equalsIgnoreCase(pciParam.getMappingType())) {
                retVal = pciParam.getMappingValue();
            }

            if (retVal == null) {
                log.info("returned value is null for {} {}", ctx.getRequestId(), paramName);
//                continue;
            }

            List<EntityQueryExprNodeInfo> exprNodeInfos = entityQueryExpressionParser.parse(paramExpr);

            if (exprNodeInfos == null || exprNodeInfos.isEmpty()) {
                String errMsg = String.format("Unknown how to update entity attribute due to invalid expression:%s",
                        paramExpr);
                log.error(errMsg);
                throw new WecubeCoreException(errMsg);
            }

            EntityQueryExprNodeInfo leafExprNodeInfo = exprNodeInfos.get(exprNodeInfos.size() - 1);
            String targetPackageName = leafExprNodeInfo.getPackageName();
            String targetEntityName = leafExprNodeInfo.getEntityName();
            String targetAttrName = leafExprNodeInfo.getQueryAttrName();

            PluginPackageAttributes targetAttrDefinition = getTargetPluginPackageAttributes(targetPackageName,
                    targetEntityName, targetAttrName);
            Object finalRetVal = validateAndConvert(targetAttrDefinition, retVal);

            DmeOutputParamAttr outputParamAttr = new DmeOutputParamAttr();
            outputParamAttr.setExprNodeInfos(exprNodeInfos);
            outputParamAttr.setInterf(pci);
            outputParamAttr.setInterfParam(pciParam);
            outputParamAttr.setParamExpr(paramExpr);
            outputParamAttr.setParamName(paramName);
            outputParamAttr.setRetVal(finalRetVal);

            allDmeOutputParamAttrs.add(outputParamAttr);
            if (outputParamAttr.isRootEntityAttr()) {
                rootDemOutputParamAttrs.add(outputParamAttr);
            }
        }

        if (rootDemOutputParamAttrs.isEmpty()) {
            // unknown rootNodeEntityId
            log.warn("There is not root DME output parameters to write.{}", outputParameterMap);
            return;
        }
        EntityQueryExprNodeInfo rootExprNodeInfo = rootDemOutputParamAttrs.get(0).getExprNodeInfos().get(0);
        String packageName = rootExprNodeInfo.getPackageName();
        String entityName = rootExprNodeInfo.getEntityName();

        Map<String, Object> objDataMap = new HashMap<String, Object>(rootDemOutputParamAttrs.size());
        DmeOutputParamAttr idAttrDef = null;
        for (DmeOutputParamAttr attr : rootDemOutputParamAttrs) {
            attr.setProcessed(true);
            EntityQueryExprNodeInfo exprNodeNodeInfo = attr.getExprNodeInfos().get(0);

            if ("id".equalsIgnoreCase(exprNodeNodeInfo.getQueryAttrName()) && (attr.getRetVal() != null)) {
                if (attr.getRetVal() instanceof String) {
                    if (StringUtils.isNoneBlank((String) attr.getRetVal())) {
                        idAttrDef = attr;
                    }
                } else {
                    idAttrDef = attr;
                }
            }
            if (StringUtils.isBlank(exprNodeNodeInfo.getQueryAttrName())) {
                String errMsg = String.format(
                        "Invalid expression due to unknown attribute name of CI for paramter [%s], expression [%s]",
                        attr.getParamName(), attr.getParamExpr());
                log.error(errMsg);
                throw new WecubeCoreException(errMsg);
            }
            objDataMap.put(exprNodeNodeInfo.getQueryAttrName(), attr.getRetVal());
        }

        log.info("try to create entity.{} {} {}", packageName, entityName, objDataMap);

        String rootEntityId = null;
        if (idAttrDef != null && idAttrDef.getRetVal() != null) {
            rootEntityId = (String) idAttrDef.getRetVal();
            EntityRouteDescription entityDef = entityDataRouteFactory.deduceEntityDescription(packageName, entityName);
            StandardEntityOperationRestClient restClient = new StandardEntityOperationRestClient(
                    this.jwtSsoRestTemplate);
            List<Map<String, Object>> objDataMaps = new ArrayList<>();
            objDataMaps.add(objDataMap);
            restClient.updateData(entityDef, objDataMaps);

        } else {
            if (verifyIfHasNormalEntityMappingExcludeAssign(rootDemOutputParamAttrs)) {
                Map<String, Object> resultMap = entityOperationService.create(packageName, entityName, objDataMap);
                rootEntityId = (String) resultMap.get(Constants.UNIQUE_IDENTIFIER);
                if (StringUtils.isBlank(rootEntityId)) {
                    log.warn("Entity created but there is not identity returned.{} {} {}", packageName, entityName,
                            objDataMap);
                    return;
                }
            } else {
                log.info("The result must has at least one entity mapping and ignored.{}", objDataMap);
                return;
            }
        }

        for (DmeOutputParamAttr attr : allDmeOutputParamAttrs) {
            if (attr.isRootEntityAttr() || attr.isProcessed()) {
                continue;
            }
            EntityOperationRootCondition condition = new EntityOperationRootCondition(attr.getParamExpr(),
                    rootEntityId);

            try {
                this.entityOperationService.update(condition, attr.getRetVal(), null);
            } catch (Exception e) {
                log.warn("Exceptions while updating entity.But still keep going to update.", e);
                String errMsg = String.format("Failed to update entity data with {} {} caused by:{}",
                        attr.getParamExpr(), rootEntityId, e.getMessage());
                throw new WecubeCoreException(errMsg);
            }
        }
    }

    private void tryHandleSingleOutputMapOnceEntityUpdate(PluginInterfaceInvocationResult pluginInvocationResult,
            PluginInterfaceInvocationContext ctx, Map<String, Object> outputParameterMap, String nodeEntityId) {
        log.info("About to update single output map for {}:{}", nodeEntityId, outputParameterMap);
        PluginConfigInterfaces pci = ctx.getPluginConfigInterface();
        List<PluginConfigInterfaceParameters> outputParameters = pci.getOutputParameters();
        for (PluginConfigInterfaceParameters pciParam : outputParameters) {
            String paramName = pciParam.getName();
            String paramExpr = pciParam.getMappingEntityExpression();

            if (StringUtils.isBlank(paramExpr)) {
                log.info("expression not configured for {}", paramName);
                continue;
            }

            if (!(Constants.MAPPING_TYPE_ENTITY.equalsIgnoreCase(pciParam.getMappingType())
                    || Constants.MAPPING_TYPE_ASSIGN.equalsIgnoreCase(pciParam.getMappingType()))) {
                continue;
            }

            Object retVal = null;
            if (Constants.MAPPING_TYPE_ENTITY.equalsIgnoreCase(pciParam.getMappingType())) {
                retVal = outputParameterMap.get(paramName);
            } else if (Constants.MAPPING_TYPE_ASSIGN.equalsIgnoreCase(pciParam.getMappingType())) {
                retVal = pciParam.getMappingValue();
            }

            if (retVal == null) {
                log.info("returned value is null for {} {}", ctx.getRequestId(), paramName);
//                continue;
            }

//            if (retVal instanceof String) {
//                if (StringUtils.isBlank((String) retVal)) {
//                    continue;
//                }
//            }

            List<EntityQueryExprNodeInfo> exprNodeInfos = entityQueryExpressionParser.parse(paramExpr);
            if (exprNodeInfos == null || exprNodeInfos.isEmpty()) {
                String errMsg = String.format("Unknown how to update entity attribute due to invalid expression:%s",
                        paramExpr);
                log.error(errMsg);
                throw new WecubeCoreException(errMsg);
            }

            EntityQueryExprNodeInfo leafExprNodeInfo = exprNodeInfos.get(exprNodeInfos.size() - 1);
            String targetPackageName = leafExprNodeInfo.getPackageName();
            String targetEntityName = leafExprNodeInfo.getEntityName();
            String targetAttrName = leafExprNodeInfo.getQueryAttrName();

            PluginPackageAttributes targetAttrDefinition = getTargetPluginPackageAttributes(targetPackageName,
                    targetEntityName, targetAttrName);
            Object finalRetVal = validateAndConvert(targetAttrDefinition, retVal);

            EntityOperationRootCondition condition = new EntityOperationRootCondition(paramExpr, nodeEntityId);

            try {
                this.entityOperationService.update(condition, finalRetVal, null);
            } catch (Exception e) {
                log.warn("Exceptions while updating entity.But still keep going to update.", e);
                String errMsg = String.format("Failed to update entity data with [%s] [%s] caused by:%s", paramExpr,
                        nodeEntityId, e.getMessage());
                throw new WecubeCoreException(errMsg);
            }

        }

    }

    private Object validateAndConvert(PluginPackageAttributes targetAttrDefinition, Object retVal) {
        if (targetAttrDefinition == null) {
            return retVal;
        }

        boolean isMultiple = Constants.DATA_MULTIPLE.equalsIgnoreCase(targetAttrDefinition.getMultiple());
        if (isMultiple) {
            if (retVal == null) {
                return new ArrayList<Object>();
            } else {
                if (retVal instanceof List) {
                    return retVal;
                } else {
                    List<Object> finalRetVal = new ArrayList<>();
                    finalRetVal.add(retVal);
                    return finalRetVal;
                }
            }
        } else {
            if (retVal != null && (retVal instanceof List)) {
                String errMsg = String.format("Data type does not match required:%s but %s",
                        targetAttrDefinition.getDataType(), "list");
                log.error(errMsg);
                throw new WecubeCoreException(errMsg);
            }

            return retVal;
        }
    }

    private PluginPackageAttributes getTargetPluginPackageAttributes(String targetPackageName, String targetEntityName,
            String targetAttrName) {
        PluginPackageAttributes attr = pluginPackageDataModelService
                .tryFetchLatestAvailablePluginPackageAttributes(targetPackageName, targetEntityName, targetAttrName);
        return attr;
    }

    private void handleSingleOutputMap(PluginInterfaceInvocationResult pluginInvocationResult,
            PluginInterfaceInvocationContext ctx, Map<String, Object> outputParameterMap) {

        PluginConfigInterfaces pci = ctx.getPluginConfigInterface();
        List<PluginConfigInterfaceParameters> outputParameters = pci.getOutputParameters();

        if (outputParameters == null) {
            return;
        }

        if (outputParameterMap == null || outputParameterMap.isEmpty()) {
            log.info("returned output is empty for request {}", ctx.getRequestId());
            return;
        }

        String nodeEntityId = (String) outputParameterMap.get(CALLBACK_PARAMETER_KEY);

        String errorCodeOfSingleRecord = (String) outputParameterMap.get(PLUGIN_RESULT_CODE_PARTIALLY_KEY);
        if (StringUtils.isNotBlank(errorCodeOfSingleRecord)
                && PLUGIN_RESULT_CODE_PARTIALLY_FAIL.equalsIgnoreCase(errorCodeOfSingleRecord)) {
            log.info("such request is partially failed for request:{} and {}:{}", ctx.getRequestId(),
                    CALLBACK_PARAMETER_KEY, nodeEntityId);
            // to store status?
            return;
        }

        if (!PLUGIN_RESULT_CODE_OK.equalsIgnoreCase(errorCodeOfSingleRecord)) {
            log.info("such request is not successful for request:{} and {}:{}", ctx.getRequestId(),
                    CALLBACK_PARAMETER_KEY, nodeEntityId);
            return;
        }

        // #2169
        if (StringUtils.isBlank(nodeEntityId) || verifySystemCallbackParameterKeyValue(nodeEntityId)) {
            log.info("callback parameter value {} for request {},and try to create entity", nodeEntityId,
                    ctx.getRequestId());
            tryHandleSingleOutputMapOnceEntityCreation(pluginInvocationResult, ctx, outputParameterMap);
            return;
        }

        tryHandleSingleOutputMapOnceEntityUpdate(pluginInvocationResult, ctx, outputParameterMap, nodeEntityId);

    }

    private void handlePluginInterfaceInvocationSuccess(PluginInterfaceInvocationResult pluginInvocationResult,
            PluginInterfaceInvocationContext ctx) {
        Date now = new Date();
        TaskNodeExecRequestEntity requestEntity = ctx.getTaskNodeExecRequestEntity();

        requestEntity.setUpdatedTime(now);
        requestEntity.setUpdatedBy(WorkflowConstants.DEFAULT_USER);
        requestEntity.setIsCompleted(true);

        taskNodeExecRequestRepository.updateByPrimaryKeySelective(requestEntity);

        TaskNodeInstInfoEntity nodeInstEntity = ctx.getTaskNodeInstEntity();

        nodeInstEntity.setUpdatedTime(now);
        nodeInstEntity.setUpdatedBy(WorkflowConstants.DEFAULT_USER);
        nodeInstEntity.setStatus(TaskNodeInstInfoEntity.COMPLETED_STATUS);
        nodeInstEntity.setErrMsg(EMPTY_ERROR_MSG);

        taskNodeInstInfoRepository.updateByPrimaryKeySelective(nodeInstEntity);
    }

    private void handlePluginInterfaceInvocationFailure(PluginInterfaceInvocationResult pluginInvocationResult,
            PluginInterfaceInvocationContext ctx, String errorCode, String errorMsg) {

        Date now = new Date();
        TaskNodeExecRequestEntity requestEntity = ctx.getTaskNodeExecRequestEntity();

        requestEntity.setUpdatedTime(now);
        requestEntity.setUpdatedBy(WorkflowConstants.DEFAULT_USER);
        requestEntity.setErrCode(errorCode);
        requestEntity.setErrMsg(errorMsg);
        requestEntity.setIsCompleted(true);

        taskNodeExecRequestRepository.updateByPrimaryKeySelective(requestEntity);

        TaskNodeInstInfoEntity nodeInstEntity = ctx.getTaskNodeInstEntity();

        nodeInstEntity.setUpdatedTime(now);
        nodeInstEntity.setUpdatedBy(WorkflowConstants.DEFAULT_USER);
        nodeInstEntity.setStatus(TaskNodeInstInfoEntity.FAULTED_STATUS);
        nodeInstEntity.setErrMsg(errorMsg);

        taskNodeInstInfoRepository.updateByPrimaryKeySelective(nodeInstEntity);

    }

}
