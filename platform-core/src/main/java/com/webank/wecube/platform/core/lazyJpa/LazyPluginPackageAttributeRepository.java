package com.webank.wecube.platform.core.lazyJpa;

import com.webank.wecube.platform.core.domain.plugin.PluginPackageAttribute;
import com.webank.wecube.platform.core.lazyDomain.plugin.LazyPluginPackageAttribute;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@CacheConfig(cacheManager = "requestScopedCacheManager", cacheNames = "pluginPackageAttributeRepository")
public interface LazyPluginPackageAttributeRepository extends CrudRepository<LazyPluginPackageAttribute, String> {

    // find all "reference by info " by referenced package name, package version, entity name
    @Cacheable("pluginPackageAttributeRepository-findAllChildrenAttributes")
    @Query(" SELECT childAttribute FROM LazyPluginPackageAttribute childAttribute " +
            "LEFT OUTER JOIN PluginPackageAttribute parentAttribute ON childAttribute.pluginPackageAttribute.id=parentAttribute.id " +
            "LEFT OUTER JOIN PluginPackageEntity entity ON parentAttribute.pluginPackageEntity.id=entity.id " +
            "LEFT OUTER JOIN PluginPackageDataModel dataModel ON entity.pluginPackageDataModel.id=dataModel.id " +
            "WHERE dataModel.packageName=:packageName AND entity.name=:entityName and dataModel.version=:dataModelVersion")
    Optional<List<LazyPluginPackageAttribute>> findAllChildrenAttributes(
            @Param("packageName") String packageName,
            @Param("entityName") String entityName,
            @Param("dataModelVersion") int dataModelVersion);

}
