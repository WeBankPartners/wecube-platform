package com.webank.wecube.platform.core.service.workflow;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.workflow.PluginAsyncInvocationResultDto;
import com.webank.wecube.platform.core.entity.plugin.PluginConfigInterfaceParameters;
import com.webank.wecube.platform.core.entity.plugin.PluginConfigInterfaces;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeDefInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeExecParamEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeExecRequestEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeInstInfoEntity;
import com.webank.wecube.platform.core.model.workflow.PluginInvocationCommand;
import com.webank.wecube.platform.core.model.workflow.PluginInvocationResult;
import com.webank.wecube.platform.core.service.dme.EntityOperationRootCondition;
import com.webank.wecube.platform.core.service.workflow.PluginInvocationProcessor.PluginInterfaceInvocationContext;
import com.webank.wecube.platform.workflow.WorkflowConstants;

/**
 * 
 * @author gavin
 *
 */
@Service
public class AsyncPluginInvocationService extends AbstractPluginInvocationService {

    /**
     * 
     * @param asyncResultDto
     */
    public void handleAsyncInvocationResult(PluginAsyncInvocationResultDto asyncResultDto) {
        validate(asyncResultDto);

        String resultCode = asyncResultDto.getResultCode();
        String resultMessage = asyncResultDto.getResultMessage();
        String requestId = asyncResultDto.getResultData().getRequestId();

        List<Object> outputs = asyncResultDto.getOutputs();

        TaskNodeExecRequestEntity reqEntity = taskNodeExecRequestRepository.selectOneByRequestId(requestId);

        if (reqEntity == null) {
            log.error("request ID is NOT available:{}", requestId);
            throw new WecubeCoreException("3153", "Request id is invalid.");
        }

        if ((reqEntity.getIsCompleted() != null && reqEntity.getIsCompleted())
                || (reqEntity.getIsCurrent() != null && (!reqEntity.getIsCurrent()))) {
            log.error("request {} state is not expected,completed:{},current:{}", reqEntity.getReqId(),
                    reqEntity.getIsCompleted(), reqEntity.getIsCurrent());
            throw new WecubeCoreException("3154", "Request state is not expected");
        }

        Integer nodeInstId = reqEntity.getNodeInstId();

        TaskNodeInstInfoEntity taskNodeInstEntity = taskNodeInstInfoRepository.selectByPrimaryKey(nodeInstId);

        if (taskNodeInstEntity == null) {
            log.error("task node instance does not exist for {} {}", nodeInstId, requestId);
            throw new WecubeCoreException("3155", "Task node instance does not exist.");
        }

        if (!TaskNodeInstInfoEntity.IN_PROGRESS_STATUS.equals(taskNodeInstEntity.getStatus())) {
            log.error("task node instance status is not valid, expected {} but {}",
                    TaskNodeInstInfoEntity.IN_PROGRESS_STATUS, taskNodeInstEntity.getStatus());
            throw new WecubeCoreException("3156", "Task node instance status is not valid.");
        }

        PluginInterfaceInvocationContext ctx = new PluginInterfaceInvocationContext() //
                .withTaskNodeExecRequestEntity(reqEntity) //
                .withRequestId(requestId) //
                .withPluginInvocationCommand(pluginInvocationCommand(reqEntity)) //
                .withTaskNodeInstEntity(taskNodeInstEntity); //

        doHandleAsyncInvocationResult(resultCode, resultMessage, outputs, ctx);

    }

    private PluginInvocationCommand pluginInvocationCommand(TaskNodeExecRequestEntity e) {

        PluginInvocationCommand cmd = new PluginInvocationCommand();
        cmd.setProcDefId(e.getProcDefKernelId());
        cmd.setProcDefKey(e.getProcDefKernelKey());
        cmd.setProcDefVersion(e.getProcDefVer());
        cmd.setProcInstKey(e.getProcInstKernelKey());
        cmd.setProcInstId(e.getProcInstKernelId());
        cmd.setNodeId(e.getNodeId());
        cmd.setNodeName(e.getNodeName());
        cmd.setExecutionId(e.getExecutionId());

        return cmd;
    }

