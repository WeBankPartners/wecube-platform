package com.webank.wecube.platform.auth.server.repository;

import com.webank.wecube.platform.auth.server.entity.UserRoleRelationshipEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRoleRelationshipRepository extends JpaRepository<UserRoleRelationshipEntity, Long> {
    List<UserRoleRelationshipEntity> findByRoleId(String roleId);

    List<UserRoleRelationshipEntity> findByUserId(Long userId);

    UserRoleRelationshipEntity findOneByUserIdAndRoleId(Long userId, String roleId);
}
