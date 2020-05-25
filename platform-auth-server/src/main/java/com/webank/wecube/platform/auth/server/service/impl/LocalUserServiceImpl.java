package com.webank.wecube.platform.auth.server.service.impl;

import java.util.List;
import java.util.Optional;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.auth.server.entity.RoleAuthorityRsEntity;

import com.webank.wecube.platform.auth.server.entity.SysAuthorityEntity;
import com.webank.wecube.platform.auth.server.entity.SysRoleEntity;
import com.webank.wecube.platform.auth.server.entity.SysUserEntity;
import com.webank.wecube.platform.auth.server.entity.UserRoleRsEntity;
import com.webank.wecube.platform.auth.server.model.CompositeAuthority;
import com.webank.wecube.platform.auth.server.model.SysUser;
import com.webank.wecube.platform.auth.server.repository.AuthorityRepository;
import com.webank.wecube.platform.auth.server.repository.RoleAuthorityRsRepository;
import com.webank.wecube.platform.auth.server.repository.RoleRepository;

import com.webank.wecube.platform.auth.server.repository.UserRepository;
import com.webank.wecube.platform.auth.server.repository.UserRoleRsRepository;
import com.webank.wecube.platform.auth.server.service.LocalUserService;

@Service("localUserService")
public class LocalUserServiceImpl implements LocalUserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRsRepository userRoleRsRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RoleAuthorityRsRepository roleAuthorityRsRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Override
    public SysUser loadUserByUsername(String username) {
        SysUserEntity userEntity = userRepository.findNotDeletedUserByUsername(username);
        if (userEntity == null) {
            return null;
        }
        
        if(!userEntity.isActive() || userEntity.isDeleted()){
            return null;
        }
        
        if(userEntity.isBlocked()){
            return null;
        }

        SysUser user = new SysUser();
        user.setUsername(userEntity.getUsername());
        user.setPassword(userEntity.getPassword());
        user.setAuthSource(userEntity.getAuthSource());
        user.setAuthContext(userEntity.getAuthContext());

        List<UserRoleRsEntity> userRoles = userRoleRsRepository.findAllByUserId(userEntity.getId());
        if (userRoles == null) {
            return user;
        }

        for (UserRoleRsEntity userRole : userRoles) {
            if (!userRole.isActive() || userRole.isDeleted()) {
                continue;
            }

            Optional<SysRoleEntity> roleOpt = roleRepository.findById(userRole.getRoleId());
            if (!roleOpt.isPresent()) {
                continue;
            }

            SysRoleEntity role = roleOpt.get();
            if (!role.isActive() || role.isDeleted()) {
                continue;
            }

            CompositeAuthority roleObject = new CompositeAuthority();
            roleObject.setAuthority(role.getName());
            roleObject.setAuthorityType(CompositeAuthority.AUTHORITY_TYPE_ROLE);
            user.addCompositeAuthority(roleObject);

            appendAuthorities(user, role);

        }
        return user;
    }

    private void appendAuthorities(SysUser user, SysRoleEntity role) {
        List<RoleAuthorityRsEntity> roleAuthorities = roleAuthorityRsRepository
                .findAllConfiguredAuthoritiesByRoleId(role.getId());
        if (roleAuthorities == null || roleAuthorities.isEmpty()) {
            return;
        }

        for (RoleAuthorityRsEntity roleAuthority : roleAuthorities) {
            if (!roleAuthority.isActive() || roleAuthority.isDeleted()) {
                continue;
            }

            Optional<SysAuthorityEntity> authorityOpt = authorityRepository.findById(roleAuthority.getAuthorityId());
            if(!authorityOpt.isPresent()){
                continue;
            }
            
            SysAuthorityEntity authority = authorityOpt.get();
            if(!authority.isActive() || authority.isDeleted()){
                continue;
            }
            
            CompositeAuthority authorityObject = new CompositeAuthority();
            authorityObject.setAuthority(authority.getCode());
            authorityObject.setAuthorityType(CompositeAuthority.AUTHORITY_TYPE_PERMISSION);
            user.addCompositeAuthority(authorityObject);

        }
    }

}
