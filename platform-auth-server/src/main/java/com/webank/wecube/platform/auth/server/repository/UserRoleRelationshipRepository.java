package com.webank.wecube.platform.auth.server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webank.wecube.platform.auth.server.entity.SysUserEntity;
import com.webank.wecube.platform.auth.server.entity.UserRoleRelationshipEntity;

public interface UserRoleRelationshipRepository extends JpaRepository<UserRoleRelationshipEntity, Long> {
	List<UserRoleRelationshipEntity> findByRoleId(Long roleId);

	List<UserRoleRelationshipEntity> findByUserId(Long userId);

	UserRoleRelationshipEntity findOneByUserIdAndRoleId(Long userId, Long roleId);
}
