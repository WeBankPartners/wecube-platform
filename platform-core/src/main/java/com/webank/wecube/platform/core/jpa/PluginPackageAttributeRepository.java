package com.webank.wecube.platform.core.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.webank.wecube.platform.core.domain.plugin.PluginPackageAttribute;

public interface PluginPackageAttributeRepository extends CrudRepository<PluginPackageAttribute, Integer> {

    // find by package name, package version, entity name and attribute name
    @Query("SELECT attribute FROM PluginPackageAttribute attribute " +
            "LEFT OUTER JOIN PluginPackageEntity entity ON attribute.pluginPackageEntity.id=entity.id " +
            "LEFT OUTER JOIN PluginPackage package ON entity.pluginPackage.id=package.id " +
            "WHERE package.name=:packageName AND package.version=:packageVersion AND entity.name=:entityName AND attribute.name=:attributeName")
    Optional<PluginPackageAttribute> findSingleAttribute(
            @Param("packageName") String packageName,
            @Param("packageVersion") String packageVersion,
            @Param("entityName") String entityName,
            @Param("attributeName") String attributeName);

    // find all "reference by info " by referenced package name, package version, entity name
    @Query("SELECT attribute FROM PluginPackageAttribute attribute " +
            "LEFT OUTER JOIN PluginPackageAttribute refAttribute ON attribute.pluginPackageAttribute.id=refAttribute.id " +
            "LEFT OUTER JOIN PluginPackageEntity entity ON refAttribute.pluginPackageEntity.id=entity.id " +
            "LEFT OUTER JOIN PluginPackage package ON entity.pluginPackage.id=package.id " +
            "WHERE package.name=:packageName AND entity.name=:entityName AND entity.dataModelVersion=:dataModelVersion")
    Optional<List<PluginPackageAttribute>> findAllReferenceByAttribute(
            @Param("packageName") String packageName,
            @Param("entityName") String entityName,
            @Param("dataModelVersion") long dataModelVersion);

    // find all "reference by" info by package name, package version
    @Query("SELECT attribute FROM PluginPackageAttribute attribute " +
            "LEFT OUTER JOIN PluginPackageEntity entity ON attribute.pluginPackageEntity.id=entity.id " +
            "LEFT OUTER JOIN PluginPackage package ON entity.pluginPackage.id=package.id " +
            "WHERE package.name=:packageName AND package.version=:version")
    Optional<List<PluginPackageAttribute>> findAllReferenceByAttribute(
            @Param("packageName") String packageName,
            @Param("version") String version);

    // count all "references by" info by given package name, package version, entity name and attribute name
    @Query("SELECT COUNT(attribute.id) FROM PluginPackageAttribute attribute " +
            "LEFT OUTER JOIN PluginPackageAttribute refAttribute ON attribute.pluginPackageAttribute.id=refAttribute.id " +
            "LEFT OUTER JOIN PluginPackageEntity entity ON refAttribute.pluginPackageEntity.id=entity.id " +
            "LEFT OUTER JOIN PluginPackage package ON entity.pluginPackage.id=package.id " +
            "WHERE package.name=:packageName AND package.version=:packageVersion AND entity.name=:entityName AND attribute.name=:attributeName")
    long countAllReferenceByAttribute(
            @Param("packageName") String packageName,
            @Param("packageVersion") String packageVersion,
            @Param("entityName") String entityName,
            @Param("attributeName") String attributeName);

}
