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
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.webank.wecube.platform.core.commons.AuthenticationContextHolder;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.BatchExecutionJob;
import com.webank.wecube.platform.core.domain.ExecutionJob;
import com.webank.wecube.platform.core.domain.ExecutionJobParameter;
import com.webank.wecube.platform.core.domain.SystemVariable;
import com.webank.wecube.platform.core.domain.plugin.PluginConfigInterface;
import com.webank.wecube.platform.core.domain.plugin.PluginConfigInterfaceParameter;
import com.webank.wecube.platform.core.domain.plugin.PluginInstance;
import com.webank.wecube.platform.core.dto.BatchExecutionRequestDto;
import com.webank.wecube.platform.core.dto.BatchExecutionResult;
import com.webank.wecube.platform.core.dto.ExecutionJobResponseDto;
import com.webank.wecube.platform.core.dto.InputParameterDefinition;
import com.webank.wecube.platform.core.dto.ItsDangerConfirmResultDto;
import com.webank.wecube.platform.core.dto.ItsDangerConfirmResultDto.ItsDangerTokenInfoDto;
import com.webank.wecube.platform.core.jpa.BatchExecutionJobRepository;
import com.webank.wecube.platform.core.jpa.PluginConfigInterfaceRepository;
import com.webank.wecube.platform.core.service.dme.EntityOperationRootCondition;
import com.webank.wecube.platform.core.service.dme.StandardEntityOperationService;
import com.webank.wecube.platform.core.service.plugin.PluginInstanceService;
import com.webank.wecube.platform.core.service.workflow.SimpleEncryptionService;
import com.webank.wecube.platform.core.support.itsdanger.ItsDanerResultDataInfoDto;
import com.webank.wecube.platform.core.support.itsdanger.ItsDangerCheckReqDto;
import com.webank.wecube.platform.core.support.itsdanger.ItsDangerCheckRespDto;
import com.webank.wecube.platform.core.support.itsdanger.ItsDangerInstanceInfoDto;
import com.webank.wecube.platform.core.support.itsdanger.ItsDangerRestClient;
import com.webank.wecube.platform.core.support.plugin.PluginServiceStub;
import com.webank.wecube.platform.core.support.plugin.dto.PluginResponse.ResultData;
import com.webank.wecube.platform.core.support.plugin.dto.PluginResponseStationaryOutput;
import com.webank.wecube.platform.core.utils.Constants;
import com.webank.wecube.platform.core.utils.JsonUtils;

