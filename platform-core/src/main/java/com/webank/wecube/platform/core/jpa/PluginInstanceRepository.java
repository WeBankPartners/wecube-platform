//package com.webank.wecube.platform.core.jpa;
//
//import java.util.List;
//
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.CrudRepository;
//import org.springframework.data.repository.query.Param;
//
//import com.webank.wecube.platform.core.domain.plugin.PluginInstance;
//
//public interface PluginInstanceRepository extends CrudRepository<PluginInstance, String> {
//
//    List<PluginInstance> findByHostAndPortAndContainerStatus(String host, Integer port, String containerStatus);
//
//    @Query("SELECT max(port) FROM PluginInstance instance WHERE instance.host IN :hosts")
//    Integer findMaxPortByHost(String hosts);
//
//    @Query("SELECT instance FROM PluginInstance instance WHERE instance.containerStatus = :containerStatus and instance.pluginPackage.id = :packageId")
//    List<PluginInstance> findByContainerStatusAndPluginPackage_Id(@Param("containerStatus") String containerStatus, @Param("packageId") String packageId);
//
//    List<PluginInstance> findByPluginPackage_Id(String packageId);
//
//    List<PluginInstance> findAllByContainerStatus(String containerStatus);
//
//    List<PluginInstance> findAllByContainerStatusAndInstanceName(String containerStatus, String instanceName);
//}
