package com.webank.wecube.platform.auth.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webank.wecube.platform.auth.server.entity.SysApiEntity;

public interface ApiRepository extends JpaRepository<SysApiEntity, Long> {

}
