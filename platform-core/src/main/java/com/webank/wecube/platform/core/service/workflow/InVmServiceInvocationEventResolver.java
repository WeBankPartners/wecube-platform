package com.webank.wecube.platform.core.service.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.workflow.delegate.ServiceInvocationEventResolver;
import com.webank.wecube.platform.workflow.model.ServiceInvocationEvent;

@Service("InVmServiceInvocationEventResolver")
public class InVmServiceInvocationEventResolver implements ServiceInvocationEventResolver {
    private static final Logger log = LoggerFactory.getLogger(InVmServiceInvocationEventResolver.class);
//    @Autowired
//    private PluginInstanceService pluginInstanceService;

    public final void resolveServiceInvocationEvent(ServiceInvocationEvent event) {
        dispatch(event);
    }

    protected void resolveProcessInstanceEndEvent(ServiceInvocationEvent event) {
//        pluginInstanceService.handleProcessInstanceEndEvent(pluginTriggerCommand(event));
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
        log.info("resolve process end notification type event, event={}", event);

        resolveProcessInstanceEndEvent(event);
    }

    protected void doResolveServiceInvocationEvent(ServiceInvocationEvent event) {
        if (log.isDebugEnabled()) {
            log.debug("resolve service invocation type event,event={}", event);
        }

//        PluginTriggerCommand cmd = pluginTriggerCommand(event);
//
//        log.info("call {} with {}", PluginInstanceService.class.getName(), cmd);
//        pluginInstanceService.invokePluginInterface(cmd);
    }

//    private PluginTriggerCommand pluginTriggerCommand(ServiceInvocationEvent event) {
//        PluginTriggerCommand cmd = new PluginTriggerCommand();
//        cmd.setProcessDefinitionId(event.getDefinitionId());
//        cmd.setProcessDefinitionKey(event.getDefinitionKey());
//        cmd.setProcessDefinitionVersion(event.getDefinitionVersion());
//        cmd.setProcessExecutionId(event.getExecutionId());
//        cmd.setProcessInstanceBizKey(event.getBusinessKey());
//        cmd.setProcessInstanceId(event.getInstanceId());
//        cmd.setServiceTaskNodeId(event.getEventSourceId());
//        cmd.setServiceTaskNodeName(event.getEventSourceName());
//
//        return cmd;
//    }

}