    protected void doHandleAsyncInvocationResult(String resultCode, String resultMessage, List<Object> resultData,
            PluginInterfaceInvocationContext ctx) {

        TaskNodeDefInfoEntity nodeDefEntity = taskNodeDefInfoRepository
                .selectByPrimaryKey(ctx.getTaskNodeInstEntity().getNodeDefId());
        if (nodeDefEntity == null) {
            log.error("such task node definition does not exist for {} {}", ctx.getTaskNodeInstEntity().getNodeDefId(),
                    ctx.getRequestId());
            throw new WecubeCoreException("3157", "Task node definition does not exist.");
        }
        
        //TODO #2109 handle user task output

        PluginConfigInterfaces pluginConfigInterface = pluginConfigMgmtService
                .getPluginConfigInterfaceByServiceName(nodeDefEntity.getServiceId());

        if (pluginConfigInterface == null) {
            log.error("Plugin config interface does not exist for {}", nodeDefEntity.getServiceId());
            throw new WecubeCoreException("3158", "Plugin config interface does not exist.");
        }

        ctx.withPluginConfigInterface(pluginConfigInterface)//
                .withTaskNodeDefEntity(nodeDefEntity);

        if (PluginAsyncInvocationResultDto.RESULT_CODE_FAIL.equalsIgnoreCase(resultCode)) {
            handleErrorInvocationResult(resultMessage, resultData, ctx);
            return;
        }

        if (resultData == null) {
            handleNullResultData(ctx);
            return;
        }

        PluginInvocationResult result = new PluginInvocationResult()
                .parsePluginInvocationCommand(ctx.getPluginInvocationCommand());

        try {
            handleResultData(ctx, resultData);
            result.setResultCode(resultCode);
            pluginInvocationResultService.responsePluginInterfaceInvocation(result);
            handlePluginInterfaceInvocationSuccess(ctx);

            return;
        } catch (Exception e) {
            log.error("result data handling failed", e);
            result.setResultCode(RESULT_CODE_ERR);
            pluginInvocationResultService.responsePluginInterfaceInvocation(result);
            String errMsg = e.getMessage() == null ? "error" : trimWithMaxLength(e.getMessage());
            handlePluginInterfaceInvocationFailure(ctx, "5002", "result data handling failed:" + errMsg);
        }

        return;

    }

    private void handleNullResultData(PluginInterfaceInvocationContext ctx) {
        PluginInvocationResult result = new PluginInvocationResult()
                .parsePluginInvocationCommand(ctx.getPluginInvocationCommand());
        PluginConfigInterfaces pluginConfigInterface = ctx.getPluginConfigInterface();
        List<PluginConfigInterfaceParameters> outputParameters = pluginConfigInterface.getOutputParameters();

        if (outputParameters == null || outputParameters.isEmpty()) {
            log.debug("output parameter is NOT configured for interface {}", pluginConfigInterface.getServiceName());
            result.setResultCode(RESULT_CODE_OK);
            pluginInvocationResultService.responsePluginInterfaceInvocation(result);
            handlePluginInterfaceInvocationSuccess(ctx);
            return;
        }

        if (outputParameters != null && !outputParameters.isEmpty()) {
            List<TaskNodeExecParamEntity> inputParameters = taskNodeExecParamRepository
                    .selectAllByRequestIdAndParamType(ctx.getRequestId(), TaskNodeExecParamEntity.PARAM_TYPE_REQUEST);
            if (inputParameters == null || inputParameters.isEmpty()) {
                log.debug("output parameter is configured but INPUT is empty for interface {}",
                        pluginConfigInterface.getServiceName());
                result.setResultCode(RESULT_CODE_OK);
                pluginInvocationResultService.responsePluginInterfaceInvocation(result);
                handlePluginInterfaceInvocationSuccess(ctx);
                return;
            } else {
                log.error("output parameter is configured but result is empty for interface {}",
                        pluginConfigInterface.getServiceName());
                result.setResultCode(RESULT_CODE_ERR);
                pluginInvocationResultService.responsePluginInterfaceInvocation(result);
                handlePluginInterfaceInvocationFailure(ctx, "5003", "output is null");
                return;
            }
        }

        return;
    }

    private void handlePluginInterfaceInvocationFailure(PluginInterfaceInvocationContext ctx, String errorCode,
            String errorMsg) {

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

        taskNodeInstInfoRepository.updateByPrimaryKeySelective(nodeInstEntity);

    }

