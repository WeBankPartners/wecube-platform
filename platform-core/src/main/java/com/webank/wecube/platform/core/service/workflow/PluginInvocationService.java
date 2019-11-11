package com.webank.wecube.platform.core.service.workflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.SystemVariable;
import com.webank.wecube.platform.core.domain.plugin.PluginConfig;
import com.webank.wecube.platform.core.domain.plugin.PluginConfigInterface;
import com.webank.wecube.platform.core.domain.plugin.PluginConfigInterfaceParameter;
import com.webank.wecube.platform.core.domain.plugin.PluginInstance;
import com.webank.wecube.platform.core.entity.workflow.ProcExecBindingEntity;
import com.webank.wecube.platform.core.entity.workflow.ProcInstInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeDefInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeExecParamEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeExecRequestEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeInstInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeParamEntity;
import com.webank.wecube.platform.core.jpa.workflow.ProcExecBindingRepository;
import com.webank.wecube.platform.core.jpa.workflow.ProcInstInfoRepository;
import com.webank.wecube.platform.core.jpa.workflow.TaskNodeDefInfoRepository;
import com.webank.wecube.platform.core.jpa.workflow.TaskNodeExecParamRepository;
import com.webank.wecube.platform.core.jpa.workflow.TaskNodeExecRequestRepository;
import com.webank.wecube.platform.core.jpa.workflow.TaskNodeInstInfoRepository;
import com.webank.wecube.platform.core.jpa.workflow.TaskNodeParamRepository;
import com.webank.wecube.platform.core.model.workflow.PluginInvocationCommand;
import com.webank.wecube.platform.core.model.workflow.PluginInvocationResult;
import com.webank.wecube.platform.core.service.PluginInstanceService;
import com.webank.wecube.platform.core.service.SystemVariableService;
import com.webank.wecube.platform.core.service.plugin.PluginConfigService;
import com.webank.wecube.platform.core.service.workflow.PluginInvocationProcessor.PluginInterfaceInvocationContext;
import com.webank.wecube.platform.core.service.workflow.PluginInvocationProcessor.PluginInterfaceInvocationResult;
import com.webank.wecube.platform.core.service.workflow.PluginInvocationProcessor.PluginInvocationOperation;
import com.webank.wecube.platform.core.support.plugin.PluginServiceStub;

@Service
public class PluginInvocationService {
    private static final Logger log = LoggerFactory.getLogger(PluginInvocationService.class);

    private static final String MAPPING_TYPE_CONTEXT = "context";
    private static final String MAPPING_TYPE_ENTITY = "entity";
    private static final String MAPPING_TYPE_SYSTEM_VARIABLE = "system_variable";

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

