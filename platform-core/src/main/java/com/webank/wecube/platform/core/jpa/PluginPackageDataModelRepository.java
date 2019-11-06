package com.webank.wecube.platform.core.jpa;

import com.webank.wecube.platform.core.domain.plugin.PluginPackageDataModel;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PluginPackageDataModelRepository extends CrudRepository<PluginPackageDataModel, Integer> {

    @Query(value = "SELECT dataModel " +
            "FROM PluginPackageDataModel dataModel " +
            "WHERE  dataModel.version = (SELECT max(dataModel.version ) from PluginPackageDataModel dataModel WHERE dataModel.pluginPackage.name=:packageName GROUP BY dataModel.pluginPackage.name) AND dataModel.pluginPackage.name=:packageName")
    Optional<PluginPackageDataModel> findLatestDataModelByPluginPackage_name(@Param("packageName") String packageName);

    @Query(value = "SELECT dataModel " +
            "FROM PluginPackageDataModel dataModel " +
            "WHERE  dataModel.version = (SELECT max(dataModel.version ) from PluginPackageDataModel dataModel WHERE dataModel.pluginPackage.id=:packageId GROUP BY dataModel.pluginPackage.name) AND dataModel.pluginPackage.id=:packageId")
    Optional<PluginPackageDataModel> findLatestByPluginPackage_Id(@Param("packageId") Integer id);

    Optional<PluginPackageDataModel> findTop1ByPluginPackage_NameAndNameOrderByDataModelVersionDesc(String packageName, String entityName);

}
