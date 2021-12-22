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
import java.nio.charset.Charset;
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
import org.springframework.util.DigestUtils;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.webank.wecube.platform.core.commons.AuthenticationContextHolder;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.plugin.BatchExecutionRequestDto;
import com.webank.wecube.platform.core.dto.plugin.BatchExecutionResultDto;
import com.webank.wecube.platform.core.dto.plugin.ExecutionJobResponseDto;
import com.webank.wecube.platform.core.dto.plugin.InputParameterDefinitionDto;
import com.webank.wecube.platform.core.dto.plugin.ItsDangerConfirmResultDto;
import com.webank.wecube.platform.core.dto.plugin.ItsDangerConfirmResultDto.ItsDangerTokenInfoDto;
import com.webank.wecube.platform.core.dto.plugin.PluginConfigInterfaceParameterDto;
import com.webank.wecube.platform.core.dto.plugin.ResourceDataDto;
import com.webank.wecube.platform.core.entity.plugin.BatchExecutionJobs;
import com.webank.wecube.platform.core.entity.plugin.CoreObjectMeta;
import com.webank.wecube.platform.core.entity.plugin.CoreObjectVar;
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
import com.webank.wecube.platform.core.repository.plugin.PluginInstancesMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginPackagesMapper;
import com.webank.wecube.platform.core.service.dme.EntityOperationRootCondition;
import com.webank.wecube.platform.core.service.dme.EntityQueryExprNodeInfo;
import com.webank.wecube.platform.core.service.dme.EntityQueryExpressionParser;
import com.webank.wecube.platform.core.service.dme.StandardEntityOperationService;
import com.webank.wecube.platform.core.service.workflow.SimpleEncryptionService;
import com.webank.wecube.platform.core.support.itsdanger.ItsDanerResultDataInfoDto;
import com.webank.wecube.platform.core.support.itsdanger.ItsDangerCheckReqDto;
import com.webank.wecube.platform.core.support.itsdanger.ItsDangerCheckRespDto;
import com.webank.wecube.platform.core.support.itsdanger.ItsDangerInstanceInfoDto;
import com.webank.wecube.platform.core.support.itsdanger.ItsDangerRestClient;
import com.webank.wecube.platform.core.support.plugin.PluginRemoteCallException;
import com.webank.wecube.platform.core.support.plugin.PluginServiceStub;
import com.webank.wecube.platform.core.support.plugin.dto.PluginResponse.ResultData;
import com.webank.wecube.platform.core.support.plugin.dto.PluginResponse;
import com.webank.wecube.platform.core.support.plugin.dto.PluginResponseStationaryOutput;
import com.webank.wecube.platform.core.utils.Constants;
import com.webank.wecube.platform.core.utils.JsonUtils;
import com.webank.wecube.platform.workflow.commons.LocalIdGenerator;