    public void invokePluginInterface(PluginInvocationCommand cmd) {
        if (log.isInfoEnabled()) {
            log.info("invoke plugin interface with:{}", cmd);
        }

        // TODO
        // 1 get
        String procInstKernelId = cmd.getProcInstId();
        ProcInstInfoEntity procInstEntity = procInstInfoRepository.findOneByProcInstKernelId(procInstKernelId);

        if (procInstEntity == null) {
            log.error("Process instance info does not exist for id:{}", procInstKernelId);
            throw new WecubeCoreException("Process instance info does not exist.");
        }

        Integer procInstId = procInstEntity.getId();

        String nodeId = cmd.getNodeId();
        TaskNodeDefInfoEntity taskNodeDefEntity = taskNodeDefInfoRepository.findOneWithProcessIdAndNodeIdAndStatus(
                procInstEntity.getProcDefId(), nodeId, TaskNodeDefInfoEntity.DEPLOYED_STATUS);

        if (taskNodeDefEntity == null) {
            log.error("Task node definition does not exist for {} {} {}", procInstEntity.getProcDefId(), nodeId,
                    TaskNodeDefInfoEntity.DEPLOYED_STATUS);
            throw new WecubeCoreException("Task node definition does not exist.");
        }

        TaskNodeInstInfoEntity taskNodeInstEntity = taskNodeInstInfoRepository
                .findOneByProcInstIdAndNodeId(procInstEntity.getId(), nodeId);
        if (taskNodeInstEntity == null) {
            log.error("Task node instance does not exist for {} {}", procInstEntity.getId(), nodeId);
            throw new WecubeCoreException("Task node instance does not exist.");
        }

        String serviceId = taskNodeDefEntity.getServiceId();
        if (StringUtils.isBlank(serviceId)) {
            log.error("service ID is invalid for {} {}", procInstEntity.getProcDefId(), nodeId);
            throw new WecubeCoreException("Service ID is invalid.");
        }

        List<ProcExecBindingEntity> nodeObjectBindings = procExecBindingRepository
                .findAllTaskNodeBindings(taskNodeInstEntity.getProcInstId(), taskNodeInstEntity.getId());

        if (nodeObjectBindings == null) {
            log.warn("node object bindings is empty for {} {}", taskNodeInstEntity.getProcInstId(),
                    taskNodeInstEntity.getId());
            nodeObjectBindings = new ArrayList<>();
        }

        PluginConfigInterface pluginConfigInterface = pluginConfigService
                .getPluginConfigInterfaceByServiceName(serviceId);

        // TODO
        Set<PluginConfigInterfaceParameter> configInterfaceParams = pluginConfigInterface.getInputParameters();

        // TODO
        List<Map<String, Object>> pluginParameters = new ArrayList<>();
        for (ProcExecBindingEntity nodeObjectBinding : nodeObjectBindings) {
            // TODO
            String entityId = nodeObjectBinding.getEntityId();

            // TODO to call data route service to get entity
            Map<String, List<Object>> parsedParamValues = new HashMap<String, List<Object>>();
            for (PluginConfigInterfaceParameter param : configInterfaceParams) {
                // TODO get from data route
                String paramName = param.getName();
                String paramType = param.getDataType();

                List<Object> objectVals = new ArrayList<Object>();
                //
                String mappingType = param.getMappingType();
                // TODO get value from entity expression
                if (MAPPING_TYPE_ENTITY.equals(mappingType)) {

                }

                // TODO get value from execution context
                if (MAPPING_TYPE_CONTEXT.equals(mappingType)) {
                    String curTaskNodeDefId = taskNodeDefEntity.getId();
                    TaskNodeParamEntity nodeParamEntity = taskNodeParamRepository
                            .findOneByTaskNodeDefIdAndParamName(curTaskNodeDefId, paramName);

                    if (nodeParamEntity == null) {
                        throw new WecubeCoreException("");
                    }

                    String bindNodeId = nodeParamEntity.getBindNodeId();
                    String bindParamType = nodeParamEntity.getBindParamType();
                    String bindParamName = nodeParamEntity.getBindParamName();

                    // get by procInstId and nodeId
                    TaskNodeInstInfoEntity bindNodeInstEntity = taskNodeInstInfoRepository
                            .findOneByProcInstIdAndNodeId(procInstId, bindNodeId);

                    if (bindNodeInstEntity == null) {
                        throw new WecubeCoreException("");
                    }

                    TaskNodeExecRequestEntity requestEntity = taskNodeExecRequestRepository
                            .findCurrentEntityByNodeInstId(bindNodeInstEntity.getId());

                    List<TaskNodeExecParamEntity> execParamEntities = taskNodeExecParamRepository
                            .findAllByRequestIdAndParamNameAndParamType(requestEntity.getRequestId(), bindParamName, bindParamType);
                    
                    if(execParamEntities == null || execParamEntities.isEmpty()){
                        if("Y".equals(param.getRequired())){
                            throw new WecubeCoreException("");
                        }
                    }
                    
                    //TODO
                    //FIXME
                    String paramVal = execParamEntities.get(0).getParamDataValue();
                    //TODO
                    Object finalInputParam = paramVal;
                    
                    objectVals.add(finalInputParam);
                }

                // TODO get value from system variable
                if (MAPPING_TYPE_SYSTEM_VARIABLE.equals(mappingType)) {
                    Integer svId = param.getMappingSystemVariableId();
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

                    Object finalInputParam = null;
                    if ("int".equals(paramType)) {
                        finalInputParam = Integer.parseInt(sVal);
                    } else {
                        finalInputParam = sVal;
                    }

                    objectVals.add(finalInputParam);
                }

                parsedParamValues.put(paramName, objectVals);

            }
            
            //TODO add parsedParamValues to pluginParameters
            //deduplicate
            
        }

        PluginInstance pluginInstance = getAvailablePluginInstance(pluginConfigInterface);
        String interfacePath = pluginConfigInterface.getPath();
        String instanceHost = String.format("%s://%s:%s", pluginConfigInterface.getHttpMethod(),
                pluginInstance.getHost(), pluginInstance.getPort());

        PluginInterfaceInvocationContext ctx = new PluginInterfaceInvocationContext();
        ctx.setNodeObjectBindings(nodeObjectBindings);
        ctx.setPluginConfigInterface(pluginConfigInterface);
        ctx.setProcInstEntity(procInstEntity);
        ctx.setTaskNodeInstEntity(taskNodeInstEntity);

        PluginInvocationOperation operation = new PluginInvocationOperation() //
                .withCallback(this::handlePluginInterfaceInvocationResult) //
                .withPluginServiceStub(this.pluginServiceStub) //
                .withPluginParameters(pluginParameters) //
                .withInstanceHost(instanceHost) //
                .withInterfacePath(interfacePath) //
                .withPluginInterfaceInvocationContext(ctx);

        pluginInvocationProcessor.process(operation);
    }

    private PluginInstance getAvailablePluginInstance(PluginConfigInterface intf) {
        PluginConfig config = intf.getPluginConfig();
        String pluginName = config.getName();

        List<PluginInstance> instances = pluginInstanceService.getRunningPluginInstances(pluginName);

        return instances.get(0);

    }

    private PluginConfigInterfaceParameter findInterfaceParameterWithParamName(
            Set<PluginConfigInterfaceParameter> configInterfaceParams, String paramName) {
        PluginConfigInterfaceParameter result = null;

        for (PluginConfigInterfaceParameter p : configInterfaceParams) {
            if (paramName.equals(p.getName())) {
                result = p;
                break;
            }
        }

        if (result == null) {
            throw new RuntimeException();
        }

        return result;
    }

    protected String asString(Object val, String sType) {
        if (val == null) {
            return null;
        }
        if (val instanceof String && "str".equals(sType)) {
            return (String) val;
        }

        if (val instanceof Integer && "int".equals(sType)) {
            return String.valueOf(val);
        }

        // TODO
        return val.toString();

    }

    protected Object fromString(String val, String sType) {
        if ("str".equals(sType)) {
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

    protected void preSavePluginInputParameters(List<Map<String, Object>> pluginParameters) {
        TaskNodeExecParamEntity entity = new TaskNodeExecParamEntity();
        entity.setParamDataType("");
        entity.setParamName("");

        // TODO
        taskNodeExecParamRepository.save(entity);
    }

    public void handlePluginInterfaceInvocationResult(PluginInterfaceInvocationResult pluginInvocationResult) {
        if (log.isDebugEnabled()) {
            log.debug("handle plugin interface invocation result");
        }

        PluginInterfaceInvocationContext ctx = pluginInvocationResult.getPluginInterfaceInvocationContext();
        // TODO

        PluginInvocationResult result = new PluginInvocationResult();
        // TODO
        pluginInvocationResultService.responsePluginInterfaceInvocation(result);
    }

}
