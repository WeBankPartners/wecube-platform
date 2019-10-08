package com.webank.wecube.core.jpa;

import com.webank.wecube.core.domain.plugin.PluginModelAttribute;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface PluginModelAttributeRepository extends CrudRepository<PluginModelAttribute, Integer> {

    Optional<PluginModelAttribute> findAllByPluginModelEntity_PluginPackage_NameAndPluginModelEntity_NameAndName(String packageName, String entityName, String name);

    void deleteByPluginModelEntity_NameAndName(String entityName, String attributeName);

    void deleteByPluginModelEntity_Name(String entityName);
}
