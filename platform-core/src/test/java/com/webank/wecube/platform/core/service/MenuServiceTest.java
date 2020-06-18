package com.webank.wecube.platform.core.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.webank.wecube.platform.core.DatabaseBasedTest;
import com.webank.wecube.platform.core.domain.MenuItem;
import com.webank.wecube.platform.core.domain.plugin.PluginPackage;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageMenu;
import com.webank.wecube.platform.core.dto.MenuItemDto;
import com.webank.wecube.platform.core.jpa.MenuItemRepository;
import com.webank.wecube.platform.core.jpa.PluginPackageMenuRepository;
import com.webank.wecube.platform.core.jpa.PluginPackageRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.util.List;

import static com.webank.wecube.platform.core.domain.plugin.PluginPackage.Status.*;
import static org.assertj.core.api.Assertions.assertThat;

public class MenuServiceTest extends DatabaseBasedTest {

    @Autowired
    private MenuService menuService;

    @Autowired
    private PluginPackageRepository pluginPackageRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Test
    public void givenMultipleVersionPackagesWithMenuWhenFindAllForAllActivePackagesThenReturnOnlyMenusForActivePackages() {

        long now = System.currentTimeMillis();
        PluginPackage unregisteredPluginPackage = new PluginPackage(null, "wecmdb", "v0.1", UNREGISTERED,
                new Timestamp(now), false);
        PluginPackage runningPluginPackage = new PluginPackage(null, "wecmdb", "v0.3", RUNNING,
                new Timestamp(now + 20000), false);
        PluginPackage stoppedPluginPackage = new PluginPackage(null, "wecmdb", "v0.4", STOPPED,
                new Timestamp(now + 30000), false);
        PluginPackage decommissionedPluginPackage = new PluginPackage(null, "wecmdb", "v0.5", DECOMMISSIONED,
                new Timestamp(now + 40000), false);

        MenuItem menuItem = new MenuItem("DESIGNING", null, null);

        PluginPackageMenu packageMenuForUnregistered = new PluginPackageMenu(unregisteredPluginPackage,
                "DESIGNING_CI_DATA_ENQUIRY", "DESIGNING", "CI Data Enquiry", "/wecmdb/designing/ci-data-enquiry");
        PluginPackageMenu packageMenuForRunning1 = new PluginPackageMenu(runningPluginPackage,
                "DESIGNING_CI_INTEGRATED_QUERY_EXECUTION", "DESIGNING", "CI Integrated Enquiry",
                "/wecmdb/designing/ci-integrated-query-execution");
        PluginPackageMenu packageMenuForRunning2 = new PluginPackageMenu(runningPluginPackage,
                "CMDB_DESIGNING_ENUM_ENQUIRY", "DESIGNING", "Enum Enquiry", "/wecmdb/designing/enum-enquiry");
        PluginPackageMenu packageMenuForStopped = new PluginPackageMenu(stoppedPluginPackage,
                "DESIGNING_CI_INTEGRATED_QUERY_MANAGEMENT", "DESIGNING", "CI Integrated Enquiry Management",
                "/wecmdb/designing/ci-integrated-query-management");
        PluginPackageMenu packageMenuForDecommissioned = new PluginPackageMenu(decommissionedPluginPackage,
                "DESIGNING_CI_DATA_MANAGEMENT", "DESIGNING", "CI Data Management",
                "/wecmdb/designing/ci-data-management");

        unregisteredPluginPackage.setPluginPackageMenus(Sets.newHashSet(packageMenuForUnregistered));
        runningPluginPackage.setPluginPackageMenus(Sets.newHashSet(packageMenuForRunning1, packageMenuForRunning2));
        stoppedPluginPackage.setPluginPackageMenus(Sets.newHashSet(packageMenuForStopped));
        decommissionedPluginPackage.setPluginPackageMenus(Sets.newHashSet(packageMenuForDecommissioned));

        menuItemRepository.save(menuItem);

        pluginPackageRepository.saveAll(Lists.newArrayList(unregisteredPluginPackage, runningPluginPackage,
                stoppedPluginPackage, decommissionedPluginPackage));

        List<MenuItemDto> allMenus = menuService.getAllMenus();
        assertThat(allMenus).isNotNull();
        assertThat(allMenus).hasSize(4);

    }

}