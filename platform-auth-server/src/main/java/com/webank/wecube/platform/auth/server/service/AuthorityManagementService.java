package com.webank.wecube.platform.auth.server.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.auth.server.common.AuthServerException;
import com.webank.wecube.platform.auth.server.dto.SimpleAuthorityDto;
import com.webank.wecube.platform.auth.server.entity.SysAuthorityEntity;
import com.webank.wecube.platform.auth.server.http.AuthenticationContextHolder;
import com.webank.wecube.platform.auth.server.repository.AuthorityRepository;

@Service("authorityManagementService")
public class AuthorityManagementService {

    private static final Logger log = LoggerFactory.getLogger(AuthorityManagementService.class);

    @Autowired
    private AuthorityRepository authorityRepository;

    public SimpleAuthorityDto registerLocalAuthority(SimpleAuthorityDto authDto) {
        if (StringUtils.isBlank(authDto.getCode())) {
            throw new AuthServerException("3000", "Authority code to register cannot be blank.");
        }

        SysAuthorityEntity authority = authorityRepository.findNotDeletedOneByCode(authDto.getCode());

        if (authority != null) {
            log.debug("authority {} to register already exists.", authDto.getCode());
            String msg = String.format(
                    "Authority registering failed,because authority code {%s} already exist.", authDto.getCode());
            throw new AuthServerException("3001", msg, authDto.getCode());
        }

        authority = new SysAuthorityEntity();
        authority.setActive(true);
        authority.setCode(authDto.getCode());
        authority.setCreatedBy(AuthenticationContextHolder.getCurrentUsername());
        authority.setDeleted(false);
        authority.setDescription(authDto.getDescription());
        authority.setDisplayName(authDto.getDisplayName());
        authority.setScope(
                StringUtils.isBlank(authDto.getScope()) ? SysAuthorityEntity.SCOPE_GLOBAL : authDto.getScope());

        SysAuthorityEntity saveAuthority = authorityRepository.saveAndFlush(authority);

        return convertToSimpleAuthorityDto(saveAuthority);
    }
    
    private SimpleAuthorityDto convertToSimpleAuthorityDto(SysAuthorityEntity authority){
        SimpleAuthorityDto dto = new SimpleAuthorityDto();
        dto.setActive(authority.isActive());
        dto.setCode(authority.getCode());
        dto.setDescription(authority.getDescription());
        dto.setDisplayName(authority.getDisplayName());
        dto.setId(authority.getId());
        dto.setScope(authority.getScope());
        
        return dto;
    }

    public List<SimpleAuthorityDto> retrieveAllLocalAuthorites() {
        List<SimpleAuthorityDto> result = new ArrayList<>();
        List<SysAuthorityEntity> authorities = authorityRepository.findAllNotDeletedAuthorities();
        if(authorities == null || authorities.isEmpty()){
            return result;
        }
        
        for(SysAuthorityEntity authority : authorities){
            SimpleAuthorityDto dto = convertToSimpleAuthorityDto(authority);
            result.add(dto);
        }
        return result;
    }
}
