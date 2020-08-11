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
        verifyParameters(batchExecutionRequest.getInputParameterDefinitions());
        BatchExecutionJob batchExeJob = saveToDb(batchExecutionRequest);

        Map<String, ExecutionJobResponseDto> exeResults = new HashMap<>();
        for (ExecutionJob exeJob : batchExeJob.getJobs()) {
            ResultData<?> exeResult = null;
            try {
                exeResult = performExecutionJob(exeJob);
                if (exeResult == null) {
                    if (exeJob.getPrepareException() != null) {
                        exeResult = buildResultDataWithError(exeJob.getPrepareException().getMessage());
                        Object resultObject = exeResult.getOutputs().get(0);
                        ExecutionJobResponseDto respDataObj = new ExecutionJobResponseDto(RESULT_CODE_ERROR,
                                resultObject);
                        exeResults.put(exeJob.getBusinessKey(), respDataObj);
                    }
                } else {
                    Object resultObject = exeResult.getOutputs().get(0);
                    String errorCode = exeJob.getErrorCode() == null ? RESULT_CODE_ERROR : exeJob.getErrorCode();
                    ExecutionJobResponseDto respDataObj = new ExecutionJobResponseDto(errorCode, resultObject);
                    exeResults.put(exeJob.getBusinessKey(), respDataObj);
                }
            } catch (Exception e) {
                log.error("errors to run execution job,{} {} {}, errorMsg:{} ", exeJob.getPackageName(),
                        exeJob.getEntityName(), exeJob.getRootEntityId(), e.getMessage());
                exeResult = buildResultDataWithError(e.getMessage());
                Object resultObject = exeResult.getOutputs().get(0);
                ExecutionJobResponseDto respDataObj = new ExecutionJobResponseDto(RESULT_CODE_ERROR, resultObject);
                log.info("biz key:{}, respDataObj:{}", exeJob.getBusinessKey(), respDataObj);
                exeResults.put(exeJob.getBusinessKey(), respDataObj);
            }

        }

        try {
            postProcessBatchExecutionJob(batchExeJob);
        } catch (Exception e) {
            log.error("errors while post processing batch execution job", e);
        }
        return exeResults;
    }

    private void verifyParameters(List<InputParameterDefinition> inputParameterDefinitions) {
        inputParameterDefinitions.forEach(inputParameterDefinition -> {
            PluginConfigInterfaceParameter inputParameter = inputParameterDefinition.getInputParameter();
            if (FIELD_REQUIRED.equalsIgnoreCase(inputParameter.getRequired())
                    && MAPPING_TYPE_CONTEXT.equalsIgnoreCase(inputParameter.getMappingType())) {
                String msg = String.format(
                        "Batch execution job does not support input parameter[%s] with [mappingType=%s] and [required=%s]",
                        inputParameter.getName(), inputParameter.getMappingType(), inputParameter.getRequired());
                throw new WecubeCoreException("3001", msg, inputParameter.getName(), inputParameter.getMappingType(),
                        inputParameter.getRequired());
            }
        });
    }

    private BatchExecutionJob saveToDb(BatchExecutionRequestDto batchExeRequest) {
        BatchExecutionJob batchExeJob = new BatchExecutionJob();
        List<ExecutionJob> exeJobs = new ArrayList<ExecutionJob>();
        batchExeRequest.getResourceDatas().forEach(resourceData -> {

            ExecutionJob exeJob = new ExecutionJob();
            exeJob.setRootEntityId(resourceData.getId());
            exeJob.setPluginConfigInterfaceId(batchExeRequest.getPluginConfigInterface().getId());
            exeJob.setPackageName(batchExeRequest.getPackageName());
            exeJob.setEntityName(batchExeRequest.getEntityName());
            exeJob.setBusinessKey(resourceData.getBusinessKeyValue().toString());
            exeJob.setParameters(transFromInputParameterDefinitionToExecutionJobParameter(
                    batchExeRequest.getInputParameterDefinitions(), exeJob));
            exeJob.setBatchExecutionJob(batchExeJob);
            exeJobs.add(exeJob);
        });
        batchExeJob.setJobs(exeJobs);
        return batchExecutionJobRepository.save(batchExeJob);
    }

    private void postProcessBatchExecutionJob(BatchExecutionJob batchExeJob) {
        batchExeJob.setCompleteTimestamp(new Timestamp(System.currentTimeMillis()));
        batchExecutionJobRepository.save(batchExeJob);
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

    public ResultData<?> performExecutionJob(ExecutionJob exeJob) throws IOException {
        if (exeJob == null) {
            throw new WecubeCoreException("3002", "execution job as input argument cannot be null.");
        }
        if (log.isInfoEnabled()) {
            log.info("perform batch execution job:{} {} {}", exeJob.getPackageName(), exeJob.getEntityName(),
                    exeJob.getRootEntityId());
        }

        Optional<PluginConfigInterface> pluginConfigInterfaceOptional = pluginConfigInterfaceRepository
                .findById(exeJob.getPluginConfigInterfaceId());
        if (!pluginConfigInterfaceOptional.isPresent()) {
            String errorMessage = String.format("Can not found plugin config interface[%s]",
                    exeJob.getPluginConfigInterfaceId());
            log.error(errorMessage);
            exeJob.setErrorWithMessage(errorMessage);

            return buildResultDataWithError(errorMessage);
        }

        PluginConfigInterface pluginConfigInterface = pluginConfigInterfaceOptional.get();

        tryPrepareInputParamValues(exeJob, pluginConfigInterface);

        if (exeJob.getPrepareException() != null) {
            log.error("Errors to calculate input parameters", exeJob.getPrepareException());
            throw new WecubeCoreException("3003",
                    "Failed to prepare input parameter due to error:" + exeJob.getPrepareException().getMessage(),
                    exeJob.getPrepareException().getMessage());
        }

        Map<String, Object> pluginInputParamMap = new HashMap<String, Object>();

        for (ExecutionJobParameter parameter : exeJob.getParameters()) {
            if (DATA_TYPE_STRING.equals(parameter.getDataType())
                    || MAPPING_TYPE_SYSTEM_VARIABLE.equals(parameter.getMappingEntityExpression())) {
                pluginInputParamMap.put(parameter.getName(), parameter.getValue());
            }
            if (DATA_TYPE_NUMBER.equals(parameter.getDataType())) {
                pluginInputParamMap.put(parameter.getName(), Integer.valueOf(parameter.getValue()));
            }
        }

        pluginInputParamMap.put(CALLBACK_PARAMETER_KEY, exeJob.getRootEntityId());

        PluginInstance pluginInstance = pluginInstanceService
                .getRunningPluginInstance(pluginConfigInterface.getPluginConfig().getPluginPackage().getName());
        ResultData<Object> responseData = new ResultData<Object>();
        try {
            responseData = pluginServiceStub.callPluginInterface(
                    String.format("%s:%s", pluginInstance.getHost(), pluginInstance.getPort()),
                    pluginConfigInterface.getPath(), Lists.newArrayList(pluginInputParamMap),
                    "RequestId-" + Long.toString(System.currentTimeMillis()));
        } catch (Exception e) {
            log.error("errors while call plugin interface", e);
            exeJob.setErrorWithMessage(e.getMessage());
            return buildResultDataWithError(e.getMessage());
        }
        log.info("returnJsonString= " + responseData.toString());
        String returnJsonString = JsonUtils.toJsonString(responseData);
        log.info("returnJsonString= " + returnJsonString);
        ResultData<PluginResponseStationaryOutput> stationaryResultData = objectMapper.readValue(returnJsonString,
                new TypeReference<ResultData<PluginResponseStationaryOutput>>() {
                });

        if (stationaryResultData.getOutputs().size() == 0) {
            String errorMessage = String.format("Call interface[%s][%s:%s%s] with parameters[%s] has no response",
                    exeJob.getPluginConfigInterfaceId(), pluginInstance.getHost(), pluginInstance.getPort(),
                    pluginConfigInterface.getPath(), pluginInputParamMap);
            log.error(errorMessage);
            exeJob.setErrorWithMessage(errorMessage);
            return buildResultDataWithError(errorMessage);
        }
        PluginResponseStationaryOutput stationaryOutput = stationaryResultData.getOutputs().get(0);
        exeJob.setReturnJson(returnJsonString);
        exeJob.setErrorCode(stationaryOutput.getErrorCode() == null ? RESULT_CODE_ERROR : RESULT_CODE_OK);
        exeJob.setErrorMessage(stationaryOutput.getErrorMessage());
        return responseData;
    }

    private ResultData<PluginResponseStationaryOutput> buildResultDataWithError(String errorMessage) {
        ResultData<PluginResponseStationaryOutput> errorReultData = new ResultData<PluginResponseStationaryOutput>();
        errorReultData.setOutputs(Lists.newArrayList(new PluginResponseStationaryOutput(
                PluginResponseStationaryOutput.ERROR_CODE_FAILED, errorMessage, null)));
        return errorReultData;
    }

    private void tryPrepareInputParamValues(ExecutionJob exeJob, PluginConfigInterface pluginConfigInterface) {
        if (log.isDebugEnabled()) {
            log.debug("try prepare input param values for {} {} {}", exeJob.getPackageName(), exeJob.getEntityName(),
                    exeJob.getRootEntityId());
        }

        String pluginPackageName = pluginConfigInterface.getPluginConfig().getPluginPackage().getName();

        for (ExecutionJobParameter param : exeJob.getParameters()) {
            String mappingType = param.getMappingType();
            if (MAPPING_TYPE_ENTITY.equals(mappingType)) {
                calculateInputParamValueFromExpr(exeJob, param);
            }

            if (MAPPING_TYPE_SYSTEM_VARIABLE.equals(mappingType)) {
                calculateInputParamValueFromSystemVariable(exeJob, param, pluginPackageName);
            }
        }
        return;
    }

    private void calculateInputParamValueFromSystemVariable(ExecutionJob executionJob, ExecutionJobParameter parameter,
            String pluginPackageName) {
        if (log.isDebugEnabled()) {
            log.debug("calculate param value from system variable and paramName={},systemVarName={}",
                    parameter.getName(), parameter.getMappingSystemVariableName());
        }
        SystemVariable sVariable = systemVariableService.getSystemVariableByPackageNameAndName(pluginPackageName,
                parameter.getMappingSystemVariableName());

        if (sVariable == null && FIELD_REQUIRED.equals(parameter.getRequired())) {
            String errorMessage = String.format("variable is null but is mandatory for paramName=%s, systemVarName=%s ",
                    parameter.getName(), parameter.getMappingSystemVariableName());
            log.error(errorMessage);
            executionJob.setErrorWithMessage(errorMessage);
            executionJob.setPrepareException(new WecubeCoreException("3004", errorMessage, parameter.getName(),
                    parameter.getMappingSystemVariableName()));
            return;
        }

        if (sVariable != null) {

            String sVal = sVariable.getValue();
            if (StringUtils.isBlank(sVal)) {
                sVal = sVariable.getDefaultValue();
            }

            if (StringUtils.isBlank(sVal) && FIELD_REQUIRED.equals(parameter.getRequired())) {
                String errorMessage = String.format(
                        "variable is null but is mandatory for paramName=%s, systemVarName=%s", parameter.getName(),
                        parameter.getMappingSystemVariableName());
                log.error(errorMessage);
                executionJob.setErrorWithMessage(errorMessage);
                executionJob.setPrepareException(new WecubeCoreException(errorMessage));
                return;
            }
            parameter.setValue(sVal);
        }
    }

    private void calculateInputParamValueFromExpr(ExecutionJob executionJob, ExecutionJobParameter parameter) {
        String mappingEntityExpression = parameter.getMappingEntityExpression();
        if (log.isDebugEnabled()) {
            log.debug("calculate param value from entity, name={} ,expression={}", parameter.getName(),
                    mappingEntityExpression);
        }

        EntityOperationRootCondition criteria = new EntityOperationRootCondition(mappingEntityExpression,
                executionJob.getRootEntityId());

        List<Object> attrValsPerExpr = standardEntityOperationService.queryAttributeValues(criteria);

        if ((attrValsPerExpr == null || attrValsPerExpr.size() == 0)
                && FIELD_REQUIRED.equals(parameter.getRequired())) {
            String errorMessage = String.format(
                    "returned empty data while fetch the mandatory input parameter[%s] with expression[%s] and root entity ID[%s]",
                    parameter.getName(), mappingEntityExpression, criteria.getEntityIdentity());
            log.error(errorMessage);
            executionJob.setErrorWithMessage(errorMessage);
            executionJob.setPrepareException(new WecubeCoreException(errorMessage));
            throw new WecubeCoreException("3004", errorMessage, parameter.getName(), mappingEntityExpression,
                    criteria.getEntityIdentity());
        }

        if (attrValsPerExpr != null && (!attrValsPerExpr.isEmpty())) {
            parameter.setValue(attrValsPerExpr.get(0) == null ? null : attrValsPerExpr.get(0).toString());
        }
    }
}
