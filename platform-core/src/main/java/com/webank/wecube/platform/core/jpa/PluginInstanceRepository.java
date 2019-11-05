package com.webank.wecube.platform.core.jpa;


import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.webank.wecube.platform.core.domain.plugin.PluginInstance;

public interface PluginInstanceRepository extends CrudRepository<PluginInstance, Integer> {

    List<PluginInstance> findByHostAndPort(String host, Integer port);

    @Query("SELECT max(port) FROM PluginInstance instance WHERE instance.host IN :hosts")
    Integer findMaxPortByHost(String hosts);

    @Query("SELECT instance FROM PluginInstance instance WHERE instance.containerStatus = :containerStatus and instance.pluginPackage.id = :packageId")
    List<PluginInstance> findByContainerStatusAndPackageId(String containerStatus, Integer packageId);

    List<PluginInstance> findByPackageId(int packageId);
    
    List<PluginInstance> findAllByContainerStatus(String containerStatus);
    
    List<PluginInstance> findAllByContainerStatusAndInstanceName(String containerStatus, String instanceName);
}
