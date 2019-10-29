package com.webank.wecube.platform.core.jpa;

import com.webank.wecube.platform.core.domain.plugin.PluginPackageResourceFile;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface PluginPackageResourceFileRepository extends CrudRepository<PluginPackageResourceFile, Integer> {
    Optional<List<PluginPackageResourceFile>> findAllByPluginPackageId(int pluginPackageId);
}
