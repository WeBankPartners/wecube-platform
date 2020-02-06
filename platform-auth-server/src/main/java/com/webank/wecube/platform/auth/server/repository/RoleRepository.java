package com.webank.wecube.platform.auth.server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.webank.wecube.platform.auth.server.entity.SysRoleEntity;

public interface RoleRepository extends JpaRepository<SysRoleEntity, String> {
    @Query("select t from SysRoleEntity t where t.name = :name and t.deleted = false")
    SysRoleEntity findNotDeletedRoleByName(@Param("name") String name);
    
    @Query("select t from SysRoleEntity t where t.active = true and t.deleted = false")
    List<SysRoleEntity> findAllActiveRoles();
}