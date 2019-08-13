package com.webank.wecube.core.jpa;

import com.webank.wecube.core.domain.plugin.PluginConfig;
import com.webank.wecube.core.domain.plugin.PluginConfig.Status;
import com.webank.wecube.core.domain.plugin.PluginConfigFilteringRule;
import com.webank.wecube.core.domain.plugin.PluginConfigInterface;
import com.webank.wecube.core.utils.VersionUtils;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static com.webank.wecube.core.domain.plugin.PluginConfig.Status.ONLINE;
import static com.webank.wecube.core.jpa.helper.PluginConfigUtil.uniqueByServiceName;
import static com.webank.wecube.core.utils.CollectionUtils.pickLastOne;

public interface PluginConfigRepository extends CrudRepository<PluginConfig, Integer> {

    @Query("SELECT DISTINCT inf FROM PluginConfig cfg JOIN cfg.interfaces inf LEFT JOIN FETCH inf.inputParameters LEFT JOIN FETCH inf.outputParameters WHERE cfg.id = :pluginConfigId")
    List<PluginConfigInterface> findAllPluginConfigInterfacesByConfigIdAndFetchParameters(int pluginConfigId);

    @Query("SELECT DISTINCT inf FROM PluginConfig cfg JOIN cfg.interfaces inf WHERE cfg.status=:status")
    List<PluginConfigInterface> findAllPluginConfigInterfacesByStatus(Status status);

    @Query("SELECT DISTINCT inf FROM PluginConfig cfg JOIN cfg.interfaces inf WHERE cfg.status=:status and cfg.cmdbCiTypeId = :cmdbCiTypeId ")
    List<PluginConfigInterface> findAllPluginConfigInterfacesByStatusAndCmdbCiTypeId(Status status, int cmdbCiTypeId);

    @Query("SELECT DISTINCT inf FROM PluginConfig cfg JOIN cfg.interfaces inf LEFT JOIN FETCH inf.inputParameters LEFT JOIN FETCH inf.outputParameters WHERE inf.serviceName = :serviceName and cfg.status=:status")
    List<PluginConfigInterface> findAllPluginConfigInterfaceByServiceNameAndStatusAndFetchParameters(String serviceName, Status status);

    @Query("SELECT DISTINCT rule FROM PluginConfig cfg JOIN cfg.filteringRules rule WHERE cfg.id = :pluginConfigId")
    List<PluginConfigFilteringRule> findAllPluginConfigFilteringRulesByConfigId(int pluginConfigId);


    default List<PluginConfigInterface> findLatestOnlinePluginInterfaces(Integer ciTypeId) {
        List<PluginConfigInterface> pluginConfigInterfaces;
        if (ciTypeId == null) {
            pluginConfigInterfaces = findAllPluginConfigInterfacesByStatus(ONLINE);
        } else {
            pluginConfigInterfaces = findAllPluginConfigInterfacesByStatusAndCmdbCiTypeId(ONLINE, ciTypeId);
        }

        pluginConfigInterfaces = uniqueByServiceName(pluginConfigInterfaces);

        return pluginConfigInterfaces;
    }

    default Optional<PluginConfigInterface> findLatestOnlinePluginConfigInterfaceByServiceNameAndFetchParameters(String serviceName) {
        List<PluginConfigInterface> onlineInterfaces = findAllPluginConfigInterfaceByServiceNameAndStatusAndFetchParameters(serviceName, ONLINE);
        PluginConfigInterface pluginConfigInterface = pickLastOne(onlineInterfaces, new PluginInterfaceVersionComparator());
        return Optional.ofNullable(pluginConfigInterface);
    }

    class PluginInterfaceVersionComparator implements Comparator<PluginConfigInterface> {
        @Override
        public int compare(PluginConfigInterface o1, PluginConfigInterface o2) {
            return VersionUtils.compare(o1.getPluginConfig().getPluginPackage().getVersion(), o2.getPluginConfig().getPluginPackage().getVersion());
        }
    }
}
