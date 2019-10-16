package com.webank.wecube.platform.core.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.webank.wecube.platform.core.domain.plugin.PluginPackageEntity;

public interface PluginPackageEntityRepository extends CrudRepository<PluginPackageEntity, Integer> {

    Optional<List<PluginPackageEntity>> findAllByPluginPackage_Name(String packageName);

    Optional<List<PluginPackageEntity>> findAllByPluginPackage_Id(Integer id);

    void deleteByPluginPackage_NameAndPluginPackage_Version(String pluginPackageName, String entityName);

    Optional<List<PluginPackageEntity>> findAllByPluginPackage_NameAndPluginPackage_Version(String packageName, String packageVersion);

    Optional<PluginPackageEntity> findByPluginPackage_NameAndPluginPackage_VersionAndName(String packageName, String packageVersion, String entityName);
}
