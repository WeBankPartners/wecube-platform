package com.webank.wecube.core.service;

import static com.webank.wecube.core.domain.plugin.PluginConfig.Status.ONLINE;
import static com.webank.wecube.core.support.cmdb.dto.v2.PaginationQuery.defaultQueryObject;
import static com.webank.wecube.core.utils.CollectionUtils.pickRandomOne;
import static com.webank.wecube.core.utils.SystemUtils.getTempFolderPath;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.trim;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.webank.wecube.core.commons.ApplicationProperties;
import com.webank.wecube.core.commons.ApplicationProperties.CmdbDataProperties;
import com.webank.wecube.core.commons.ApplicationProperties.PluginProperties;
import com.webank.wecube.core.commons.WecubeCoreException;
import com.webank.wecube.core.domain.plugin.PluginConfig;
import com.webank.wecube.core.domain.plugin.PluginConfigInterface;
import com.webank.wecube.core.domain.plugin.PluginConfigInterfaceParameter;
import com.webank.wecube.core.domain.plugin.PluginInstance;
import com.webank.wecube.core.domain.plugin.PluginPackage;
import com.webank.wecube.core.domain.plugin.PluginTriggerCommand;
import com.webank.wecube.core.domain.workflow.CiRoutineItem;
import com.webank.wecube.core.domain.workflow.ProcessDefinitionTaskServiceEntity;
import com.webank.wecube.core.domain.workflow.ProcessTaskEntity;
import com.webank.wecube.core.domain.workflow.TaskNodeExecLogEntity;
import com.webank.wecube.core.domain.workflow.TaskNodeExecVariableEntity;
import com.webank.wecube.core.interceptor.UsernameStorage;
import com.webank.wecube.core.jpa.PluginConfigRepository;
import com.webank.wecube.core.jpa.PluginInstanceRepository;
import com.webank.wecube.core.jpa.PluginPackageRepository;
import com.webank.wecube.core.jpa.ProcessDefinitionEntityRepository;
import com.webank.wecube.core.jpa.ProcessDefinitionTaskServiceEntityRepository;
import com.webank.wecube.core.jpa.ProcessTaskEntityRepository;
import com.webank.wecube.core.jpa.TaskNodeExecLogEntityRepository;
import com.webank.wecube.core.jpa.TaskNodeExecVariableEntityRepository;
import com.webank.wecube.core.service.workflow.PluginWorkService;
import com.webank.wecube.core.support.cmdb.CmdbServiceV2Stub;
import com.webank.wecube.core.support.cmdb.dto.v2.AdhocIntegrationQueryDto;
import com.webank.wecube.core.support.cmdb.dto.v2.CatCodeDto;
import com.webank.wecube.core.support.cmdb.dto.v2.CatTypeDto;
import com.webank.wecube.core.support.cmdb.dto.v2.CategoryDto;
import com.webank.wecube.core.support.cmdb.dto.v2.CiTypeAttrDto;
import com.webank.wecube.core.support.cmdb.dto.v2.IntegrationQueryDto;
import com.webank.wecube.core.support.cmdb.dto.v2.OperateCiDto;
import com.webank.wecube.core.support.cmdb.dto.v2.PaginationQuery;
import com.webank.wecube.core.support.cmdb.dto.v2.PaginationQueryResult;
import com.webank.wecube.core.support.cmdb.dto.v2.Relationship;
import com.webank.wecube.core.support.plugin.PluginInterfaceInvoker;
import com.webank.wecube.core.support.plugin.PluginInterfaceInvoker.InvocationResult;
import com.webank.wecube.core.support.plugin.PluginServiceStub;
import com.webank.wecube.core.support.plugin.dto.PluginRequest.PluginLoggingInfoRequest;
import com.webank.wecube.core.support.plugin.dto.PluginRequest.PluginLoggingInfoSearchDetailRequest;
import com.webank.wecube.core.support.plugin.dto.PortalRequestBody.SearchPluginLogRequest;
import com.webank.wecube.core.support.s3.S3Client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
public class PluginInstanceService {

    private static final String CONSTANT_RUN_COMMAND = "Run command: ";
    private static final String CONSTANT_CAT_CAT_TYPE = "cat.catType";

    @Autowired
    private PluginProperties pluginProperties;

    @Autowired
    private CmdbDataProperties cmdbDataProperties;

    @Autowired
    PluginInstanceRepository pluginInstanceRepository;
    @Autowired
    PluginPackageRepository pluginPackageRepository;
    @Autowired
    PluginConfigRepository pluginConfigRepository;
    @Autowired
    ScpService scpService;
    @Autowired
    PluginServiceStub pluginServiceStub;
    @Autowired
    CommandService commandService;
    @Autowired
    CmdbServiceV2Stub cmdbServiceV2Stub;

    @Autowired
    PluginWorkService pluginWorkService;
    @Autowired
    ApplicationProperties.S3Properties s3Properties;

    @Autowired
    ProcessDefinitionTaskServiceEntityRepository processDefinitionTaskServiceEntityRepository;

    @Autowired
    ProcessTaskEntityRepository processTaskEntityRepository;

    @Autowired
    ProcessDefinitionEntityRepository processDefinitionEntityRepository;

