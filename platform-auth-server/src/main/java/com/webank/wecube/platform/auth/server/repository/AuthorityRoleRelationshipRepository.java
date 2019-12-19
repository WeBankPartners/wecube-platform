package com.webank.wecube.platform.auth.server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webank.wecube.platform.auth.server.entity.ApiRoleRelationshipEntity;
import com.webank.wecube.platform.auth.server.entity.AuthorityRoleRelationshipEntity;
import com.webank.wecube.platform.auth.server.entity.SysUserEntity;
import com.webank.wecube.platform.auth.server.entity.UserRoleRelationshipEntity;

public interface AuthorityRoleRelationshipRepository extends JpaRepository<AuthorityRoleRelationshipEntity, Long> {
	List<AuthorityRoleRelationshipEntity> findByRoleId(Long roleId);

	List<AuthorityRoleRelationshipEntity> findByAuthorityId(Long authorityId);

	AuthorityRoleRelationshipEntity findOneByAuthorityIdAndRoleId(Long authorityId, Long roleId);
}
