//package com.webank.wecube.platform.core.jpa;
//
//import com.webank.wecube.platform.core.domain.plugin.PluginPackageDependency;
//import org.springframework.data.repository.CrudRepository;
//
//import java.util.List;
//import java.util.Optional;
//
//public interface PluginPackageDependencyRepository extends CrudRepository<PluginPackageDependency, String> {
//    Optional<List<PluginPackageDependency>> findAllByPluginPackageNameAndPluginPackageVersion(String packageName, String version);
//}