    @Autowired
    TaskNodeExecLogEntityRepository taskNodeExecLogEntityRepository;

    @Autowired
    TaskNodeExecVariableEntityRepository taskNodeExecVariableEntityRepository;

    @Autowired
    PluginConfigService pluginConfigService;
    

    private static final String CONSTANT_CONFIRM = "confirm";
    private static final int PLUGIN_WORK_SUCC = 0;
    private static final int PLUGIN_WORK_FAIL = 1;
    private static final int PLUGIN_DEFAULT_START_PORT = 20000;
    private static final int PLUGIN_DEFAULT_END_PORT = 30000;
    private static final String PLUGIN_INSTANCE_STATUS_RUNNING = "RUNNING";
    
    private static final String CUSTOM_SERVICE_BEAN_PREFIX = "srvBeanST-";

    public Integer getAvailablePortByHostIp(String hostIp) {
        if (!(isHostIpAvailable(hostIp))) {
            throw new RuntimeException("Invalid host ip");
        }

        List<Integer> ports = pluginInstanceRepository.findPortsByHostOrderByPort(hostIp,
                PLUGIN_INSTANCE_STATUS_RUNNING);
        if (ports.size() == 0) {
            return PLUGIN_DEFAULT_START_PORT;
        }
        for (int i = PLUGIN_DEFAULT_START_PORT; i < PLUGIN_DEFAULT_END_PORT; i++) {
            if (!ports.contains(i)) {
                return i;
            }
        }
        throw new WecubeCoreException("There is no available ports in specified host");
    }

    private boolean isHostIpAvailable(String hostIp) {
        String[] hostIps = pluginProperties.getPluginHosts();
        for (String ip : hostIps) {
            if (ip.equals(hostIp)) {
                return true;
            }
        }
        return false;
    }

    public boolean isIpValidity(String ip) {
        if (ip != null && !ip.isEmpty()) {
            String ipValidityRegularExpression = "^(([1-9])|([1-9][0-9])|(1[0-9][0-9])|(2[0-4][0-9])|(25[0-5]))((\\.([0-9]|([1-9][0-9])|(1[0-9][0-9])|(2[0-4][0-9])|(25[0-5]))){3})$";
            return ip.matches(ipValidityRegularExpression);
        }
        return false;
    }

    public void createPluginInstance(Integer packageId, String hostIp, Integer port, String additionalStartParameters)
            throws Exception {

        if (!isIpValidity(hostIp)) {
            throw new WecubeCoreException("Invalid IP: " + hostIp);
        }

        if (!isHostIpAvailable(hostIp)) {
            throw new WecubeCoreException("Unavailable host ip");
        }

        Optional<PluginPackage> pluginPackageResult = pluginPackageRepository.findById(packageId);
        if (!pluginPackageResult.isPresent())
            throw new WecubeCoreException("Plugin package id does not exist, id = " + packageId);
        PluginPackage pp = pluginPackageResult.get();

        List<PluginInstance> pluginInstances = pluginInstanceRepository.findByHostAndPort(hostIp, port);
        if (pluginInstances.size() != 0) {
            throw new IllegalArgumentException(String.format(
                    "The port[%d] of host[%s] is already in use by container[%s], please try to reassignment port",
                    port, hostIp, pluginInstances.get(0).getInstanceContainerId()));
        }

        // download package from MinIO
        String tmpFolderName = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
        String tmpFilePath = getTempFolderPath() + tmpFolderName + "/" + pp.getDockerImageFile();
        String s3FileName = pp.getName() + "/" + pp.getVersion() + "/" + pp.getDockerImageFile();

        log.info("bucketName={}, fileName={}, tmpFilePath= {}", pluginProperties.getPluginPackageBucketName(),
                s3FileName, tmpFilePath);
        new S3Client(s3Properties.getEndpoint(), s3Properties.getAccessKey(), s3Properties.getSecretKey())
                .downFile(pluginProperties.getPluginPackageBucketName(), s3FileName, tmpFilePath);

        log.info("scp from local:{} to remote{}", tmpFilePath, pluginProperties.getPluginDeployPath());
        try {
            scpService.put(hostIp, pluginProperties.getDefaultHostSshPort(), pluginProperties.getDefaultHostSshUser(),
                    pluginProperties.getDefaultHostSshPassword(), tmpFilePath, pluginProperties.getPluginDeployPath());
        } catch (Exception e) {
            log.error("Put file to remote host meet error: {}", e.getMessage());
            throw new WecubeCoreException("Put file to remote host meet error: " + e.getMessage());
        }

        String loadCmd = "docker load -i " + pluginProperties.getPluginDeployPath().trim()
                + pp.getDockerImageFile().trim();
        log.info(CONSTANT_RUN_COMMAND + loadCmd);
        try {
            commandService.runAtRemote(hostIp, pluginProperties.getDefaultHostSshUser(),
                    pluginProperties.getDefaultHostSshPassword(), pluginProperties.getDefaultHostSshPort(), loadCmd);
        } catch (Exception e) {
        	log.error("Run command [{}] meet error: {}",loadCmd, e.getMessage());
            throw new WecubeCoreException(String.format("Run remote command meet error: %s", e.getMessage()));
        }

        String runCommand = "docker run -d -p " + port + ":" + pp.getContainerPort().trim() + " "
                + additionalStartParameters + " -it " + pp.getDockerImageRepository().trim() + ":"
                + pp.getDockerImageTag();
        log.info(CONSTANT_RUN_COMMAND + runCommand);
        
        String containerId;
        try {
        	containerId= commandService.runAtRemote(hostIp, pluginProperties.getDefaultHostSshUser(),
        			pluginProperties.getDefaultHostSshPassword(), pluginProperties.getDefaultHostSshPort(), runCommand);
        log.info("'{}' command run successfully", runCommand);
        } catch (Exception e) {
        	log.error("Run command [{}] meet error: {}",runCommand, e.getMessage());
            throw new WecubeCoreException(String.format("Run remote command meet error: %s", e.getMessage()));
		}

        PluginInstance newPluginInstance = new PluginInstance(null, pp, containerId, hostIp, port,
                PluginInstance.STATUS_RUNNING);
        pluginInstanceRepository.save(newPluginInstance);
    }

