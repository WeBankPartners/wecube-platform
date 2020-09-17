package com.webank.wecube.platform.core.lazyJpa;

import com.webank.wecube.platform.core.lazyDomain.plugin.LazyPluginPackageResourceFile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface LazyPluginPackageResourceFileRepository extends CrudRepository<LazyPluginPackageResourceFile, String> {

    @Query("SELECT DISTINCT resourceFile FROM LazyPluginPackageResourceFile resourceFile JOIN resourceFile.pluginPackage package WHERE package.id IN :pluginPackageIds")
    Optional<List<LazyPluginPackageResourceFile>> findPluginPackageResourceFileByPluginPackageIds(String... pluginPackageIds);

}
