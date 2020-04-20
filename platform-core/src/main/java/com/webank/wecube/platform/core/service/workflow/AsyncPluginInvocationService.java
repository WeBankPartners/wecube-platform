package com.webank.wecube.platform.core.service.workflow;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.plugin.PluginConfigInterface;
import com.webank.wecube.platform.core.domain.plugin.PluginConfigInterfaceParameter;
import com.webank.wecube.platform.core.dto.workflow.PluginAsyncInvocationResultDto;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeDefInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeExecParamEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeExecRequestEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeInstInfoEntity;
import com.webank.wecube.platform.core.model.workflow.PluginInvocationCommand;
import com.webank.wecube.platform.core.model.workflow.PluginInvocationResult;
import com.webank.wecube.platform.core.service.dme.EntityOperationRootCondition;
import com.webank.wecube.platform.core.service.workflow.PluginInvocationProcessor.PluginInterfaceInvocationContext;

@Service
public class AsyncPluginInvocationService extends AbstractPluginInvocationService {

    public void handleAsyncInvocationResult(PluginAsyncInvocationResultDto asyncResultDto) {
        validate(asyncResultDto);

        String resultCode = asyncResultDto.getResultCode();
        String resultMessage = asyncResultDto.getResultMessage();
        String requestId = asyncResultDto.getResultData().getRequestId();

        List<Object> outputs = asyncResultDto.getOutputs();

        TaskNodeExecRequestEntity reqEntity = taskNodeExecRequestRepository.findOneByRequestId(requestId);

        if (reqEntity == null) {
            log.error("request ID is NOT available:{}", requestId);
            throw new WecubeCoreException("Request id is invalid.");
        }

        if (reqEntity.isCompleted() || !reqEntity.isCurrent()) {
            log.error("request state is not expected,completed:{},current:{}", reqEntity.isCompleted(),
                    reqEntity.isCurrent());
            throw new WecubeCoreException("Request state is not expected");
        }

        Integer nodeInstId = reqEntity.getNodeInstId();

        Optional<TaskNodeInstInfoEntity> taskNodeInstEntityOpt = taskNodeInstInfoRepository.findById(nodeInstId);

        if (!taskNodeInstEntityOpt.isPresent()) {
            log.error("task node instance does not exist for {} {}", nodeInstId, requestId);
            throw new WecubeCoreException("Task node instance does not exist.");
        }

        TaskNodeInstInfoEntity taskNodeInstEntity = taskNodeInstEntityOpt.get();
        if (!TaskNodeInstInfoEntity.IN_PROGRESS_STATUS.equals(taskNodeInstEntity.getStatus())) {
            log.error("task node instance status is not valid, expected {} but {}",
                    TaskNodeInstInfoEntity.IN_PROGRESS_STATUS, taskNodeInstEntity.getStatus());
            throw new WecubeCoreException("Task node instance status is not valid.");
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
        cmd.setProcDefVersion(e.getProcDefVersion());
        cmd.setProcInstKey(e.getProcInstKernelKey());
        cmd.setProcInstId(e.getProcInstKernelId());
        cmd.setNodeId(e.getNodeId());
        cmd.setNodeName(e.getNodeName());
        cmd.setExecutionId(e.getExecutionId());

        return cmd;
    }

    protected void doHandleAsyncInvocationResult(String resultCode, String resultMessage, List<Object> resultData,
            PluginInterfaceInvocationContext ctx) {
        
        Optional<TaskNodeDefInfoEntity> nodeDefEntityOpt = taskNodeDefInfoRepository
                .findById(ctx.getTaskNodeInstEntity().getNodeDefId());
        if (!nodeDefEntityOpt.isPresent()) {
            log.error("such task node definition does not exist for {} {}", ctx.getTaskNodeInstEntity().getNodeDefId(),
                    ctx.getRequestId());
            throw new WecubeCoreException("Task node definition does not exist.");
        }

        TaskNodeDefInfoEntity nodeDefEntity = nodeDefEntityOpt.get();

        PluginConfigInterface pluginConfigInterface = pluginConfigService
                .getPluginConfigInterfaceByServiceName(nodeDefEntity.getServiceId());

        if (pluginConfigInterface == null) {
            log.error("Plugin config interface does not exist for {}", nodeDefEntity.getServiceId());
            throw new WecubeCoreException("Plugin config interface does not exist.");
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
            result.setResultCode(Integer.parseInt(resultCode));
            pluginInvocationResultService.responsePluginInterfaceInvocation(result);
            handlePluginInterfaceInvocationSuccess(ctx);

            return;
        } catch (Exception e) {
            log.error("result data handling failed", e);
            result.setResultCode(RESULT_CODE_ERR);
            pluginInvocationResultService.responsePluginInterfaceInvocation(result);
            String errMsg = e.getMessage() == null ? "error" : trimWithMaxLength(e.getMessage());
            handlePluginInterfaceInvocationFailure(ctx, "101", "result data handling failed:" + errMsg);
        }

        return;

    }

    private void handleNullResultData(PluginInterfaceInvocationContext ctx) {
        PluginInvocationResult result = new PluginInvocationResult()
                .parsePluginInvocationCommand(ctx.getPluginInvocationCommand());
        PluginConfigInterface pluginConfigInterface = ctx.getPluginConfigInterface();
        Set<PluginConfigInterfaceParameter> outputParameters = pluginConfigInterface.getOutputParameters();

        if (outputParameters == null || outputParameters.isEmpty()) {
            log.debug("output parameter is NOT configured for interface {}", pluginConfigInterface.getServiceName());
            result.setResultCode(RESULT_CODE_OK);
            pluginInvocationResultService.responsePluginInterfaceInvocation(result);
            handlePluginInterfaceInvocationSuccess(ctx);
            return;
        }

        if (outputParameters != null && !outputParameters.isEmpty()) {
            List<TaskNodeExecParamEntity> inputParameters = taskNodeExecParamRepository
                    .findAllByRequestIdAndParamType(ctx.getRequestId(), TaskNodeExecParamEntity.PARAM_TYPE_REQUEST);
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
                handlePluginInterfaceInvocationFailure(ctx, "100", "output is null");
                return;
            }
        }

        return;
    }

