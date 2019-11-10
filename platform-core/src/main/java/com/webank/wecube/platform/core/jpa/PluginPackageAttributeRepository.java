package com.webank.wecube.platform.core.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.webank.wecube.platform.core.domain.plugin.PluginPackageAttribute;

public interface PluginPackageAttributeRepository extends CrudRepository<PluginPackageAttribute, Integer> {

    // find all "reference by info " by referenced package name, package version, entity name
    @Query("SELECT attribute FROM PluginPackageAttribute attribute " +
            "LEFT OUTER JOIN PluginPackageAttribute refAttribute ON attribute.pluginPackageAttribute.id=refAttribute.id " +
            "LEFT OUTER JOIN PluginPackageEntity entity ON refAttribute.pluginPackageEntity.id=entity.id " +
            "LEFT OUTER JOIN PluginPackageDataModel dataModel ON entity.packageName=dataModel.packageName and entity.dataModelVersion=dataModel.version " +
            "WHERE dataModel.packageName=:packageName and dataModel.version=:dataModelVersion AND entity.name=:entityName")
    Optional<List<PluginPackageAttribute>> findAllReferenceByAttribute(
            @Param("packageName") String packageName,
            @Param("entityName") String entityName,
            @Param("dataModelVersion") int dataModelVersion);

}
