package com.webank.wecube.platform.auth.server.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.webank.wecube.platform.auth.server.entity.ApiRoleRelationshipEntity;
import com.webank.wecube.platform.auth.server.entity.RoleAuthorityRsEntity;
import com.webank.wecube.platform.auth.server.entity.SysApiEntity;
import com.webank.wecube.platform.auth.server.entity.SysAuthorityEntity;
import com.webank.wecube.platform.auth.server.entity.SysRoleEntity;
import com.webank.wecube.platform.auth.server.entity.SysUserEntity;
import com.webank.wecube.platform.auth.server.entity.UserRoleRsEntity;
import com.webank.wecube.platform.auth.server.repository.ApiRepository;
import com.webank.wecube.platform.auth.server.repository.ApiRoleRelationshipRepository;
import com.webank.wecube.platform.auth.server.repository.AuthorityRoleRelationshipRepository;
import com.webank.wecube.platform.auth.server.repository.UserRepository;
import com.webank.wecube.platform.auth.server.repository.UserRoleRsRepository;

@Service("authorityRoleRelationshipService")
public class AuthorityRoleRelationshipService {

    private static final Logger log = LoggerFactory.getLogger(AuthorityRoleRelationshipService.class);

    @Autowired
    private AuthorityRoleRelationshipRepository authorityRoleRelationshipRepository;

    @Autowired
    private RoleManagementService roleService;
    @Autowired
    private AuthorityService authorityService;

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
