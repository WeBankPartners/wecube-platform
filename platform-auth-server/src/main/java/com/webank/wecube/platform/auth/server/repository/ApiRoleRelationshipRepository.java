package com.webank.wecube.platform.auth.server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webank.wecube.platform.auth.server.entity.ApiRoleRelationshipEntity;
import com.webank.wecube.platform.auth.server.entity.SysUserEntity;
import com.webank.wecube.platform.auth.server.entity.UserRoleRsEntity;

public interface ApiRoleRelationshipRepository extends JpaRepository<ApiRoleRelationshipEntity, Long> {
	List<ApiRoleRelationshipEntity> findByRoleId(String roleId);

	List<ApiRoleRelationshipEntity> findByApiId(Long apiId);

	ApiRoleRelationshipEntity findOneByApiIdAndRoleId(Long apiId, String roleId);
}