    private void handlePluginInterfaceInvocationFailure(PluginInterfaceInvocationContext ctx, String errorCode,
            String errorMsg) {

        Date now = new Date();
        TaskNodeExecRequestEntity requestEntity = ctx.getTaskNodeExecRequestEntity();
        Optional<TaskNodeExecRequestEntity> requestEntityOpt = taskNodeExecRequestRepository
                .findById(requestEntity.getRequestId());

        if (!requestEntityOpt.isPresent()) {
            int round = 0;
            while (round < 10) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }

                requestEntityOpt = taskNodeExecRequestRepository.findById(requestEntity.getRequestId());

                if (requestEntityOpt.isPresent()) {
                    break;
                }

                round++;
            }
        }

        if (!requestEntityOpt.isPresent()) {
            log.warn("request entity does not exist for {}", requestEntity.getRequestId());

        } else {
            requestEntity = requestEntityOpt.get();
            requestEntity.setUpdatedTime(now);
            requestEntity.setErrorCode(errorCode);
            requestEntity.setErrorMessage(errorMsg);
            requestEntity.setCompleted(true);

            taskNodeExecRequestRepository.save(requestEntity);
        }

        TaskNodeInstInfoEntity nodeInstEntity = ctx.getTaskNodeInstEntity();
        Optional<TaskNodeInstInfoEntity> nodeInstEntityOpt = taskNodeInstInfoRepository
                .findById(nodeInstEntity.getId());

        if (!nodeInstEntityOpt.isPresent()) {
            int round = 0;
            while (round < 10) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }

                nodeInstEntityOpt = taskNodeInstInfoRepository.findById(nodeInstEntity.getId());

                if (nodeInstEntityOpt.isPresent()) {
                    break;
                }

                round++;
            }
        }

        if (!nodeInstEntityOpt.isPresent()) {
            log.warn("task node instance entity does not exist for {}", nodeInstEntity.getId());
        } else {
            nodeInstEntity = nodeInstEntityOpt.get();
            nodeInstEntity.setUpdatedTime(now);
            nodeInstEntity.setStatus(TaskNodeInstInfoEntity.FAULTED_STATUS);

            taskNodeInstInfoRepository.save(nodeInstEntity);
        }

    }

    private void handlePluginInterfaceInvocationSuccess(PluginInterfaceInvocationContext ctx) {
        Date now = new Date();
        TaskNodeExecRequestEntity requestEntity = ctx.getTaskNodeExecRequestEntity();
        Optional<TaskNodeExecRequestEntity> requestEntityOpt = taskNodeExecRequestRepository
                .findById(requestEntity.getRequestId());

        if (!requestEntityOpt.isPresent()) {
            log.warn("request entity does not exist for {}", requestEntity.getRequestId());

        } else {
            requestEntity = requestEntityOpt.get();
            requestEntity.setUpdatedTime(now);
            requestEntity.setCompleted(true);

            taskNodeExecRequestRepository.save(requestEntity);
        }

        TaskNodeInstInfoEntity nodeInstEntity = ctx.getTaskNodeInstEntity();
        Optional<TaskNodeInstInfoEntity> nodeInstEntityOpt = taskNodeInstInfoRepository
                .findById(nodeInstEntity.getId());

        if (!nodeInstEntityOpt.isPresent()) {
            log.warn("task node instance entity does not exist for {}", nodeInstEntity.getId());
        } else {
            nodeInstEntity = nodeInstEntityOpt.get();
            nodeInstEntity.setUpdatedTime(now);
            nodeInstEntity.setStatus(TaskNodeInstInfoEntity.COMPLETED_STATUS);
            nodeInstEntity.setErrorMessage("");

            taskNodeInstInfoRepository.save(nodeInstEntity);
        }
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

        PluginConfigInterface pci = ctx.getPluginConfigInterface();
        Set<PluginConfigInterfaceParameter> outputParameters = pci.getOutputParameters();

        if (outputParameters == null) {
            return;
        }

        if (outputParameterMap == null || outputParameterMap.isEmpty()) {
            log.info("returned output is empty for request {}", ctx.getRequestId());
            return;
        }

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
            return;
        }

        for (PluginConfigInterfaceParameter pciParam : outputParameters) {
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
            	this.entityOperationService.update(condition, retVal);
            }catch(Exception e) {
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

        String requestId = ctx.getTaskNodeExecRequestEntity().getRequestId();

        String callbackParameter = (String) outputParameterMap.get(CALLBACK_PARAMETER_KEY);
        TaskNodeExecParamEntity callbackParameterInputEntity = null;
        if (StringUtils.isNotBlank(callbackParameter)) {
            callbackParameterInputEntity = taskNodeExecParamRepository
                    .findOneByRequestIdAndParamTypeAndParamNameAndValue(requestId,
                            TaskNodeExecParamEntity.PARAM_TYPE_REQUEST, CALLBACK_PARAMETER_KEY, callbackParameter);
        }

        if (callbackParameterInputEntity != null) {
            objectId = callbackParameterInputEntity.getObjectId();
            entityTypeId = callbackParameterInputEntity.getEntityTypeId();
            entityDataId = callbackParameterInputEntity.getEntityDataId();
        }

        Set<PluginConfigInterfaceParameter> outputParameters = ctx.getPluginConfigInterface().getOutputParameters();

        for (Map.Entry<String, Object> entry : outputParameterMap.entrySet()) {

            PluginConfigInterfaceParameter p = findPreConfiguredPluginConfigInterfaceParameter(outputParameters,
                    entry.getKey());

            String paramDataType = null;
            if (p == null) {
                paramDataType = DATA_TYPE_STRING;
            } else {
                paramDataType = p.getDataType();
            }

            TaskNodeExecParamEntity paramEntity = new TaskNodeExecParamEntity();
            paramEntity.setEntityTypeId(entityTypeId);
            paramEntity.setEntityDataId(entityDataId);
            paramEntity.setObjectId(objectId);
            paramEntity.setParamType(TaskNodeExecParamEntity.PARAM_TYPE_RESPONSE);
            paramEntity.setParamName(entry.getKey());
            paramEntity.setParamDataType(paramDataType);
            paramEntity.setParamDataValue(
                    trimExceedParamValue(asString(entry.getValue(), paramDataType), MAX_PARAM_VAL_SIZE));
            paramEntity.setRequestId(requestId);

            taskNodeExecParamRepository.save(paramEntity);
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
        handlePluginAsyncInvocationFailure(ctx, "500", "system errors:" + trimWithMaxLength(errorMsg));

        return;
    }

    private void handlePluginAsyncInvocationFailure(PluginInterfaceInvocationContext ctx, String errorCode,
            String errorMsg) {

        Date now = new Date();
        TaskNodeExecRequestEntity requestEntity = ctx.getTaskNodeExecRequestEntity();
        Optional<TaskNodeExecRequestEntity> requestEntityOpt = taskNodeExecRequestRepository
                .findById(requestEntity.getRequestId());

        if (!requestEntityOpt.isPresent()) {
            int round = 0;
            while (round < 10) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }

                requestEntityOpt = taskNodeExecRequestRepository.findById(requestEntity.getRequestId());

                if (requestEntityOpt.isPresent()) {
                    break;
                }

                round++;
            }
        }

        if (!requestEntityOpt.isPresent()) {
            log.warn("request entity does not exist for {}", requestEntity.getRequestId());

        } else {
            requestEntity = requestEntityOpt.get();
            requestEntity.setUpdatedTime(now);
            requestEntity.setErrorCode(errorCode);
            requestEntity.setErrorMessage(errorMsg);
            requestEntity.setCompleted(true);

            taskNodeExecRequestRepository.save(requestEntity);
        }

        TaskNodeInstInfoEntity nodeInstEntity = ctx.getTaskNodeInstEntity();
        Optional<TaskNodeInstInfoEntity> nodeInstEntityOpt = taskNodeInstInfoRepository
                .findById(nodeInstEntity.getId());

        if (!nodeInstEntityOpt.isPresent()) {
            int round = 0;
            while (round < 10) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }

                nodeInstEntityOpt = taskNodeInstInfoRepository.findById(nodeInstEntity.getId());

                if (nodeInstEntityOpt.isPresent()) {
                    break;
                }

                round++;
            }
        }

        if (!nodeInstEntityOpt.isPresent()) {
            log.warn("task node instance entity does not exist for {}", nodeInstEntity.getId());
        } else {
            nodeInstEntity = nodeInstEntityOpt.get();
            nodeInstEntity.setUpdatedTime(now);
            nodeInstEntity.setStatus(TaskNodeInstInfoEntity.FAULTED_STATUS);
            nodeInstEntity.setErrorMessage(errorMsg);

            taskNodeInstInfoRepository.save(nodeInstEntity);
        }

    }

    private void validate(PluginAsyncInvocationResultDto asyncResultDto) {
        if (asyncResultDto == null) {
            throw new WecubeCoreException("asynchonous result cannot be null.");
        }

        if (StringUtils.isBlank(asyncResultDto.getResultCode())) {
            throw new WecubeCoreException("result code cannot be blank.");
        }

        if (asyncResultDto.getResultData() == null) {
            throw new WecubeCoreException("result data cannot be null.");
        }

        if (StringUtils.isBlank(asyncResultDto.getResultData().getRequestId())) {
            throw new WecubeCoreException("request id is blank.");
        }
    }
}
