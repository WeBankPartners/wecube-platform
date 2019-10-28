package com.webank.wecube.platform.core.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import com.webank.wecube.platform.core.domain.plugin.PluginMysqlInstance;

public interface PluginMysqlInstanceRepository extends JpaRepository<PluginMysqlInstance, Integer> {
}