    public void removePluginInstanceById(Integer instanceId) throws Exception {
        Optional<PluginInstance> instanceRepository = pluginInstanceRepository.findById(instanceId);
        PluginInstance pluginInstance;
        if (instanceRepository.isPresent()) {
            pluginInstance = instanceRepository.get();
        } else {
            throw new WecubeCoreException("Invalid instance id: " + instanceId);
        }
        String command = "docker rm -f " + pluginInstance.getInstanceContainerId();
        log.info(CONSTANT_RUN_COMMAND + command);
        CommandService c = new CommandService();
        c.runAtRemote(pluginInstance.getHost(), pluginProperties.getDefaultHostSshUser(),
                pluginProperties.getDefaultHostSshPassword(), pluginProperties.getDefaultHostSshPort(), command);

        log.info("Command({}) run successfully", command);

        pluginInstanceRepository.deleteById(pluginInstance.getId());
        log.info("Update plugin_instance table successfully");
    }

    public List<PluginInstance> getAllInstances() {
        return Lists.newArrayList(pluginInstanceRepository.findAll());
    }

    public List<PluginInstance> getAvailableInstancesByPackageId(int packageId) {
        return pluginInstanceRepository.findByStatusAndPackageId(PluginInstance.STATUS_RUNNING, packageId);
    }

    public Map<Integer, Object> getPluginsLog(SearchPluginLogRequest request) {
        if (request.getInstanceIds().size() == 0) {
            throw new WecubeCoreException("InstancesId list is empty");
        }
        Map<Integer, Object> results = new LinkedHashMap<>();
        List<Integer> instanceIds = request.getInstanceIds();
        for (Integer instanceId : instanceIds) {
            Object result = getPluginLogByKeyWord(request.getPluginRequest(), instanceId);
            results.put(instanceId, result);
        }
        return results;
    }

    public Object getPluginLogByKeyWord(PluginLoggingInfoRequest request, int instanceId) {
        return pluginServiceStub.getPluginLogByKeyWord(getInstanceAddress(instanceId), request);
    }

    public Object getPluginLogDetail(PluginLoggingInfoSearchDetailRequest request, int instanceId) {
        return pluginServiceStub.getPluginLogDetail(getInstanceAddress(instanceId), request);
    }

    public void handleProcessInstanceEndEvent(PluginTriggerCommand cmd) {
        log.info("handle process instance end event");
        String bizKey = cmd.getProcessInstanceBizKey();

        List<ProcessTaskEntity> tasks = processTaskEntityRepository.findTaskByProcessInstanceKey(bizKey);

        if (tasks.isEmpty()) {
            log.error("cannot find process tasks with cmd={}", cmd);
            throw new WecubeCoreException("cannot find process tasks");
        }

        ProcessTaskEntity task = tasks.get(0);

        UsernameStorage.getIntance().set(task.getOperator());

        List<OperateCiDto> operateCiObjects = pluginWorkService.getOperateCiObjects(bizKey);
        
        if (!operateCiObjects.isEmpty()) {
            log.info("to confirm ci while process instance ended");
            cmdbServiceV2Stub.operateCiForState(operateCiObjects, CONSTANT_CONFIRM);
        }
    }

