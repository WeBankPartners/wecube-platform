package com.webank.wecube.platform.auth.server.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.webank.wecube.platform.auth.server.entity.AuthorityRoleRelationshipEntity;
import com.webank.wecube.platform.auth.server.entity.SysAuthorityEntity;
import com.webank.wecube.platform.auth.server.entity.SysRoleEntity;
import com.webank.wecube.platform.auth.server.repository.AuthorityRepository;
import com.webank.wecube.platform.auth.server.repository.AuthorityRoleRelationshipRepository;

@Service("authorityRoleRelationshipService")
public class AuthorityRoleRelationshipService {

    private static final Logger log = LoggerFactory.getLogger(AuthorityRoleRelationshipService.class);

    @Autowired
    private AuthorityRoleRelationshipRepository authorityRoleRelationshipRepository;

    @Autowired
    private RoleService roleService;
    @Autowired
    private AuthorityService authorityService;

    @Autowired
    private AuthorityRepository authorityRepository;

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

    public void grantRoleForAuthoritiesByCode(String roleId, List<String> authorityCodes) throws Exception {
        SysRoleEntity role = roleService.getRoleByIdIfExisted(roleId);
        for (String authorityCode : authorityCodes) {
            SysAuthorityEntity authorityEntity = authorityService.getAuthorityByCode(authorityCode);
            if (authorityEntity == null) {
                SysAuthorityEntity authority = new SysAuthorityEntity();
                authority.setCode(authorityCode);
                authorityEntity = authorityRepository.save(authority);
            }

            if (authorityRoleRelationshipRepository.findOneByAuthorityIdAndRoleId(authorityEntity.getId(),
                    roleId) == null)
                authorityRoleRelationshipRepository.save(new AuthorityRoleRelationshipEntity(authorityEntity, role));
        }
    }

    public void revokeRoleForAuthoritiesByCode(String roleId, List<String> authorityCodes) throws Exception {
        roleService.getRoleByIdIfExisted(roleId);
        for (String authorityCode : authorityCodes) {
            SysAuthorityEntity authorityEntity = authorityService.getAuthorityByCode(authorityCode);
            if (authorityEntity == null) {
                continue;
            }

            AuthorityRoleRelationshipEntity authorityRoleRelationshipEntity = authorityRoleRelationshipRepository
                    .findOneByAuthorityIdAndRoleId(authorityEntity.getId(), roleId);
            if (authorityRoleRelationshipEntity != null) {
                authorityRoleRelationshipRepository.delete(authorityRoleRelationshipEntity);
            }
        }
    }

}
