package com.webank.wecube.platform.core.service.plugin;

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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.plugin.BatchExecutionRequestDto;
import com.webank.wecube.platform.core.dto.plugin.ExecutionJobResponseDto;
import com.webank.wecube.platform.core.dto.plugin.InputParameterDefinition;
import com.webank.wecube.platform.core.dto.plugin.PluginConfigInterfaceParameterDto;
import com.webank.wecube.platform.core.dto.plugin.ResourceDataDto;
import com.webank.wecube.platform.core.entity.plugin.BatchExecutionJobs;
import com.webank.wecube.platform.core.entity.plugin.ExecutionJobParameters;
import com.webank.wecube.platform.core.entity.plugin.ExecutionJobs;
import com.webank.wecube.platform.core.entity.plugin.PluginConfigInterfaceParameters;
import com.webank.wecube.platform.core.entity.plugin.PluginConfigInterfaces;
import com.webank.wecube.platform.core.entity.plugin.PluginConfigs;
import com.webank.wecube.platform.core.entity.plugin.PluginInstances;
import com.webank.wecube.platform.core.entity.plugin.PluginPackages;
import com.webank.wecube.platform.core.entity.plugin.SystemVariables;
import com.webank.wecube.platform.core.repository.plugin.BatchExecutionJobsMapper;
import com.webank.wecube.platform.core.repository.plugin.ExecutionJobParametersMapper;
import com.webank.wecube.platform.core.repository.plugin.ExecutionJobsMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginConfigInterfaceParametersMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginConfigInterfacesMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginConfigsMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginPackagesMapper;
import com.webank.wecube.platform.core.service.dme.EntityOperationRootCondition;
import com.webank.wecube.platform.core.service.dme.StandardEntityOperationService;
import com.webank.wecube.platform.core.service.workflow.SimpleEncryptionService;
import com.webank.wecube.platform.core.support.plugin.PluginServiceStub;
import com.webank.wecube.platform.core.support.plugin.dto.PluginResponse.ResultData;
import com.webank.wecube.platform.core.support.plugin.dto.PluginResponseStationaryOutput;
import com.webank.wecube.platform.core.utils.Constants;
import com.webank.wecube.platform.core.utils.JsonUtils;
import com.webank.wecube.platform.workflow.commons.LocalIdGenerator;

