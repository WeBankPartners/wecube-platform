package com.webank.wecube.platform.auth.server.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webank.wecube.platform.auth.server.common.AuthServerException;
import com.webank.wecube.platform.auth.server.common.util.StringUtilsEx;
import com.webank.wecube.platform.auth.server.dto.SimpleAuthorityDto;
import com.webank.wecube.platform.auth.server.dto.SimpleLocalRoleDto;
import com.webank.wecube.platform.auth.server.entity.RoleAuthorityRsEntity;
import com.webank.wecube.platform.auth.server.entity.SysAuthorityEntity;
import com.webank.wecube.platform.auth.server.entity.SysRoleEntity;
import com.webank.wecube.platform.auth.server.entity.UserRoleRsEntity;
import com.webank.wecube.platform.auth.server.http.AuthenticationContextHolder;
import com.webank.wecube.platform.auth.server.repository.AuthorityRepository;
import com.webank.wecube.platform.auth.server.repository.RoleAuthorityRsRepository;
import com.webank.wecube.platform.auth.server.repository.RoleRepository;
import com.webank.wecube.platform.auth.server.repository.UserRoleRsRepository;

@Service("roleManagementService")
public class RoleManagementService {

    private static final Logger log = LoggerFactory.getLogger(RoleManagementService.class);
    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRsRepository userRoleRsRepository;

    @Autowired
    private RoleAuthorityRsRepository roleAuthorityRsRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    public SimpleLocalRoleDto registerLocalRole(SimpleLocalRoleDto roleDto) {
        validateSimpleLocalRoleDto(roleDto);

        SysRoleEntity existedRole = roleRepository.findNotDeletedRoleByName(roleDto.getName());

        if (existedRole != null) {
            throw new AuthServerException(String.format("Role with name {%s} already existed.", roleDto.getName()));
        }

        SysRoleEntity role = buildSysRoleEntity(roleDto);
        roleRepository.saveAndFlush(role);

        return convertToSimpleLocalRoleDto(role);
    }

    private SimpleLocalRoleDto convertToSimpleLocalRoleDto(SysRoleEntity role) {
        SimpleLocalRoleDto dto = new SimpleLocalRoleDto();
        dto.setId(role.getId());
        dto.setEmail(role.getEmailAddress());
        dto.setName(role.getName());
        dto.setDisplayName(role.getDisplayName());

        return dto;
    }

    private SysRoleEntity buildSysRoleEntity(SimpleLocalRoleDto dto) {
        SysRoleEntity role = new SysRoleEntity();
        role.setCreatedBy(AuthenticationContextHolder.getCurrentUsername());
        role.setDisplayName(dto.getDisplayName());
        role.setName(dto.getName());
        role.setEmailAddress(dto.getEmail());

        return role;
    }

    public List<SimpleLocalRoleDto> retrieveAllLocalRoles() {
        List<SysRoleEntity> roles = roleRepository.findAllActiveRoles();
        List<SimpleLocalRoleDto> result = new ArrayList<>();

        if (roles == null || roles.isEmpty()) {
            return result;
        }

        roles.forEach(r -> {
            result.add(convertToSimpleLocalRoleDto(r));
        });

        return result;
    }

    @Transactional
    public void unregisterLocalRoleById(String roleId) {
        Optional<SysRoleEntity> roleOpt = roleRepository.findById(roleId);
        if (!roleOpt.isPresent()) {
            throw new AuthServerException("Such role does not exist.");
        }

        SysRoleEntity role = roleOpt.get();
        if (role.isDeleted()) {
            throw new AuthServerException(String.format("Such role with ID {%s} has been deleted.", roleId));
        }

        role.setActive(false);
        role.setDeleted(true);
        role.setUpdatedBy(AuthenticationContextHolder.getCurrentUsername());
        role.setUpdatedTime(new Date());
        roleRepository.save(role);

        List<UserRoleRsEntity> userRoles = userRoleRsRepository.findAllByRoleId(role.getId());

        if (userRoles != null) {
            if (log.isInfoEnabled()) {
                log.info("total {} user role relationships to delete.", userRoles.size());
            }
            for (UserRoleRsEntity userRole : userRoles) {
                userRole.setActive(false);
                userRole.setUpdatedBy(AuthenticationContextHolder.getCurrentUsername());
                userRole.setDeleted(true);
                userRole.setUpdatedTime(new Date());
                userRoleRsRepository.save(userRole);
            }
        }
    }

    public SimpleLocalRoleDto retriveLocalRoleByRoleId(String roleId) {
        Optional<SysRoleEntity> roleEntityOptional = roleRepository.findById(roleId);
        if (!roleEntityOptional.isPresent()) {
            throw new AuthServerException(String.format("Role ID [%s] does not exist", roleId));
        }
        SysRoleEntity role = roleEntityOptional.get();
        return convertToSimpleLocalRoleDto(role);
    }

