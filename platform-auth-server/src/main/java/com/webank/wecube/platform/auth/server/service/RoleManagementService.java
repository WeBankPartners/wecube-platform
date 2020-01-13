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

import com.google.common.collect.Lists;
import com.webank.wecube.platform.auth.server.common.AuthServerException;
import com.webank.wecube.platform.auth.server.common.util.StringUtilsEx;
import com.webank.wecube.platform.auth.server.dto.SimpleLocalRoleDto;
import com.webank.wecube.platform.auth.server.entity.RoleAuthorityRsEntity;
import com.webank.wecube.platform.auth.server.entity.SysAuthorityEntity;
import com.webank.wecube.platform.auth.server.entity.SysRoleEntity;
import com.webank.wecube.platform.auth.server.entity.UserRoleRsEntity;
import com.webank.wecube.platform.auth.server.http.AuthenticationContextHolder;
import com.webank.wecube.platform.auth.server.repository.RoleRepository;
import com.webank.wecube.platform.auth.server.repository.UserRoleRsRepository;

@Service("roleManagementService")
public class RoleManagementService {

    private static final Logger log = LoggerFactory.getLogger(RoleManagementService.class);
    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRsRepository userRoleRsRepository;

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
    public void unregisterLocalRole(String roleId) {
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
    
    public List<SysAuthorityEntity> getAuthoritysByRoleId(String roleId) {
        List<SysAuthorityEntity> authoritys = Lists.newArrayList();
        authorityRoleRelationshipRepository.findByRoleId(roleId).forEach(authorityRole -> {
            authoritys.add(authorityRole.getAuthority());
        });
        return authoritys;
    }

    public List<SysRoleEntity> getRolesByAuthorityId(Long authorityId) {
        List<SysRoleEntity> roles = Lists.newArrayList();
        authorityRoleRelationshipRepository.findByAuthorityId(authorityId).forEach(authorityRole -> {
            roles.add(authorityRole.getRole());
        });
        return roles;
    }

    public void grantRoleForAuthoritys(String roleId, List<Long> authorityIds) throws Exception {
        SysRoleEntity role = roleService.getRoleByIdIfExisted(roleId);
        for (Long authorityId : authorityIds) {
            SysAuthorityEntity authorityEntity = authorityService.getAuthorityByIdIfExisted(authorityId);
            if (null == authorityRoleRelationshipRepository.findOneByAuthorityIdAndRoleId(authorityId, roleId))
                authorityRoleRelationshipRepository.save(new RoleAuthorityRsEntity(authorityEntity, role));
        }
    }

    public void revokeRoleForAuthoritys(String roleId, List<Long> authorityIds) throws Exception {
        roleService.getRoleByIdIfExisted(roleId);
        for (Long authorityId : authorityIds) {
            authorityService.getAuthorityByIdIfExisted(authorityId);
            RoleAuthorityRsEntity authorityRoleRelationshipEntity = authorityRoleRelationshipRepository
                    .findOneByAuthorityIdAndRoleId(authorityId, roleId);
            if (null != authorityRoleRelationshipEntity)
                authorityRoleRelationshipRepository.delete(authorityRoleRelationshipEntity);
        }
    }
}