@Service
public class BatchExecutionService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private PluginServiceStub pluginServiceStub;

    @Autowired
    protected PluginInstanceMgmtService pluginInstanceMgmtService;

    @Autowired
    private SystemVariableService systemVariableService;
    @Autowired
    private BatchExecutionJobsMapper batchExecutionJobsMapper;
    @Autowired
    private PluginConfigInterfacesMapper pluginConfigInterfacesMapper;
    @Autowired
    private PluginConfigInterfaceParametersMapper pluginConfigInterfaceParametersMapper;
    @Autowired
    private PluginConfigsMapper pluginConfigsMapper;
    @Autowired
    private PluginPackagesMapper pluginPackagesMapper;
    @Autowired
    private ExecutionJobParametersMapper executionJobParametersMapper;
    @Autowired
    private ExecutionJobsMapper executionJobsMapper;
    @Autowired
    protected StandardEntityOperationService standardEntityOperationService;

    @Autowired
    @Qualifier("userJwtSsoTokenRestTemplate")
    private RestTemplate userJwtSsoTokenRestTemplate;

    @Autowired
    private SimpleEncryptionService encryptionService;

    private ObjectMapper objectMapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);

    @Transactional
    public Map<String, ExecutionJobResponseDto> handleBatchExecutionJob(
            BatchExecutionRequestDto batchExecutionRequest) {
        try {
            return doHandleBatchExecutionJob(batchExecutionRequest);
        } catch (IOException e) {
            log.error("Errors while processing batch execution.", e);
            throw new WecubeCoreException("Errors while processing batch execution.");
        }
    }

    private Map<String, ExecutionJobResponseDto> doHandleBatchExecutionJob(
            BatchExecutionRequestDto batchExecutionRequest) throws IOException {
        verifyParameters(batchExecutionRequest.getInputParameterDefinitions());
        BatchExecutionJobs batchExeJob = saveToDb(batchExecutionRequest);

        Map<String, ExecutionJobResponseDto> exeResults = new HashMap<>();
        for (ExecutionJobs exeJob : batchExeJob.getJobs()) {
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
        if (inputParameterDefinitions == null) {
            return;
        }
        for (InputParameterDefinition inputParameterDefinition : inputParameterDefinitions) {
            PluginConfigInterfaceParameterDto inputParameter = inputParameterDefinition.getInputParameter();
            if (FIELD_REQUIRED.equalsIgnoreCase(inputParameter.getRequired())
                    && MAPPING_TYPE_CONTEXT.equalsIgnoreCase(inputParameter.getMappingType())) {
                String msg = String.format(
                        "Batch execution job does not support input parameter[%s] with [mappingType=%s] and [required=%s]",
                        inputParameter.getName(), inputParameter.getMappingType(), inputParameter.getRequired());
                throw new WecubeCoreException("3001", msg, inputParameter.getName(), inputParameter.getMappingType(),
                        inputParameter.getRequired());
            }
        }
    }

    private BatchExecutionJobs saveToDb(BatchExecutionRequestDto batchExeRequest) {
        BatchExecutionJobs batchExeJobEntity = new BatchExecutionJobs();
        batchExeJobEntity.setId(LocalIdGenerator.generateId());
        batchExeJobEntity.setCreateTimestamp(new Date());

        List<ExecutionJobs> exeJobsEntities = new ArrayList<ExecutionJobs>();

        List<ResourceDataDto> resourceDatas = batchExeRequest.getResourceDatas();

        if (resourceDatas == null) {
            throw new WecubeCoreException("Resource data cannot be empty.");
        }

        for (ResourceDataDto resourceData : resourceDatas) {
            ExecutionJobs exeJobEntity = new ExecutionJobs();
            exeJobEntity.setRootEntityId(resourceData.getId());
            exeJobEntity.setPluginConfigInterfaceId(batchExeRequest.getPluginConfigInterface().getId());
            exeJobEntity.setPackageName(batchExeRequest.getPackageName());
            exeJobEntity.setEntityName(batchExeRequest.getEntityName());
            exeJobEntity.setBusinessKey(resourceData.getBusinessKeyValue().toString());

            executionJobsMapper.insert(exeJobEntity);

            List<ExecutionJobParameters> parametersList = transFromInputParameterDefinitionToExecutionJobParameter(
                    batchExeRequest.getInputParameterDefinitions(), exeJobEntity);
            exeJobEntity.setParameters(parametersList);
            exeJobEntity.setBatchExecutionJob(batchExeJobEntity);
            exeJobsEntities.add(exeJobEntity);
        }

        batchExeJobEntity.setJobs(exeJobsEntities);

        batchExecutionJobsMapper.insert(batchExeJobEntity);
        return batchExeJobEntity;
    }

    private void postProcessBatchExecutionJob(BatchExecutionJobs batchExeJob) {
        batchExeJob.setCompleteTimestamp(new Timestamp(System.currentTimeMillis()));
        
        //TODO
        batchExecutionJobsMapper.updateByPrimaryKeySelective(batchExeJob);
    }

    private List<ExecutionJobParameters> transFromInputParameterDefinitionToExecutionJobParameter(
            List<InputParameterDefinition> inputParameterDefinitions, ExecutionJobs executionJob) {
        List<ExecutionJobParameters> executionJobParametersList = new ArrayList<ExecutionJobParameters>();
        for (InputParameterDefinition inputParameterDefinition : inputParameterDefinitions) {
            PluginConfigInterfaceParameterDto interfaceParameter = inputParameterDefinition.getInputParameter();

            if (inputParameterDefinition.getInputParameterValue() != null) {

                String paramValue = inputParameterDefinition.getInputParameterValue().toString();
                if ("Y".equalsIgnoreCase(interfaceParameter.getSensitiveData())) {
                    paramValue = tryEncryptParamValue(paramValue);
                }
                ExecutionJobParameters executionJobParameter = new ExecutionJobParameters(interfaceParameter.getName(),
                        interfaceParameter.getDataType(), interfaceParameter.getMappingType(),
                        interfaceParameter.getMappingEntityExpression(),
                        interfaceParameter.getMappingSystemVariableName(), interfaceParameter.getRequired(),
                        paramValue);
                executionJobParameter.setExecutionJob(executionJob);
                executionJobParametersList.add(executionJobParameter);

                executionJobParameter.setParameterDefinition(interfaceParameter);

                executionJobParametersMapper.insert(executionJobParameter);
            } else {
                ExecutionJobParameters executionJobParameter = new ExecutionJobParameters(interfaceParameter.getName(),
                        interfaceParameter.getDataType(), interfaceParameter.getMappingType(),
                        interfaceParameter.getMappingEntityExpression(),
                        interfaceParameter.getMappingSystemVariableName(), interfaceParameter.getRequired(), null);
                executionJobParameter.setExecutionJob(executionJob);
                executionJobParametersList.add(executionJobParameter);

                executionJobParameter.setParameterDefinition(interfaceParameter);

                executionJobParametersMapper.insert(executionJobParameter);
            }

        }
        return executionJobParametersList;
    }

    private String tryEncryptParamValue(String paramValue) {
        if (StringUtils.isBlank(paramValue)) {
            return paramValue;
        }

        return encryptionService.encodeToAesBase64(paramValue);
    }

    private String tryDecryptParamValue(String cipherValue) {
        if (StringUtils.isBlank(cipherValue)) {
            return cipherValue;
        }

        return encryptionService.decodeFromAesBase64(cipherValue);
    }

    protected ResultData<?> performExecutionJob(ExecutionJobs exeJob) throws IOException {
        if (exeJob == null) {
            throw new WecubeCoreException("3002", "execution job as input argument cannot be null.");
        }
        if (log.isInfoEnabled()) {
            log.info("perform batch execution job:{} {} {}", exeJob.getPackageName(), exeJob.getEntityName(),
                    exeJob.getRootEntityId());
        }

        PluginConfigInterfaces pluginConfigInterfaceEntity = pluginConfigInterfacesMapper
                .selectByPrimaryKey(exeJob.getPluginConfigInterfaceId());
        
        if (pluginConfigInterfaceEntity == null) {
            String errorMessage = String.format("Can not found plugin config interface[%s]",
                    exeJob.getPluginConfigInterfaceId());
            log.error(errorMessage);
            exeJob.setErrorWithMessage(errorMessage);

            return buildResultDataWithError(errorMessage);
        }

        PluginConfigs pluginConfigEntity = pluginConfigsMapper
                .selectByPrimaryKey(pluginConfigInterfaceEntity.getPluginConfigId());
        PluginPackages pluginPackagesEntity = pluginPackagesMapper
                .selectByPrimaryKey(pluginConfigEntity.getPluginPackageId());
        pluginConfigEntity.setPluginPackage(pluginPackagesEntity);
        pluginConfigInterfaceEntity.setPluginConfig(pluginConfigEntity);

        

        tryPrepareInputParamValues(exeJob, pluginConfigInterfaceEntity);

        if (exeJob.getPrepareException() != null) {
            log.error("Errors to calculate input parameters", exeJob.getPrepareException());
            throw new WecubeCoreException("3003",
                    "Failed to prepare input parameter due to error:" + exeJob.getPrepareException().getMessage(),
                    exeJob.getPrepareException().getMessage());
        }

        Map<String, Object> pluginInputParamMap = new HashMap<String, Object>();

        for (ExecutionJobParameters parameter : exeJob.getParameters()) {
            if (DATA_TYPE_STRING.equals(parameter.getDataType())
                    || MAPPING_TYPE_SYSTEM_VARIABLE.equals(parameter.getMappingEntityExpression())) {
                String paramValue = parameter.getValue();
                if (parameter.getParameterDefinition() != null
                        && "Y".equalsIgnoreCase(parameter.getParameterDefinition().getSensitiveData())) {
                    paramValue = tryDecryptParamValue(paramValue);
                }
                pluginInputParamMap.put(parameter.getName(), paramValue);
            }
            if (DATA_TYPE_NUMBER.equals(parameter.getDataType())) {
                pluginInputParamMap.put(parameter.getName(), Integer.valueOf(parameter.getValue()));
            }
        }

        pluginInputParamMap.put(CALLBACK_PARAMETER_KEY, exeJob.getRootEntityId());

        PluginInstances pluginInstance = pluginInstanceMgmtService
                .getRunningPluginInstance(pluginPackagesEntity.getName());
        ResultData<Object> responseData = new ResultData<Object>();
        try {
            responseData = pluginServiceStub.callPluginInterface(
                    String.format("%s:%s", pluginInstance.getHost(), pluginInstance.getPort()),
                    pluginConfigInterfaceEntity.getPath(), Lists.newArrayList(pluginInputParamMap),
                    "RequestId-" + Long.toString(System.currentTimeMillis()));

            handleResultData(responseData, exeJob);
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
                    pluginConfigInterfaceEntity.getPath(), pluginInputParamMap);
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

    @SuppressWarnings("unchecked")
    private void handleResultData(ResultData<Object> responseData, ExecutionJobs exeJob) {
        // #2046
        if (responseData == null || responseData.getOutputs() == null) {
            log.info("response data is empty for execution job {}", exeJob.getId());
            return;
        }

        List<Object> resultObjects = responseData.getOutputs();
        if (resultObjects.isEmpty()) {
            log.info("result object is empty for execution job {}", exeJob.getId());
            return;
        }

        for (Object resultObject : resultObjects) {
            if (resultObject == null) {
                continue;
            }

            if (resultObject instanceof Map) {
                Map<String, Object> resultObjectMap = (Map<String, Object>) resultObject;
                handleSingleResultObject(resultObjectMap, exeJob);
            }
        }

    }

    private void handleSingleResultObject(Map<String, Object> resultObjectMap, ExecutionJobs exeJob) {
        String rootEntityId = (String) resultObjectMap.get(CALLBACK_PARAMETER_KEY);

        if (StringUtils.isBlank(rootEntityId)) {
            log.info("There is no root entity ID found in output for execution job {}", exeJob.getId());
            return;
        }

        String pluginConfigInterfaceId = exeJob.getPluginConfigInterfaceId();
        if (StringUtils.isBlank(pluginConfigInterfaceId)) {
            log.info("Plugin config interface ID is not found for execution job {}", exeJob.getId());
            return;
        }

        PluginConfigInterfaces pluginConfigInterfaceEntity = pluginConfigInterfacesMapper
                .selectByPrimaryKey(pluginConfigInterfaceId);
        if (pluginConfigInterfaceEntity == null) {
            log.info("Plugin config interface does not exist for ID:{}", pluginConfigInterfaceId);
            return;
        }

        List<PluginConfigInterfaceParameters> outputParameters = pluginConfigInterfaceParametersMapper
                .selectAllByConfigInterfaceAndParamType(pluginConfigInterfaceEntity.getId(),
                        PluginConfigInterfaceParameters.TYPE_OUTPUT);


        for (PluginConfigInterfaceParameters pciParam : outputParameters) {
            String paramName = pciParam.getName();
            String paramExpr = pciParam.getMappingEntityExpression();
            String paramMappingType = pciParam.getMappingType();

            if (!Constants.MAPPING_TYPE_ENTITY.equalsIgnoreCase(paramMappingType)) {
                continue;
            }

            if (StringUtils.isBlank(paramExpr)) {
                continue;
            }
            log.info("expression is configured for paramName:{} and interface:{}", paramName, pluginConfigInterfaceId);

            Object retVal = resultObjectMap.get(paramName);

            if (retVal == null) {
                log.info("returned value is null for {} {}", exeJob.getId(), paramName);
                continue;
            }

            EntityOperationRootCondition condition = new EntityOperationRootCondition(paramExpr, rootEntityId);

            try {
                this.standardEntityOperationService.update(condition, retVal, this.userJwtSsoTokenRestTemplate);
            } catch (Exception e) {
                log.error("Exceptions while updating entity.But still keep going to update.", e);
                throw new WecubeCoreException(e.getMessage());
            }

        }
    }

    private ResultData<PluginResponseStationaryOutput> buildResultDataWithError(String errorMessage) {
        ResultData<PluginResponseStationaryOutput> errorReultData = new ResultData<PluginResponseStationaryOutput>();
        errorReultData.setOutputs(Lists.newArrayList(new PluginResponseStationaryOutput(
                PluginResponseStationaryOutput.ERROR_CODE_FAILED, errorMessage, null)));
        return errorReultData;
    }

    private void tryPrepareInputParamValues(ExecutionJobs exeJob, PluginConfigInterfaces pluginConfigInterface) {
        if (log.isDebugEnabled()) {
            log.debug("try prepare input param values for {} {} {}", exeJob.getPackageName(), exeJob.getEntityName(),
                    exeJob.getRootEntityId());
        }

        PluginConfigs pluginConfigEntity = pluginConfigInterface.getPluginConfig();
        PluginPackages pluginPackageEntity = pluginConfigEntity.getPluginPackage();
        String pluginPackageName = pluginPackageEntity.getName();

        for (ExecutionJobParameters param : exeJob.getParameters()) {
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

    private void calculateInputParamValueFromSystemVariable(ExecutionJobs executionJob,
            ExecutionJobParameters parameter, String pluginPackageName) {
        if (log.isDebugEnabled()) {
            log.debug("calculate param value from system variable and paramName={},systemVarName={}",
                    parameter.getName(), parameter.getMappingSystemVariableName());
        }
        SystemVariables sVariable = systemVariableService.getSystemVariableByPackageNameAndName(pluginPackageName,
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

            // #2046
            if (parameter.getParameterDefinition() != null
                    && "Y".equalsIgnoreCase(parameter.getParameterDefinition().getSensitiveData())) {
                sVal = tryEncryptParamValue(sVal);
            }
            parameter.setValue(sVal);
        }
    }

    private void calculateInputParamValueFromExpr(ExecutionJobs executionJob, ExecutionJobParameters parameter) {
        String mappingEntityExpression = parameter.getMappingEntityExpression();
        if (log.isDebugEnabled()) {
            log.debug("calculate param value from entity, name={} ,expression={}", parameter.getName(),
                    mappingEntityExpression);
        }

        EntityOperationRootCondition criteria = new EntityOperationRootCondition(mappingEntityExpression,
                executionJob.getRootEntityId());

        List<Object> attrValsPerExpr = standardEntityOperationService.queryAttributeValues(criteria,
                userJwtSsoTokenRestTemplate);

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
            // #2046
            String paramValue = attrValsPerExpr.get(0) == null ? null : attrValsPerExpr.get(0).toString();
            if (parameter.getParameterDefinition() != null
                    && "Y".equalsIgnoreCase(parameter.getParameterDefinition().getSensitiveData())) {
                paramValue = tryEncryptParamValue(paramValue);
            }
            parameter.setValue(paramValue);
        }
    }
}