    public void invokePluginInterface(PluginTriggerCommand cmd) {
        log.info("invoke plugin interface with cmd={}", cmd);

        String procDefKey = cmd.getProcessDefinitionKey();
        int procDefVersion = cmd.getProcessDefinitionVersion();
        String taskNodeId = cmd.getServiceTaskNodeId();
        String processInstanceBizKey = cmd.getProcessInstanceBizKey();
        int rootCiTypeId = getCiTypeIdAndSetOperator(processInstanceBizKey);
        String operator = UsernameStorage.getIntance().get();
        if (taskNodeId.indexOf(CUSTOM_SERVICE_BEAN_PREFIX) >= 0) {
            taskNodeId = taskNodeId.substring(CUSTOM_SERVICE_BEAN_PREFIX.length());
        }

        log.info("processing taskNode:{}", taskNodeId);

        List<ProcessDefinitionTaskServiceEntity> taskEntities = processDefinitionTaskServiceEntityRepository
                .findTaskServicesByProcDefKeyAndVersionAndTaskNodeId(procDefKey, procDefVersion, taskNodeId);

        if (taskEntities == null || taskEntities.isEmpty()) {
            log.error("cannot find task service information to proceed plugin invocation with pluginTriggerCommand={}",
                    cmd);
            throw new WecubeCoreException("configuration errors");
        }

        if (taskEntities.size() > 1) {
            throw new WecubeCoreException("configuration errors");
        }

        ProcessDefinitionTaskServiceEntity taskEntity = taskEntities.get(0);

        String serviceName = taskEntity.getBindServiceName();
        PluginConfigInterface inf = pluginConfigService.getPluginConfigInterfaceByServiceName(serviceName);

        PluginConfig pluginConfig = inf.getPluginConfig();
        if (!ONLINE.equals(pluginConfig.getStatus())) {
            throw new WecubeCoreException(
                    "Invoke plugin failure due to unexpected status - " + pluginConfig.getStatus());
        }
        if (inf.getCmdbQueryTemplateId() == null) {
            throw new WecubeCoreException("Invoke plugin failure due to no cmdb query template found.");
        }
        PluginPackage pluginPackage = pluginConfig.getPluginPackage();

        List<Map<String, Object>> pluginParameters = queryCmdb(inf, processInstanceBizKey);

        List<PluginInstance> availableInstances = pluginInstanceRepository
                .findByStatusAndPackageId(PluginInstance.STATUS_RUNNING, pluginPackage.getId());
        if (isEmpty(availableInstances))
            throw new WecubeCoreException(String.format("No running instance found for plugin package %s/%s [id:%s].",
                    pluginPackage.getName(), pluginPackage.getVersion(), pluginPackage.getId()));

        PluginInstance chosenInstance = chooseOne(availableInstances);
        
        pluginWorkService.saveTaskNodeInvocationParameter(cmd, processInstanceBizKey, rootCiTypeId, operator, serviceName, inf,
        		pluginConfig, pluginParameters, chosenInstance, taskNodeId);


        new Thread(new PluginInterfaceInvoker(getInstanceAddress(chosenInstance), operator, rootCiTypeId, serviceName,
                inf.getPath(), inf, cmd, pluginParameters, pluginServiceStub, this::handlePluginResponse)).start();
    }

    private String getPluginSeedCodeById(Integer id) {
        CatCodeDto catCode = cmdbServiceV2Stub.getEnumCodeById(id);
        if (catCode == null) {
            return null;
        }
        return catCode.getCode();
    }

    private int getCiTypeIdAndSetOperator(String processInstanceBizKey) {
        List<ProcessTaskEntity> taskInstanceEntities = processTaskEntityRepository
                .findTaskByProcessInstanceKey(processInstanceBizKey);

        if (taskInstanceEntities.isEmpty()) {
            log.error("cannot find ProcessTaskEntity,processInstanceBizKey={}", processInstanceBizKey);
            throw new WecubeCoreException("system data configuration error");
        }

        ProcessTaskEntity taskInstance = taskInstanceEntities.get(0);

        setOperator(taskInstance.getOperator());

        return taskInstance.getRootCiTypeId();
    }

    private void setOperator(String operator) {
        UsernameStorage.getIntance().set(operator);
    }


