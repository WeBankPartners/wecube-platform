package com.webank.wecube.platform.auth.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webank.wecube.platform.auth.server.entity.SysRoleEntity;

public interface RoleRepository extends JpaRepository<SysRoleEntity, Long> {
	SysRoleEntity findOneByName(String name);
}