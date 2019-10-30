package com.webank.wecube.platform.core.jpa;


import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.webank.wecube.platform.core.domain.plugin.PluginInstance;

public interface PluginInstanceRepository extends CrudRepository<PluginInstance, Integer> {

    List<PluginInstance> findByHostAndPort(String host, Integer port);

    @Query("SELECT max(port) FROM PluginInstance instance WHERE instance.host IN :hosts")
    Integer findMaxPortByHost(String hosts);

    @Query("SELECT port FROM PluginInstance instance WHERE instance.host IN :hosts and instance.status = :status order by port")
    List<Integer> findPortsByHostOrderByPort(String hosts, String status);

    @Query("SELECT instance FROM PluginInstance instance WHERE instance.status = :status and instance.pluginPackage.id = :packageId")
    List<PluginInstance> findByStatusAndPackageId(String status, Integer packageId);

}
