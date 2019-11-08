package com.webank.wecube.platform.core.service;

import com.webank.wecube.platform.core.DatabaseBasedTest;
import com.webank.wecube.platform.core.domain.plugin.PluginPackage;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageMenu;
import com.webank.wecube.platform.core.dto.MenuItemDto;
import com.webank.wecube.platform.core.jpa.PluginPackageRepository;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

public class UserManagementServiceTest extends DatabaseBasedTest {
    @Autowired
    UserManagerService userManagerService;
    @Autowired
    PluginPackageRepository pluginPackageRepository;

    private void prepareDatabase() {
        executeSqlScripts(newArrayList(new ClassPathResource("/database/03.wecube.test.data.sql")));
    }

    private void executeSqlScripts(List<Resource> scipts) {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.setContinueOnError(false);
        populator.setIgnoreFailedDrops(false);
        populator.setSeparator(";");
        scipts.forEach(populator::addScript);
        populator.execute(dataSource);
    }

    @Test
    public void sortMenusByIdShouldSuccess() {
        prepareDatabase();

        boolean sortByMenuCodeFailed = false;
        for (int i = 0; i < 10; i++) {
            List<MenuItemDto> menusDtos = userManagerService.getAllSysMenus();
            if (menusDtos.get(menusDtos.size() - 1).getId().compareTo(menusDtos.get(0).getId()) < 0) {
                sortByMenuCodeFailed = true;
            }
        }
        assertThat(sortByMenuCodeFailed).isEqualTo(false);
    }

    @Test
    public void sortSubMenusByIdShouldSuccess() {
        prepareDatabase();

        Optional<PluginPackage> pluginPackage = pluginPackageRepository.findById(25);
        Set<PluginPackageMenu> packageMenus = pluginPackage.get().getPluginPackageMenus();
        List<PluginPackageMenu> menusDtos = userManagerService.sortPluginPackageMenusById(packageMenus);
        if (menusDtos.size() > 0) {
            assertThat(menusDtos.get(menusDtos.size() - 1).getId().compareTo(menusDtos.get(0).getId()))
                    .isGreaterThanOrEqualTo(0);
        }
    }
}