@Service
public class BatchExecutionService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private static final String PLUGIN_NAME_ITSDANGEROUS = "itsdangerous";

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

    @Autowired
    @Qualifier("userJwtSsoTokenRestTemplate")
    private RestTemplate userJwtSsoTokenRestTemplate;

    @Autowired
    private SimpleEncryptionService encryptionService;

    @Autowired
    private ItsDangerRestClient itsDangerRestClient;

    private ObjectMapper objectMapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);

    @Transactional
    public BatchExecutionResult handleBatchExecutionJob(BatchExecutionRequestDto batchExecutionRequest,
            String continueToken) throws IOException {
        verifyParameters(batchExecutionRequest.getInputParameterDefinitions());
        BatchExecutionJob batchExeJob = saveToDb(batchExecutionRequest);

        List<BatchExecutionContext> ctxes = new ArrayList<>();

        for (ExecutionJob exeJob : batchExeJob.getJobs()) {
            BatchExecutionContext ctx = prepareExecutionContext(exeJob);
            ctxes.add(ctx);
        }

        if (needPerformDangerousCommandsChecking(batchExecutionRequest, continueToken)) {
            BatchExecutionResult result = performDangerCheck(batchExecutionRequest, batchExeJob, ctxes);
            if (result != null) {
                return result;
            }
        }

        Map<String, ExecutionJobResponseDto> exeResults = new HashMap<>();
        for (BatchExecutionContext ctx : ctxes) {
            ResultData<?> exeResult = null;
            ExecutionJob exeJob = ctx.getExeJob();
            try {
                exeResult = performExecutionJob(ctx);
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

        BatchExecutionResult result = new BatchExecutionResult();
        result.setResult(exeResults);
        return result;
    }

    private BatchExecutionResult performDangerCheck(BatchExecutionRequestDto batchExecutionRequest,
            BatchExecutionJob batchExeJob, List<BatchExecutionContext> ctxes) {
        if (batchExeJob == null) {
            return null;
        }

        List<ExecutionJob> jobs = batchExeJob.getJobs();
        if (jobs == null || jobs.isEmpty()) {
            return null;
        }

        if (ctxes == null || ctxes.isEmpty()) {
            return null;
        }

        ItsDangerCheckReqDto req = new ItsDangerCheckReqDto();
        req.setOperator(AuthenticationContextHolder.getCurrentUsername());
        req.setEntityType(batchExecutionRequest.getEntityName());
        req.setServiceName(batchExecutionRequest.getPluginConfigInterface().getServiceName());
        req.setServicePath(batchExecutionRequest.getPluginConfigInterface().getPath());

        for (BatchExecutionContext ctx : ctxes) {
            if (ctx.getExeResult() != null) {
                continue;
            }

            ExecutionJob exeJob = ctx.getExeJob();

            ItsDangerInstanceInfoDto instance = new ItsDangerInstanceInfoDto();
            instance.setId(exeJob.getRootEntityId());
            req.getEntityInstances().add(instance);

            Map<String, Object> pluginInputParamMap = ctx.getPluginInputParamMap();
            if (pluginInputParamMap == null) {
                pluginInputParamMap = new HashMap<String, Object>();
            }

            req.getInputParams().add(pluginInputParamMap);
        }

        ItsDangerCheckRespDto resp = itsDangerRestClient.check(req);

        if (resp == null) {
            return null;
        }

        ItsDanerResultDataInfoDto respData = resp.getData();
        if (respData == null) {
            return null;
        }

        List<Object> checkData = respData.getData();

        if (checkData == null || checkData.isEmpty()) {
            return null;
        }

        BatchExecutionResult result = new BatchExecutionResult();
        ItsDangerConfirmResultDto itsDangerConfirmResultDto = new ItsDangerConfirmResultDto();
        itsDangerConfirmResultDto.setMessage(respData.getText());
        itsDangerConfirmResultDto.setStatus("CONFIRM");

        String md5 = buildContinueToken(batchExecutionRequest);

        ItsDangerTokenInfoDto tokenInfo = new ItsDangerTokenInfoDto();
        tokenInfo.setContinueToken(md5);
        itsDangerConfirmResultDto.setData(tokenInfo);

        result.setItsDangerConfirmResultDto(itsDangerConfirmResultDto);

        return result;

    }

    private boolean needPerformDangerousCommandsChecking(BatchExecutionRequestDto requestDto, String continueToken) {

        PluginInstance itsdangerInstance = pluginInstanceService.getRunningPluginInstance(PLUGIN_NAME_ITSDANGEROUS);
        if (itsdangerInstance == null) {
            return false;
        }

        if (StringUtils.isNoneBlank(continueToken)) {
            String md5 = buildContinueToken(requestDto);
            if (md5.equals(continueToken)) {
                return false;
            } else {
                throw new WecubeCoreException("Bad continue token!");
            }
        }

        return true;
    }

    private String buildContinueToken(BatchExecutionRequestDto dto) {
        StringBuilder data = new StringBuilder();
        String seperator = ":";
        data.append(dto.getClass().getName()).append(seperator);
        data.append(dto.getPackageName()).append(seperator);
        data.append(dto.getEntityName()).append(seperator);

        String md5 = DigestUtils.md5DigestAsHex(data.toString().getBytes(Charset.forName("UTF-8")));
        return md5;
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

            List<ExecutionJobParameter> parameters = transFromInputParameterDefinitionToExecutionJobParameter(
                    batchExeRequest.getInputParameterDefinitions(), exeJob);
            exeJob.setParameters(parameters);
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
        for (InputParameterDefinition inputParameterDefinition : inputParameterDefinitions) {
            PluginConfigInterfaceParameter interfaceParameter = inputParameterDefinition.getInputParameter();

            if (inputParameterDefinition.getInputParameterValue() != null) {

                String paramValue = inputParameterDefinition.getInputParameterValue().toString();
                if ("Y".equalsIgnoreCase(interfaceParameter.getSensitiveData())) {
                    paramValue = tryEncryptParamValue(paramValue);
                }
                ExecutionJobParameter executionJobParameter = new ExecutionJobParameter(interfaceParameter.getName(),
                        interfaceParameter.getDataType(), interfaceParameter.getMappingType(),
                        interfaceParameter.getMappingEntityExpression(),
                        interfaceParameter.getMappingSystemVariableName(), interfaceParameter.getRequired(),
                        paramValue);
                executionJobParameter.setExecutionJob(executionJob);
                executionJobParameters.add(executionJobParameter);

                executionJobParameter.setParameterDefinition(interfaceParameter);
            } else {
                ExecutionJobParameter executionJobParameter = new ExecutionJobParameter(interfaceParameter.getName(),
                        interfaceParameter.getDataType(), interfaceParameter.getMappingType(),
                        interfaceParameter.getMappingEntityExpression(),
                        interfaceParameter.getMappingSystemVariableName(), interfaceParameter.getRequired(), null);
                executionJobParameter.setExecutionJob(executionJob);
                executionJobParameters.add(executionJobParameter);

                executionJobParameter.setParameterDefinition(interfaceParameter);
            }

        }
        return executionJobParameters;
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

    private BatchExecutionContext prepareExecutionContext(ExecutionJob exeJob) {
        BatchExecutionContext ctx = new BatchExecutionContext();
        ctx.setExeJob(exeJob);
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

            ResultData<PluginResponseStationaryOutput> resultData = buildResultDataWithError(errorMessage);
            ctx.setExeResult(resultData);
            return ctx;
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

        ctx.setPluginConfigInterface(pluginConfigInterface);
        ctx.setPluginInputParamMap(pluginInputParamMap);

        return ctx;
    }

    protected ResultData<?> performExecutionJob(BatchExecutionContext ctx) throws IOException {
        if (ctx.getExeResult() != null) {
            return ctx.getExeResult();
        }

        PluginConfigInterface pluginConfigInterface = ctx.getPluginConfigInterface();

        ExecutionJob exeJob = ctx.getExeJob();

        Map<String, Object> pluginInputParamMap = ctx.getPluginInputParamMap();

        ResultData<?> responseData = performBatchExecutionRequest(pluginConfigInterface, pluginInputParamMap, exeJob);
        return responseData;

    }

    private ResultData<?> performBatchExecutionRequest(PluginConfigInterface pluginConfigInterface,
            Map<String, Object> pluginInputParamMap, ExecutionJob exeJob) throws IOException {
        PluginInstance pluginInstance = pluginInstanceService
                .getRunningPluginInstance(pluginConfigInterface.getPluginConfig().getPluginPackage().getName());
        ResultData<Object> responseData = new ResultData<Object>();
        try {
            responseData = pluginServiceStub.callPluginInterface(
                    String.format("%s:%s", pluginInstance.getHost(), pluginInstance.getPort()),
                    pluginConfigInterface.getPath(), Lists.newArrayList(pluginInputParamMap),
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

    @SuppressWarnings("unchecked")
    private void handleResultData(ResultData<Object> responseData, ExecutionJob exeJob) {
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

    private void handleSingleResultObject(Map<String, Object> resultObjectMap, ExecutionJob exeJob) {
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

        Optional<PluginConfigInterface> pluginConfigInterfOpt = pluginConfigInterfaceRepository
                .findById(pluginConfigInterfaceId);
        if (!pluginConfigInterfOpt.isPresent()) {
            log.info("Plugin config interface does not exist for ID:{}", pluginConfigInterfaceId);
            return;
        }

        PluginConfigInterface pluginConfigInterf = pluginConfigInterfOpt.get();

        Set<PluginConfigInterfaceParameter> outputParameters = pluginConfigInterf.getOutputParameters();

        for (PluginConfigInterfaceParameter pciParam : outputParameters) {
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

            // #2046
            if (parameter.getParameterDefinition() != null
                    && "Y".equalsIgnoreCase(parameter.getParameterDefinition().getSensitiveData())) {
                sVal = tryEncryptParamValue(sVal);
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
