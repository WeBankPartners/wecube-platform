package com.webank.wecube.platform.core.service;

import static com.webank.wecube.platform.core.utils.Constants.CALLBACK_PARAMETER_KEY;
import static com.webank.wecube.platform.core.utils.Constants.DATA_TYPE_NUMBER;
import static com.webank.wecube.platform.core.utils.Constants.DATA_TYPE_STRING;
import static com.webank.wecube.platform.core.utils.Constants.FIELD_REQUIRED;
import static com.webank.wecube.platform.core.utils.Constants.MAPPING_TYPE_CONTEXT;
import static com.webank.wecube.platform.core.utils.Constants.MAPPING_TYPE_ENTITY;
import static com.webank.wecube.platform.core.utils.Constants.MAPPING_TYPE_SYSTEM_VARIABLE;
import static com.webank.wecube.platform.core.utils.Constants.RESULT_CODE_ERROR;
import static com.webank.wecube.platform.core.utils.Constants.RESULT_CODE_OK;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.BatchExecutionJob;
import com.webank.wecube.platform.core.domain.ExecutionJob;
import com.webank.wecube.platform.core.domain.ExecutionJobParameter;
import com.webank.wecube.platform.core.domain.SystemVariable;
import com.webank.wecube.platform.core.domain.plugin.PluginConfigInterface;
import com.webank.wecube.platform.core.domain.plugin.PluginConfigInterfaceParameter;
import com.webank.wecube.platform.core.domain.plugin.PluginInstance;
import com.webank.wecube.platform.core.dto.BatchExecutionRequestDto;
import com.webank.wecube.platform.core.dto.ExecutionJobResponseDto;
import com.webank.wecube.platform.core.dto.InputParameterDefinition;
import com.webank.wecube.platform.core.jpa.BatchExecutionJobRepository;
import com.webank.wecube.platform.core.jpa.PluginConfigInterfaceRepository;
import com.webank.wecube.platform.core.service.dme.EntityOperationRootCondition;
import com.webank.wecube.platform.core.service.dme.StandardEntityOperationService;
import com.webank.wecube.platform.core.service.plugin.PluginInstanceService;
import com.webank.wecube.platform.core.support.plugin.PluginServiceStub;
import com.webank.wecube.platform.core.support.plugin.dto.PluginResponse.ResultData;
import com.webank.wecube.platform.core.support.plugin.dto.PluginResponseStationaryOutput;
import com.webank.wecube.platform.core.utils.JsonUtils;