    private void handlePluginInterfaceInvocationSuccess(PluginInterfaceInvocationContext ctx) {
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

    private void handleResultData(PluginInterfaceInvocationContext ctx, List<Object> resultData) {

        List<Map<String, Object>> outputParameterMaps = validateAndCastResultData(resultData);
        storeOutputParameterMaps(ctx, outputParameterMaps);

        if (log.isInfoEnabled()) {
            log.info("about to process output parameters for {}", ctx.getPluginConfigInterface().getServiceName());
        }
        for (Map<String, Object> outputParameterMap : outputParameterMaps) {
            handleSingleOutputMap(ctx, outputParameterMap);
        }

        if (log.isInfoEnabled()) {
            log.info("finished processing {} output parameters for {}", outputParameterMaps.size(),
                    ctx.getPluginConfigInterface().getServiceName());
        }

        return;
    }

    private void handleSingleOutputMap(PluginInterfaceInvocationContext ctx, Map<String, Object> outputParameterMap) {

        PluginConfigInterfaces pci = ctx.getPluginConfigInterface();
        List<PluginConfigInterfaceParameters> outputParameters = pci.getOutputParameters();

        if (outputParameters == null) {
            return;
        }

        if (outputParameterMap == null || outputParameterMap.isEmpty()) {
            log.info("returned output is empty for request {}", ctx.getRequestId());
            return;
        }

        //TODO #2169 to support entity creation
        String nodeEntityId = (String) outputParameterMap.get(CALLBACK_PARAMETER_KEY);

        if (StringUtils.isBlank(nodeEntityId)) {
            log.info("none entity ID found in output for request {}", ctx.getRequestId());
            return;
        }

        String errorCodeOfSingleRecord = (String) outputParameterMap.get(PLUGIN_RESULT_CODE_PARTIALLY_KEY);
        if (StringUtils.isNotBlank(errorCodeOfSingleRecord)
                && PLUGIN_RESULT_CODE_PARTIALLY_FAIL.equalsIgnoreCase(errorCodeOfSingleRecord)) {
            log.info("such request is partially failed for request:{} and {}:{}", ctx.getRequestId(),
                    CALLBACK_PARAMETER_KEY, nodeEntityId);
            
            //TODO to store status
            return;
        }

        //TODO #2169 to support entity creation
        for (PluginConfigInterfaceParameters pciParam : outputParameters) {
            String paramName = pciParam.getName();
            String paramExpr = pciParam.getMappingEntityExpression();

            if (StringUtils.isBlank(paramExpr)) {
                log.info("expression not configured for {}", paramName);
                continue;
            }

            Object retVal = outputParameterMap.get(paramName);

            if (retVal == null) {
                log.info("returned value is null for {} {}", ctx.getRequestId(), paramName);
                continue;
            }

            EntityOperationRootCondition condition = new EntityOperationRootCondition(paramExpr, nodeEntityId);

            try {
                this.entityOperationService.update(condition, retVal, null);
            } catch (Exception e) {
                log.warn("Exceptions to update entity.", e);
            }

        }
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
            Map<String, Object> outputParameterMap, String objectId) {

        String entityTypeId = null;
        String entityDataId = null;
        String fullEntityDataId = null;

        String requestId = ctx.getTaskNodeExecRequestEntity().getReqId();

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

        if (callbackParameterInputEntity != null) {
            objectId = callbackParameterInputEntity.getObjId();
            entityTypeId = callbackParameterInputEntity.getEntityTypeId();
            entityDataId = callbackParameterInputEntity.getEntityDataId();
            fullEntityDataId = callbackParameterInputEntity.getFullEntityDataId();
        }

        List<PluginConfigInterfaceParameters> outputParameters = ctx.getPluginConfigInterface().getOutputParameters();

        for (Map.Entry<String, Object> entry : outputParameterMap.entrySet()) {

            PluginConfigInterfaceParameters p = findPreConfiguredPluginConfigInterfaceParameter(outputParameters,
                    entry.getKey());

            String paramDataType = null;
            boolean isSensitiveData = false;
            if (p == null) {
                paramDataType = DATA_TYPE_STRING;
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
            paramEntity.setFullEntityDataId(fullEntityDataId);
            paramEntity.setObjId(objectId);
            paramEntity.setParamType(TaskNodeExecParamEntity.PARAM_TYPE_RESPONSE);
            paramEntity.setParamName(entry.getKey());
            paramEntity.setParamDataType(paramDataType);
            paramEntity.setParamDataValue(paramDataValue);
            paramEntity.setReqId(requestId);
            paramEntity.setCreatedBy(WorkflowConstants.DEFAULT_USER);
            paramEntity.setCreatedTime(new Date());

            taskNodeExecParamRepository.insert(paramEntity);
        }
    }

    private void handleErrorInvocationResult(String errorMsg, List<Object> resultData,
            PluginInterfaceInvocationContext ctx) {

        if (resultData != null) {
            handleResultData(ctx, resultData);
        }

        PluginInvocationResult result = new PluginInvocationResult()
                .parsePluginInvocationCommand(ctx.getPluginInvocationCommand());

        log.error("system errors:{}", errorMsg);
        result.setResultCode(RESULT_CODE_ERR);
        pluginInvocationResultService.responsePluginInterfaceInvocation(result);
        handlePluginAsyncInvocationFailure(ctx, "5001", "system errors:" + trimWithMaxLength(errorMsg));

        return;
    }

    private void handlePluginAsyncInvocationFailure(PluginInterfaceInvocationContext ctx, String errorCode,
            String errorMsg) {

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

    private void validate(PluginAsyncInvocationResultDto asyncResultDto) {
        if (asyncResultDto == null) {
            throw new WecubeCoreException("3159", "asynchonous result cannot be null.");
        }

        if (StringUtils.isBlank(asyncResultDto.getResultCode())) {
            throw new WecubeCoreException("3160", "result code cannot be blank.");
        }

        if (asyncResultDto.getResultData() == null) {
            throw new WecubeCoreException("3161", "result data cannot be null.");
        }

        if (StringUtils.isBlank(asyncResultDto.getResultData().getRequestId())) {
            throw new WecubeCoreException("3162", "request id is blank.");
        }
    }
}
