package com.webank.wecube.platform.core.jpa;

import org.springframework.data.repository.CrudRepository;
import com.webank.wecube.platform.core.domain.plugin.PluginMysqlInstance;

public interface PluginMysqlInstanceRepository extends CrudRepository<PluginMysqlInstance, Integer> {
}
