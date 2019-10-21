package com.webank.wecube.platform.core.jpa;

import com.webank.wecube.platform.core.domain.plugin.PluginConfig;
import com.webank.wecube.platform.core.domain.plugin.PluginConfigInterface;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PluginConfigRepository extends CrudRepository<PluginConfig, Integer> {
    @Query("SELECT DISTINCT inf FROM PluginConfig cfg JOIN cfg.interfaces inf LEFT JOIN FETCH inf.inputParameters LEFT JOIN FETCH inf.outputParameters WHERE cfg.id = :pluginConfigId")
    List<PluginConfigInterface> findAllPluginConfigInterfacesByConfigIdAndFetchParameters(int pluginConfigId);

}
