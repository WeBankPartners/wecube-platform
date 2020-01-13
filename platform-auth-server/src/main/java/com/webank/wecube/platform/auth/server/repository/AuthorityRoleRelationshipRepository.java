package com.webank.wecube.platform.auth.server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webank.wecube.platform.auth.server.entity.ApiRoleRelationshipEntity;
import com.webank.wecube.platform.auth.server.entity.RoleAuthorityRsEntity;
import com.webank.wecube.platform.auth.server.entity.SysUserEntity;
import com.webank.wecube.platform.auth.server.entity.UserRoleRsEntity;

public interface AuthorityRoleRelationshipRepository extends JpaRepository<RoleAuthorityRsEntity, Long> {
    List<RoleAuthorityRsEntity> findByRoleId(String roleId);

    List<RoleAuthorityRsEntity> findByAuthorityId(Long authorityId);

    RoleAuthorityRsEntity findOneByAuthorityIdAndRoleId(Long authorityId, String roleId);
}