    private void validateSimpleLocalRoleDto(SimpleLocalRoleDto roleDto) {
        if (StringUtils.isBlank(roleDto.getName()) || StringUtils.isBlank(roleDto.getDisplayName())) {
            throw new AuthServerException("Role name and display name should provide.");
        }

        if (StringUtils.isNotBlank(roleDto.getEmail())) {
            if (!StringUtilsEx.isEmailValid(roleDto.getEmail())) {
                throw new AuthServerException("Unexpected email address.");
            }
        }
    }

    public List<SimpleAuthorityDto> retrieveAllAuthoritiesByRoleId(String roleId) {
        List<SimpleAuthorityDto> result = new ArrayList<>();
        Optional<SysRoleEntity> roleOpt = roleRepository.findById(roleId);
        if (!roleOpt.isPresent()) {
            log.error("such role entity does not exist,role id {}", roleId);
            return result;
        }

        SysRoleEntity role = roleOpt.get();
        if (role.isDeleted() || !role.isActive()) {
            log.error("such role is deleted or inactive,role id {}", roleId);
            return result;
        }

        List<RoleAuthorityRsEntity> roleAuthorities = roleAuthorityRsRepository
                .findAllConfiguredAuthoritiesByRoleId(role.getId());

        for (RoleAuthorityRsEntity roleAuthority : roleAuthorities) {
            Optional<SysAuthorityEntity> authorityOpt = authorityRepository.findById(roleAuthority.getAuthorityId());
            if (!authorityOpt.isPresent()) {
                log.error("authority entity does not exist, authority id {}", roleAuthority.getAuthorityId());
                continue;
            }

            SysAuthorityEntity authority = authorityOpt.get();
            if (authority.isDeleted()) {
                log.error("such authority is deleted,authority:{} {}", authority.getId(), authority.getCode());
                continue;
            }

            SimpleAuthorityDto dto = new SimpleAuthorityDto();
            dto.setActive(authority.isActive());
            dto.setCode(authority.getCode());
            dto.setDescription(authority.getDescription());
            dto.setDisplayName(authority.getDisplayName());
            dto.setId(authority.getId());
            dto.setScope(authority.getScope());

            result.add(dto);
        }

        return result;
    }

    @Transactional
    public void configureRoleWithAuthoritiesById(String roleId, List<SimpleAuthorityDto> authorityDtos) {
        Optional<SysRoleEntity> roleOpt = roleRepository.findById(roleId);
        if (!roleOpt.isPresent()) {
            log.error("such role entity does not exist,role id {}", roleId);
            throw new AuthServerException("Such role entity does not exist.");
        }

        SysRoleEntity role = roleOpt.get();

        for (SimpleAuthorityDto authorityDto : authorityDtos) {
            if (StringUtils.isBlank(authorityDto.getId())) {
                log.error("The ID of authority to configure roles is blank.");
                throw new AuthServerException("The ID of authority to configure roles is blank.");
            }

            log.info("configure role {} with authority {}-{}", role.getName(), authorityDto.getId(),
                    authorityDto.getCode());

            Optional<SysAuthorityEntity> authorityOpt = authorityRepository.findById(authorityDto.getId());
            if (!authorityOpt.isPresent()) {
                log.error("such authority entity does not exist,authority id {}", authorityDto.getId());
                throw new AuthServerException(
                        String.format("Authority with {%s} does not exist.", authorityDto.getId()));

            }

            SysAuthorityEntity authority = authorityOpt.get();

            RoleAuthorityRsEntity roleAuthority = roleAuthorityRsRepository.findOneByRoleIdAndAuthorityId(role.getId(),
                    authority.getId());

            if (roleAuthority != null) {
                continue;
            }

            roleAuthority = new RoleAuthorityRsEntity();
            roleAuthority.setActive(true);
            roleAuthority.setAuthorityCode(authority.getCode());
            roleAuthority.setAuthorityId(authority.getId());
            roleAuthority.setCreatedBy(AuthenticationContextHolder.getCurrentUsername());
            roleAuthority.setDeleted(false);
            roleAuthority.setRoleId(role.getId());
            roleAuthority.setRoleName(role.getName());

            roleAuthorityRsRepository.save(roleAuthority);
        }
    }

    @Transactional
    public void revokeRoleAuthoritiesById(String roleId, List<SimpleAuthorityDto> authorityDtos) {
        Optional<SysRoleEntity> roleOpt = roleRepository.findById(roleId);
        if (!roleOpt.isPresent()) {
            log.error("such role entity does not exist,role id {}", roleId);
            throw new AuthServerException("Such role entity does not exist.");
        }

        SysRoleEntity role = roleOpt.get();

        for (SimpleAuthorityDto authorityDto : authorityDtos) {
            RoleAuthorityRsEntity roleAuthority = roleAuthorityRsRepository.findOneByRoleIdAndAuthorityId(role.getId(),
                    authorityDto.getId());
            if (roleAuthority == null) {
                continue;
            }

            roleAuthority.setActive(false);
            roleAuthority.setDeleted(true);
            roleAuthority.setUpdatedBy(AuthenticationContextHolder.getCurrentUsername());
            roleAuthority.setUpdatedTime(new Date());

            roleAuthorityRsRepository.save(roleAuthority);
        }
    }
}
