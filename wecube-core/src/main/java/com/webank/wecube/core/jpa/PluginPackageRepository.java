package com.webank.wecube.core.jpa;

import com.google.common.collect.Sets;
import com.webank.wecube.core.domain.plugin.PluginPackage;
import com.webank.wecube.core.utils.VersionUtils;
import org.springframework.data.repository.CrudRepository;

import java.util.*;
import java.util.stream.Collectors;

import static com.webank.wecube.core.utils.CollectionUtils.pickLastOne;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

public interface PluginPackageRepository extends CrudRepository<PluginPackage, Integer> {

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

    Optional<PluginPackage> findByNameAndVersion(String packageName, String version);

    long countByNameAndVersion(String name, String version);

    class PluginPackageVersionComparator implements Comparator<PluginPackage> {
        @Override
        public int compare(PluginPackage o1, PluginPackage o2) {
            return VersionUtils.compare(o1.getVersion(), o2.getVersion());
        }
    }
}
