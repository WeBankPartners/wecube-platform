package com.webank.wecube.platform.auth.server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.webank.wecube.platform.auth.server.entity.SysUserEntity;

public interface UserRepository extends JpaRepository<SysUserEntity, String> {

    @Query("select t from SysUserEntity t " + " " + "where t.username = :username and t.deleted = false ")
    SysUserEntity findNotDeletedUserByUsername(@Param("username") String username);

    @Query("select t from SysUserEntity t where t.deleted = false and t.active = true and t.blocked = false")
    List<SysUserEntity> findAllActiveUsers();
}
