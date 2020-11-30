//package com.webank.wecube.platform.core.lazyJpa;
//
//import com.google.common.collect.Sets;
//import com.webank.wecube.platform.core.domain.plugin.PluginPackage;
//import com.webank.wecube.platform.core.lazyDomain.plugin.LazyPluginPackage;
//import com.webank.wecube.platform.core.utils.VersionUtils;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//
//import java.util.*;
//import java.util.stream.Collectors;
//
//import static com.webank.wecube.platform.core.utils.CollectionUtils.pickLastOne;
//import static org.apache.commons.collections4.CollectionUtils.isEmpty;
//
//public interface LazyPluginPackageRepository extends JpaRepository<LazyPluginPackage, String> {
//
//    @Query("SELECT p FROM LazyPluginPackage p WHERE p.status IN :statuses")
//    Optional<List<LazyPluginPackage>> findAllByStatus(LazyPluginPackage.Status... statuses);
//
//    default Optional<List<LazyPluginPackage>> findAllActive() {
//        return findAllByStatus(LazyPluginPackage.ACTIVE_STATUS.toArray(new LazyPluginPackage.Status[0]));
//    }
//
//    List<LazyPluginPackage> findAllByName(String name);
//
//    Optional<List<LazyPluginPackage>> findAllByNameAndStatusInOrderByUploadTimestampAsc(String name, Collection<LazyPluginPackage.Status> statuses);
//
//    default Optional<List<LazyPluginPackage>> findAllActiveByNameOrderByUploadTimestampAsc(String name) {
//        return findAllByNameAndStatusInOrderByUploadTimestampAsc(name, LazyPluginPackage.ACTIVE_STATUS);
//    }
//
//    default Optional<LazyPluginPackage> findLatestActiveVersionByName(String name) {
//        Optional<List<LazyPluginPackage>> pluginPackages = findAllActiveByNameOrderByUploadTimestampAsc(name);
//        if (!pluginPackages.isPresent()) return Optional.empty();
//
//        List<LazyPluginPackage> packageList=pluginPackages.get();
//        return Optional.ofNullable(packageList.get(packageList.size()-1));
//    }
//    
//    default List<LazyPluginPackage> findLatestActiveVersionPluginPackagesByName(String name) {
//        Optional<List<LazyPluginPackage>> pluginPackagesOpt = findAllActiveByNameOrderByUploadTimestampAsc(name);
//        if (!pluginPackagesOpt.isPresent()) {
//        	return Collections.emptyList();
//        }
//        
//        return pluginPackagesOpt.get();
//
//    }
//
//    default Optional<LazyPluginPackage> findLatestVersionByName(String name, String... excludeVersions) {
//        List<LazyPluginPackage> pluginPackages = findAllByName(name);
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
//            "FROM LazyPluginPackage package " +
//            "GROUP BY package.name")
//    Optional<List<String>> findAllDistinctPackage();
//
//    boolean existsByName(String name);
//
//    Optional<LazyPluginPackage> findTop1ByNameOrderByVersionDesc(String packageName);
//
//    long countByNameAndVersion(String name, String version);
//
//    class PluginPackageVersionComparator implements Comparator<LazyPluginPackage> {
//        @Override
//        public int compare(LazyPluginPackage o1, LazyPluginPackage o2) {
//            return VersionUtils.compare(o1.getVersion(), o2.getVersion());
//        }
//    }
//
//    default Optional<Set<LazyPluginPackage>> findLatestPluginPackagesByStatusGroupByPackageName(LazyPluginPackage.Status... statuses) {
//        Optional<List<LazyPluginPackage>> idsByStatusOptional = findAllByStatus(statuses);
//        if (idsByStatusOptional.isPresent()) {
//            List<LazyPluginPackage> pluginPackages = idsByStatusOptional.get();
//            Map<String, TreeSet<LazyPluginPackage>> packageNameByUploadTimestampTreeSet = new HashMap<>();
//            for (LazyPluginPackage pluginPackage: pluginPackages) {
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
//    class PluginPackageUploadTimestampComparator implements Comparator<LazyPluginPackage> {
//        @Override
//        public int compare(LazyPluginPackage o1, LazyPluginPackage o2) {
//            return o1.getUploadTimestamp().compareTo(o2.getUploadTimestamp());
//        }
//    }
//}
