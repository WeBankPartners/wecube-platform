package com.webank.wecube.platform.core.service;

import com.webank.wecube.platform.core.DatabaseBasedTest;
import com.webank.wecube.platform.core.domain.plugin.PluginPackage;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageMenu;
import com.webank.wecube.platform.core.dto.MenuItemDto;
import com.webank.wecube.platform.core.jpa.PluginPackageRepository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class UserManagementServiceTest extends DatabaseBasedTest {
    @Autowired
    UserManagerService userManagerService;
    @Autowired
    PluginPackageRepository pluginPackageRepository;

    @Test
    public void sortMenusByIdShouldSuccess() {
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
        Optional<PluginPackage> pluginPackage = pluginPackageRepository.findById(25);
        Set<PluginPackageMenu> packageMenus = pluginPackage.get().getPluginPackageMenus();
        List<PluginPackageMenu> menusDtos = userManagerService.sortPluginPackageMenusById(packageMenus);
        if (menusDtos.size() > 0) {
            assertThat(menusDtos.get(menusDtos.size() - 1).getId().compareTo(menusDtos.get(0).getId()))
                    .isGreaterThanOrEqualTo(0);
        }
    }
}
