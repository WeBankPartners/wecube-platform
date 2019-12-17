package com.webank.wecube.platform.core.jpa;

import com.webank.wecube.platform.core.domain.plugin.PluginPackageEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface PluginPackageEntityRepository extends CrudRepository<PluginPackageEntity, String> {

    Optional<PluginPackageEntity> findByPackageNameAndName(String packageName, String name);

}
