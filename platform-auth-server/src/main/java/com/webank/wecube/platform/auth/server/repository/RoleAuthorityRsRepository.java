package com.webank.wecube.platform.auth.server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.webank.wecube.platform.auth.server.entity.RoleAuthorityRsEntity;

public interface RoleAuthorityRsRepository extends JpaRepository<RoleAuthorityRsEntity, String> {

    @Query("select t from RoleAuthorityRsEntity t where t.roleId = :roleId and t.deleted = false")
    List<RoleAuthorityRsEntity> findAllConfiguredAuthoritiesByRoleId(@Param("roleId") String roleId);

    @Query("select t from RoleAuthorityRsEntity t "
            + " where t.roleId = :roleId and t.authorityId = :authorityId and t.deleted = false")
    RoleAuthorityRsEntity findOneByRoleIdAndAuthorityId(@Param("roleId") String roleId,
            @Param("authorityId") String authorityId);
    
    @Query("select t from RoleAuthorityRsEntity t "
            + " where t.roleId = :roleId and t.authorityCode = :authorityCode and t.deleted = false")
    RoleAuthorityRsEntity findOneByRoleIdAndAuthorityCode(@Param("roleId") String roleId,
            @Param("authorityCode") String authorityCode);
}
