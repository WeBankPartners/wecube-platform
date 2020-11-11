package com.webank.wecube.platform.core.service.plugin;

import java.io.Closeable;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;
import com.webank.wecube.platform.core.boot.ApplicationVersionInfo;
import com.webank.wecube.platform.core.commons.ApplicationProperties.PluginProperties;
import com.webank.wecube.platform.core.entity.plugin.PluginPackages;
import com.webank.wecube.platform.core.entity.plugin.SystemVariables;
import com.webank.wecube.platform.core.repository.plugin.SystemVariablesMapper;
import com.webank.wecube.platform.core.service.CommandService;
import com.webank.wecube.platform.core.service.ScpService;
import com.webank.wecube.platform.core.service.user.UserManagementService;
import com.webank.wecube.platform.core.support.S3Client;

public abstract class AbstractPluginMgmtService {
    private static final Logger log = LoggerFactory.getLogger(AbstractPluginMgmtService.class);
    
    
    
    @Autowired
    protected ApplicationVersionInfo applicationVersionInfo;
    
    @Autowired
    protected SystemVariablesMapper systemVariablesMapper;
    
    @Autowired
    protected PluginProperties pluginProperties;
    
    @Autowired
    protected S3Client s3Client;
    
    @Autowired
    protected ScpService scpService;
    @Autowired
    protected CommandService commandService;
    
    @Autowired
    protected UserManagementService userManagementService;

    
    protected String getGlobalSystemVariableByName(String varName) {
        List<SystemVariables> vars = systemVariablesMapper.findByNameAndScopeAndStatus(varName,
                SystemVariables.SCOPE_GLOBAL, SystemVariables.ACTIVE);
        if (vars == null || vars.isEmpty()) {
            return null;
        }

        SystemVariables var = vars.get(0);
        String varVal = var.getValue();
        if (StringUtils.isBlank(varVal)) {
            varVal = var.getDefaultValue();
        }
        return varVal;
    }
    
    protected String stripString(String s) {
        if (s == null) {
            return null;
        }

        if (s.length() > 250) {
            return s.substring(0, 250);
        }

        return s;
    }
    
    protected void closeSilently(Closeable c) {
        if (c == null) {
            return;
        }

        try {
            c.close();
        } catch (Exception e) {
        }
    }

}
