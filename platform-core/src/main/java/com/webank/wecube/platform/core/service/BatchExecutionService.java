package com.webank.wecube.platform.core.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Lists;
import com.webank.wecube.platform.core.domain.BatchExecutionJob;
import com.webank.wecube.platform.core.domain.ExecutionJob;
import com.webank.wecube.platform.core.domain.ExecutionJobParameter;
import com.webank.wecube.platform.core.domain.SystemVariable;
import com.webank.wecube.platform.core.domain.plugin.*;
import com.webank.wecube.platform.core.dto.BatchExecutionRequestDto;
import com.webank.wecube.platform.core.jpa.BatchExecutionJobRepository;
import com.webank.wecube.platform.core.jpa.PluginConfigInterfaceRepository;
import com.webank.wecube.platform.core.dto.InputParameterDefinition;
import com.webank.wecube.platform.core.model.datamodel.DataModelExpressionToRootData;
import com.webank.wecube.platform.core.service.datamodel.ExpressionService;
import com.webank.wecube.platform.core.support.plugin.PluginServiceStub;
import com.webank.wecube.platform.core.support.plugin.dto.PluginResponse.ResultData;

import com.webank.wecube.platform.core.support.plugin.dto.PluginResponseStationaryOutput;
import com.webank.wecube.platform.core.utils.JsonUtils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.webank.wecube.platform.core.utils.Constants.*;

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
    protected ExpressionService expressionService;

    public Map<String, Object> handleBatchExecutionJob(BatchExecutionRequestDto batchExecutionRequest)
            throws IOException {
        // check parameter

        BatchExecutionJob batchExecutionJob = saveToDb(batchExecutionRequest);

        Map<String, Object> executionResults = new HashMap<>();
        for (ExecutionJob job : batchExecutionJob.getJobs()) {
            ResultData<?> executionResult = runExecutionJob(job);
            Object resultObject = executionResult.getOutputs().get(0);
            executionResults.put(job.getBusinessKey(), resultObject);
        }

        completeBatchExecutionJob(batchExecutionJob);
        return executionResults;
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
        ResultData<PluginResponseStationaryOutput> stationaryResultData = JSON.parseObject(returnJsonString,
                new TypeReference<ResultData<PluginResponseStationaryOutput>>() {
                });

        if (stationaryResultData.getOutputs().size() == 0) {
            errorMessage = String.format("Call interface[%s][%s:%s%s] with parameters[%s] has no respond",
                    executionJob.getPluginConfigInterfaceId(), pluginInstance.getHost(), pluginInstance.getPort(),
                    pluginConfigInterface.getPath(), callInterfaceParameterMap);
            log.error(errorMessage);
            executionJob.setErrorWithMessage(errorMessage);
            return buildResultDataWithError(errorMessage);
        }
        PluginResponseStationaryOutput stationaryOutput = stationaryResultData.getOutputs().get(0);
        executionJob.setReturnJson(returnJsonString);
        executionJob.setErrorCode(stationaryOutput.getErrorCode());
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

                DataModelExpressionToRootData criteria = new DataModelExpressionToRootData(mappingEntityExpression,
                        executionJob.getRootEntityId());

                List<Object> attrValsPerExpr = expressionService.fetchData(criteria);

                if ((attrValsPerExpr == null || attrValsPerExpr.size() == 0)
                        && FIELD_REQUIRED.equals(parameter.getRequired())) {
                    errorMessage = String.format(
                            "returned empty data while fetch the mandatory input parameter[%s] with expression[%s] and root entity ID[%s]",
                            parameter.getName(), mappingEntityExpression, criteria.getRootData());
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
