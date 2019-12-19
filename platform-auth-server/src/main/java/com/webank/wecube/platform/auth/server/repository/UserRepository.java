package com.webank.wecube.platform.auth.server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webank.wecube.platform.auth.server.entity.SysUserEntity;

public interface UserRepository extends JpaRepository<SysUserEntity, Long> {
	SysUserEntity findOneByUsername(String username);

	List<SysUserEntity> findByActive(Boolean active);

	SysUserEntity findByIdAndActive(Long id, Boolean active);
}
