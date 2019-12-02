package com.webank.wecube.platform.core.service;

import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.MenuItem;
import com.webank.wecube.platform.core.domain.plugin.PluginPackage;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageMenu;
import com.webank.wecube.platform.core.dto.MenuItemDto;
import com.webank.wecube.platform.core.jpa.MenuItemRepository;
import com.webank.wecube.platform.core.jpa.PluginPackageRepository;
import com.webank.wecube.platform.core.service.plugin.PluginPackageService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
public class MenuService {

    @Autowired
    MenuItemRepository menuItemRepository;

    @Autowired
    PluginPackageService pluginPackageService;
    @Autowired
    PluginPackageRepository pluginPackageRepository;

    public List<MenuItem> getAllSysMenuItems() {
        return Lists.newArrayList(menuItemRepository.findAll());
    }

    public List<MenuItemDto> getAllSysMenus() {
        List<MenuItemDto> returnMenuDto = new ArrayList<>();
        Iterable<MenuItem> systemMenus = menuItemRepository.findAll();
        for (MenuItem systemMenu : systemMenus) {
            MenuItemDto systemMenuDto = MenuItemDto.fromSystemMenuItem(systemMenu);
            returnMenuDto.add(systemMenuDto);
        }
        return returnMenuDto;
    }

    public List<MenuItemDto> getAllMenus() {
        List<MenuItemDto> returnMenuDto;

        List<MenuItemDto> allSysMenus = getAllSysMenus();
        returnMenuDto = new ArrayList<>(allSysMenus);

        PluginPackage.Status[] statusArray = { PluginPackage.Status.REGISTERED, PluginPackage.Status.RUNNING,
                PluginPackage.Status.STOPPED };
        Optional<List<PluginPackage>> pluginPackagesOptional = pluginPackageRepository.findAllByStatus(statusArray);
        if (pluginPackagesOptional.isPresent()) {
            List<PluginPackage> packages = pluginPackagesOptional.get();

            for (PluginPackage packageDomain : packages) {
                Set<PluginPackageMenu> packageMenus = packageDomain.getPluginPackageMenus();
                List<PluginPackageMenu> packageMenusList = sortPluginPackageMenusById(packageMenus);

                for (int i = 0; i < packageMenusList.size(); i++) {
                    PluginPackageMenu packageMenu = packageMenusList.get(i);
                    if (! menuItemRepository.existsByCode(packageMenu.getCategory())) {
                        String msg = String.format("Cannot find system menu item by package menu's category: [%s]",
                                packageMenu.getCategory());
                        log.error(msg);
                        throw new WecubeCoreException(msg);
                    }
                    MenuItemDto packageMenuDto = MenuItemDto.fromPackageMenuItem(packageMenu);
                    returnMenuDto.add(packageMenuDto);
                }
            }
        }
        Collections.sort(returnMenuDto);

        return returnMenuDto;
    }

    public List<PluginPackageMenu> sortPluginPackageMenusById(Set<PluginPackageMenu> packageMenus) {
        List<PluginPackageMenu> packageMenusList = new ArrayList<>(packageMenus);
        Collections.sort(packageMenusList);
        return packageMenusList;
    }

}
