package com.webank.wecube.core.jpa;

import com.webank.wecube.core.domain.plugin.PluginModelAttribute;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface PluginModelAttributeRepository extends CrudRepository<PluginModelAttribute, Integer> {

    Optional<PluginModelAttribute> findAllByPackageNameAndEntityNameAndName(String packageName, String entityName, String name);
}
