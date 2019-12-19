package com.webank.wecube.platform.auth.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webank.wecube.platform.auth.server.entity.SysApiEntity;
import com.webank.wecube.platform.auth.server.entity.SysRoleEntity;

public interface ApiRepository extends JpaRepository<SysApiEntity, Long> {
	SysApiEntity findOneByHttpMethodAndApiUrl(String httpMethod,String apiUrl);
}
