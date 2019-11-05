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
import com.webank.wecube.platform.core.domain.plugin.PluginConfigInterface;
import com.webank.wecube.platform.core.domain.plugin.PluginConfigInterfaceParameter;
import com.webank.wecube.platform.core.entity.workflow.ProcExecBindingEntity;
import com.webank.wecube.platform.core.entity.workflow.ProcInstInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeDefInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeInstInfoEntity;
import com.webank.wecube.platform.core.jpa.workflow.ProcExecBindingRepository;
import com.webank.wecube.platform.core.jpa.workflow.ProcInstInfoRepository;
import com.webank.wecube.platform.core.jpa.workflow.TaskNodeDefInfoRepository;
import com.webank.wecube.platform.core.jpa.workflow.TaskNodeInstInfoRepository;
import com.webank.wecube.platform.core.model.workflow.PluginInvocationCommand;
import com.webank.wecube.platform.core.model.workflow.PluginInvocationResult;
import com.webank.wecube.platform.core.service.plugin.PluginConfigService;
import com.webank.wecube.platform.core.service.workflow.PluginInvocationProcessor.PluginInterfaceInvocationResult;
import com.webank.wecube.platform.core.service.workflow.PluginInvocationProcessor.PluginInvocationOperation;
import com.webank.wecube.platform.core.support.plugin.PluginServiceStub;

@Service
public class PluginInvocationService {
    private static final Logger log = LoggerFactory.getLogger(PluginInvocationService.class);

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
            String objectId = nodeObjectBinding.getObjectId();
            // TODO

            Map<String, List<Object>> parsedParamValues = new HashMap<String, List<Object>>();
            for (PluginConfigInterfaceParameter param : configInterfaceParams) {
                // TODO get from dataroute
                String paramName = param.getName();
                List<Object> objectVals = new ArrayList<Object>();
                parsedParamValues.put(paramName, objectVals);

            }
        }

        String interfacePath = "";
        String instanceHost = "";

        PluginInvocationOperation operation = new PluginInvocationOperation();
        operation.withCallback(this::handlePluginInterfaceInvocationResult)
                .withPluginServiceStub(this.pluginServiceStub).withPluginParameters(pluginParameters)
                .withInstanceHost(instanceHost).withInterfacePath(interfacePath);

        pluginInvocationProcessor.process(operation);
    }

    public void handlePluginInterfaceInvocationResult(PluginInterfaceInvocationResult pluginInvocationResult) {
        if (log.isDebugEnabled()) {
            log.debug("handle plugin interface invocation result");
        }

        PluginInvocationResult result = new PluginInvocationResult();
        pluginInvocationResultService.responsePluginInterfaceInvocation(result);
    }

}
