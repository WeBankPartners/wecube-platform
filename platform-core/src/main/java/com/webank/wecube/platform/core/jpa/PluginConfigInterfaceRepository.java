package com.webank.wecube.platform.core.jpa;

import com.webank.wecube.platform.core.domain.plugin.PluginConfig.Status;
import com.webank.wecube.platform.core.domain.plugin.PluginConfigInterface;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PluginConfigInterfaceRepository extends CrudRepository<PluginConfigInterface, String> {

    Optional<List<PluginConfigInterface>> findPluginConfigInterfaceByPluginConfig_Status(Status status);

    Optional<List<PluginConfigInterface>> findPluginConfigInterfaceByPluginConfig_EntityIdAndPluginConfig_Status(String entityId, Status status);
    
    Optional<List<PluginConfigInterface>> findAllByPluginConfig_Id(String pluginConfigId);

    Optional<List<PluginConfigInterface>> findPluginConfigInterfaceByPluginConfig_EntityNameAndPluginConfig_Status(String entityName, Status status);

    @Query("select configInterface from PluginConfigInterface configInterface where configInterface.pluginConfig.status = :status and (configInterface.pluginConfig.entityName is null or configInterface.pluginConfig.entityName='')")
    Optional<List<PluginConfigInterface>> findAllByEntityNameEmptyAndStatus(@Param("status") Status status);
    default Optional<List<PluginConfigInterface>> findAllEnabledWithEntityNameNull() {
        return findAllByEntityNameEmptyAndStatus(Status.ENABLED);
    }
}
