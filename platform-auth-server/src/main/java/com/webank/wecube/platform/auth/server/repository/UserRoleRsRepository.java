package com.webank.wecube.platform.auth.server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.webank.wecube.platform.auth.server.entity.UserRoleRsEntity;

public interface UserRoleRsRepository extends JpaRepository<UserRoleRsEntity, String> {

    @Query("select t from UserRoleRsEntity t where t.roleId = :roleId and t.deleted = false")
    List<UserRoleRsEntity> findAllByRoleId(@Param("roleId") String roleId);

    @Query("select t from UserRoleRsEntity t where t.userId = :userId and t.deleted = false")
    List<UserRoleRsEntity> findAllByUserId(@Param("userId") String userId);

    @Query("select t from UserRoleRsEntity t "
            + " where t.userId = :userId and t.roleId = :roleId and t.deleted = false")
    UserRoleRsEntity findOneByUserIdAndRoleId(@Param("userId") String userId, @Param("roleId") String roleId);
}
