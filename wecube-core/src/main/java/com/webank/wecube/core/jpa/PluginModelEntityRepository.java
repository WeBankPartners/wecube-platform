package com.webank.wecube.core.jpa;

import com.webank.wecube.core.domain.plugin.PluginModelEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface PluginModelEntityRepository extends CrudRepository<PluginModelEntity, Integer> {

    List<PluginModelEntity> findAllByPackageId(Integer packageId);

    Optional<PluginModelEntity> findAllByPackageNameAndName(String packageName, String entityName);

    Optional<PluginModelEntity> findAllByPackageName(String packageName);

    void delete(Integer packageId, String entityName);

    void delete(Integer packageId);
}
