//package com.webank.wecube.platform.core.jpa;
//
//import com.webank.wecube.platform.core.domain.plugin.PluginPackageDataModel;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.CrudRepository;
//import org.springframework.data.repository.query.Param;
//
//import java.util.Optional;
//
//public interface PluginPackageDataModelRepository extends CrudRepository<PluginPackageDataModel, String> {
//
//    @Query(value = "SELECT dataModel " +
//            "FROM PluginPackageDataModel dataModel " +
//            "WHERE  dataModel.version = (SELECT max(dataModel.version ) from PluginPackageDataModel dataModel WHERE dataModel.packageName=:packageName GROUP BY dataModel.packageName) AND dataModel.packageName=:packageName")
//    Optional<PluginPackageDataModel> findLatestDataModelByPackageName(@Param("packageName") String packageName);
//
//}
