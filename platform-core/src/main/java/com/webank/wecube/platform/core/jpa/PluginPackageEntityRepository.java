package com.webank.wecube.platform.core.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.webank.wecube.platform.core.domain.plugin.PluginPackageEntity;
import org.springframework.data.repository.query.Param;

public interface PluginPackageEntityRepository extends CrudRepository<PluginPackageEntity, Integer> {

    Optional<List<PluginPackageEntity>> findAllByPluginPackage_Name(String packageName);

    @Query(value = "SELECT entity " +
            "FROM PluginPackageEntity entity " +
            "WHERE  entity.dataModelVersion = (SELECT max(entity.dataModelVersion ) from PluginPackageEntity entity WHERE entity.pluginPackage.name=:packageName GROUP BY entity.pluginPackage.name) AND entity.pluginPackage.name=:packageName")
    Optional<List<PluginPackageEntity>> findAllLatestEntityByPluginPackage_name(@Param("packageName") String packageName);

    @Query(value = "SELECT entity " +
            "FROM PluginPackageEntity entity " +
            "WHERE  entity.dataModelVersion = (SELECT max(entity.dataModelVersion ) from PluginPackageEntity entity WHERE entity.pluginPackage.id=:packageId GROUP BY entity.pluginPackage.name) AND entity.pluginPackage.id=:packageId")
    Optional<List<PluginPackageEntity>> findAllLatestByPluginPackage_Id(@Param("packageId") Integer id);

    Optional<PluginPackageEntity> findTop1ByPluginPackage_NameAndNameOrderByDataModelVersionDesc(String packageName, String entityName);

}
