package com.webank.wecube.platform.core.service.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.model.workflow.PluginInvocationCommand;

@Service
public class PluginInvocationService {
    private static final Logger log = LoggerFactory.getLogger(PluginInvocationService.class);
    
    @Autowired
    private PluginInvocationResultService pluginInvocationResultService;
    
    public void invokePluginInterface(PluginInvocationCommand cmd){
        if(log.isInfoEnabled()){
            log.info("invoke plugin interface with:{}", cmd);
        }
    }

}
