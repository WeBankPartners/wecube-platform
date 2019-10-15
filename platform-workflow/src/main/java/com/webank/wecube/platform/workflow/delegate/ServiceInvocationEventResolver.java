package com.webank.wecube.platform.workflow.delegate;

import com.webank.wecube.platform.workflow.model.ServiceInvocationEvent;

public interface ServiceInvocationEventResolver {
    void resolveServiceInvocationEvent(ServiceInvocationEvent event);
    
    
    //void resolveProcessInstanceEndEvent(ServiceInvocationEvent event);
}
