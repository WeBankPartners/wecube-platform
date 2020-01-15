package com.webank.wecube.platform.auth.server.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.auth.server.entity.AuthorityRoleRelationshipEntity;
import com.webank.wecube.platform.auth.server.entity.SysAuthorityEntity;
import com.webank.wecube.platform.auth.server.entity.SysRoleEntity;
import com.webank.wecube.platform.auth.server.entity.SysUserEntity;
import com.webank.wecube.platform.auth.server.model.CompositeAuthority;
import com.webank.wecube.platform.auth.server.model.SysUser;
import com.webank.wecube.platform.auth.server.repository.AuthorityRoleRelationshipRepository;
import com.webank.wecube.platform.auth.server.repository.UserRepository;
import com.webank.wecube.platform.auth.server.service.LocalUserService;
import com.webank.wecube.platform.auth.server.service.UserRoleRelationshipService;

@Service("localUserService")
public class LocalUserServiceImpl implements LocalUserService {

    @Autowired
    private UserRepository userRepo;
    @Autowired
    private UserRoleRelationshipService userRoleRelationshipService;

    @Autowired
    private AuthorityRoleRelationshipRepository authorityRoleRelationshipRepository;

    @Override
    public SysUser loadUserByUsername(String username) {
        SysUserEntity userEntity = userRepo.findOneByUsername(username);
        if (userEntity == null) {
            return null;
        }

        SysUser user = new SysUser();
        user.setUsername(userEntity.getUsername());
        user.setPassword(userEntity.getPassword());

        List<SysRoleEntity> roles = userRoleRelationshipService.getRolesByUserName(userEntity.getUsername());

        if (roles == null || roles.isEmpty()) {
            return user;
        }

        for (SysRoleEntity role : roles) {
            CompositeAuthority ca = new CompositeAuthority();
            ca.setAuthority(role.getName());
            ca.setAuthorityType("role");
            
            appendAuthorities(user, role);

            
        }

        return user;
    }
    
    private void appendAuthorities(SysUser user, SysRoleEntity role){
        List<AuthorityRoleRelationshipEntity> authorityRoles = authorityRoleRelationshipRepository
                .findByRoleId(role.getId());
        
        if(authorityRoles == null || authorityRoles.isEmpty()){
            return;
        }
        
        for(AuthorityRoleRelationshipEntity authorityRole : authorityRoles){
            SysAuthorityEntity authority = authorityRole.getAuthority();
            if(authority != null){
                CompositeAuthority ca = new CompositeAuthority();
                ca.setAuthority(authority.getCode());
                ca.setAuthorityType("permission");
                
                user.addCompositeAuthority(ca);
            }
        }
    }

}
