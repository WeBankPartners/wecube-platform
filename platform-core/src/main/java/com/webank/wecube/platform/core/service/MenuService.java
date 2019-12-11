package com.webank.wecube.platform.core.service;

import java.util.*;

import com.webank.wecube.platform.core.jpa.PluginPackageMenuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.MenuItem;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageMenu;
import com.webank.wecube.platform.core.dto.MenuItemDto;
import com.webank.wecube.platform.core.jpa.MenuItemRepository;
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
    private PluginPackageMenuRepository pluginPackageMenuRepository;

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

        Optional<List<PluginPackageMenu>> optionalPluginPackageMenus = pluginPackageMenuRepository.findAndMergePluginMenus();
        if (optionalPluginPackageMenus.isPresent()) {
            optionalPluginPackageMenus.get().forEach(packageMenu -> {
                MenuItemDto packageMenuDto = transferPackageMenuToMenuItemDto(packageMenu);
                returnMenuDto.add(packageMenuDto);
            });
        }
        Collections.sort(returnMenuDto);

        return returnMenuDto;
    }

    public MenuItemDto transferPackageMenuToMenuItemDto(PluginPackageMenu packageMenu) throws WecubeCoreException {
        MenuItem menuItem = menuItemRepository.findByCode(packageMenu.getCategory());
        if (null == menuItem) {
            String msg = String.format("Cannot find system menu item by package menu's category: [%s]",
                    packageMenu.getCategory());
            log.error(msg);
            throw new WecubeCoreException(msg);
        }
        return MenuItemDto.fromPackageMenuItem(packageMenu, menuItem);
    }

    public List<PluginPackageMenu> sortPluginPackageMenusById(Set<PluginPackageMenu> packageMenus) {
        List<PluginPackageMenu> packageMenusList = new ArrayList<>(packageMenus);
        Collections.sort(packageMenusList);
        return packageMenusList;
    }

}
