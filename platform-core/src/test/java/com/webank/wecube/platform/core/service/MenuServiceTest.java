package com.webank.wecube.platform.core.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.webank.wecube.platform.core.DatabaseBasedTest;
import com.webank.wecube.platform.core.dto.plugin.MenuItemDto;
import com.webank.wecube.platform.core.entity.plugin.MenuItems;
import com.webank.wecube.platform.core.entity.plugin.PluginPackageMenus;
import com.webank.wecube.platform.core.entity.plugin.PluginPackages;
import com.webank.wecube.platform.core.repository.plugin.MenuItemsMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginPackageMenusMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginPackagesMapper;
import com.webank.wecube.platform.core.service.plugin.MenuService;
import com.webank.wecube.platform.workflow.commons.LocalIdGenerator;

public class MenuServiceTest extends DatabaseBasedTest {

    @Autowired
    private MenuService menuService;

    @Autowired
    private PluginPackagesMapper pluginPackageRepository;

    @Autowired
    private MenuItemsMapper menuItemRepository;
    
    @Autowired
    private PluginPackageMenusMapper pluginPackageMenusMapper;

    @Test
    public void givenMultiVerPkgsWithMenuWhenFindAllForAllActivePackagesThenReturnOnlyMenusForActivePackages() {

        long now = System.currentTimeMillis();
        PluginPackages unregisteredPluginPackage = buildPluginPackages(LocalIdGenerator.generateId(), "wecmdb", "v0.1",
                PluginPackages.UNREGISTERED, new Date(now), false);
        PluginPackages runningPluginPackage = buildPluginPackages(LocalIdGenerator.generateId(), "wecmdb", "v0.3", PluginPackages.RUNNING,
                new Date(now + 20000), false);
        PluginPackages stoppedPluginPackage = buildPluginPackages(LocalIdGenerator.generateId(), "wecmdb", "v0.4", PluginPackages.STOPPED,
                new Date(now + 30000), false);
        PluginPackages decommissionedPluginPackage = buildPluginPackages(LocalIdGenerator.generateId(), "wecmdb", "v0.5",
                PluginPackages.DECOMMISSIONED, new Date(now + 40000), false);

        MenuItems menuItem = new MenuItems();
        menuItem.setId(LocalIdGenerator.generateId());
        menuItem.setCode("DESIGNING");

        PluginPackageMenus packageMenuForUnregistered = buildPluginPackageMenus(unregisteredPluginPackage,
                "DESIGNING_CI_DATA_ENQUIRY", "DESIGNING", "CI Data Enquiry", "/wecmdb/designing/ci-data-enquiry");
        
        pluginPackageMenusMapper.insert(packageMenuForUnregistered);
        PluginPackageMenus packageMenuForRunning1 = buildPluginPackageMenus(runningPluginPackage,
                "DESIGNING_CI_INTEGRATED_QUERY_EXECUTION", "DESIGNING", "CI Integrated Enquiry",
                "/wecmdb/designing/ci-integrated-query-execution");
        
        pluginPackageMenusMapper.insert(packageMenuForRunning1);
        PluginPackageMenus packageMenuForRunning2 = buildPluginPackageMenus(runningPluginPackage,
                "CMDB_DESIGNING_ENUM_ENQUIRY", "DESIGNING", "Enum Enquiry", "/wecmdb/designing/enum-enquiry");
        
        pluginPackageMenusMapper.insert(packageMenuForRunning2);
        PluginPackageMenus packageMenuForStopped = buildPluginPackageMenus(stoppedPluginPackage,
                "DESIGNING_CI_INTEGRATED_QUERY_MANAGEMENT", "DESIGNING", "CI Integrated Enquiry Management",
                "/wecmdb/designing/ci-integrated-query-management");
        
        pluginPackageMenusMapper.insert(packageMenuForStopped);
        PluginPackageMenus packageMenuForDecommissioned = buildPluginPackageMenus(decommissionedPluginPackage,
                "DESIGNING_CI_DATA_MANAGEMENT", "DESIGNING", "CI Data Management",
                "/wecmdb/designing/ci-data-management");
        
        
        pluginPackageMenusMapper.insert(packageMenuForDecommissioned);

        // unregisteredPluginPackage.setPluginPackageMenus(Sets.newHashSet(packageMenuForUnregistered));
        // runningPluginPackage.setPluginPackageMenus(Sets.newHashSet(packageMenuForRunning1,
        // packageMenuForRunning2));
        // stoppedPluginPackage.setPluginPackageMenus(Sets.newHashSet(packageMenuForStopped));
        // decommissionedPluginPackage.setPluginPackageMenus(Sets.newHashSet(packageMenuForDecommissioned));

        menuItemRepository.insert(menuItem);

        pluginPackageRepository.insert(unregisteredPluginPackage);
        pluginPackageRepository.insert(runningPluginPackage);

        pluginPackageRepository.insert(stoppedPluginPackage);

        pluginPackageRepository.insert(decommissionedPluginPackage);

        List<MenuItemDto> allMenus = menuService.getAllMenus();
        assertThat(allMenus).isNotNull();
        assertThat(allMenus).hasSize(4);

    }

    private PluginPackageMenus buildPluginPackageMenus(PluginPackages pluginPackage, String code, String category,
            String displayName, String path) {

        PluginPackageMenus e = new PluginPackageMenus();
        // e.setActive(active);
        e.setCategory(category);
        e.setCode(code);
        e.setDisplayName(displayName);
        e.setPath(path);
        e.setCategory(category);
        e.setPluginPackageId(pluginPackage.getId());
        
        e.setId(LocalIdGenerator.generateId());

        return e;

    }

    private PluginPackages buildPluginPackages(String id, String name, String version, String status,
            Date uploadTimestamp, boolean uiPackageIncluded) {
        PluginPackages e = new PluginPackages();
        e.setId(id);
        e.setName(name);
        e.setStatus(status);
        e.setUiPackageIncluded(uiPackageIncluded);
        e.setUploadTimestamp(uploadTimestamp);
        e.setVersion(version);
        

        return e;

    }

}