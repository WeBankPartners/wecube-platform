package com.webank.wecube.platform.auth.server.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.auth.server.common.ApplicationConstants;
import com.webank.wecube.platform.auth.server.entity.SysSubSystemEntity;
import com.webank.wecube.platform.auth.server.model.SysSubSystemInfo;
import com.webank.wecube.platform.auth.server.repository.SubSystemRepository;
import com.webank.wecube.platform.auth.server.service.SubSystemInfoDataService;

@Service("subSystemInfoDataService")
public class SubSystemInfoDataServiceImpl implements SubSystemInfoDataService {
    private static final Logger log = LoggerFactory.getLogger(SubSystemInfoDataServiceImpl.class);
    
    @Autowired
    private SubSystemRepository subSystemRepo;

    @Override
    public SysSubSystemInfo retrieveSysSubSystemInfoWithSystemCode(String systemCode) {
        if(StringUtils.isBlank(systemCode)){
            log.error("system code is blank.");
            throw new IllegalArgumentException("system code cannot be blank.");
        }
        
        SysSubSystemEntity entity = subSystemRepo.findOneBySystemCode(systemCode);
        
        if(entity == null){
            if(log.isDebugEnabled()){
                log.debug("cannot find sub system with system code:{}", systemCode);
            }
            return null;
        }
        
        List<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
        grantedAuthorities.add(new SimpleGrantedAuthority(ApplicationConstants.Authority.SUBSYSTEM));
        
        SysSubSystemInfo returnSystemInfo =  buildSysSubSystemInfo(entity);
        
        returnSystemInfo.setAuthorities(grantedAuthorities);
        
        return returnSystemInfo;
    }
    
    protected SysSubSystemInfo buildSysSubSystemInfo(SysSubSystemEntity entity){
        SysSubSystemInfo m = new SysSubSystemInfo();
        m.setId(entity.getId());
        m.setName(entity.getName());
        m.setPubApiKey(entity.getPubApiKey());
        m.setSystemCode(entity.getSystemCode());
        m.setActive(entity.getActive());
        m.setApiKey(entity.getApiKey());
        
        return m;
    }

}
