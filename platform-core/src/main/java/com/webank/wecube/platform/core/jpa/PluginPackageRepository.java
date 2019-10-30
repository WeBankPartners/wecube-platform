package com.webank.wecube.platform.core.jpa;

import com.google.common.collect.Sets;
import com.webank.wecube.platform.core.domain.plugin.PluginPackage;
import com.webank.wecube.platform.core.utils.VersionUtils;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.*;
import java.util.stream.Collectors;

import static com.webank.wecube.platform.core.utils.CollectionUtils.pickLastOne;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

public interface PluginPackageRepository extends CrudRepository<PluginPackage, Integer> {

    @Query("SELECT p FROM PluginPackage p WHERE p.status IN :statuses")
    Optional<List<PluginPackage>> findAllByStatus(PluginPackage.Status... statuses);

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

    default Optional<Set<PluginPackage>> findLatestPluginPackagesByStatusGroupByPackageName(PluginPackage.Status... statuses) {
        Optional<List<PluginPackage>> idsByStatusOptional = findAllByStatus(statuses);
        if (idsByStatusOptional.isPresent()) {
            List<PluginPackage> pluginPackages = idsByStatusOptional.get();
            Map<String, TreeSet<PluginPackage>> packageNameByUploadTimestampTreeSet = new HashMap<>();
            for (PluginPackage pluginPackage: pluginPackages) {
                String packageName = pluginPackage.getName();
                if (null == packageNameByUploadTimestampTreeSet.get(packageName)) {
                    packageNameByUploadTimestampTreeSet.put(packageName, new TreeSet<>(new PluginPackageUploadTimestampComparator()));
                }
                packageNameByUploadTimestampTreeSet.get(packageName).add(pluginPackage);
            }
            return Optional.of(packageNameByUploadTimestampTreeSet.values().stream().map(ps->ps.last()).collect(Collectors.toSet()));
        }
        return Optional.empty();
    }

    class PluginPackageUploadTimestampComparator implements Comparator<PluginPackage> {
        @Override
        public int compare(PluginPackage o1, PluginPackage o2) {
            return o1.getUploadTimestamp().compareTo(o2.getUploadTimestamp());
        }
    }
}
