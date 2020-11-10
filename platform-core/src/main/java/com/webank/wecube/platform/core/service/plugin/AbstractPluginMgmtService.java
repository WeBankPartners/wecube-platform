package com.webank.wecube.platform.core.service.plugin;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.webank.wecube.platform.core.entity.plugin.SystemVariables;
import com.webank.wecube.platform.core.repository.plugin.SystemVariablesMapper;

public abstract class AbstractPluginMgmtService {
    private static final Logger log = LoggerFactory.getLogger(AbstractPluginMgmtService.class);
    
    @Autowired
    private SystemVariablesMapper systemVariablesMapper;
    
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

}
