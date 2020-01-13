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
    private AuthorityManagementService authorityService;

    

}
