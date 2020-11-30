//package com.webank.wecube.platform.core.jpa;
//
//import com.webank.wecube.platform.core.domain.plugin.PluginPackageResourceFile;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.CrudRepository;
//
//import java.util.List;
//import java.util.Optional;
//
//public interface PluginPackageResourceFileRepository extends CrudRepository<PluginPackageResourceFile, String> {
//
//    @Query("SELECT DISTINCT resourceFile FROM PluginPackageResourceFile resourceFile JOIN resourceFile.pluginPackage package WHERE package.id IN :pluginPackageIds")
//    Optional<List<PluginPackageResourceFile>> findPluginPackageResourceFileByPluginPackageIds(String... pluginPackageIds);
//
//}
