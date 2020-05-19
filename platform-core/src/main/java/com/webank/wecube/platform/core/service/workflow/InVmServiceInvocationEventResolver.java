package com.webank.wecube.platform.core.service.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.model.workflow.PluginInvocationCommand;
import com.webank.wecube.platform.workflow.delegate.ServiceInvocationEventResolver;
import com.webank.wecube.platform.workflow.model.ServiceInvocationEvent;

/**
 * 
 * @author gavin
 *
 */
@Service("InVmServiceInvocationEventResolver")
public class InVmServiceInvocationEventResolver implements ServiceInvocationEventResolver {
    private static final Logger log = LoggerFactory.getLogger(InVmServiceInvocationEventResolver.class);
    private static final String CUSTOM_SERVICE_BEAN_PREFIX = "srvBeanST-";
    @Autowired
    private PluginInvocationService pluginInvocationService;

    public final void resolveServiceInvocationEvent(ServiceInvocationEvent event) {
        dispatch(event);
    }

    protected void resolveProcessInstanceEndEvent(ServiceInvocationEvent event) {
        pluginInvocationService.handleProcessInstanceEndEvent(pluginInvocationCommand(event));
    }

    protected void dispatch(ServiceInvocationEvent event) {
        if (ServiceInvocationEvent.EventType.SERVICE_INVOCATION == event.getEventType()) {
            doResolveServiceInvocationEvent(event);
        } else if (ServiceInvocationEvent.EventType.PROCESS_END_NOTIFICATION == event.getEventType()) {
            doSendProcessEndNotification(event);
        } else {
            doProcessUnkownTypeEvent(event);
        }
    }

    protected void doProcessUnkownTypeEvent(ServiceInvocationEvent event) {
        log.warn("unkown type event,event={}", event);
        // the event will be discarded
    }

    protected void doSendProcessEndNotification(ServiceInvocationEvent event) {
        log.debug("resolve process end notification type event, event={}", event);

        resolveProcessInstanceEndEvent(event);
    }

    protected void doResolveServiceInvocationEvent(ServiceInvocationEvent event) {
        if (log.isDebugEnabled()) {
            log.debug("resolve service invocation type event,event={}", event);
        }

        PluginInvocationCommand cmd = pluginInvocationCommand(event);

        log.debug("call {} with {}", pluginInvocationService.getClass().getName(), cmd);
        pluginInvocationService.invokePluginInterface(cmd);
    }

    private PluginInvocationCommand pluginInvocationCommand(ServiceInvocationEvent event) {

        PluginInvocationCommand cmd = new PluginInvocationCommand();
        cmd.setProcDefId(event.getDefinitionId());
        cmd.setProcDefKey(event.getDefinitionKey());
        cmd.setProcDefVersion(event.getDefinitionVersion());
        cmd.setProcInstKey(event.getBusinessKey());
        cmd.setProcInstId(event.getInstanceId());
        cmd.setNodeId(calculateNodeId(event));
        cmd.setNodeName(event.getEventSourceName());
        cmd.setExecutionId(event.getExecutionId());
        
        cmd.addAllowedOptions(event.getAllowedOptions());

        return cmd;
    }

    private String calculateNodeId(ServiceInvocationEvent event) {
        if (event == null) {
            return "";
        }

        String nodeId = event.getEventSourceId();
        if (nodeId == null) {
            nodeId = "";
        }

        if (nodeId.indexOf(CUSTOM_SERVICE_BEAN_PREFIX) >= 0) {
            nodeId = nodeId.substring(CUSTOM_SERVICE_BEAN_PREFIX.length());
        }

        return nodeId;
    }

}
