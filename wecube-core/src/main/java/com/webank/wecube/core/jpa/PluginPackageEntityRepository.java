package com.webank.wecube.core.jpa;

import com.webank.wecube.core.domain.plugin.PluginPackageEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface PluginPackageEntityRepository extends CrudRepository<PluginPackageEntity, Integer> {

    Optional<List<PluginPackageEntity>> findAllByPluginPackage_Name(String packageName);

    Optional<List<PluginPackageEntity>> findAllByPluginPackage_Id(Integer id);

    void deleteByPluginPackage_NameAndPluginPackage_Version(String pluginPackageName, String entityName);

    Optional<List<PluginPackageEntity>> findAllByPluginPackage_NameAndPluginPackage_Version(String packageName, String packageVersion);

    Optional<PluginPackageEntity> findByPluginPackage_NameAndPluginPackage_VersionAndName(String packageName, String packageVersion, String entityName);
}
