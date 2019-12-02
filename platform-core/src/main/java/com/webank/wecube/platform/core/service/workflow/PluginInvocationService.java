package com.webank.wecube.platform.core.service.workflow;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.SystemVariable;
import com.webank.wecube.platform.core.domain.plugin.*;
import com.webank.wecube.platform.core.entity.workflow.*;
import com.webank.wecube.platform.core.jpa.workflow.*;
import com.webank.wecube.platform.core.model.datamodel.DataModelExpressionToRootData;
import com.webank.wecube.platform.core.model.workflow.InputParamAttr;
import com.webank.wecube.platform.core.model.workflow.InputParamObject;
import com.webank.wecube.platform.core.model.workflow.PluginInvocationCommand;
import com.webank.wecube.platform.core.model.workflow.PluginInvocationResult;
import com.webank.wecube.platform.core.service.PluginInstanceService;
import com.webank.wecube.platform.core.service.SystemVariableService;
import com.webank.wecube.platform.core.service.datamodel.ExpressionService;
import com.webank.wecube.platform.core.service.plugin.PluginConfigService;
import com.webank.wecube.platform.core.service.workflow.PluginInvocationProcessor.PluginInterfaceInvocationContext;
import com.webank.wecube.platform.core.service.workflow.PluginInvocationProcessor.PluginInterfaceInvocationResult;
import com.webank.wecube.platform.core.service.workflow.PluginInvocationProcessor.PluginInvocationOperation;
import com.webank.wecube.platform.core.support.plugin.PluginServiceStub;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 
 * @author gavin
 *
 */
@Service
public class PluginInvocationService {
    private static final Logger log = LoggerFactory.getLogger(PluginInvocationService.class);

    private static final String MAPPING_TYPE_CONTEXT = "context";
    private static final String MAPPING_TYPE_ENTITY = "entity";
    private static final String MAPPING_TYPE_SYSTEM_VARIABLE = "system_variable";

    private static final String CALLBACK_PARAMETER_KEY = "callbackParameter";

    private static final int RESULT_CODE_OK = 0;
    private static final int RESULT_CODE_ERR = 1;

    @Autowired
    private PluginInvocationResultService pluginInvocationResultService;

    @Autowired
    private PluginServiceStub pluginServiceStub;

    @Autowired
    private PluginInvocationProcessor pluginInvocationProcessor;

    @Autowired
    private ProcInstInfoRepository procInstInfoRepository;

    @Autowired
    private TaskNodeInstInfoRepository taskNodeInstInfoRepository;

    @Autowired
    private TaskNodeDefInfoRepository taskNodeDefInfoRepository;

    @Autowired
    private PluginConfigService pluginConfigService;

    @Autowired
    private ProcExecBindingRepository procExecBindingRepository;

    @Autowired
    private TaskNodeExecParamRepository taskNodeExecParamRepository;

    @Autowired
    private SystemVariableService systemVariableService;

    @Autowired
    private PluginInstanceService pluginInstanceService;

    @Autowired
    private TaskNodeParamRepository taskNodeParamRepository;

    @Autowired
    private TaskNodeExecRequestRepository taskNodeExecRequestRepository;

    @Autowired
    private ExpressionService expressionService;

    public void handleProcessInstanceEndEvent(PluginInvocationCommand cmd) {
        if (log.isInfoEnabled()) {
            log.info("handle end event:{}", cmd);
        }

        Date currTime = new Date();

        ProcInstInfoEntity procInstEntity = procInstInfoRepository.findOneByProcInstKernelId(cmd.getProcInstId());
        procInstEntity.setUpdatedTime(currTime);
        procInstEntity.setStatus(ProcInstInfoEntity.COMPLETED_STATUS);
        procInstInfoRepository.save(procInstEntity);

        log.info("updated process instance {} to {}", procInstEntity.getId(), ProcInstInfoEntity.COMPLETED_STATUS);

        List<TaskNodeInstInfoEntity> nodeInstEntities = taskNodeInstInfoRepository
                .findAllByProcInstId(procInstEntity.getId());

        for (TaskNodeInstInfoEntity n : nodeInstEntities) {
            if ("endEvent".equals(n.getNodeType())) {
                n.setUpdatedTime(currTime);
                n.setStatus(TaskNodeInstInfoEntity.COMPLETED_STATUS);

                taskNodeInstInfoRepository.save(n);

                log.info("updated node {} to {}", n.getId(), TaskNodeInstInfoEntity.COMPLETED_STATUS);
            }
        }

    }

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
            log.error("errors while processing {} {}", cmd.getClass().getSimpleName(), cmd, e);
            pluginInvocationResultService.responsePluginInterfaceInvocation(
                    new PluginInvocationResult().parsePluginInvocationCommand(cmd).withResultCode(RESULT_CODE_ERR));

