package com.webank.wecube.platform.core.jpa;

import com.webank.wecube.platform.core.domain.plugin.PluginPackage;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageMenu;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PluginPackageMenuRepository extends CrudRepository<PluginPackageMenu, String> {
    Optional<List<PluginPackageMenu>> findAllByPluginPackage_statusIn(Collection<PluginPackage.Status> statuses);

    default Optional<List<PluginPackageMenu>> findAllForAllActivePackages() {
        return findAllByPluginPackage_statusIn(PluginPackage.ACTIVE_STATUS);
    }

}
