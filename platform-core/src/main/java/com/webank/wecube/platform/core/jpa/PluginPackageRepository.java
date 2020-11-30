//package com.webank.wecube.platform.core.jpa;
//
//import static com.webank.wecube.platform.core.utils.CollectionUtils.pickLastOne;
//import static org.apache.commons.collections4.CollectionUtils.isEmpty;
//
//import java.util.Collection;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//import java.util.Set;
//import java.util.TreeSet;
//import java.util.stream.Collectors;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//
//import com.google.common.collect.Sets;
//import com.webank.wecube.platform.core.domain.plugin.PluginPackage;
//import com.webank.wecube.platform.core.utils.VersionUtils;
//
//public interface PluginPackageRepository extends JpaRepository<PluginPackage, String> {
//
//    @Query("SELECT p FROM PluginPackage p WHERE p.status IN :statuses")
//    Optional<List<PluginPackage>> findAllByStatus(PluginPackage.Status... statuses);
//
//    default Optional<List<PluginPackage>> findAllActive() {
//        return findAllByStatus(PluginPackage.ACTIVE_STATUS.toArray(new PluginPackage.Status[0]));
//    }
//
//    List<PluginPackage> findAllByName(String name);
//
//    Optional<List<PluginPackage>> findAllByNameAndStatusInOrderByUploadTimestampAsc(String name, Collection<PluginPackage.Status> statuses);
//
//    default Optional<List<PluginPackage>> findAllActiveByNameOrderByUploadTimestampAsc(String name) {
//        return findAllByNameAndStatusInOrderByUploadTimestampAsc(name, PluginPackage.ACTIVE_STATUS);
//    }
//
//    default Optional<PluginPackage> findLatestActiveVersionByName(String name) {
//        Optional<List<PluginPackage>> pluginPackages = findAllActiveByNameOrderByUploadTimestampAsc(name);
//        if (!pluginPackages.isPresent()) return Optional.empty();
//
//        List<PluginPackage> packageList=pluginPackages.get();
//        return Optional.ofNullable(packageList.get(packageList.size()-1));
//    }
//    
//    default List<PluginPackage> findLatestActiveVersionPluginPackagesByName(String name) {
//        Optional<List<PluginPackage>> pluginPackagesOpt = findAllActiveByNameOrderByUploadTimestampAsc(name);
//        if (!pluginPackagesOpt.isPresent()) {
//        	return Collections.emptyList();
//        }
//        
//        return pluginPackagesOpt.get();
//
//    }
//
//    default Optional<PluginPackage> findLatestVersionByName(String name, String... excludeVersions) {
//        List<PluginPackage> pluginPackages = findAllByName(name);
//        if (isEmpty(pluginPackages)) return Optional.empty();
//        if (excludeVersions != null) {
//            Set<String> excludeVersionSet = Sets.newHashSet(excludeVersions);
//            pluginPackages = pluginPackages.stream()
//                    .filter(pluginPackage -> !excludeVersionSet.contains(pluginPackage.getVersion()))
//                    .collect(Collectors.toList());
//        }
//        return Optional.ofNullable(pickLastOne(pluginPackages, new PluginPackageVersionComparator()));
//    }
//
//    @Query(value = "SELECT DISTINCT package.name " +
//            "FROM PluginPackage package " +
//            "GROUP BY package.name")
//    Optional<List<String>> findAllDistinctPackage();
//
//    boolean existsByName(String name);
//
//    Optional<PluginPackage> findTop1ByNameOrderByVersionDesc(String packageName);
//
//    long countByNameAndVersion(String name, String version);
//
//    class PluginPackageVersionComparator implements Comparator<PluginPackage> {
//        @Override
//        public int compare(PluginPackage o1, PluginPackage o2) {
//            return VersionUtils.compare(o1.getVersion(), o2.getVersion());
//        }
//    }
//
//    default Optional<Set<PluginPackage>> findLatestPluginPackagesByStatusGroupByPackageName(PluginPackage.Status... statuses) {
//        Optional<List<PluginPackage>> idsByStatusOptional = findAllByStatus(statuses);
//        if (idsByStatusOptional.isPresent()) {
//            List<PluginPackage> pluginPackages = idsByStatusOptional.get();
//            Map<String, TreeSet<PluginPackage>> packageNameByUploadTimestampTreeSet = new HashMap<>();
//            for (PluginPackage pluginPackage: pluginPackages) {
//                String packageName = pluginPackage.getName();
//                if (null == packageNameByUploadTimestampTreeSet.get(packageName)) {
//                    packageNameByUploadTimestampTreeSet.put(packageName, new TreeSet<>(new PluginPackageUploadTimestampComparator()));
//                }
//                packageNameByUploadTimestampTreeSet.get(packageName).add(pluginPackage);
//            }
//            return Optional.of(packageNameByUploadTimestampTreeSet.values().stream().map(ps->ps.last()).collect(Collectors.toSet()));
//        }
//        return Optional.empty();
//    }
//
//    class PluginPackageUploadTimestampComparator implements Comparator<PluginPackage> {
//        @Override
//        public int compare(PluginPackage o1, PluginPackage o2) {
//            return o1.getUploadTimestamp().compareTo(o2.getUploadTimestamp());
//        }
//    }
//}
