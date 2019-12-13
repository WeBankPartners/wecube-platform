package com.webank.wecube.platform.core.jpa;

import com.webank.wecube.platform.core.domain.plugin.PluginConfig.Status;
import com.webank.wecube.platform.core.domain.plugin.PluginConfigInterface;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface PluginConfigInterfaceRepository extends CrudRepository<PluginConfigInterface, String> {

    Optional<List<PluginConfigInterface>> findPluginConfigInterfaceByPluginConfig_Status(Status status);

    Optional<List<PluginConfigInterface>> findPluginConfigInterfaceByPluginConfig_EntityIdAndPluginConfig_Status(String entityId, Status status);
    
    Optional<List<PluginConfigInterface>> findAllByPluginConfig_Id(String pluginConfigId);

}
