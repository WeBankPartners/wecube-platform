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
import com.webank.wecube.platform.auth.server.dto.RoleAuthoritiesDto;
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

    public SimpleLocalRoleDto retriveLocalRoleByRoleName(String roleName) {
        if (StringUtils.isBlank(roleName)) {
            throw new AuthServerException("3002", "Role name as input argument cannot be blank.");
        }

        SysRoleEntity existedRole = roleRepository.findNotDeletedRoleByName(roleName);

        if (existedRole == null) {
            String msg = String.format("Role with name {%s} does not exist.", roleName);
            throw new AuthServerException("3003", msg, roleName);
        }

        return convertToSimpleLocalRoleDto(existedRole);
    }

    public SimpleLocalRoleDto registerLocalRole(SimpleLocalRoleDto roleDto) {
        validateSimpleLocalRoleDto(roleDto);

        SysRoleEntity existedRole = roleRepository.findNotDeletedRoleByName(roleDto.getName());

        if (existedRole != null) {
            String msg = String.format("Role with name {%s} already existed.", roleDto.getName());
            throw new AuthServerException("3004", msg, roleDto.getName());
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
            throw new AuthServerException("3012", "Such role entity does not exist.");
        }

        SysRoleEntity role = roleOpt.get();
        if (role.isDeleted()) {
            String msg = String.format("Such role with ID {%s} has been deleted.", roleId);
            throw new AuthServerException("3005", msg, roleId);
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
            String msg = String.format("Role ID [%s] does not exist.", roleId);
            throw new AuthServerException("3006", msg, roleId);
        }
        SysRoleEntity role = roleEntityOptional.get();
        return convertToSimpleLocalRoleDto(role);
    }

    private void validateSimpleLocalRoleDto(SimpleLocalRoleDto roleDto) {
        if (StringUtils.isBlank(roleDto.getName()) || StringUtils.isBlank(roleDto.getDisplayName())) {
            throw new AuthServerException("3007", "Role name and display name should provide.");
        }

        if (StringUtils.isNotBlank(roleDto.getEmail())) {
            if (!StringUtilsEx.isEmailValid(roleDto.getEmail())) {
                throw new AuthServerException("3008", "Unexpected email address.");
            }
        }
    }

    public List<SimpleAuthorityDto> retrieveAllAuthoritiesByRoleId(String roleId) {
        List<SimpleAuthorityDto> result = new ArrayList<>();
        Optional<SysRoleEntity> roleOpt = roleRepository.findById(roleId);
        if (!roleOpt.isPresent()) {
            log.debug("such role entity does not exist,role id {}", roleId);
            return result;
        }

        SysRoleEntity role = roleOpt.get();
        if (role.isDeleted() || !role.isActive()) {
            log.debug("such role is deleted or inactive,role id {}", roleId);
            return result;
        }

        List<RoleAuthorityRsEntity> roleAuthorities = roleAuthorityRsRepository
                .findAllConfiguredAuthoritiesByRoleId(role.getId());

        for (RoleAuthorityRsEntity roleAuthority : roleAuthorities) {
            Optional<SysAuthorityEntity> authorityOpt = authorityRepository.findById(roleAuthority.getAuthorityId());
            if (!authorityOpt.isPresent()) {
                log.debug("authority entity does not exist, authority id {}", roleAuthority.getAuthorityId());
                continue;
            }

            SysAuthorityEntity authority = authorityOpt.get();
            if (authority.isDeleted()) {
                log.debug("such authority is deleted,authority:{} {}", authority.getId(), authority.getCode());
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
    public void configureRoleWithAuthorities(RoleAuthoritiesDto grantDto) {
        SysRoleEntity role = null;

        if (StringUtils.isNotBlank(grantDto.getRoleId())) {
            Optional<SysRoleEntity> roleOpt = roleRepository.findById(grantDto.getRoleId());
            if (roleOpt.isPresent()) {
                role = roleOpt.get();
            }
        }

        if (role == null && StringUtils.isNotBlank(grantDto.getRoleName())) {
            role = roleRepository.findNotDeletedRoleByName(grantDto.getRoleName());
        }

        if (role == null) {
            log.debug("such role entity does not exist,role id {}, role name {} ", grantDto.getRoleId(),
                    grantDto.getRoleName());
            throw new AuthServerException("3009", "Such role entity does not exist.");
        }

        for (SimpleAuthorityDto authorityDto : grantDto.getAuthorities()) {
            if (StringUtils.isBlank(authorityDto.getId()) && StringUtils.isBlank(authorityDto.getCode())) {
                log.debug("The ID and code of authority to configure is blank.");
                throw new AuthServerException("3010", "The ID and code of authority to configure is blank.");
            }

            log.info("configure role {} with authority {}-{}", role.getName(), authorityDto.getId(),
                    authorityDto.getCode());

            SysAuthorityEntity authority = null;
            if (StringUtils.isNoneBlank(authorityDto.getId())) {
                Optional<SysAuthorityEntity> authorityOpt = authorityRepository.findById(authorityDto.getId());
                if (!authorityOpt.isPresent()) {
                    log.debug("such authority entity does not exist,authority id {}", authorityDto.getId());
                    String msg = String.format("Authority with {%s} does not exist.", authorityDto.getId());
                    throw new AuthServerException("3011", msg, authorityDto.getId());

                }

                authority = authorityOpt.get();
            } else {
                authority = authorityRepository.findNotDeletedOneByCode(authorityDto.getCode());
                if (authority == null) {
                    authority = new SysAuthorityEntity();
                    authority.setActive(true);
                    authority.setCode(authorityDto.getCode());
                    authority.setCreatedBy(AuthenticationContextHolder.getCurrentUsername());
                    authority.setDeleted(false);
                    authority.setScope(StringUtils.isBlank(authorityDto.getScope()) ? SysAuthorityEntity.SCOPE_GLOBAL
                            : authorityDto.getScope());
                    authority.setDescription(authorityDto.getDescription());
                    authority.setDisplayName(StringUtils.isBlank(authorityDto.getDisplayName()) ? authorityDto.getCode()
                            : authorityDto.getDisplayName());

                    authorityRepository.save(authority);
                }
            }

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
    public void configureRoleWithAuthoritiesById(String roleId, List<SimpleAuthorityDto> authorityDtos) {
        Optional<SysRoleEntity> roleOpt = roleRepository.findById(roleId);
        if (!roleOpt.isPresent()) {
            log.debug("such role entity does not exist,role id {}", roleId);
            throw new AuthServerException("3012", "Such role entity does not exist.");
        }

        SysRoleEntity role = roleOpt.get();

        for (SimpleAuthorityDto authorityDto : authorityDtos) {
            if (StringUtils.isBlank(authorityDto.getId()) && StringUtils.isBlank(authorityDto.getCode())) {
                log.debug("The ID and code of authority to configure is blank.");
                throw new AuthServerException("3013", "The ID and code of authority to configure is blank.");
            }

            log.info("configure role {} with authority {}-{}", role.getName(), authorityDto.getId(),
                    authorityDto.getCode());

            SysAuthorityEntity authority = null;
            if (StringUtils.isNoneBlank(authorityDto.getId())) {
                Optional<SysAuthorityEntity> authorityOpt = authorityRepository.findById(authorityDto.getId());
                if (!authorityOpt.isPresent()) {
                    log.debug("such authority entity does not exist,authority id {}", authorityDto.getId());
                    String msg = String.format("Authority with ID {%s} does not exist.", authorityDto.getId());
                    throw new AuthServerException("3014", msg, authorityDto.getId());

                }

                authority = authorityOpt.get();
            } else {
                authority = authorityRepository.findNotDeletedOneByCode(authorityDto.getCode());
                if (authority == null) {
                    authority = new SysAuthorityEntity();
                    authority.setActive(true);
                    authority.setCode(authorityDto.getCode());
                    authority.setCreatedBy(AuthenticationContextHolder.getCurrentUsername());
                    authority.setDeleted(false);
                    authority.setScope(StringUtils.isBlank(authorityDto.getScope()) ? SysAuthorityEntity.SCOPE_GLOBAL
                            : authorityDto.getScope());
                    authority.setDescription(authorityDto.getDescription());
                    authority.setDisplayName(StringUtils.isBlank(authorityDto.getDisplayName()) ? authorityDto.getCode()
                            : authorityDto.getDisplayName());

                    authorityRepository.save(authority);
                }
            }

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
    public void revokeRoleAuthorities(RoleAuthoritiesDto revocationDto) {

        SysRoleEntity role = null;

        if (StringUtils.isNotBlank(revocationDto.getRoleId())) {
            Optional<SysRoleEntity> roleOpt = roleRepository.findById(revocationDto.getRoleId());
            if (roleOpt.isPresent()) {
                role = roleOpt.get();
            }
        }

        if (role == null && StringUtils.isNotBlank(revocationDto.getRoleName())) {
            role = roleRepository.findNotDeletedRoleByName(revocationDto.getRoleName());
        }

        if (role == null) {
            log.debug("such role entity does not exist,role id {}, role name {} ", revocationDto.getRoleId(),
                    revocationDto.getRoleName());
            throw new AuthServerException("3012", "Such role entity does not exist.");
        }

        for (SimpleAuthorityDto authorityDto : revocationDto.getAuthorities()) {
            if (StringUtils.isBlank(authorityDto.getId()) && StringUtils.isBlank(authorityDto.getCode())) {
                continue;
            }

            RoleAuthorityRsEntity roleAuthority = null;
            if (StringUtils.isBlank(authorityDto.getId())) {
                roleAuthority = roleAuthorityRsRepository.findOneByRoleIdAndAuthorityCode(role.getId(),
                        authorityDto.getCode());
            } else {
                roleAuthority = roleAuthorityRsRepository.findOneByRoleIdAndAuthorityId(role.getId(),
                        authorityDto.getId());
            }

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

    @Transactional
    public void revokeRoleAuthoritiesById(String roleId, List<SimpleAuthorityDto> authorityDtos) {
        Optional<SysRoleEntity> roleOpt = roleRepository.findById(roleId);
        if (!roleOpt.isPresent()) {
            log.debug("such role entity does not exist,role id {}", roleId);
            throw new AuthServerException("3012", "Such role entity does not exist.");
        }

        SysRoleEntity role = roleOpt.get();

        for (SimpleAuthorityDto authorityDto : authorityDtos) {
            if (StringUtils.isBlank(authorityDto.getId()) && StringUtils.isBlank(authorityDto.getCode())) {
                continue;
            }

            RoleAuthorityRsEntity roleAuthority = null;
            if (StringUtils.isBlank(authorityDto.getId())) {
                roleAuthority = roleAuthorityRsRepository.findOneByRoleIdAndAuthorityCode(role.getId(),
                        authorityDto.getCode());
            } else {
                roleAuthority = roleAuthorityRsRepository.findOneByRoleIdAndAuthorityId(role.getId(),
                        authorityDto.getId());
            }

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
