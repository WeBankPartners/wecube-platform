package com.webank.wecube.platform.auth.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webank.wecube.platform.auth.server.entity.SysAuthorityEntity;

public interface AuthorityRepository extends JpaRepository<SysAuthorityEntity, Long> {
	SysAuthorityEntity findOneByCode(String code);
}
