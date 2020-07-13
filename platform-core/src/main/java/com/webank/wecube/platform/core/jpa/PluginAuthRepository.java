package com.webank.wecube.platform.core.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webank.wecube.platform.core.entity.PluginAuthEntity;

public interface PluginAuthRepository extends JpaRepository<PluginAuthEntity, String>{

}
