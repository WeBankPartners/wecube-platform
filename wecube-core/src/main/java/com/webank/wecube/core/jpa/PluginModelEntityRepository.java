package com.webank.wecube.core.jpa;

import com.webank.wecube.core.domain.plugin.PluginModelEntity;
import com.webank.wecube.core.domain.plugin.PluginPackage;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface PluginModelEntityRepository extends CrudRepository<PluginModelEntity, Integer> {

    List<PluginModelEntity> findAllByPluginPackage_Name(String packageName);

    List<PluginModelEntity> findAllByPluginPackage(PluginPackage pluginPackage);

    Optional<List<PluginModelEntity>> findAllByPluginPackage_Id(Integer id);

    void deleteByPluginPackage_NameAndName(String pluginPackageName, String entityName);

    void deleteByPluginPackage_Name(String packageName);

    Optional<PluginModelEntity> findByPluginPackage_NameAndName(String packageName, String name);
}