@Service
public class BatchExecutionService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public static final String PLUGIN_NAME_ITSDANGEROUS = "itsdangerous";

    protected static final String CONFIRM_TOKEN_KEY = "confirmToken";

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
    @Qualifier(value = "jwtSsoRestTemplate")
    private RestTemplate jwtSsoRestTemplate;

    @Autowired
    private SimpleEncryptionService encryptionService;

    @Autowired
    private ItsDangerRestClient itsDangerRestClient;

    @Autowired
    private PluginInstancesMapper pluginInstancesMapper;

    @Autowired
    private PluginParamObjectMetaStorage pluginParamObjectMetaStorage;

    @Autowired
    protected PluginParamObjectVarStorage pluginParamObjectVarStorageService;

    @Autowired
    protected PluginParamObjectVarCalculator pluginParamObjectVarCalculator;

    @Autowired
    protected PluginParamObjectVarMarshaller pluginParamObjectVarAssembleService;

    @Autowired
    protected EntityQueryExpressionParser entityQueryExpressionParser;

    private ObjectMapper objectMapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);

    @Transactional
    public BatchExecutionResultDto handleBatchExecutionJob(BatchExecutionRequestDto batchExecutionRequest,
            String continueToken) {
        try {
            return doHandleBatchExecutionJob(batchExecutionRequest, continueToken);
        } catch (IOException e) {
            log.error("Errors while processing batch execution.", e);
            throw new WecubeCoreException("Errors while processing batch execution.");
        }
    }

    private BatchExecutionResultDto doHandleBatchExecutionJob(BatchExecutionRequestDto batchExecutionRequest,
            String continueToken) throws IOException {
        verifyParameters(batchExecutionRequest.getInputParameterDefinitions());

        PluginConfigInterfaces pluginConfigInterfaces = tryFetchEnrichedPluginConfigInterfaces(
                batchExecutionRequest.getPluginConfigInterface().getId());

        BatchExecutionJobs batchExeJob = saveToDb(batchExecutionRequest, pluginConfigInterfaces);

        List<BatchExecutionContext> ctxes = new ArrayList<>();

        for (ExecutionJobs exeJob : batchExeJob.getJobs()) {
            BatchExecutionContext ctx = prepareExecutionContext(exeJob, pluginConfigInterfaces);
            ctxes.add(ctx);
        }

        if (needPerformDangerousCommandsChecking(batchExecutionRequest, continueToken)) {
            BatchExecutionResultDto result = performDangerCheck(batchExecutionRequest, batchExeJob, ctxes);
            if (result != null) {
                return result;
            }
        }

        Map<String, ExecutionJobResponseDto> exeResults = new HashMap<>();
        for (BatchExecutionContext ctx : ctxes) {
            ResultData<?> exeResult = null;
            ExecutionJobs exeJob = ctx.getExeJob();
            String exeResultsKey = exeJob.getBusinessKey();
            if (StringUtils.isBlank(exeResultsKey)) {
                exeResultsKey = exeJob.getRootEntityId();
            }
            try {
                exeResult = performExecutionJob(exeJob);
                if (exeResult == null) {
                    if (exeJob.getPrepareException() != null) {
                        exeResult = buildResultDataWithError(exeJob.getPrepareException().getMessage(), exeJob);
                        Object resultObject = exeResult.getOutputs().get(0);
                        ExecutionJobResponseDto respDataObj = new ExecutionJobResponseDto(RESULT_CODE_ERROR,
                                resultObject);
                        respDataObj.setRequestData(exeJob.getRequestData());
                        exeResults.put(exeResultsKey, respDataObj);
                    }
                } else {
                    Object resultObject = exeResult.getOutputs().get(0);
                    String errorCode = exeJob.getErrorCode() == null ? RESULT_CODE_ERROR : exeJob.getErrorCode();
                    ExecutionJobResponseDto respDataObj = new ExecutionJobResponseDto(errorCode, resultObject);
                    respDataObj.setRequestData(exeJob.getRequestData());
                    exeResults.put(exeResultsKey, respDataObj);
                }
            } catch (Exception e) {
                log.error("errors to run execution job,{} {} {}, errorMsg:{} ", exeJob.getPackageName(),
                        exeJob.getEntityName(), exeJob.getRootEntityId(), e.getMessage());
                exeResult = buildResultDataWithError(e.getMessage(), exeJob);
                Object resultObject = exeResult.getOutputs().get(0);
                ExecutionJobResponseDto respDataObj = new ExecutionJobResponseDto(RESULT_CODE_ERROR, resultObject);
                respDataObj.setRequestData(exeJob.getRequestData());
                log.info("biz key:{}, respDataObj:{}", exeResultsKey, respDataObj);
                exeResults.put(exeResultsKey, respDataObj);
            }

        }

        try {
            postProcessBatchExecutionJob(batchExeJob);
        } catch (Exception e) {
            log.error("errors while post processing batch execution job", e);

        }

        BatchExecutionResultDto result = new BatchExecutionResultDto();
        result.setResult(exeResults);
        return result;
    }

    private PluginConfigInterfaces tryFetchEnrichedPluginConfigInterfaces(String intfId) {
        PluginConfigInterfaces pluginConfigIntf = pluginConfigInterfacesMapper.selectByPrimaryKey(intfId);

        if (pluginConfigIntf == null) {
            return null;
        }

        PluginConfigs pluginConfig = pluginConfigsMapper.selectByPrimaryKey(pluginConfigIntf.getPluginConfigId());
        if (pluginConfig == null) {
            log.debug("cannot find such plugin config with id : {}", pluginConfigIntf.getPluginConfigId());
            return null;
        }

        pluginConfigIntf.setPluginConfig(pluginConfig);

        PluginPackages pluginPackage = pluginPackagesMapper.selectByPrimaryKey(pluginConfig.getPluginPackageId());

        if (pluginPackage == null) {
            log.debug("cannot find such plugin package with id : {}", pluginConfig.getPluginPackageId());
            return null;
        }

        pluginConfig.setPluginPackage(pluginPackage);

        return pluginConfigIntf;

    }

    private BatchExecutionResultDto performDangerCheck(BatchExecutionRequestDto batchExecutionRequest,
            BatchExecutionJobs batchExeJob, List<BatchExecutionContext> ctxes) {
        if (batchExeJob == null) {
            return null;
        }

        List<ExecutionJobs> jobs = batchExeJob.getJobs();
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

            ExecutionJobs exeJob = ctx.getExeJob();

            ItsDangerInstanceInfoDto instance = new ItsDangerInstanceInfoDto();
            instance.setId(exeJob.getRootEntityId());
            instance.setDisplayName(exeJob.getBusinessKey());
            req.getEntityInstances().add(instance);

            Map<String, Object> pluginInputParamMap = ctx.getPluginInputParamMap();
            if (pluginInputParamMap == null) {
                pluginInputParamMap = new HashMap<String, Object>();
            }

            req.getInputParams().add(pluginInputParamMap);
        }

        ItsDangerCheckRespDto resp = itsDangerRestClient.checkFromBackend(req);

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

        BatchExecutionResultDto result = new BatchExecutionResultDto();
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

    private BatchExecutionContext prepareExecutionContext(ExecutionJobs exeJob,
            PluginConfigInterfaces pluginConfigInterfaces) {
        BatchExecutionContext ctx = new BatchExecutionContext();
        ctx.setExeJob(exeJob);
        if (exeJob == null) {
            throw new WecubeCoreException("3002", "execution job as input argument cannot be null.");
        }
        if (log.isInfoEnabled()) {
            log.info("perform batch execution job:{} {} {}", exeJob.getPackageName(), exeJob.getEntityName(),
                    exeJob.getRootEntityId());
        }

//        PluginConfigInterfaces pluginConfigInterfaces = tryFetchEnrichedPluginConfigInterfaces(
//                exeJob.getPluginConfigInterfaceId());
        if (pluginConfigInterfaces == null) {
            String errorMessage = String.format("Can not found plugin config interface[%s]",
                    exeJob.getPluginConfigInterfaceId());
            log.error(errorMessage);
            exeJob.setErrorWithMessage(errorMessage);

            ResultData<PluginResponseStationaryOutput> resultData = buildResultDataWithError(errorMessage, exeJob);
            ctx.setExeResult(resultData);
            return ctx;
        }

        tryPrepareInputParamValues(exeJob, pluginConfigInterfaces);

        if (exeJob.getPrepareException() != null) {
            log.error("Errors to calculate input parameters", exeJob.getPrepareException());
            throw new WecubeCoreException("3003",
                    "Failed to prepare input parameter due to error:" + exeJob.getPrepareException().getMessage(),
                    exeJob.getPrepareException().getMessage());
        }

        Map<String, Object> pluginInputParamMap = new HashMap<String, Object>();

        for (ExecutionJobParameters parameter : exeJob.getParameters()) {
            pluginInputParamMap.put(parameter.getName(), getExpectedValue(parameter));
        }

        pluginInputParamMap.put(CALLBACK_PARAMETER_KEY, exeJob.getRootEntityId());

        ctx.setPluginConfigInterface(pluginConfigInterfaces);
        ctx.setPluginInputParamMap(pluginInputParamMap);

        return ctx;
    }

    private boolean needPerformDangerousCommandsChecking(BatchExecutionRequestDto requestDto, String continueToken) {

        int countRunningPluginInstances = pluginInstancesMapper
                .countAllRunningPluginInstancesByPackage(PLUGIN_NAME_ITSDANGEROUS);
        if (countRunningPluginInstances < 1) {
            log.info("There is not any running instance currently of package :{}", PLUGIN_NAME_ITSDANGEROUS);
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

    private void verifyParameters(List<InputParameterDefinitionDto> inputParameterDefinitions) {
        if (inputParameterDefinitions == null) {
            return;
        }
        for (InputParameterDefinitionDto inputParameterDefinition : inputParameterDefinitions) {
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

    private BatchExecutionJobs saveToDb(BatchExecutionRequestDto batchExeRequest,
            PluginConfigInterfaces pluginConfigInterfaces) {
        BatchExecutionJobs batchExeJobEntity = new BatchExecutionJobs();
        batchExeJobEntity.setId(LocalIdGenerator.generateId());
        batchExeJobEntity.setCreateTimestamp(new Date());

        List<ExecutionJobs> exeJobsEntities = new ArrayList<ExecutionJobs>();

        List<ResourceDataDto> resourceDatas = batchExeRequest.getResourceDatas();
        batchExecutionJobsMapper.insert(batchExeJobEntity);

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
            exeJobEntity.setBatchExecutionJobId(batchExeJobEntity.getId());

            exeJobEntity.setConfirmToken(resourceData.getConfirmToken());

            executionJobsMapper.insert(exeJobEntity);

            List<ExecutionJobParameters> parametersList = transFromInputParameterDefinitionToExecutionJobParameter(
                    batchExeRequest.getInputParameterDefinitions(), exeJobEntity, pluginConfigInterfaces);
            exeJobEntity.setParameters(parametersList);
            exeJobEntity.setBatchExecutionJob(batchExeJobEntity);
            exeJobsEntities.add(exeJobEntity);
        }

        batchExeJobEntity.setJobs(exeJobsEntities);

        return batchExeJobEntity;
    }

    private void postProcessBatchExecutionJob(BatchExecutionJobs batchExeJob) {
        batchExeJob.setCompleteTimestamp(new Timestamp(System.currentTimeMillis()));

        batchExecutionJobsMapper.updateByPrimaryKeySelective(batchExeJob);

        List<ExecutionJobs> jobs = batchExeJob.getJobs();
        if (jobs == null || jobs.isEmpty()) {
            return;
        }

        for (ExecutionJobs job : jobs) {
            postProcessExecutionJobs(job);
        }
    }

    private void postProcessExecutionJobs(ExecutionJobs job) {
        job.setCompleteTime(new Timestamp(System.currentTimeMillis()));
        executionJobsMapper.updateByPrimaryKeySelective(job);

        List<ExecutionJobParameters> parameters = job.getParameters();

        if (parameters == null || parameters.isEmpty()) {
            return;
        }

        for (ExecutionJobParameters param : parameters) {
            postProcessExecutionJobParameters(param);
        }

    }

    private void postProcessExecutionJobParameters(ExecutionJobParameters param) {

        executionJobParametersMapper.updateByPrimaryKeySelective(param);
    }

    private List<ExecutionJobParameters> transFromInputParameterDefinitionToExecutionJobParameter(
            List<InputParameterDefinitionDto> inputParameterDefinitionDtos, ExecutionJobs executionJob,
            PluginConfigInterfaces pluginConfigInterfaces) {

        List<ExecutionJobParameters> executionJobParametersList = new ArrayList<ExecutionJobParameters>();
        for (InputParameterDefinitionDto inputParameterDefinitionDto : inputParameterDefinitionDtos) {
            PluginConfigInterfaceParameterDto interfaceParameterDto = inputParameterDefinitionDto.getInputParameter();

            // support object and multiple
            if (inputParameterDefinitionDto.getInputParameterValue() != null) {

                String paramValue = inputParameterDefinitionDto.getInputParameterValue().toString();
                if (Constants.DATA_SENSITIVE.equalsIgnoreCase(interfaceParameterDto.getSensitiveData())) {
                    paramValue = tryEncryptParamValue(paramValue);
                }
                ExecutionJobParameters executionJobParameter = new ExecutionJobParameters(
                        interfaceParameterDto.getName(), interfaceParameterDto.getDataType(),
                        interfaceParameterDto.getMappingType(), interfaceParameterDto.getMappingEntityExpression(),
                        interfaceParameterDto.getMappingSystemVariableName(), interfaceParameterDto.getRequired(),
                        paramValue);
                executionJobParameter.setExecutionJob(executionJob);
                executionJobParameter.setExecutionJobId(executionJob.getId());
                // #2233
                executionJobParameter.setMultiple(interfaceParameterDto.getMultiple());
                executionJobParameter.setRefObjectName(interfaceParameterDto.getRefObjectName());
                executionJobParametersList.add(executionJobParameter);

                executionJobParameter.setParameterDefinition(interfaceParameterDto);

                executionJobParameter.setPluginConfigInterfaces(pluginConfigInterfaces);

                if (Constants.DATA_TYPE_OBJECT.equalsIgnoreCase(executionJobParameter.getDataType())
                        && StringUtils.isNoneBlank(executionJobParameter.getRefObjectName())) {
                    CoreObjectMeta refObjectMeta = tryFetchEnrichCoreObjectMeta(executionJobParameter);
                    if (refObjectMeta != null) {
                        executionJobParameter.setRefObjectMeta(refObjectMeta);
                    }
                }

                executionJobParametersMapper.insert(executionJobParameter);
            } else {
                ExecutionJobParameters executionJobParameter = new ExecutionJobParameters(
                        interfaceParameterDto.getName(), interfaceParameterDto.getDataType(),
                        interfaceParameterDto.getMappingType(), interfaceParameterDto.getMappingEntityExpression(),
                        interfaceParameterDto.getMappingSystemVariableName(), interfaceParameterDto.getRequired(),
                        null);
                executionJobParameter.setExecutionJobId(executionJob.getId());
                executionJobParameter.setExecutionJob(executionJob);
                executionJobParametersList.add(executionJobParameter);

                // #2233
                executionJobParameter.setMultiple(interfaceParameterDto.getMultiple());
                executionJobParameter.setRefObjectName(interfaceParameterDto.getRefObjectName());

                executionJobParameter.setParameterDefinition(interfaceParameterDto);
                executionJobParameter.setPluginConfigInterfaces(pluginConfigInterfaces);

                if (Constants.DATA_TYPE_OBJECT.equalsIgnoreCase(executionJobParameter.getDataType())
                        && StringUtils.isNoneBlank(executionJobParameter.getRefObjectName())) {
                    CoreObjectMeta refObjectMeta = tryFetchEnrichCoreObjectMeta(executionJobParameter);
                    if (refObjectMeta != null) {
                        executionJobParameter.setRefObjectMeta(refObjectMeta);
                    }
                }

                executionJobParametersMapper.insert(executionJobParameter);
            }

        }
        return executionJobParametersList;
    }

    protected CoreObjectMeta tryFetchEnrichCoreObjectMeta(ExecutionJobParameters param) {

        PluginConfigInterfaces intfDef = param.getPluginConfigInterfaces();
        if (intfDef == null) {
            log.debug("Cannot find plugin config interface for {}", param.getId());
            return null;
        }

        PluginConfigs pluginConfig = intfDef.getPluginConfig();
        if (pluginConfig == null) {
            log.debug("Cannot find plugin config for {}", intfDef.getId());
            return null;
        }

        PluginPackages pluginPackage = pluginConfig.getPluginPackage();
        if (pluginPackage == null) {
            log.debug("cannot find plugin package for {}", pluginConfig.getId());
            return null;
        }

        String packageName = pluginPackage.getName();
        if (StringUtils.isBlank(param.getRefObjectName())) {
            log.info("object name value is blank for {}", param.getId());
            return null;
        }

        String configId = pluginConfig.getId();
        String objectName = param.getRefObjectName();
        CoreObjectMeta objectMeta = pluginParamObjectMetaStorage.fetchAssembledCoreObjectMeta(packageName, objectName,
                configId);
        if (objectMeta == null) {
            log.info("Cannot fetch core object meta for interface param:{},and packge:{},objectName:{}", param.getId(),
                    packageName, objectName);
            return null;
        }

        return objectMeta;
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

            return buildResultDataWithError(errorMessage, exeJob);
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
            pluginInputParamMap.put(parameter.getName(), getExpectedValue(parameter));
        }

        pluginInputParamMap.put(CALLBACK_PARAMETER_KEY, exeJob.getRootEntityId());

        if (StringUtils.isNoneBlank(exeJob.getConfirmToken())) {
            log.info("confirm token [{}] found for entity [{}].", exeJob.getConfirmToken(), exeJob.getRootEntityId());
            pluginInputParamMap.put(CONFIRM_TOKEN_KEY, exeJob.getConfirmToken());
        }

        exeJob.setRequestData(pluginInputParamMap);

        PluginInstances pluginInstance = pluginInstanceMgmtService
                .getRunningPluginInstance(pluginPackagesEntity.getName());
        ResultData<Object> responseData = new ResultData<Object>();
        try {
            responseData = pluginServiceStub.callPluginInterface(
                    String.format("%s:%s", pluginInstance.getHost(), pluginInstance.getPort()),
                    pluginConfigInterfaceEntity.getPath(), Lists.newArrayList(pluginInputParamMap),
                    "RequestId-" + Long.toString(System.currentTimeMillis()));

            handleResultData(responseData, exeJob);

        } catch (PluginRemoteCallException e1) {
            log.error("errors while call remote plugin interface.", e1);
            exeJob.setErrorWithMessage(e1.getMessage());
            return buildResultDataWithError(e1, exeJob);
        } catch (Exception e) {
            log.error("errors while call plugin interface", e);
            exeJob.setErrorWithMessage(e.getMessage());
            return buildResultDataWithError(e.getMessage(), exeJob);
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
            return buildResultDataWithError(errorMessage, exeJob);
        }
        PluginResponseStationaryOutput stationaryOutput = stationaryResultData.getOutputs().get(0);
        exeJob.setReturnJson(returnJsonString);
        exeJob.setErrorCode(stationaryOutput.getErrorCode() == null ? RESULT_CODE_ERROR : RESULT_CODE_OK);
        exeJob.setErrorMessage(stationaryOutput.getErrorMessage());
        return responseData;
    }

    @SuppressWarnings("rawtypes")
    public Object getExpectedValue(ExecutionJobParameters parameter) {
        // #2226

        if (isMultiple(parameter.getMultiple())) {
            Object rawParamValue = parameter.getRawValue();
            if (rawParamValue == null) {
                return new ArrayList<>();
            }

            List<Object> clonedListValues = new ArrayList<>();

            if (rawParamValue instanceof List) {

                for (Object val : (List) rawParamValue) {
                    if (val instanceof PluginParamObject) {
                        PluginParamObject objVal = (PluginParamObject) val;
                        PluginParamObject clonedObjVal = PluginParamObject.wipeOffObjectIdAndClone(objVal);
                        if (clonedObjVal != null) {
                            clonedListValues.add(clonedObjVal);
                        }
                    } else {
                        clonedListValues.add(val);
                    }
                }
            } else {
                if (rawParamValue instanceof PluginParamObject) {
                    PluginParamObject objVal = (PluginParamObject) rawParamValue;
                    PluginParamObject clonedObjVal = PluginParamObject.wipeOffObjectIdAndClone(objVal);
                    if (clonedObjVal != null) {
                        clonedListValues.add(clonedObjVal);
                    }
                } else {
                    clonedListValues.add(rawParamValue);
                }
            }

            return clonedListValues;
        } else {

            if (isObjectDataType(parameter.getDataType())) {
                Object rawParamValue = parameter.getRawValue();
                if (rawParamValue == null) {
                    return null;
                }
                if (rawParamValue instanceof PluginParamObject) {
                    PluginParamObject objVal = (PluginParamObject) rawParamValue;
                    PluginParamObject clonedObjVal = PluginParamObject.wipeOffObjectIdAndClone(objVal);

                    return clonedObjVal;
                } else {
                    return rawParamValue;
                }
            } else {
                String paramValue = parameter.getValue();

                if (DATA_TYPE_STRING.equals(parameter.getDataType())) {
                    if (paramValue == null) {
                        return null;
                    }

                    if (parameter.getParameterDefinition() != null && Constants.DATA_SENSITIVE
                            .equalsIgnoreCase(parameter.getParameterDefinition().getSensitiveData())) {
                        paramValue = tryDecryptParamValue(paramValue);
                    }

                    return paramValue;
                }

                if (DATA_TYPE_NUMBER.equals(parameter.getDataType())) {
                    if (paramValue == null) {
                        return 0;
                    }

                    Integer retVal = Integer.valueOf(parameter.getValue());
                    return retVal;
                }

                return paramValue;
            }

        }
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
                .selectAllByConfigInterfaceAndParamType(pluginConfigInterfaceEntity.getId(), Constants.TYPE_OUTPUT);

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
                this.standardEntityOperationService.update(condition, retVal, this.userJwtSsoTokenRestTemplate, null);
            } catch (Exception e) {
                log.error("Exceptions while updating entity.But still keep going to update.", e);
                throw new WecubeCoreException(e.getMessage());
            }

        }
    }

    @SuppressWarnings("unchecked")
    private ResultData<PluginResponseStationaryOutput> buildResultDataWithError(PluginRemoteCallException e1,
            ExecutionJobs exeJob) {
        ResultData<PluginResponseStationaryOutput> errorReultData = new ResultData<PluginResponseStationaryOutput>();
        PluginResponse<?> pluginResponse = e1.getPluginResponse();
        if (pluginResponse == null) {
            PluginResponseStationaryOutput errOut = new PluginResponseStationaryOutput(
                    PluginResponseStationaryOutput.ERROR_CODE_FAILED, e1.getErrorMessage(), exeJob.getRootEntityId());
            List<PluginResponseStationaryOutput> outputs = Lists.newArrayList(errOut);
            errorReultData.setOutputs(outputs);

            return errorReultData;
        }

        List<?> resultData = pluginResponse.getOutputs();
        if (resultData == null || resultData.isEmpty()) {
            PluginResponseStationaryOutput errOut = new PluginResponseStationaryOutput(
                    PluginResponseStationaryOutput.ERROR_CODE_FAILED, e1.getErrorMessage(), exeJob.getRootEntityId());
            List<PluginResponseStationaryOutput> outputs = Lists.newArrayList(errOut);
            errorReultData.setOutputs(outputs);

            return errorReultData;
        }

        List<PluginResponseStationaryOutput> outputs = new ArrayList<>();
        for (Object resultObj : resultData) {
            if (resultObj instanceof Map) {
                Map<String, Object> resultMap = (Map<String, Object>) resultObj;
                String errCode = (String) resultMap.get("errorCode");
                String errorMessage = (String) resultMap.get("errorMessage");
                String callbackParameter = (String) resultMap.get("callbackParameter");

                PluginResponseStationaryOutput errOut = new PluginResponseStationaryOutput(errCode, errorMessage,
                        callbackParameter);
                outputs.add(errOut);
            } else {
                PluginResponseStationaryOutput errOut = new PluginResponseStationaryOutput(
                        PluginResponseStationaryOutput.ERROR_CODE_FAILED, e1.getErrorMessage(),
                        exeJob.getRootEntityId());
                outputs.add(errOut);
            }
        }
        errorReultData.setOutputs(outputs);
        return errorReultData;
    }

    private ResultData<PluginResponseStationaryOutput> buildResultDataWithError(String errorMessage,
            ExecutionJobs exeJob) {
        ResultData<PluginResponseStationaryOutput> errorReultData = new ResultData<PluginResponseStationaryOutput>();
        errorReultData.setOutputs(Lists.newArrayList(new PluginResponseStationaryOutput(
                PluginResponseStationaryOutput.ERROR_CODE_FAILED, errorMessage, exeJob.getRootEntityId())));
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
            String dataType = param.getDataType();
            if (Constants.DATA_TYPE_OBJECT.equalsIgnoreCase(dataType)) {
                calculateInputParamValueFromObject(exeJob, param, pluginPackageName);
            } else {
                if (MAPPING_TYPE_ENTITY.equals(mappingType)) {
                    calculateInputParamValueFromExpr(exeJob, param);
                }

                if (MAPPING_TYPE_SYSTEM_VARIABLE.equals(mappingType)) {
                    calculateInputParamValueFromSystemVariable(exeJob, param, pluginPackageName);
                }
            }

        }
        return;
    }

    private void calculateInputParamValueFromObject(ExecutionJobs executionJob, ExecutionJobParameters parameter,
            String pluginPackageName) {
        CoreObjectVarCalculationContext calCtx = new CoreObjectVarCalculationContext();
        calCtx.setExternalCacheMap(null);
        calCtx.setProcDefInfo(null);
        calCtx.setProcInstInfo(null);
        calCtx.setRootEntityDataId(executionJob.getRootEntityId());
        calCtx.setRootEntityFullDataId(null);
        calCtx.setRootEntityTypeId(null);
        calCtx.setTaskNodeDefInfo(null);
        calCtx.setTaskNodeInstInfo(null);

        CoreObjectMeta objectMeta = parameter.getRefObjectMeta();

        if (objectMeta == null) {
            if (StringUtils.isBlank(parameter.getMappingEntityExpression())
                    || parameter.getMappingEntityExpression().endsWith(".NONE")) {
                return;
            }

            String entityAttrName = null;
            List<EntityQueryExprNodeInfo> currExprNodeInfos = this.entityQueryExpressionParser
                    .parse(parameter.getMappingEntityExpression());
            if (currExprNodeInfos == null || currExprNodeInfos.isEmpty()) {
                // nothing
            } else {
                EntityQueryExprNodeInfo leafNode = currExprNodeInfos.get(currExprNodeInfos.size() - 1);
                entityAttrName = leafNode.getQueryAttrName();
            }
            List<Map<String, Object>> rawObjectMapVals = pluginParamObjectVarCalculator
                    .calculateRawObjectVarList(objectMeta, calCtx, parameter.getMappingEntityExpression());
            if (isMultiple(parameter.getMultiple())) {
                List<Object> objectVals = new ArrayList<>();
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

                String paramValue = JsonUtils.toJsonString(objectVals);
                parameter.setValue(paramValue);
                parameter.setRawValue(objectVals);

            } else {
                if (!rawObjectMapVals.isEmpty()) {
                    Map<String, Object> recordMap = rawObjectMapVals.get(0);
                    if (StringUtils.isBlank(entityAttrName)) {
                        String paramValue = JsonUtils.toJsonString(recordMap);
                        parameter.setValue(paramValue);
                        parameter.setRawValue(recordMap);
                    } else {
                        Object objVal = recordMap.get(entityAttrName);
                        if (objVal != null) {
                            String paramValue = objVal.toString();
                            parameter.setValue(paramValue);
                            parameter.setRawValue(objVal);
                        }
                    }
                }

            }

            return;
        }

        // store objects here
        List<CoreObjectVar> objectVars = pluginParamObjectVarCalculator.calculateCoreObjectVarList(objectMeta, calCtx,
                parameter.getMappingEntityExpression());

        if (objectVars == null || objectVars.isEmpty()) {
            log.info("Got empty object values for : {}", objectMeta.getName());
            return;
        }

        if (isMultiple(parameter.getMultiple())) {
            List<Object> objectVals = new ArrayList<>();
            for (CoreObjectVar objectVar : objectVars) {
                PluginParamObject paramObject = pluginParamObjectVarAssembleService.marshalPluginParamObject(objectVar,
                        calCtx);
                objectVals.add(paramObject);

                pluginParamObjectVarStorageService.storeCoreObjectVar(objectVar);
            }

            String paramValue = JsonUtils.toJsonString(objectVals);
            parameter.setValue(paramValue);
            parameter.setRawValue(objectVals);

            return;
        } else {

            if (objectVars.size() > 1) {
                String errMsg = String.format("Required data type %s but %s objects returned.", parameter.getDataType(),
                        objectVars.size());
                log.error(errMsg);

                throw new WecubeCoreException(errMsg);
            }

            CoreObjectVar objectVar = objectVars.get(0);

            PluginParamObject paramObject = pluginParamObjectVarAssembleService.marshalPluginParamObject(objectVar,
                    calCtx);

            pluginParamObjectVarStorageService.storeCoreObjectVar(objectVar);

            String paramValue = JsonUtils.toJsonString(paramObject);
            parameter.setValue(paramValue);
            parameter.setRawValue(paramObject);

            return;
        }
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
            if (parameter.getParameterDefinition() != null && Constants.DATA_SENSITIVE
                    .equalsIgnoreCase(parameter.getParameterDefinition().getSensitiveData())) {
                String cipheredVal = tryEncryptParamValue(sVal);
                parameter.setValue(cipheredVal);
            } else {
                parameter.setValue(sVal);
            }
        }
    }

    private void calculateInputParamValueFromExpr(ExecutionJobs executionJob, ExecutionJobParameters parameter) {
        String mappingEntityExpression = parameter.getMappingEntityExpression();
        if (log.isDebugEnabled()) {
            log.debug("calculate param value from entity, name={} ,expression={}", parameter.getName(),
                    mappingEntityExpression);
        }

        //TODO #2314
        EntityOperationRootCondition criteria = new EntityOperationRootCondition(mappingEntityExpression,
                executionJob.getRootEntityId());

        List<Object> attrValsPerExpr = standardEntityOperationService.queryAttributeValues(criteria,
                userJwtSsoTokenRestTemplate, null);

        if (attrValsPerExpr == null || attrValsPerExpr.isEmpty()) {
            if (FIELD_REQUIRED.equals(parameter.getRequired())) {
                String errorMessage = String.format(
                        "returned empty data while fetch the mandatory input parameter[%s] with expression[%s] and root entity ID[%s]",
                        parameter.getName(), mappingEntityExpression, criteria.getEntityIdentity());
                log.error(errorMessage);
                executionJob.setErrorWithMessage(errorMessage);
                executionJob.setPrepareException(new WecubeCoreException(errorMessage));
                throw new WecubeCoreException("3004", errorMessage, parameter.getName(), mappingEntityExpression,
                        criteria.getEntityIdentity());
            } else {
                if (isMultiple(parameter.getMultiple())) {
                    List<Object> rawParamValue = new ArrayList<>();
                    parameter.setValue(JsonUtils.toJsonString(rawParamValue));
                    parameter.setRawValue(rawParamValue);

                    return;
                } else {
                    parameter.setValue(null);
                    parameter.setRawValue(null);

                    return;
                }
            }
        }

        if (isMultiple(parameter.getMultiple())) {
            String paramValue = JsonUtils.toJsonString(attrValsPerExpr);
            parameter.setValue(paramValue);

            parameter.setRawValue(attrValsPerExpr);
        } else {

            Object rawParamValue = tryDetermineRawParamValue(attrValsPerExpr, parameter.getDataType());

            // #2046
            String paramValue = (rawParamValue == null ? null : rawParamValue.toString());
            if (parameter.getParameterDefinition() != null && Constants.DATA_SENSITIVE
                    .equalsIgnoreCase(parameter.getParameterDefinition().getSensitiveData())) {
                paramValue = tryEncryptParamValue(paramValue);
            }
            parameter.setValue(paramValue);

            parameter.setRawValue(rawParamValue);
        }
    }

    private Object tryDetermineRawParamValue(List<Object> attrValsPerExpr, String dataType) {
        if (attrValsPerExpr.size() == 1) {
            return attrValsPerExpr.get(0);
        }

        if (Constants.DATA_TYPE_STRING.equalsIgnoreCase(dataType)) {
            return assembleValueList(attrValsPerExpr);
        } else {
            return attrValsPerExpr.get(0);
        }
    }

    protected String assembleValueList(List<Object> retDataValues) {
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        sb.append("[");

        for (Object dv : retDataValues) {
            if (!isFirst) {
                sb.append(",");
            } else {
                isFirst = false;
            }

            sb.append(dv == null ? "" : dv);
        }

        sb.append("]");

        return sb.toString();
    }

    private boolean isObjectDataType(String dataType) {
        return Constants.DATA_TYPE_OBJECT.equalsIgnoreCase(dataType);
    }

    private boolean isMultiple(String multipleFlag) {
        return Constants.DATA_MULTIPLE.equalsIgnoreCase(multipleFlag);
    }
}
