package com.webank.wecube.core.jpa;

import com.webank.wecube.core.domain.plugin.PluginModelEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface PluginModelEntityRepository extends CrudRepository<PluginModelEntity, Integer> {

    Optional<List<PluginModelEntity>> findAllByPluginPackage_Name(String packageName);

    Optional<List<PluginModelEntity>> findAllByPluginPackage_Id(Integer id);

    void deleteByPluginPackage_NameAndPluginPackage_Version(String pluginPackageName, String entityName);

    Optional<List<PluginModelEntity>> findAllByPluginPackage_NameAndPluginPackage_Version(String packageName, String packageVersion);

    Optional<PluginModelEntity> findByPluginPackage_NameAndPluginPackage_VersionAndName(String packageName, String packageVersion, String entityName);
}