            if (taskNodeInstEntity != null) {
                log.info("mark task node instance {} as {}", taskNodeInstEntity.getId(),
                        TaskNodeInstInfoEntity.FAULTED_STATUS);
                taskNodeInstEntity.setStatus(TaskNodeInstInfoEntity.FAULTED_STATUS);
                taskNodeInstEntity.setUpdatedTime(new Date());

                taskNodeInstInfoRepository.save(taskNodeInstEntity);
            }

        }
    }

    protected void doInvokePluginInterface(ProcInstInfoEntity procInstEntity, TaskNodeInstInfoEntity taskNodeInstEntity,
            PluginInvocationCommand cmd) {
        TaskNodeDefInfoEntity taskNodeDefEntity = retrieveTaskNodeDefInfoEntity(procInstEntity.getProcDefId(),
                cmd.getNodeId());
        List<ProcExecBindingEntity> nodeObjectBindings = retrieveProcExecBindingEntities(taskNodeInstEntity);
        PluginConfigInterface pluginConfigInterface = retrievePluginConfigInterface(taskNodeDefEntity, cmd.getNodeId());

        List<InputParamObject> inputParamObjs = calculateInputParamObjects(procInstEntity, taskNodeInstEntity,
                taskNodeDefEntity, nodeObjectBindings, pluginConfigInterface);

        PluginInterfaceInvocationContext ctx = new PluginInterfaceInvocationContext() //
                .withNodeObjectBindings(nodeObjectBindings) //
                .withPluginConfigInterface(pluginConfigInterface) //
                .withProcInstEntity(procInstEntity) //
                .withTaskNodeInstEntity(taskNodeInstEntity)//
                .withTaskNodeDefEntity(taskNodeDefEntity)//
                .withPluginInvocationCommand(cmd);

        parsePluginInstance(ctx);

        buildTaskNodeExecRequestEntity(ctx);
        List<Map<String, Object>> pluginParameters = calculateInputParameters(inputParamObjs, ctx.getRequestId());

        PluginInvocationOperation operation = new PluginInvocationOperation() //
                .withCallback(this::handlePluginInterfaceInvocationResult) //
                .withPluginServiceStub(this.pluginServiceStub) //
                .withPluginParameters(pluginParameters) //
                .withInstanceHost(ctx.getInstanceHost()) //
                .withInterfacePath(ctx.getInterfacePath()) //
                .withPluginInterfaceInvocationContext(ctx) //
                .withRequestId(ctx.getRequestId());

        pluginInvocationProcessor.process(operation);
    }

    private void buildTaskNodeExecRequestEntity(PluginInterfaceInvocationContext ctx) {

        TaskNodeExecRequestEntity formerRequestEntity = taskNodeExecRequestRepository
                .findCurrentEntityByNodeInstId(ctx.getTaskNodeInstEntity().getId());

        if (formerRequestEntity != null) {
            formerRequestEntity.setCurrent(false);
            formerRequestEntity.setUpdatedTime(new Date());
            taskNodeExecRequestRepository.save(formerRequestEntity);
        }

        String requestId = UUID.randomUUID().toString();

        TaskNodeInstInfoEntity taskNodeInstEntity = ctx.getTaskNodeInstEntity();

        PluginInvocationCommand cmd = ctx.getPluginInvocationCommand();
        TaskNodeExecRequestEntity requestEntity = new TaskNodeExecRequestEntity();
        requestEntity.setNodeInstId(taskNodeInstEntity.getId());
        requestEntity.setRequestId(requestId);
        requestEntity.setRequestUrl(ctx.getInstanceHost() + ctx.getInterfacePath());

        requestEntity.setExecutionId(cmd.getExecutionId());
        requestEntity.setNodeId(cmd.getNodeId());
        requestEntity.setNodeName(cmd.getNodeName());
        requestEntity.setProcDefKernelId(cmd.getProcDefId());
        requestEntity.setProcDefKernelKey(cmd.getProcDefKey());
        requestEntity.setProcDefVersion(cmd.getProcDefVersion());
        requestEntity.setProcInstKernelId(cmd.getProcInstId());
        requestEntity.setProcInstKernelKey(cmd.getProcInstKey());

        taskNodeExecRequestRepository.save(requestEntity);

        ctx.withTaskNodeExecRequestEntity(requestEntity);
        ctx.setRequestId(requestId);

    }

    private void parsePluginInstance(PluginInterfaceInvocationContext ctx) {
        PluginConfigInterface pluginConfigInterface = ctx.getPluginConfigInterface();
        PluginInstance pluginInstance = retrieveAvailablePluginInstance(pluginConfigInterface);
        String interfacePath = pluginConfigInterface.getPath();
        String instanceHost = String.format("%s:%s", pluginInstance.getHost(), pluginInstance.getPort());

        ctx.setInstanceHost(instanceHost);
        ctx.setInterfacePath(interfacePath);
    }

    private List<InputParamObject> calculateInputParamObjects(ProcInstInfoEntity procInstEntity,
            TaskNodeInstInfoEntity taskNodeInstEntity, TaskNodeDefInfoEntity taskNodeDefEntity,
            List<ProcExecBindingEntity> nodeObjectBindings, PluginConfigInterface pluginConfigInterface) {

        List<InputParamObject> inputParamObjs = new ArrayList<InputParamObject>();

        Set<PluginConfigInterfaceParameter> configInterfaceInputParams = pluginConfigInterface.getInputParameters();
        for (ProcExecBindingEntity nodeObjectBinding : nodeObjectBindings) {
            String entityTypeId = nodeObjectBinding.getEntityTypeId();
            String entityDataId = nodeObjectBinding.getEntityDataId();

            InputParamObject inputObj = new InputParamObject();
            inputObj.setEntityTypeId(entityTypeId);
            inputObj.setEntityDataId(entityDataId);

            for (PluginConfigInterfaceParameter param : configInterfaceInputParams) {
                String paramName = param.getName();
                String paramType = param.getDataType();

                inputObj.addAttrNames(paramName);

                InputParamAttr inputAttr = new InputParamAttr();
                inputAttr.setName(paramName);
                inputAttr.setType(paramType);

                List<Object> objectVals = new ArrayList<Object>();
                //
                String mappingType = param.getMappingType();
                inputAttr.setMapType(mappingType);

                if (MAPPING_TYPE_ENTITY.equals(mappingType)) {
                    String mappingEntityExpression = param.getMappingEntityExpression();

                    if (log.isDebugEnabled()) {
                        log.debug("expression:{}", mappingEntityExpression);
                    }

                    DataModelExpressionToRootData criteria = new DataModelExpressionToRootData(mappingEntityExpression,
                            entityDataId);

                    List<Object> attrValsPerExpr = expressionService.fetchData(criteria);

                    if (attrValsPerExpr == null) {
                        log.error("returned null while fetch data with expression:{}", mappingEntityExpression);
                        attrValsPerExpr = new ArrayList<>();
                    }

                    objectVals.addAll(attrValsPerExpr);

                }

                if (MAPPING_TYPE_CONTEXT.equals(mappingType)) {
                    String curTaskNodeDefId = taskNodeDefEntity.getId();
                    TaskNodeParamEntity nodeParamEntity = taskNodeParamRepository
                            .findOneByTaskNodeDefIdAndParamName(curTaskNodeDefId, paramName);

                    if (nodeParamEntity == null) {
                        log.error("mapping type is {} but node parameter entity is null for {}", mappingType,
                                curTaskNodeDefId);
                        throw new WecubeCoreException("Task node parameter entity does not exist.");
                    }

                    String bindNodeId = nodeParamEntity.getBindNodeId();
                    String bindParamType = nodeParamEntity.getBindParamType();
                    String bindParamName = nodeParamEntity.getBindParamName();

                    // get by procInstId and nodeId
                    TaskNodeInstInfoEntity bindNodeInstEntity = taskNodeInstInfoRepository
                            .findOneByProcInstIdAndNodeId(procInstEntity.getId(), bindNodeId);

                    if (bindNodeInstEntity == null) {
                        log.error("Bind node instance entity does not exist for {} {}", procInstEntity.getId(),
                                bindNodeId);
                        throw new WecubeCoreException("");
                    }

                    TaskNodeExecRequestEntity requestEntity = taskNodeExecRequestRepository
                            .findCurrentEntityByNodeInstId(bindNodeInstEntity.getId());

                    List<TaskNodeExecParamEntity> execParamEntities = taskNodeExecParamRepository
                            .findAllByRequestIdAndParamNameAndParamType(requestEntity.getRequestId(), bindParamName,
                                    bindParamType);

                    if (execParamEntities == null || execParamEntities.isEmpty()) {
                        if ("Y".equals(param.getRequired())) {
                            log.error(
                                    "parameter entity does not exist but such plugin parameter is mandatory for {} {}",
                                    bindParamName, bindParamType);
                            throw new WecubeCoreException("Parameter entity does not exist.");
                        }
                    }

                    Object finalInputParam = calculateContextValue(paramType, execParamEntities);
                    
                    log.info("context final input parameter {} {} {}" , paramName, paramType, finalInputParam);

                    objectVals.add(finalInputParam);
                }

                if (MAPPING_TYPE_SYSTEM_VARIABLE.equals(mappingType)) {
                    String svId = param.getMappingSystemVariableId();
                    SystemVariable sVariable = systemVariableService.getSystemVariableById(svId);

                    if (sVariable == null && "Y".equals(param.getRequired())) {
                        log.error("variable is null but is mandatory for {}", paramName);
                        throw new WecubeCoreException("Variable is null but mandatory.");
                    }

                    String sVal = sVariable.getValue();
                    if (StringUtils.isBlank(sVal)) {
                        sVal = sVariable.getDefaultValue();
                    }

                    if (StringUtils.isBlank(sVal) && "Y".equals(param.getRequired())) {
                        log.error("variable is blank but is mandatory for {}", paramName);
                        throw new WecubeCoreException("Variable is blank but mandatory.");
                    }

                    objectVals.add(sVal);
                }

                inputAttr.addValues(objectVals);

                inputObj.addAttrs(inputAttr);
            }

            inputParamObjs.add(inputObj);

        }

        return inputParamObjs;
    }
    
    private Object calculateContextValue(String paramType, List<TaskNodeExecParamEntity> execParamEntities){
        List<Object> retDataValues = parseDataValueFromContext(execParamEntities);
        if(retDataValues == null || retDataValues.isEmpty()){
            return null;
        }
        
        if(retDataValues.size() == 1){
            return retDataValues.get(0);
        }
        
        if("string".equalsIgnoreCase(paramType)){
            return assembleValueList(retDataValues);
        }else{
            return retDataValues;
        }
    }
    
    private String assembleValueList(List<Object> retDataValues){
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        sb.append("[");
        
        for(Object dv : retDataValues){
            if(!isFirst){
                sb.append(",");
            }else{
                isFirst = false;
            }
            
            sb.append(dv);
        }
        
        sb.append("]");
        
        return sb.toString();
    }
    
    private List<Object> parseDataValueFromContext(List<TaskNodeExecParamEntity> execParamEntities){
        List<Object> retDataValues = new ArrayList<>();
        if(execParamEntities == null){
            return retDataValues;
        }
        
        for(TaskNodeExecParamEntity e : execParamEntities){
            retDataValues.add(fromString(e.getParamDataValue(), e.getParamDataType()));
        }
        
        return retDataValues;
    }

    private PluginConfigInterface retrievePluginConfigInterface(TaskNodeDefInfoEntity taskNodeDefEntity,
            String nodeId) {
        String serviceId = retrieveServiceId(taskNodeDefEntity, nodeId);
        PluginConfigInterface pluginConfigInterface = pluginConfigService
                .getPluginConfigInterfaceByServiceName(serviceId);

        if (pluginConfigInterface == null) {
            log.error("Plugin config interface does not exist for {} {} {}", taskNodeDefEntity.getId(), nodeId,
                    serviceId);
            throw new WecubeCoreException("Plugin config interface does not exist.");
        }

        return pluginConfigInterface;
    }

    private List<ProcExecBindingEntity> retrieveProcExecBindingEntities(TaskNodeInstInfoEntity taskNodeInstEntity) {
        List<ProcExecBindingEntity> nodeObjectBindings = procExecBindingRepository
                .findAllTaskNodeBindings(taskNodeInstEntity.getProcInstId(), taskNodeInstEntity.getId());

        if (nodeObjectBindings == null) {
            log.warn("node object bindings is empty for {} {}", taskNodeInstEntity.getProcInstId(),
                    taskNodeInstEntity.getId());
            nodeObjectBindings = new ArrayList<>();
        }

        return nodeObjectBindings;
    }

    private String retrieveServiceId(TaskNodeDefInfoEntity taskNodeDefEntity, String nodeId) {
        String serviceId = taskNodeDefEntity.getServiceId();
        if (StringUtils.isBlank(serviceId)) {
            log.error("service ID is invalid for {} {}", taskNodeDefEntity.getProcDefId(), nodeId);
            throw new WecubeCoreException("Service ID is invalid.");
        }

        if (log.isDebugEnabled()) {
            log.info("retrieved service id {} for {},{}", serviceId, taskNodeDefEntity.getProcDefId(), nodeId);
        }
        return serviceId;
    }

    private TaskNodeInstInfoEntity retrieveTaskNodeInstInfoEntity(Integer procInstId, String nodeId) {
        TaskNodeInstInfoEntity taskNodeInstEntity = taskNodeInstInfoRepository.findOneByProcInstIdAndNodeId(procInstId,
                nodeId);
        if (taskNodeInstEntity == null) {
            log.error("Task node instance does not exist for {} {}", procInstId, nodeId);
            throw new WecubeCoreException("Task node instance does not exist.");
        }

        if (!TaskNodeInstInfoEntity.IN_PROGRESS_STATUS.equals(taskNodeInstEntity.getStatus())) {
            String originalStatus = taskNodeInstEntity.getStatus();
            taskNodeInstEntity.setStatus(TaskNodeInstInfoEntity.IN_PROGRESS_STATUS);
            taskNodeInstEntity.setUpdatedTime(new Date());

            if (log.isDebugEnabled()) {
                log.debug("task node instance {} update status from {} to {}", taskNodeInstEntity.getId(),
                        originalStatus, taskNodeInstEntity.getStatus());
            }

            taskNodeInstInfoRepository.save(taskNodeInstEntity);
        }

        return taskNodeInstEntity;
    }

    private TaskNodeDefInfoEntity retrieveTaskNodeDefInfoEntity(String procDefId, String nodeId) {
        TaskNodeDefInfoEntity taskNodeDefEntity = taskNodeDefInfoRepository
                .findOneWithProcessIdAndNodeIdAndStatus(procDefId, nodeId, TaskNodeDefInfoEntity.DEPLOYED_STATUS);

        if (taskNodeDefEntity == null) {
            log.error("Task node definition does not exist for {} {} {}", procDefId, nodeId,
                    TaskNodeDefInfoEntity.DEPLOYED_STATUS);
            throw new WecubeCoreException("Task node definition does not exist.");
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
        while (round < 10) {
            procInstEntity = procInstInfoRepository.findOneByProcInstKernelId(procInstKernelId);

            if (procInstEntity != null) {
                break;
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }

            round++;
        }

        if (procInstEntity == null) {
            log.error("Process instance info does not exist for id:{}", procInstKernelId);
            throw new WecubeCoreException("Process instance information does not exist.");
        }

        if (!ProcInstInfoEntity.IN_PROGRESS_STATUS.equals(procInstEntity.getStatus())) {

            String orignalStatus = procInstEntity.getStatus();
            procInstEntity.setUpdatedTime(new Date());
            procInstEntity.setStatus(ProcInstInfoEntity.IN_PROGRESS_STATUS);

            if (log.isDebugEnabled()) {
                log.debug("process instance {} update status from {} to {}", procInstEntity.getId(), orignalStatus,
                        ProcInstInfoEntity.IN_PROGRESS_STATUS);
            }

            procInstInfoRepository.save(procInstEntity);
        }

        return procInstEntity;
    }

    private List<Map<String, Object>> calculateInputParameters(List<InputParamObject> inputParamObjs,
            String requestId) {
        List<Map<String, Object>> pluginParameters = new ArrayList<Map<String, Object>>();

        int objectId = 0;

        for (InputParamObject ipo : inputParamObjs) {
            if (log.isDebugEnabled()) {
                log.debug("process input parameters for entity:{} {}", ipo.getEntityTypeId(), ipo.getEntityDataId());
            }

            String sObjectId = String.valueOf(objectId);
            String entityTypeId = ipo.getEntityTypeId();
            String entityDataId = ipo.getEntityDataId();

            Map<String, Object> inputMap = new HashMap<String, Object>();
            inputMap.put(CALLBACK_PARAMETER_KEY, entityDataId);

            for (InputParamAttr attr : ipo.getAttrs()) {
                TaskNodeExecParamEntity e = new TaskNodeExecParamEntity();
                e.setRequestId(requestId);
                e.setParamName(attr.getName());
                e.setParamType(TaskNodeExecParamEntity.PARAM_TYPE_REQUEST);
                e.setParamDataType(attr.getType());
                e.setObjectId(sObjectId);
                e.setParamDataValue(attr.getValuesAsString());
                e.setEntityDataId(entityDataId);
                e.setEntityTypeId(entityTypeId);

                taskNodeExecParamRepository.save(e);

                inputMap.put(attr.getName(), attr.getExpectedValue());
            }

            pluginParameters.add(inputMap);

            objectId++;
        }

        return pluginParameters;
    }

    private PluginInstance retrieveAvailablePluginInstance(PluginConfigInterface itf) {
        PluginConfig config = itf.getPluginConfig();
        PluginPackage pkg = config.getPluginPackage();
        String pluginName = pkg.getName();

        List<PluginInstance> instances = pluginInstanceService.getRunningPluginInstances(pluginName);

        return instances.get(0);

    }

    protected String asString(Object val, String sType) {
        if (val == null) {
            return null;
        }
        if (val instanceof String && "string".equals(sType)) {
            return (String) val;
        }

        if (val instanceof Integer && "int".equals(sType)) {
            return String.valueOf(val);
        }

        return val.toString();

    }

    protected Object fromString(String val, String sType) {
        if ("string".equals(sType)) {
            return val;
        }

        if (StringUtils.isBlank(val)) {
            return null;
        }

        if ("int".equals(sType)) {
            return Integer.parseInt(val);
        }

        // TODO
        return val;
    }

    public void handlePluginInterfaceInvocationResult(PluginInterfaceInvocationResult pluginInvocationResult,
            PluginInterfaceInvocationContext ctx) {
        if (log.isDebugEnabled()) {
            log.debug("handle plugin interface invocation result");
        }

        if (!pluginInvocationResult.isSuccess() || pluginInvocationResult.hasErrors()) {
            handleErrorInvocationResult(pluginInvocationResult, ctx);

            return;
        }

        PluginConfigInterface pci = ctx.getPluginConfigInterface();
        if ("Y".equalsIgnoreCase(pci.getIsAsyncProcessing())) {
            log.info("such interface is asynchronous service : {} ", pci.getServiceName());
            return;
        }

        List<Object> resultData = pluginInvocationResult.getResultData();

        if (resultData == null) {
            handleNullResultData(pluginInvocationResult, ctx);
            return;
        }

        PluginInvocationResult result = new PluginInvocationResult()
                .parsePluginInvocationCommand(ctx.getPluginInvocationCommand());
        try {
            handleResultData(pluginInvocationResult, ctx, resultData);
            result.setResultCode(RESULT_CODE_OK);
            pluginInvocationResultService.responsePluginInterfaceInvocation(result);
            handlePluginInterfaceInvocationSuccess(pluginInvocationResult, ctx);

            return;
        } catch (Exception e) {
            log.error("result data handling failed", e);
            result.setResultCode(RESULT_CODE_ERR);
            pluginInvocationResultService.responsePluginInterfaceInvocation(result);
            handlePluginInterfaceInvocationFailure(pluginInvocationResult, ctx, "101",
                    "result data handling failed:" + e.getMessage());
        }

        return;
    }

    private void handleErrorInvocationResult(PluginInterfaceInvocationResult pluginInvocationResult,
            PluginInterfaceInvocationContext ctx) {
        PluginInvocationResult result = new PluginInvocationResult()
                .parsePluginInvocationCommand(ctx.getPluginInvocationCommand());

        log.error("system errors:{}", pluginInvocationResult.getErrMsg());
        result.setResultCode(RESULT_CODE_ERR);
        pluginInvocationResultService.responsePluginInterfaceInvocation(result);
        handlePluginInterfaceInvocationFailure(pluginInvocationResult, ctx, "400",
                "system errors:" + trimWithMaxLength(pluginInvocationResult.getErrMsg()));

        return;
    }

    private String trimWithMaxLength(String s) {
        if (s == null) {
            return "";
        }

        if (s.length() < 100) {
            return s;
        }

        return s.substring(0, 100);
    }

    private void handleNullResultData(PluginInterfaceInvocationResult pluginInvocationResult,
            PluginInterfaceInvocationContext ctx) {
        PluginInvocationResult result = new PluginInvocationResult()
                .parsePluginInvocationCommand(ctx.getPluginInvocationCommand());
        PluginConfigInterface pluginConfigInterface = ctx.getPluginConfigInterface();
        Set<PluginConfigInterfaceParameter> outputParameters = pluginConfigInterface.getOutputParameters();

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
                log.error("output parameter is configured but result is empty for interface {}",
                        pluginConfigInterface.getServiceName());
                result.setResultCode(RESULT_CODE_ERR);
                pluginInvocationResultService.responsePluginInterfaceInvocation(result);
                handlePluginInterfaceInvocationFailure(pluginInvocationResult, ctx, "100", "output is null");
                return;
            }
        }

        return;
    }

    private void handleResultData(PluginInterfaceInvocationResult pluginInvocationResult,
            PluginInterfaceInvocationContext ctx, List<Object> resultData) {

        List<Map<String, Object>> outputParameterMaps = validateAndCastResultData(resultData);
        storeOutputParameterMaps(ctx, outputParameterMaps);

        if (log.isInfoEnabled()) {
            log.info("about to process output parameters for {}", ctx.getPluginConfigInterface().getServiceName());
        }
        for (Map<String, Object> outputParameterMap : outputParameterMaps) {
            handleSingleOutputMap(pluginInvocationResult, ctx, outputParameterMap);
        }

        if (log.isInfoEnabled()) {
            log.info("finished processing {} output parameters for {}", outputParameterMaps.size(),
                    ctx.getPluginConfigInterface().getServiceName());
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
            Map<String, Object> outputParameterMap, String objectId) {

        String entityTypeId = null;
        String entityDataId = null;

        String requestId = ctx.getTaskNodeExecRequestEntity().getRequestId();

        Set<PluginConfigInterfaceParameter> outputParameters = ctx.getPluginConfigInterface().getOutputParameters();

        for (Map.Entry<String, Object> entry : outputParameterMap.entrySet()) {

            PluginConfigInterfaceParameter p = findPreConfiguredPluginConfigInterfaceParameter(outputParameters,
                    entry.getKey());

            String paramDataType = null;
            if (p == null) {
                paramDataType = "string";
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
            paramEntity.setParamDataValue(asString(entry.getValue(), paramDataType));
            paramEntity.setRequestId(requestId);

            taskNodeExecParamRepository.save(paramEntity);
        }
    }

    private PluginConfigInterfaceParameter findPreConfiguredPluginConfigInterfaceParameter(
            Set<PluginConfigInterfaceParameter> outputParameters, String paramName) {
        for (PluginConfigInterfaceParameter p : outputParameters) {
            if (p.getName().equals(paramName)) {
                return p;
            }
        }

        return null;
    }

    private List<Map<String, Object>> validateAndCastResultData(List<Object> resultData) {
        List<Map<String, Object>> outputParameterMaps = new ArrayList<Map<String, Object>>();

        for (Object obj : resultData) {
            if (obj == null) {
                continue;
            }

            if (!(obj instanceof Map)) {
                log.error("unexpected data type:returned object is not a instance of map, obj={}", obj);
                throw new WecubeCoreException("Unexpected data type");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> retRecord = (Map<String, Object>) obj;

            outputParameterMaps.add(retRecord);
        }

        return outputParameterMaps;

    }

    private void handleSingleOutputMap(PluginInterfaceInvocationResult pluginInvocationResult,
            PluginInterfaceInvocationContext ctx, Map<String, Object> outputParameterMap) {

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

            DataModelExpressionToRootData dmeCriteria = new DataModelExpressionToRootData(paramExpr, nodeEntityId);

            this.expressionService.writeBackData(dmeCriteria, retVal);
            
        }
    }

    private void handlePluginInterfaceInvocationSuccess(PluginInterfaceInvocationResult pluginInvocationResult,
            PluginInterfaceInvocationContext ctx) {
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

            taskNodeInstInfoRepository.save(nodeInstEntity);
        }
    }

    private void handlePluginInterfaceInvocationFailure(PluginInterfaceInvocationResult pluginInvocationResult,
            PluginInterfaceInvocationContext ctx, String errorCode, String errorMsg) {

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

}