    private String marshalRequestData(Object data) {
        if (data == null) {
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            String json = mapper.writeValueAsString(data);
            return json;
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private String getInstanceAddress(Integer instanceId) {
        Optional<PluginInstance> instanceRepository = pluginInstanceRepository.findById(instanceId);
        if (!instanceRepository.isPresent())
            throw new WecubeCoreException("Invalid InstanceId: " + instanceId);
        PluginInstance instance = instanceRepository.get();
        return trim(instance.getHost()) + ":" + trim(instance.getPort().toString());
    }

    private void handlePluginResponse(InvocationResult pluginResponse) {
        PluginTriggerCommand cmd = pluginResponse.getTriggerCmd();
        PluginConfigInterface inf = pluginResponse.getInf();

        setOperatorWithChecking(pluginResponse.getOperator());

        String processInstanceBizKey = cmd.getProcessInstanceBizKey();
        String serviceCode = pluginResponse.getServiceName();
        String executionId = cmd.getProcessExecutionId();

        String procDefKey = cmd.getProcessDefinitionKey();

        String taskNodeId = cmd.getServiceTaskNodeId();

        if (taskNodeId.indexOf(CUSTOM_SERVICE_BEAN_PREFIX) >= 0) {
            taskNodeId = taskNodeId.substring(CUSTOM_SERVICE_BEAN_PREFIX.length());
        }

        if (pluginResponse.getPluginResponse() == null) {
            log.error("notify workflow engine with failure message.");
            pluginWorkService.logFailureExecution(processInstanceBizKey, taskNodeId, "no response");
            pluginWorkService.responseServiceTaskResult(processInstanceBizKey, executionId, serviceCode,
                    PLUGIN_WORK_FAIL);
            return;
        }

        if (pluginResponse.getPluginResponse().isEmpty()) {
            log.warn("empty plugin response returned");
            pluginWorkService.logCompleteExecution(processInstanceBizKey, taskNodeId,
                    marshalRequestData(pluginResponse.getPluginResponse()), "response data is blank");

            pluginWorkService.responseServiceTaskResult(processInstanceBizKey, executionId, serviceCode,
                    PLUGIN_WORK_SUCC);

            return;
        }

        updateCiDataByInvocationResult(pluginResponse);

        int rootCiTypeId = pluginResponse.getRootCiTypeId();
        CatTypeDto catTypeDto = cmdbServiceV2Stub.getEnumCategoryTypeByCiTypeId(rootCiTypeId);

        PaginationQuery queryObject = defaultQueryObject().addEqualsFilter("catName", "orchestration")
                .addEqualsFilter("catTypeId", catTypeDto.getCatTypeId());
        PaginationQueryResult<CategoryDto> result = cmdbServiceV2Stub.queryEnumCategories(queryObject);

        CategoryDto category = result.getContents().get(0);
        PaginationQuery queryObjectCatCodes = defaultQueryObject().addEqualsFilter("code", procDefKey)
                .addEqualsFilter("catId", category.getCatId());
        PaginationQueryResult<CatCodeDto> resultCatCodes = cmdbServiceV2Stub.queryEnumCodes(queryObjectCatCodes);
        CatCodeDto catCode = resultCatCodes.getContents().get(0);

        ParsePluginParametersResult parsePluginParametersResult = parsePluginParameters(pluginResponse, catCode);

        operateCiByInvocationResult(pluginResponse, parsePluginParametersResult);

        pluginWorkService.logCompleteExecution(processInstanceBizKey, taskNodeId,
                marshalRequestData(pluginResponse.getPluginResponse()), "success");

        log.info("update cmdb and notify workflow engine with success message. Response is " + pluginResponse);
        pluginWorkService.responseServiceTaskResult(processInstanceBizKey, executionId, serviceCode, PLUGIN_WORK_SUCC);
    }

    private void setOperatorWithChecking(String operator) {
        if (UsernameStorage.getIntance().get() == null) {
            setOperator(operator);
        }
    }

    private void operateCiByInvocationResult(InvocationResult pluginResponse,
            ParsePluginParametersResult parsePluginParametersResult) {
        List<OperateCiDto> operateCiObjects = parsePluginParametersResult.getOperateCiObjects();
        List<Map<String, Object>> cmdbWfUpdateItems = parsePluginParametersResult.getCmdbWfUpdateItems();
        PluginConfigInterface inf = pluginResponse.getInf();

        String beforeStatus = inf.getFilterStatus();
        String afterStatus = inf.getResultStatus();

        if (StringUtils.isNotBlank(beforeStatus) && StringUtils.isNotBlank(afterStatus)
                && (beforeStatus.equals(afterStatus))) {
            log.info("to confirm ci data,size={}", operateCiObjects.size());
            cmdbServiceV2Stub.operateCiForState(operateCiObjects, CONSTANT_CONFIRM);
        }

        if (StringUtils.isNotBlank(beforeStatus) && StringUtils.isNotBlank(afterStatus)
                && (!beforeStatus.equals(afterStatus))) {
            log.info("to confirm ci data,size={}", operateCiObjects.size());
            cmdbServiceV2Stub.operateCiForState(operateCiObjects, CONSTANT_CONFIRM);

            log.info("###### to update ci data after confirm, size={}, beforeStatus={},afterStatus={} ",
                    cmdbWfUpdateItems.size(), beforeStatus, afterStatus);
            List<String> operations = cmdbServiceV2Stub.queryOperation(Integer.parseInt(beforeStatus),
                    Integer.parseInt(afterStatus));
            if (operations != null && (operations.size() >= 1)) {
                if (operations.size() > 1) {
                    log.error("more than one operations found for beforeStatus={},afterStatus={}", beforeStatus,
                            afterStatus);
                    throw new WecubeCoreException("more than one operations found");
                }

                String oper = operations.get(0);

                cmdbServiceV2Stub.operateCiForState(operateCiObjects, oper);
                cmdbServiceV2Stub.operateCiForState(operateCiObjects, CONSTANT_CONFIRM);
            }
            cmdbServiceV2Stub.updateCiData(inf.getPluginConfig().getCmdbCiTypeId(), cmdbWfUpdateItems.toArray());
        }
    }

    @Data
    @AllArgsConstructor
    private static class ParsePluginParametersResult {
        private List<Map<String, Object>> cmdbWfUpdateItems;
        private List<OperateCiDto> operateCiObjects;
    }

    private ParsePluginParametersResult parsePluginParameters(InvocationResult pluginResponse, CatCodeDto catCode) {
        List<Map<String, Object>> cmdbWfUpdateItems = new ArrayList<Map<String, Object>>();
        List<OperateCiDto> operateCiObjects = new ArrayList<OperateCiDto>();

        List<Map<String, Object>> pluginParameters = pluginResponse.getPluginParameters();
        PluginConfigInterface inf = pluginResponse.getInf();
        String processInstanceBizKey = pluginResponse.getTriggerCmd().getProcessInstanceBizKey();

        for (Map<String, Object> pluginParametersMap : pluginParameters) {
            String guid = (String) pluginParametersMap.get("guid");
            if (StringUtils.isBlank(guid)) {
                log.warn("cannot find guid as plugin parameter,data={}", pluginParametersMap);
                continue;
            }

            Integer ciTypeId = inf.getPluginConfig().getCmdbCiTypeId();

            OperateCiDto dto = new OperateCiDto(guid, ciTypeId);
            operateCiObjects.add(dto);

            Map<String, Object> cmdbWfUpdateItem = new HashMap<String, Object>();
            cmdbWfUpdateItem.put("guid", guid);
            cmdbWfUpdateItem.put("biz_key", processInstanceBizKey);

            if (ciTypeId == pluginResponse.getRootCiTypeId()) {
                cmdbWfUpdateItem.put("orchestration", catCode.getCodeId());
            }

            cmdbWfUpdateItems.add(cmdbWfUpdateItem);
        }

        return new ParsePluginParametersResult(cmdbWfUpdateItems, operateCiObjects);
    }

    private void updateCiDataByInvocationResult(InvocationResult pluginResponse) {
        PluginConfigInterface inf = pluginResponse.getInf();
        for (Object obj : pluginResponse.getPluginResponse()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> responseItems = (Map<String, Object>) obj;

            Map<String, Object> cmdbReqItems = calCmdbUpdateRequestItems(responseItems, inf.getOutputParameters());

            if (cmdbReqItems.isEmpty()) {
                log.info("no need to update cmdb with such response,serviceCode={}", pluginResponse.getServiceName());
                continue;
            }

            Integer ciTypeId = inf.getPluginConfig().getCmdbCiTypeId();

            cmdbServiceV2Stub.updateCiData(ciTypeId, cmdbReqItems);
        }
    }

    private Map<String, Object> calCmdbUpdateRequestItems(Map<String, Object> pluginRespItems,
            Set<PluginConfigInterfaceParameter> predefinedOutputParams) {
        Map<String, Object> reqItems = new HashMap<>();
        for (PluginConfigInterfaceParameter param : predefinedOutputParams) {
            String paramName = param.getName();
            Object paramValue = pluginRespItems.get(paramName);
            String cmdbColumnName = param.getCmdbColumnName();

            if (cmdbColumnName == null || (cmdbColumnName.length() < 1)) {
                continue;
            }

            reqItems.put(cmdbColumnName, paramValue);
        }

        return reqItems;
    }

    private Set<PluginConfigInterfaceParameter> filterPluginConfigInterfaceParameter(
            Set<PluginConfigInterfaceParameter> srcInputParameters, String mappingType) {
        Set<PluginConfigInterfaceParameter> inputParameters = new HashSet<>();
        srcInputParameters.forEach(p -> {
            if (mappingType.equals(p.getMappingType())) {
                inputParameters.add(p);
            }
        });
        return inputParameters;
    }

    private List<Map<String, Object>> queryCmdb(PluginConfigInterface pluginConfigInterface, String bizKey) {
        Set<PluginConfigInterfaceParameter> fullInputParameters = pluginConfigInterface.getInputParameters();
        Set<PluginConfigInterfaceParameter> intQryInputParameters = filterPluginConfigInterfaceParameter(
                fullInputParameters, "CMDB_CI_TYPE");
        Set<PluginConfigInterfaceParameter> enumKindInputParameters = filterPluginConfigInterfaceParameter(
                fullInputParameters, "CMDB_ENUM_CODE");

        List<String> resultColumns = intQryInputParameters.stream()
                .map(PluginConfigInterfaceParameter::getCmdbColumnName).collect(Collectors.toList());

        Map<String, Object> equalsFilters = new LinkedHashMap<>();
        Map<String, Object> inFilters = new LinkedHashMap<>();
        equalsFilters.put(cmdbDataProperties.getBusinessKeyAttributeName(), bizKey);
        if (StringUtils.isNotEmpty(pluginConfigInterface.getFilterStatus())) {
            if (pluginConfigInterface.getFilterStatus().contains(",")) {
                inFilters.put(cmdbDataProperties.getStatusAttributeName(),
                        Lists.newArrayList(pluginConfigInterface.getFilterStatus().split(",")));
            } else {
                equalsFilters.put(cmdbDataProperties.getStatusAttributeName(), pluginConfigInterface.getFilterStatus());
            }
        }

        List<Map<String, Object>> ciDataList = cmdbServiceV2Stub.executeIntegratedQueryTemplate(
                pluginConfigInterface.getCmdbQueryTemplateId(), equalsFilters, inFilters, resultColumns);
        List<Map<String, Object>> convertedCiDataList = ciDataList.stream()
                .map(m -> convertCmdbColumnNamesToPluginParameterNames(intQryInputParameters, m))
                .collect(Collectors.toList());
        log.info("Converted Ci data list is " + convertedCiDataList);

        convertedCiDataList.forEach(m -> enumKindInputParameters.forEach(p -> {
            Integer enumCodeId = p.getCmdbEnumCode();
            if (enumCodeId != null) {
                String enumCodeValue = getPluginSeedCodeById(enumCodeId);
                if (StringUtils.isNotBlank(enumCodeValue)) {
                    m.put(p.getName(), enumCodeValue);
                }
            }

        }));

        log.info("Ci data list is " + convertedCiDataList);
        return convertedCiDataList;
    }

    private Map<String, Object> convertCmdbColumnNamesToPluginParameterNames(
            Set<PluginConfigInterfaceParameter> inputParameters, Map<String, Object> data) {
        Map<String, Object> convertedData = new HashMap<>();
        for (PluginConfigInterfaceParameter inputParameter : inputParameters) {
            String pluginColumnName = inputParameter.getName();
            String cmdbColumnName = inputParameter.getCmdbColumnName();
            Object value;
            if (data.containsKey(cmdbColumnName)) {
                value = data.get(cmdbColumnName);
            } else if (data.containsKey(pluginColumnName)) {
                value = data.get(pluginColumnName);
            } else {
                throw new WecubeCoreException(
                        String.format("Column %s not found in ci data [%s].", cmdbColumnName, data));
            }

            value = extractValueFromSpecialCiType(value, inputParameters, data);
            if (value == null) {
                log.warn("value from cmdb is null,column name:{} plugin column name:{}", cmdbColumnName,
                        pluginColumnName);
            }

            if (value != null && "number".equalsIgnoreCase(inputParameter.getDatatype())
                    && String.class.isAssignableFrom(value.getClass())) {
                value = Integer.parseInt((String) value);
            }
            convertedData.put(pluginColumnName, value);
        }
        return convertedData;
    }

    @SuppressWarnings("unchecked")
    private Object extractValueFromSpecialCiType(Object rawValue, Set<PluginConfigInterfaceParameter> inputParameters,
            Map<String, Object> data) {
        // check if it is enum type
        if (rawValue != null && rawValue instanceof Map) {
            Map<String, Object> valueMap = (Map<String, Object>) rawValue;
            Object valueOfCode = valueMap.get("code");
            if (valueOfCode != null) {
                //check if diff conf variable
                if (isDiffConfVariable(valueOfCode)) {
                    return extractDiffConfVariable((String)valueOfCode, inputParameters, data);
                } else {
                    return valueOfCode;
                }
            }
        }else if(rawValue != null && rawValue instanceof List){
            //check if multi enum values
            List<?> valueAsList = (List<?>)rawValue;
            return extractMultiDiffConfVariables(valueAsList,inputParameters, data);
        }
        
        return rawValue;
    }
    
    private Object extractMultiDiffConfVariables(List<?> valueAsList, Set<PluginConfigInterfaceParameter> inputParameters,
            Map<String, Object> data){
        String retAsKeyValuePairs = "";
        for(Object obj : valueAsList){
            if(obj != null && obj instanceof Map){
                @SuppressWarnings("unchecked")
                Map<String, Object> objMap = (Map<String, Object>)obj;
                String enumNameAsStr = (String) objMap.get("value");
                Object codeAsObj = objMap.get("code");
                
                Object codeValue = null;
                
                if(codeAsObj != null && isDiffConfVariable(codeAsObj)){
                    codeValue = extractDiffConfVariable((String)codeAsObj, inputParameters, data);
                }else{
                    codeValue = codeAsObj;
                }
                
                retAsKeyValuePairs += String.format("%s=%s,", enumNameAsStr, codeValue);
            }else{
                log.warn("not a enum value, {}", obj);
            }
        }
        
        if(retAsKeyValuePairs.endsWith(",")){
            retAsKeyValuePairs = retAsKeyValuePairs.substring(0, retAsKeyValuePairs.length() - 1);
        }
        
        return retAsKeyValuePairs;
    }
    
    
    private Object extractDiffConfVariable(String valueOfCode, Set<PluginConfigInterfaceParameter> inputParameters,
            Map<String, Object> data){
        log.info("start to extract diff conf variable for value:{}", valueOfCode);
        
        List<CiRoutineItem> ciRoutineItems = null;
        try {
            ciRoutineItems = buildRoutines(valueOfCode);
        } catch (IOException e) {
            log.error("build routines errors", e);
            return valueOfCode;
        }
        
        if(ciRoutineItems == null){
            log.error("diff conf expression cannot parse correctly");
            return valueOfCode;
        }
        
        String rootGuid = extractRootCiGuid(inputParameters, data);
        if(StringUtils.isBlank(rootGuid)){
            log.error("guid is expected but cannot find for value of code:{}", valueOfCode);
            return valueOfCode;
        }
        
        try {
            List<Map<String, Object>> retDataList = buildIntegrationQueryAndGetQueryResult(rootGuid, ciRoutineItems);
            if(retDataList != null && !retDataList.isEmpty()){
                Map<String,Object> retDataMap = retDataList.get(0);
                return retDataMap.get("tail$attr");
            }
        } catch (IOException e) {
            log.error("errors while retrieving data from cmdb", e);
            return valueOfCode;
        }
        
        log.warn("didnt extract expected diff conf variable for value of code:{}", valueOfCode);
        return valueOfCode;
    }
    
    private List<Map<String, Object>> buildIntegrationQueryAndGetQueryResult(
            String rootCiDataGuid, List<CiRoutineItem> routines) throws IOException {

        AdhocIntegrationQueryDto rootDto = buildRootDto(routines.get(0), rootCiDataGuid);

        IntegrationQueryDto childQueryDto = travelRoutine(routines, rootDto, 1);
        if (childQueryDto != null) {
            rootDto.getCriteria().setChildren(Collections.singletonList(childQueryDto));
        }
        
        return cmdbServiceV2Stub.adhocIntegrationQuery(rootDto).getContents();
    }
    
    protected AdhocIntegrationQueryDto buildRootDto(CiRoutineItem rootRoutineItem, String rootCiGuid) {
        AdhocIntegrationQueryDto dto = new AdhocIntegrationQueryDto();
        PaginationQuery queryRequest = new PaginationQuery();

        queryRequest.addEqualsFilter("root$guid", rootCiGuid);

        IntegrationQueryDto root = new IntegrationQueryDto();
        dto.setCriteria(root);
        dto.setQueryRequest(queryRequest);

        root.setName("root");
        root.setCiTypeId(rootRoutineItem.getCiTypeId());
        root.setAttrs(Arrays.asList(getGuidAttrIdByCiTypeId(rootRoutineItem.getCiTypeId())));
        root.setAttrKeyNames(Arrays.asList("root$guid"));

        return dto;
    }
    
    protected Integer getGuidAttrIdByCiTypeId(int ciTypeId) {
        List<CiTypeAttrDto> attrDtos = cmdbServiceV2Stub.getCiTypeAttributesByCiTypeId(ciTypeId);
        for (CiTypeAttrDto dto : attrDtos) {
            if ("guid".equalsIgnoreCase(dto.getPropertyName())) {
                return dto.getCiTypeAttrId();
            }
        }

        return null;
    }

    protected IntegrationQueryDto travelRoutine(List<CiRoutineItem> routines, AdhocIntegrationQueryDto rootDto, int position) {
        if (position >= (routines.size() - 1)) {
            return null;
        }

        CiRoutineItem item = routines.get(position);
        IntegrationQueryDto dto = new IntegrationQueryDto();
        dto.setName("a" + position);
        dto.setCiTypeId(item.getCiTypeId());

        Relationship parentRs = new Relationship();
        parentRs.setAttrId(item.getParentRs().getAttrId());
        parentRs.setIsReferedFromParent(item.getParentRs().getIsReferedFromParent() == 1);
        dto.setParentRs(parentRs);

        IntegrationQueryDto childDto = travelRoutine(routines, rootDto, position+1);
        if (childDto == null) {
            CiRoutineItem attrItem = routines.get(position+1);
            if(item.getCiTypeId() != attrItem.getCiTypeId()){
                throw new WecubeCoreException("citype id is error");
            }
            
            dto.setAttrs(Arrays.asList(attrItem.getParentRs().getAttrId()));

            List<String> attrKeyNames = new ArrayList<String>();
            attrKeyNames.add("tail$attr");

            dto.setAttrKeyNames(attrKeyNames);

            
        } else {
            dto.setChildren(Arrays.asList(childDto));
        }

        return dto;
    }
    
    private String extractRootCiGuid(Set<PluginConfigInterfaceParameter> inputParameters,
            Map<String, Object> data){
        for(PluginConfigInterfaceParameter parameter : inputParameters){
            if("guid".equals(parameter.getName())){
                return (String) data.get(parameter.getCmdbColumnName());
            }
        }
        
        return null;
    }
    
    
    private List<CiRoutineItem> buildRoutines(String ciRoutineExpStr) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(ciRoutineExpStr.getBytes(),
                mapper.getTypeFactory().constructCollectionType(ArrayList.class, CiRoutineItem.class));
    }

    private boolean isDiffConfVariable(Object value) {
        if (value instanceof String) {
            String valStr = (String) value;
            if (valStr.startsWith("[{") && valStr.endsWith("}]")) {
                return true;
            }
        }

        return false;
    }

    // Use random selection as a short term solution of load-balancing.
    // This can be enhanced once the health check function is ready.
    // This can be enhanced when a weighted based algorithm is required in the
    // future.
    public PluginInstance chooseOne(List<PluginInstance> pluginInstances) {
        return pickRandomOne(pluginInstances);
    }

    public List<PluginInstance> getRunningPluginInstances(String pluginName) {
        Optional<PluginPackage> pkg = pluginPackageRepository.findLatestVersionByName(pluginName);
        if (!pkg.isPresent()) {
            throw new WecubeCoreException(String.format("Plugin pacakge [%s] not found.", pluginName));
        }

        List<PluginInstance> instances = pluginInstanceRepository
                .findByStatusAndPackageId(PluginInstance.STATUS_RUNNING, pkg.get().getId());
        if (instances == null || instances.size() == 0) {
            throw new WecubeCoreException(String.format("No instance for plugin [%s] is available.", pluginName));
        }
        return instances;
    }

    public String getInstanceAddress(PluginInstance instance) {
        return trim(instance.getHost()) + ":" + trim(instance.getPort().toString());
    }
}