@Service
@Transactional
public class BatchExecutionService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private PluginServiceStub pluginServiceStub;

    @Autowired
    protected PluginInstanceService pluginInstanceService;

    @Autowired
    private SystemVariableService systemVariableService;
    @Autowired
    private BatchExecutionJobRepository batchExecutionJobRepository;
    @Autowired
    private PluginConfigInterfaceRepository pluginConfigInterfaceRepository;
    @Autowired
    protected StandardEntityOperationService standardEntityOperationService;
    
    private ObjectMapper objectMapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);

    public Map<String, ExecutionJobResponseDto> handleBatchExecutionJob(BatchExecutionRequestDto batchExecutionRequest)
            throws IOException {
        checkParameters(batchExecutionRequest.getInputParameterDefinitions());
        BatchExecutionJob batchExecutionJob = saveToDb(batchExecutionRequest);

        Map<String, ExecutionJobResponseDto> executionResults = new HashMap<>();
        for (ExecutionJob job : batchExecutionJob.getJobs()) {
            ResultData<?> executionResult = runExecutionJob(job);
            Object resultObject = executionResult.getOutputs().get(0);
            executionResults.put(job.getBusinessKey(), new ExecutionJobResponseDto(
                    job.getErrorCode() == null ? RESULT_CODE_ERROR : job.getErrorCode(), resultObject));
        }

        completeBatchExecutionJob(batchExecutionJob);
        return executionResults;
    }

    private void checkParameters(List<InputParameterDefinition> inputParameterDefinitions) {
        inputParameterDefinitions.forEach(inputParameterDefinition -> {
            PluginConfigInterfaceParameter inputParameter = inputParameterDefinition.getInputParameter();
            if (inputParameter.getRequired().equals(FIELD_REQUIRED)
                    && inputParameter.getMappingType().equals(MAPPING_TYPE_CONTEXT)) {
                throw new WecubeCoreException(String.format(
                        "Batch execution job does not support input parameter[%s] with [mappingType=%s] and [required=%s]",
                        inputParameter.getName(), inputParameter.getMappingType(), inputParameter.getRequired()));
            }
        });
    }

    private BatchExecutionJob saveToDb(BatchExecutionRequestDto batchExecutionRequest) {
        BatchExecutionJob batchExecutionJob = new BatchExecutionJob();
        List<ExecutionJob> executionJobs = new ArrayList<ExecutionJob>();
        batchExecutionJob.setJobs(executionJobs);
        batchExecutionRequest.getResourceDatas().forEach(resourceData -> {
            ExecutionJob executionJob = new ExecutionJob(resourceData.getId(),
                    batchExecutionRequest.getPluginConfigInterface().getId(), batchExecutionRequest.getPackageName(),
                    batchExecutionRequest.getEntityName(), resourceData.getBusinessKeyValue().toString());
            executionJob.setParameters(transFromInputParameterDefinitionToExecutionJobParameter(
                    batchExecutionRequest.getInputParameterDefinitions(), executionJob));
            executionJob.setBatchExecutionJob(batchExecutionJob);
            executionJobs.add(executionJob);
        });
        return batchExecutionJobRepository.save(batchExecutionJob);
    }

    private void completeBatchExecutionJob(BatchExecutionJob batchExecutionJob) {
        batchExecutionJob.setCompleteTimestamp(new Timestamp(System.currentTimeMillis()));
        batchExecutionJobRepository.save(batchExecutionJob);
    }

    private List<ExecutionJobParameter> transFromInputParameterDefinitionToExecutionJobParameter(
            List<InputParameterDefinition> inputParameterDefinitions, ExecutionJob executionJob) {
        List<ExecutionJobParameter> executionJobParameters = new ArrayList<ExecutionJobParameter>();
        inputParameterDefinitions.forEach(inputParameterDefinition -> {
            PluginConfigInterfaceParameter interfaceParameter = inputParameterDefinition.getInputParameter();

            if (null != inputParameterDefinition.getInputParameterValue()) {
                ExecutionJobParameter executionJobParameter = new ExecutionJobParameter(interfaceParameter.getName(),
                        interfaceParameter.getDataType(), interfaceParameter.getMappingType(),
                        interfaceParameter.getMappingEntityExpression(),
                        interfaceParameter.getMappingSystemVariableName(), interfaceParameter.getRequired(),
                        inputParameterDefinition.getInputParameterValue().toString());
                executionJobParameter.setExecutionJob(executionJob);
                executionJobParameters.add(executionJobParameter);
            } else {
                ExecutionJobParameter executionJobParameter = new ExecutionJobParameter(interfaceParameter.getName(),
                        interfaceParameter.getDataType(), interfaceParameter.getMappingType(),
                        interfaceParameter.getMappingEntityExpression(),
                        interfaceParameter.getMappingSystemVariableName(), interfaceParameter.getRequired(), null);
                executionJobParameter.setExecutionJob(executionJob);
                executionJobParameters.add(executionJobParameter);
            }
        });
        return executionJobParameters;
    }

    public ResultData<?> runExecutionJob(ExecutionJob executionJob) throws IOException {
        if (log.isInfoEnabled()) {
            log.info("run batch execution with:{}", executionJob);
        }
        String errorMessage;
        prepareInputParameterValues(executionJob);

        Map<String, Object> callInterfaceParameterMap = new HashMap<String, Object>();

        for (ExecutionJobParameter parameter : executionJob.getParameters()) {
            if (DATA_TYPE_STRING.equals(parameter.getDataType())
                    || MAPPING_TYPE_SYSTEM_VARIABLE.equals(parameter.getMappingEntityExpression())) {
                callInterfaceParameterMap.put(parameter.getName(), parameter.getValue());
            }
            if (DATA_TYPE_NUMBER.equals(parameter.getDataType())) {
                callInterfaceParameterMap.put(parameter.getName(), Integer.valueOf(parameter.getValue()));
            }
        }

        callInterfaceParameterMap.put(CALLBACK_PARAMETER_KEY, executionJob.getRootEntityId());
        Optional<PluginConfigInterface> pluginConfigInterfaceOptional = pluginConfigInterfaceRepository
                .findById(executionJob.getPluginConfigInterfaceId());
        if (!pluginConfigInterfaceOptional.isPresent()) {
            errorMessage = String.format("Can not found plugin config interface[%s]",
                    executionJob.getPluginConfigInterfaceId());
            log.error(errorMessage);
            executionJob.setErrorWithMessage(errorMessage);

            return buildResultDataWithError(errorMessage);
        }

        PluginConfigInterface pluginConfigInterface = pluginConfigInterfaceOptional.get();

        PluginInstance pluginInstance = pluginInstanceService
                .getRunningPluginInstance(pluginConfigInterface.getPluginConfig().getPluginPackage().getName());
        ResultData<Object> responseData = new ResultData<Object>();
        try {
            responseData = pluginServiceStub.callPluginInterface(
                    String.format("%s:%s", pluginInstance.getHost(), pluginInstance.getPort()),
                    pluginConfigInterface.getPath(), Lists.newArrayList(callInterfaceParameterMap),
                    "RequestId-" + Long.toString(System.currentTimeMillis()));
        } catch (Exception e) {
            errorMessage = e.getMessage();
            log.error(errorMessage);
            executionJob.setErrorWithMessage(errorMessage);
            return buildResultDataWithError(errorMessage);
        }
        log.info("returnJsonString= " + responseData.toString());
        String returnJsonString = JsonUtils.toJsonString(responseData);
        log.info("returnJsonString= " + returnJsonString);
        ResultData<PluginResponseStationaryOutput> stationaryResultData = objectMapper.readValue(returnJsonString, new TypeReference<ResultData<PluginResponseStationaryOutput>>() {
        });

        if (stationaryResultData.getOutputs().size() == 0) {
            errorMessage = String.format("Call interface[%s][%s:%s%s] with parameters[%s] has no response",
                    executionJob.getPluginConfigInterfaceId(), pluginInstance.getHost(), pluginInstance.getPort(),
                    pluginConfigInterface.getPath(), callInterfaceParameterMap);
            log.error(errorMessage);
            executionJob.setErrorWithMessage(errorMessage);
            return buildResultDataWithError(errorMessage);
        }
        PluginResponseStationaryOutput stationaryOutput = stationaryResultData.getOutputs().get(0);
        executionJob.setReturnJson(returnJsonString);
        executionJob.setErrorCode(stationaryOutput.getErrorCode() == null ? RESULT_CODE_ERROR : RESULT_CODE_OK);
        executionJob.setErrorMessage(stationaryOutput.getErrorMessage());
        return responseData;
    }

    private ResultData<PluginResponseStationaryOutput> buildResultDataWithError(String errorMessage) {
        ResultData<PluginResponseStationaryOutput> errorReultData = new ResultData<PluginResponseStationaryOutput>();
        errorReultData.setOutputs(Lists.newArrayList(new PluginResponseStationaryOutput(
                PluginResponseStationaryOutput.ERROR_CODE_FAILED, errorMessage, null)));
        return errorReultData;
    }

    private void prepareInputParameterValues(ExecutionJob executionJob) {
        String errorMessage;

        for (ExecutionJobParameter parameter : executionJob.getParameters()) {
            String mappingType = parameter.getMappingType();
            if (MAPPING_TYPE_ENTITY.equals(mappingType)) {
                String mappingEntityExpression = parameter.getMappingEntityExpression();
                if (log.isDebugEnabled()) {
                    log.debug("expression:{}", mappingEntityExpression);
                }

                EntityOperationRootCondition criteria = new EntityOperationRootCondition(mappingEntityExpression,
                        executionJob.getRootEntityId());

                List<Object> attrValsPerExpr = standardEntityOperationService.queryAttributeValues(criteria);

                if ((attrValsPerExpr == null || attrValsPerExpr.size() == 0)
                        && FIELD_REQUIRED.equals(parameter.getRequired())) {
                    errorMessage = String.format(
                            "returned empty data while fetch the mandatory input parameter[%s] with expression[%s] and root entity ID[%s]",
                            parameter.getName(), mappingEntityExpression, criteria.getEntityIdentity());
                    log.error(errorMessage);
                    executionJob.setErrorWithMessage(errorMessage);
                    break;
                }
                parameter.setValue(attrValsPerExpr.get(0).toString());
            }

            if (MAPPING_TYPE_SYSTEM_VARIABLE.equals(mappingType)) {
                SystemVariable sVariable = systemVariableService.getSystemVariableByPackageNameAndName(
                        executionJob.getPackageName(), parameter.getMappingSystemVariableName());

                if (sVariable == null && FIELD_REQUIRED.equals(parameter.getRequired())) {
                    errorMessage = String.format("variable is null but is mandatory for %s", parameter.getName());
                    log.error(errorMessage);
                    executionJob.setErrorWithMessage(errorMessage);
                    return;
                }

                String sVal = sVariable.getValue();
                if (StringUtils.isBlank(sVal)) {
                    sVal = sVariable.getDefaultValue();
                }

                if (StringUtils.isBlank(sVal) && FIELD_REQUIRED.equals(parameter.getRequired())) {
                    errorMessage = String.format("variable is null but is mandatory for %s", parameter.getName());
                    log.error(errorMessage);
                    executionJob.setErrorWithMessage(errorMessage);
                    return;
                }
                parameter.setValue(sVal);
            }
        }
        return;
    }
}
