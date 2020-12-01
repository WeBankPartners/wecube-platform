package com.webank.wecube.platform.core.service.plugin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.repository.plugin.SystemVariablesMapper;

@Service
public class SystemVariableDataService {
    
    @Autowired
    private SystemVariablesMapper systemVariablesMapper;
    
    

}
