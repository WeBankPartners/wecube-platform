package com.webank.wecube.platform.core.service.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.webank.wecube.platform.core.service.CommandService;
import com.webank.wecube.platform.core.service.ScpService;

public class PluginPackageMgmtService {
    private static final Logger log = LoggerFactory.getLogger(PluginPackageMgmtService.class);

    


    public static final String PLATFORM_NAME = "platform";

    private static final String DEFAULT_USER = "sys";

    

    
    @Autowired
    private ScpService scpService;
    @Autowired
    private CommandService commandService;

    

    

    
}
