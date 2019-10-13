package com.webank.wecube.core.jpa;

import com.webank.wecube.core.domain.plugin.PluginPackageAttribute;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface PluginPackageAttributeRepository extends CrudRepository<PluginPackageAttribute, Integer> {

    // find by package name, package version, entity name and attribute name
    Optional<PluginPackageAttribute> findByPluginPackageEntity_PluginPackage_NameAndPluginPackageEntity_PluginPackage_VersionAndPluginPackageEntity_NameAndName(String packageName, String packageVersion, String entityName, String name);

    // find all by referenced package name, package version, entity name
    Optional<List<PluginPackageAttribute>> findAllByPluginPackageAttribute_PluginPackageEntity_PluginPackage_NameAndPluginPackageAttribute_PluginPackageEntity_PluginPackage_VersionAndPluginPackageAttribute_PluginPackageEntity_Name(String packageName, String packageVersion, String entityName);

    // find all by package name, package version
    Optional<List<PluginPackageAttribute>> findAllByPluginPackageEntity_PluginPackage_NameAndPluginPackageEntity_PluginPackage_Version(String packageName, String version);

    // count all references by given package name, package nersion, entity name and attribute name
    long countAllByPluginPackageAttribute_PluginPackageEntity_PluginPackage_NameAndPluginPackageAttribute_PluginPackageEntity_PluginPackage_VersionAndPluginPackageAttribute_PluginPackageEntity_NameAndName(String packageName, String packageVersion, String entityName, String attributeName);

}
