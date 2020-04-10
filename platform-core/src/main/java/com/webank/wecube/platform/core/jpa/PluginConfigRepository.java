package com.webank.wecube.platform.core.jpa;

import static com.webank.wecube.platform.core.domain.plugin.PluginConfig.Status.ENABLED;
import static com.webank.wecube.platform.core.utils.CollectionUtils.pickLastOne;

import java.util.*;

import com.webank.wecube.platform.core.domain.plugin.PluginPackage;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.webank.wecube.platform.core.domain.plugin.PluginConfig;
import com.webank.wecube.platform.core.domain.plugin.PluginConfig.Status;
import com.webank.wecube.platform.core.domain.plugin.PluginConfigInterface;
import com.webank.wecube.platform.core.utils.VersionUtils;

public interface PluginConfigRepository extends CrudRepository<PluginConfig, String> {
    
    boolean existsByPluginPackage_idAndNameAndRegisterName(String pluginPackageId, String name, String registerName);

    Optional<List<PluginConfig>> findAllByPluginPackage_idAndNameAndRegisterName(String pluginPackageId, String name, String registerName);

    @Query("SELECT DISTINCT inf FROM PluginConfig cfg JOIN cfg.interfaces inf LEFT JOIN FETCH inf.inputParameters LEFT JOIN FETCH inf.outputParameters WHERE cfg.id = :pluginConfigId")
    List<PluginConfigInterface> findAllPluginConfigInterfacesByConfigIdAndFetchParameters(String pluginConfigId);

    @Query("SELECT DISTINCT inf FROM PluginConfig cfg JOIN cfg.interfaces inf LEFT JOIN FETCH inf.inputParameters LEFT JOIN FETCH inf.outputParameters WHERE inf.serviceName = :serviceName and cfg.status=:status")
    List<PluginConfigInterface> findAllPluginConfigInterfaceByServiceNameAndStatusAndFetchParameters(@Param("serviceName") String serviceName, @Param("status") Status status);

    @Query("select cfg from PluginConfig cfg where cfg.status ='ENABLED' group by cfg.targetPackage,cfg.targetEntity,cfg.targetEntityFilterRule")
    List<PluginConfig> findAllPluginConfigGroupByTargetEntityWithFilterRule();

    Optional<List<PluginConfig>> findByStatus(Status status);

    Optional<List<PluginConfig>> findByPluginPackage_idOrderByName(String pluginPackageId);

    default Optional<PluginConfigInterface> findLatestOnlinePluginConfigInterfaceByServiceNameAndFetchParameters(String serviceName) {
        List<PluginConfigInterface> onlineInterfaces = findAllPluginConfigInterfaceByServiceNameAndStatusAndFetchParameters(serviceName, ENABLED);
        PluginConfigInterface pluginConfigInterface = pickLastOne(onlineInterfaces, new PluginInterfaceVersionComparator());
        return Optional.ofNullable(pluginConfigInterface);
    }

    class PluginInterfaceVersionComparator implements Comparator<PluginConfigInterface> {
        @Override
        public int compare(PluginConfigInterface o1, PluginConfigInterface o2) {
            return VersionUtils.compare(o1.getPluginConfig().getPluginPackage().getVersion(), o2.getPluginConfig().getPluginPackage().getVersion());
        }
    }

    Optional<List<PluginConfig>> findAllByStatusAndPluginPackage_statusIn(Status status, Collection<PluginPackage.Status> statuses);
    default Optional<List<PluginConfig>> findAllForAllActivePackages() {
        return findAllByStatusAndPluginPackage_statusIn(Status.ENABLED, PluginPackage.ACTIVE_STATUS);
    }

    default Optional<List<PluginConfigInterface>> findAllLatestEnabledForAllActivePackages() {
        Optional<List<PluginConfig>> allForAllActivePackagesOptional = findAllForAllActivePackages();
        if (allForAllActivePackagesOptional.isPresent()) {
            List<PluginConfig> pluginConfigs = allForAllActivePackagesOptional.get();
            Map<String, PluginConfigInterface> packageConfigInterfaceMap = new HashMap<>();
            pluginConfigs.forEach(pluginConfig -> {
                Set<PluginConfigInterface> configInterfaces = pluginConfig.getInterfaces();
                if (null != configInterfaces && configInterfaces.size() > 0) {
                    configInterfaces.forEach(configInterface -> {
                                String mapKey = buildPackageConfigInterfaceMapKey(configInterface);
                                if (packageConfigInterfaceMap.containsKey(mapKey)) {
                                    PluginConfigInterface existingConfigInterface = packageConfigInterfaceMap.get(mapKey);
                                    if (configInterface.getPluginConfig().getPluginPackage().getUploadTimestamp().compareTo(existingConfigInterface.getPluginConfig().getPluginPackage().getUploadTimestamp()) > 0) {
                                        packageConfigInterfaceMap.put(mapKey, configInterface);
                                    }
                                } else {
                                    packageConfigInterfaceMap.put(mapKey, configInterface);
                                }
                            }
                    );
                }
            });
            Set<PluginConfigInterface> pluginConfigInterfaces = new TreeSet<>(new PluginConfigInterfaceComparator());
            pluginConfigInterfaces.addAll(packageConfigInterfaceMap.values());
            return Optional.of(new ArrayList<>(pluginConfigInterfaces));
        }

        return Optional.empty();
    }

    default String buildPackageConfigInterfaceMapKey(PluginConfigInterface pluginConfigInterface) {
        PluginConfig pluginConfig = pluginConfigInterface.getPluginConfig();
        return String.join(":", pluginConfig.getPluginPackage().getName(), pluginConfig.getName(), pluginConfig.getTargetEntity(), pluginConfigInterface.getAction());
    }

    class PluginConfigInterfaceComparator implements Comparator<PluginConfigInterface> {
        @Override
        public int compare(PluginConfigInterface interface1, PluginConfigInterface interface2) {
            return interface1.getId().compareTo(interface2.getId());
        }
    }

}
