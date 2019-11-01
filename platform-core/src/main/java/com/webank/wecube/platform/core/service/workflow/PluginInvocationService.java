package com.webank.wecube.platform.core.service.workflow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.model.workflow.PluginInvocationCommand;
import com.webank.wecube.platform.core.model.workflow.PluginInvocationResult;
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

    public void invokePluginInterface(PluginInvocationCommand cmd) {
        if (log.isInfoEnabled()) {
            log.info("invoke plugin interface with:{}", cmd);
        }

        // TODO

        List<Map<String, Object>> pluginParameters = new ArrayList<>();
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
