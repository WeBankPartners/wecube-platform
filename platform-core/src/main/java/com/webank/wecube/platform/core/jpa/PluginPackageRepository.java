package com.webank.wecube.platform.core.jpa;

import static com.webank.wecube.platform.core.utils.CollectionUtils.pickLastOne;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.webank.wecube.platform.core.domain.plugin.PluginPackageEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.google.common.collect.Sets;
import com.webank.wecube.platform.core.domain.plugin.PluginPackage;
import com.webank.wecube.platform.core.utils.VersionUtils;

public interface PluginPackageRepository extends CrudRepository<PluginPackage, Integer> {
    @Query("SELECT DISTINCT package FROM PluginPackage package WHERE package.status IN :status")
    List<PluginPackage> findAllByStatus(PluginPackage.Status... status);

    List<PluginPackage> findAllByName(String name);

    default Optional<PluginPackage> findLatestVersionByName(String name, String... excludeVersions) {
        List<PluginPackage> pluginPackages = findAllByName(name);
        if (isEmpty(pluginPackages)) return Optional.empty();
        if (excludeVersions != null) {
            Set<String> excludeVersionSet = Sets.newHashSet(excludeVersions);
            pluginPackages = pluginPackages.stream()
                    .filter(pluginPackage -> !excludeVersionSet.contains(pluginPackage.getVersion()))
                    .collect(Collectors.toList());
        }
        return Optional.ofNullable(pickLastOne(pluginPackages, new PluginPackageVersionComparator()));
    }

    @Query(value = "SELECT package.name " +
            "FROM PluginPackage package " +
            "GROUP BY package.name")
    Optional<List<String>> findAllDistinctPackage();

    Optional<PluginPackage> findTop1ByNameOrderByVersionDesc(String packageName);

    long countByNameAndVersion(String name, String version);

    class PluginPackageVersionComparator implements Comparator<PluginPackage> {
        @Override
        public int compare(PluginPackage o1, PluginPackage o2) {
            return VersionUtils.compare(o1.getVersion(), o2.getVersion());
        }
    }
}
