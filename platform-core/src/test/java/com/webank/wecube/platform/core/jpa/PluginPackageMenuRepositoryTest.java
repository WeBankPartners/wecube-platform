package com.webank.wecube.platform.core.jpa;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.webank.wecube.platform.core.DatabaseBasedTest;
import com.webank.wecube.platform.core.domain.plugin.PluginPackage;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageMenu;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import static com.webank.wecube.platform.core.domain.plugin.PluginPackage.Status.*;
import static org.assertj.core.api.Assertions.assertThat;

public class PluginPackageMenuRepositoryTest extends DatabaseBasedTest {
    @Autowired
    private PluginPackageRepository packageRepository;
    @Autowired
    private PluginPackageMenuRepository menuRepository;

    @Test
    public void givenMultipleVersionPackagesWithMenuWhenFindAllForAllActivePackagesThenReturnOnlyMenusForActivePackages() {

        long now = System.currentTimeMillis();
        PluginPackage unregisteredPluginPackage     = new PluginPackage(null, "wecmdb", "v0.1", UNREGISTERED, new Timestamp(now), false);
        PluginPackage registeredPluginPackage       = new PluginPackage(null, "wecmdb", "v0.2", REGISTERED, new Timestamp(now + 10000), false);
        PluginPackage runningPluginPackage          = new PluginPackage(null, "wecmdb", "v0.3", RUNNING, new Timestamp(now + 20000), false);
        PluginPackage stoppedPluginPackage          = new PluginPackage(null, "wecmdb", "v0.4", STOPPED, new Timestamp(now + 30000), false);
        PluginPackage decommissionedPluginPackage   = new PluginPackage(null, "wecmdb", "v0.5", DECOMMISSIONED, new Timestamp(now + 40000), false);

        PluginPackageMenu packageMenuForUnregistered    = new PluginPackageMenu(unregisteredPluginPackage, "DESIGNING_CI_DATA_ENQUIRY", "DESIGNING", "CI Data Enquiry", "/wecmdb/designing/ci-data-enquiry");
        PluginPackageMenu packageMenuForRegistered      = new PluginPackageMenu(registeredPluginPackage, "DESIGNING_CI_INTEGRATED_QUERY_EXECUTION", "DESIGNING", "CI Integrated Enquiry", "/wecmdb/designing/ci-integrated-query-execution");
        PluginPackageMenu packageMenuForRunning         = new PluginPackageMenu(runningPluginPackage, "CMDB_DESIGNING_ENUM_ENQUIRY", "DESIGNING", "Enum Enquiry", "/wecmdb/designing/enum-enquiry");
        PluginPackageMenu packageMenuForStopped         = new PluginPackageMenu(stoppedPluginPackage, "DESIGNING_CI_INTEGRATED_QUERY_MANAGEMENT", "DESIGNING", "CI Integrated Enquiry Management", "/wecmdb/designing/ci-integrated-query-management");
        PluginPackageMenu packageMenuForDecommissioned  = new PluginPackageMenu(decommissionedPluginPackage, "DESIGNING_CI_DATA_MANAGEMENT", "DESIGNING", "CI Data Management", "/wecmdb/designing/ci-data-management");

        unregisteredPluginPackage.setPluginPackageMenus(Sets.newHashSet(packageMenuForUnregistered));
        registeredPluginPackage.setPluginPackageMenus(Sets.newHashSet(packageMenuForRegistered));
        runningPluginPackage.setPluginPackageMenus(Sets.newHashSet(packageMenuForRunning));
        stoppedPluginPackage.setPluginPackageMenus(Sets.newHashSet(packageMenuForStopped));
        decommissionedPluginPackage.setPluginPackageMenus(Sets.newHashSet(packageMenuForDecommissioned));

        packageRepository.saveAll(Lists.newArrayList(unregisteredPluginPackage, registeredPluginPackage, runningPluginPackage, stoppedPluginPackage, decommissionedPluginPackage));

        Iterable<PluginPackageMenu> menuIterable = menuRepository.findAll();
        assertThat(menuIterable).hasSize(5);

        Optional<List<PluginPackageMenu>> allMenusForAllActivePackages = menuRepository.findAllForAllActivePackages();
        assertThat(allMenusForAllActivePackages.isPresent()).isTrue();
        List<PluginPackageMenu> pluginPackageMenus = allMenusForAllActivePackages.get();
        assertThat(pluginPackageMenus).hasSize(3);

    }
}