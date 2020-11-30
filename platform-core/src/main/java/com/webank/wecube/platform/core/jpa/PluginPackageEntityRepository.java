//package com.webank.wecube.platform.core.jpa;
//
//import com.webank.wecube.platform.core.domain.plugin.PluginPackageEntity;
//import org.springframework.data.repository.CrudRepository;
//
//import java.util.Optional;
//
//public interface PluginPackageEntityRepository extends CrudRepository<PluginPackageEntity, String> {
//
//    boolean existsByPackageNameAndNameAndDataModelVersion(String packageName, String name, int dataModelVersion);
//
//    Optional<PluginPackageEntity> findByPackageNameAndNameAndDataModelVersion(String packageName, String name,
//            int dataModelVersion);
//
//}
