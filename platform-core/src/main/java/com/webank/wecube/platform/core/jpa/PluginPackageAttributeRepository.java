//package com.webank.wecube.platform.core.jpa;
//
//import java.util.List;
//import java.util.Optional;
//
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.CrudRepository;
//import org.springframework.data.repository.query.Param;
//
//import com.webank.wecube.platform.core.domain.plugin.PluginPackageAttribute;
//
//public interface PluginPackageAttributeRepository extends CrudRepository<PluginPackageAttribute, String> {
//
//    // find all "reference by info " by referenced package name, package version, entity name
//    @Query(" SELECT childAttribute FROM PluginPackageAttribute childAttribute " +
//            "LEFT OUTER JOIN PluginPackageAttribute parentAttribute ON childAttribute.pluginPackageAttribute.id=parentAttribute.id " +
//            "LEFT OUTER JOIN PluginPackageEntity entity ON parentAttribute.pluginPackageEntity.id=entity.id " +
//            "LEFT OUTER JOIN PluginPackageDataModel dataModel ON entity.pluginPackageDataModel.id=dataModel.id " +
//            "WHERE dataModel.packageName=:packageName AND entity.name=:entityName and dataModel.version=:dataModelVersion")
//    Optional<List<PluginPackageAttribute>> findAllChildrenAttributes(
//            @Param("packageName") String packageName,
//            @Param("entityName") String entityName,
//            @Param("dataModelVersion") int dataModelVersion);
//
//}
