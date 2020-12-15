package com.webank.wecube.platform.core.service.plugin;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.webank.wecube.platform.core.boot.ApplicationVersionInfo;
import com.webank.wecube.platform.core.commons.ApplicationProperties;
import com.webank.wecube.platform.core.commons.ApplicationProperties.PluginProperties;
import com.webank.wecube.platform.core.entity.plugin.SystemVariables;
import com.webank.wecube.platform.core.propenc.RsaEncryptor;
import com.webank.wecube.platform.core.repository.plugin.SystemVariablesMapper;
import com.webank.wecube.platform.core.service.cmder.ssh2.CommandService;
import com.webank.wecube.platform.core.service.cmder.ssh2.ScpService;
import com.webank.wecube.platform.core.service.user.UserManagementService;
import com.webank.wecube.platform.core.support.S3Client;
import com.webank.wecube.platform.core.support.authserver.AuthServerRestClient;

public abstract class AbstractPluginMgmtService {
    private static final Logger log = LoggerFactory.getLogger(AbstractPluginMgmtService.class);

    @Autowired
    protected ApplicationVersionInfo applicationVersionInfo;
    
    @Autowired
    protected ApplicationProperties applicationProperties;

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

    @Autowired
    protected AuthServerRestClient authServerRestClient;

    protected String getGlobalSystemVariableByName(String varName) {
        List<SystemVariables> vars = systemVariablesMapper.selectAllByNameAndScopeAndStatus(varName,
                SystemVariables.SCOPE_GLOBAL, SystemVariables.ACTIVE);
        if (vars == null || vars.isEmpty()) {
            log.info("Cannot find global system variables for {}", varName);
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
    
    protected String genRandomPassword() {
        String md5String = DigestUtils.md5Hex(String.valueOf(System.currentTimeMillis()));
        return md5String.length() > 16 ? md5String.substring(0, 16) : md5String;
    }
    
    protected String readInputStream(InputStream inputStream) throws IOException {

        if (inputStream == null) {
            throw new IllegalArgumentException();
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, RsaEncryptor.DEF_CHARSET));
        String sLine = null;
        StringBuilder content = new StringBuilder();
        while ((sLine = br.readLine()) != null) {
            if (sLine.startsWith("-")) {
                continue;
            }

            content.append(sLine.trim());
        }

        return content.toString();
    }

}
