package com.webank.wecube.core.service.workflow.delegate;

import com.webank.wecube.core.domain.workflow.ServiceInvocationEvent;

public interface ServiceInvocationEventResolver {
    void resolveServiceInvocationEvent(ServiceInvocationEvent event);
    
    
    //void resolveProcessInstanceEndEvent(ServiceInvocationEvent event);
}
