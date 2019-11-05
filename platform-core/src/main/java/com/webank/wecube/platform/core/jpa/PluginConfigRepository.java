package com.webank.wecube.platform.core.jpa;

import com.webank.wecube.platform.core.utils.VersionUtils;
import com.webank.wecube.platform.core.domain.plugin.PluginConfig;
import com.webank.wecube.platform.core.domain.plugin.PluginConfig.Status;
import com.webank.wecube.platform.core.domain.plugin.PluginConfigInterface;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import static com.webank.wecube.platform.core.domain.plugin.PluginConfig.Status.ENABLED;
import static com.webank.wecube.platform.core.utils.CollectionUtils.pickLastOne;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public interface PluginConfigRepository extends CrudRepository<PluginConfig, Integer> {
    @Query("SELECT DISTINCT inf FROM PluginConfig cfg JOIN cfg.interfaces inf LEFT JOIN FETCH inf.inputParameters LEFT JOIN FETCH inf.outputParameters WHERE cfg.id = :pluginConfigId")
    List<PluginConfigInterface> findAllPluginConfigInterfacesByConfigIdAndFetchParameters(int pluginConfigId);

    @Query("SELECT DISTINCT inf FROM PluginConfig cfg JOIN cfg.interfaces inf LEFT JOIN FETCH inf.inputParameters LEFT JOIN FETCH inf.outputParameters WHERE inf.serviceName = :serviceName and cfg.status=:status")
    List<PluginConfigInterface> findAllPluginConfigInterfaceByServiceNameAndStatusAndFetchParameters(String serviceName, Status status);
    
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
}
