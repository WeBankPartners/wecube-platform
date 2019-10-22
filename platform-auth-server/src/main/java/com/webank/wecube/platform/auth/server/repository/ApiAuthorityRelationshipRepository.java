package com.webank.wecube.platform.auth.server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webank.wecube.platform.auth.server.entity.ApiAuthorityRelationshipEntity;
import com.webank.wecube.platform.auth.server.entity.ApiRoleRelationshipEntity;
import com.webank.wecube.platform.auth.server.entity.AuthorityRoleRelationshipEntity;
import com.webank.wecube.platform.auth.server.entity.SysUserEntity;
import com.webank.wecube.platform.auth.server.entity.UserRoleRelationshipEntity;

public interface ApiAuthorityRelationshipRepository extends JpaRepository<ApiAuthorityRelationshipEntity, Long> {
	List<ApiAuthorityRelationshipEntity> findByApiId(Long apiId);

	List<ApiAuthorityRelationshipEntity> findByAuthorityId(Long authorityId);

	ApiAuthorityRelationshipEntity findOneByApiIdAndAuthorityId(Long apiId, Long authorityId);
}
