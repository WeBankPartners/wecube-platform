package com.webank.wecube.platform.auth.server.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.auth.server.common.ApplicationConstants;
import com.webank.wecube.platform.auth.server.entity.SubSystemAuthorityRsEntity;
import com.webank.wecube.platform.auth.server.entity.SysAuthorityEntity;
import com.webank.wecube.platform.auth.server.entity.SysSubSystemEntity;
import com.webank.wecube.platform.auth.server.model.SysSubSystemInfo;
import com.webank.wecube.platform.auth.server.repository.AuthorityRepository;
import com.webank.wecube.platform.auth.server.repository.SubSystemAuthorityRsRepository;
import com.webank.wecube.platform.auth.server.repository.SubSystemRepository;
import com.webank.wecube.platform.auth.server.service.SubSystemInfoDataService;

@Service("subSystemInfoDataService")
public class SubSystemInfoDataServiceImpl implements SubSystemInfoDataService {
    private static final Logger log = LoggerFactory.getLogger(SubSystemInfoDataServiceImpl.class);

    @Autowired
    private SubSystemRepository subSystemRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private SubSystemAuthorityRsRepository subSystemAuthorityRsRepository;

    @Override
    public SysSubSystemInfo retrieveSysSubSystemInfoWithSystemCode(String systemCode) {
        if (StringUtils.isBlank(systemCode)) {
            log.debug("system code is blank.");
            throw new IllegalArgumentException("system code cannot be blank.");
        }

        SysSubSystemEntity subSystem = subSystemRepository.findOneBySystemCode(systemCode);

        if (subSystem == null) {
            if (log.isDebugEnabled()) {
                log.debug("cannot find sub system with system code:{}", systemCode);
            }
            return null;
        }

        List<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
        grantedAuthorities.add(new SimpleGrantedAuthority(ApplicationConstants.Authority.SUBSYSTEM));

        List<SubSystemAuthorityRsEntity> subSystemAuthorities = subSystemAuthorityRsRepository
                .findAllBySubSystemId(subSystem.getId());

        if (subSystemAuthorities != null) {
            for (SubSystemAuthorityRsEntity subSystemAuthority : subSystemAuthorities) {
                if (!subSystemAuthority.isActive() || subSystemAuthority.isDeleted()) {
                    continue;
                }

                Optional<SysAuthorityEntity> authorityOpt = authorityRepository
                        .findById(subSystemAuthority.getAuthorityId());
                if (!authorityOpt.isPresent()) {
                    continue;
                }

                SysAuthorityEntity authority = authorityOpt.get();
                if (!authority.isActive() || authority.isDeleted()) {
                    continue;
                }

                grantedAuthorities.add(new SimpleGrantedAuthority(authority.getCode()));
            }
        }

        SysSubSystemInfo returnSystemInfo = buildSysSubSystemInfo(subSystem);

        returnSystemInfo.addAuthorities(grantedAuthorities);

        return returnSystemInfo;
    }

    protected SysSubSystemInfo buildSysSubSystemInfo(SysSubSystemEntity entity) {
        SysSubSystemInfo m = new SysSubSystemInfo();
        m.setId(entity.getId());
        m.setName(entity.getName());
        m.setPubApiKey(entity.getPubApiKey());
        m.setSystemCode(entity.getSystemCode());
        m.setActive(entity.isActive());
        m.setApiKey(entity.getApiKey());

        return m;
    }

}
