package com.webank.wecube.platform.core.jpa;

import com.webank.wecube.platform.core.domain.plugin.PluginConfig;
import org.springframework.data.repository.CrudRepository;

public interface PluginConfigRepository extends CrudRepository<PluginConfig, Integer> {


}
