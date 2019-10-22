package com.webank.wecube.platform.auth.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webank.wecube.platform.auth.server.entity.SysSubSystemEntity;

public interface SubSystemRepository extends JpaRepository<SysSubSystemEntity, Long> {
	SysSubSystemEntity findOneBySystemCode(String systemCode);
}
