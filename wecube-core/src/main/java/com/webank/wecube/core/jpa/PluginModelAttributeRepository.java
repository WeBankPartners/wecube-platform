package com.webank.wecube.core.jpa;

import com.webank.wecube.core.domain.plugin.PluginModelAttribute;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface PluginModelAttributeRepository extends CrudRepository<PluginModelAttribute, Integer> {

    // find by package name, package version, entity name and attribute name
    Optional<PluginModelAttribute> findByPluginModelEntity_PluginPackage_NameAndPluginModelEntity_PluginPackage_VersionAndPluginModelEntity_NameAndName(String packageName, String packageVersion, String entityName, String name);

    // find all by referenced package name, package version, entity name
    Optional<List<PluginModelAttribute>> findAllByPluginModelAttribute_PluginModelEntity_PluginPackage_NameAndPluginModelAttribute_PluginModelEntity_PluginPackage_VersionAndPluginModelAttribute_PluginModelEntity_Name(String packageName, String packageVersion, String entityName);

    // find all by package name, package version
    Optional<List<PluginModelAttribute>> findAllByPluginModelEntity_PluginPackage_NameAndPluginModelEntity_PluginPackage_Version(String packageName, String version);

    // count all references by given package name, package nersion, entity name and attribute name
    long countAllByPluginModelAttribute_PluginModelEntity_PluginPackage_NameAndPluginModelAttribute_PluginModelEntity_PluginPackage_VersionAndPluginModelAttribute_PluginModelEntity_NameAndName(String packageName, String packageVersion, String entityName, String attributeName);

}
