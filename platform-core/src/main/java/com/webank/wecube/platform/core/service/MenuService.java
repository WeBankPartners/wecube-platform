package com.webank.wecube.platform.core.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.commons.AuthenticationContextHolder;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.plugin.MenuItemDto;
import com.webank.wecube.platform.core.entity.plugin.MenuItems;
import com.webank.wecube.platform.core.entity.plugin.PluginPackageMenus;
import com.webank.wecube.platform.core.entity.plugin.PluginPackages;
import com.webank.wecube.platform.core.repository.plugin.MenuItemsMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginPackageMenusMapper;
import com.webank.wecube.platform.core.service.user.RoleMenuService;

@Service
public class MenuService {
    private static final Logger log = LoggerFactory.getLogger(MenuService.class);

    @Autowired
    private MenuItemsMapper menuItemsMapper;

    @Autowired
    private PluginPackageMenusMapper pluginPackageMenusMapper;

    @Autowired
    private RoleMenuService roleMenuService;

    private PluginPackageMenuOrderComparator pluginPackageMenuOrderComparator = new PluginPackageMenuOrderComparator();

    /**
     * 
     * 
     * @return
     */
    public List<MenuItemDto> getAllSysMenus() {
        List<MenuItemDto> menuItemDtos = new ArrayList<>();
        List<MenuItems> menuItemsEntities = menuItemsMapper.selectAll();
        if (menuItemsEntities == null || menuItemsEntities.isEmpty()) {
            return menuItemDtos;
        }

        for (MenuItems menuItemsEntity : menuItemsEntities) {
            MenuItemDto menuItemDto = buildSystemMenuItemDto(menuItemsEntity);
            menuItemDtos.add(menuItemDto);
        }

        return menuItemDtos;
    }

    /**
     * 
     * @return
     */
    public List<MenuItemDto> getAllSysRootMenus() {
        List<MenuItemDto> menuItemDtos = new ArrayList<>();
        List<MenuItems> menuItemsEntities = menuItemsMapper.selectAllRootMenuItems();

        if (menuItemsEntities == null || menuItemsEntities.isEmpty()) {
            return menuItemDtos;
        }

        for (MenuItems menuItemsEntity : menuItemsEntities) {
            MenuItemDto menuItemDto = buildSystemMenuItemDto(menuItemsEntity);
            menuItemDtos.add(menuItemDto);
        }

        return menuItemDtos;
    }

    /**
     * 
     * @return
     */
    public List<MenuItemDto> getAllMenus() {

        List<MenuItemDto> resultMenuItemDtos = new ArrayList<>();
        List<MenuItemDto> allSysMenusDtos = getAllSysMenus();

        resultMenuItemDtos.addAll(allSysMenusDtos);

        List<PluginPackageMenus> pluginPackageMenusEntities = calAvailablePluginPackgeMenus();
        if(pluginPackageMenusEntities == null ){
            return resultMenuItemDtos;
        }
        for (PluginPackageMenus pluginPackageMenuEntity : pluginPackageMenusEntities) {
            MenuItemDto pluginPackageMenuItemDto = buildPackageMenuItemDto(pluginPackageMenuEntity);
            resultMenuItemDtos.add(pluginPackageMenuItemDto);
        }
        Collections.sort(resultMenuItemDtos);
        return resultMenuItemDtos;
    }

    /**
     * 
     * @return
     */
    public List<MenuItemDto> getCurrentUserAllMenus() {
        List<MenuItemDto> resultMenuItemDtos = new ArrayList<>();

        List<MenuItemDto> rootSysMenuItemDtos = getAllSysRootMenus();
        resultMenuItemDtos.addAll(rootSysMenuItemDtos);

        Set<String> assignedMenuCodesOfCurrRoles = calAssignedMenuCodesByCurrentUser();

        if (assignedMenuCodesOfCurrRoles == null || assignedMenuCodesOfCurrRoles.isEmpty()) {
            Collections.sort(resultMenuItemDtos);
            return resultMenuItemDtos;
        }

        List<MenuItemDto> menuItemsByAllMenuCodes = new ArrayList<>();
        for (String menuCode : assignedMenuCodesOfCurrRoles) {
            MenuItems sysMenuItemEntity = menuItemsMapper.selectByMenuCode(menuCode);
            if (sysMenuItemEntity != null) {
                MenuItemDto menuItemDto = buildSystemMenuItemDto(sysMenuItemEntity);
                menuItemsByAllMenuCodes.add(menuItemDto);
            } else {
                List<PluginPackageMenus> assignedPluginPackageMenusEntities = calAssignedPluginPackageMenusByMenuCode(
                        menuCode);
                if (assignedPluginPackageMenusEntities == null) {
                    continue;
                }

                for (PluginPackageMenus pluginPackageMenusEntity : assignedPluginPackageMenusEntities) {
                    MenuItemDto pluginPackageMenusDto = buildPackageMenuItemDto(pluginPackageMenusEntity);
                    menuItemsByAllMenuCodes.add(pluginPackageMenusDto);
                }
            }
        }

        resultMenuItemDtos.addAll(menuItemsByAllMenuCodes);
        Collections.sort(resultMenuItemDtos);
        return resultMenuItemDtos;
    }

    private Set<String> calAssignedMenuCodesByCurrentUser() {
        Set<String> currentUserRoles = AuthenticationContextHolder.getCurrentUserRoles();
        Set<String> assignedMenuCodesOfCurrRoles = new HashSet<>();

        if (currentUserRoles == null || currentUserRoles.isEmpty()) {
            return assignedMenuCodesOfCurrRoles;
        }
        for (String userRole : currentUserRoles) {
            List<String> assignedMenuCodesOfRole = roleMenuService.getMenuCodeListByRoleName(userRole);
            if (assignedMenuCodesOfRole == null) {
                continue;
            }
            for (String menuCode : assignedMenuCodesOfRole) {
                assignedMenuCodesOfCurrRoles.add(menuCode);
            }
        }

        return assignedMenuCodesOfCurrRoles;
    }

    private MenuItemDto buildPackageMenuItemDto(PluginPackageMenus pluginPackageMenus) {
        MenuItems menuItemEntity = menuItemsMapper.selectByMenuCode(pluginPackageMenus.getCategory());
        if (menuItemEntity == null) {
            String msg = String.format("Cannot find system menu item by package menu's category: [%s]",
                    pluginPackageMenus.getCategory());
            log.error(msg);
            throw new WecubeCoreException("3000", msg, pluginPackageMenus.getCategory());
        }
        MenuItemDto packageMenuItemDto = buildPackageMenuItemDto(pluginPackageMenus, menuItemEntity);

        return packageMenuItemDto;
    }

    private MenuItemDto buildPackageMenuItemDto(PluginPackageMenus packageMenu, MenuItems menuItem) {
        MenuItemDto pluginPackageMenuDto = new MenuItemDto();
        pluginPackageMenuDto.setId(packageMenu.getId());
        pluginPackageMenuDto.setCategory(packageMenu.getCategory());
        pluginPackageMenuDto.setCode(packageMenu.getCode());
        pluginPackageMenuDto.setSource(packageMenu.getSource());
        pluginPackageMenuDto.setMenuOrder(menuItem.getMenuOrder() * 10000 + packageMenu.getMenuOrder());
        pluginPackageMenuDto.setDisplayName(packageMenu.getDisplayName());
        pluginPackageMenuDto.setLocalDisplayName(packageMenu.getLocalDisplayName());
        pluginPackageMenuDto.setPath(packageMenu.getPath());
        pluginPackageMenuDto.setActive(packageMenu.getActive());
        return pluginPackageMenuDto;
    }

    private MenuItemDto buildSystemMenuItemDto(MenuItems systemMenu) {
        MenuItemDto pluginPackageMenuDto = new MenuItemDto();
        pluginPackageMenuDto.setId(systemMenu.getId());
        String category = systemMenu.getParentCode();
        if (category != null) {
            pluginPackageMenuDto.setCategory(category);
        }
        pluginPackageMenuDto.setCode(systemMenu.getCode());
        pluginPackageMenuDto.setSource(systemMenu.getSource());
        pluginPackageMenuDto.setMenuOrder(systemMenu.getMenuOrder());
        pluginPackageMenuDto.setDisplayName(systemMenu.getDescription());
        pluginPackageMenuDto.setLocalDisplayName(systemMenu.getLocalDisplayName());
        pluginPackageMenuDto.setPath(null);
        pluginPackageMenuDto.setActive(true);
        return pluginPackageMenuDto;
    }

    private List<PluginPackageMenus> calAssignedPluginPackageMenusByMenuCode(String menuCodeToFind) {
        List<PluginPackageMenus> resultPluginPackageMenus = new ArrayList<>();
        List<String> pluginPackageActiveStatues = new ArrayList<String>();
        pluginPackageActiveStatues.addAll(PluginPackages.PLUGIN_PACKAGE_ACTIVE_STATUSES);
        List<PluginPackageMenus> allPackageMenusForActivePackages = pluginPackageMenusMapper
                .selectAllMenusByCodeAndPackageStatuses(menuCodeToFind, pluginPackageActiveStatues);

        if (allPackageMenusForActivePackages == null || allPackageMenusForActivePackages.isEmpty()) {
            return resultPluginPackageMenus;
        }

        Map<String, PluginPackageMenus> codeAndMenus = new HashMap<String, PluginPackageMenus>();

        for (PluginPackageMenus menuEntityToCheck : allPackageMenusForActivePackages) {
            String menuCode = menuEntityToCheck.getCode();
            PluginPackageMenus existMenuEntity = codeAndMenus.get(menuCode);
            if (existMenuEntity == null) {
                codeAndMenus.put(menuCode, menuEntityToCheck);
            } else {
                if (isBetterThanExistOne(menuEntityToCheck, existMenuEntity)) {
                    codeAndMenus.put(menuCode, menuEntityToCheck);
                }
            }
        }

        resultPluginPackageMenus.addAll(codeAndMenus.values());
        return resultPluginPackageMenus;
    }

    private List<PluginPackageMenus> calAvailablePluginPackgeMenus() {
        List<PluginPackageMenus> resultPluginPackageMenus = new ArrayList<>();
        List<String> pluginPackageActiveStatues = new ArrayList<String>();
        pluginPackageActiveStatues.addAll(PluginPackages.PLUGIN_PACKAGE_ACTIVE_STATUSES);
        List<PluginPackageMenus> allPackageMenusForActivePackages = pluginPackageMenusMapper
                .selectAllMenusByPackageStatuses(pluginPackageActiveStatues);

        if (allPackageMenusForActivePackages == null || allPackageMenusForActivePackages.isEmpty()) {
            return resultPluginPackageMenus;
        }

        Map<String, PluginPackageMenus> codeAndMenus = new HashMap<String, PluginPackageMenus>();

        for (PluginPackageMenus menuEntityToCheck : allPackageMenusForActivePackages) {
            String menuCode = menuEntityToCheck.getCode();
            PluginPackageMenus existMenuEntity = codeAndMenus.get(menuCode);
            if (existMenuEntity == null) {
                codeAndMenus.put(menuCode, menuEntityToCheck);
            } else {
                if (isBetterThanExistOne(menuEntityToCheck, existMenuEntity)) {
                    codeAndMenus.put(menuCode, menuEntityToCheck);
                }
            }
        }

        resultPluginPackageMenus.addAll(codeAndMenus.values());
        return resultPluginPackageMenus;
    }

    private boolean isBetterThanExistOne(PluginPackageMenus menuEntityToCheck, PluginPackageMenus existMenuEntity) {
        if (existMenuEntity.getActive() == null) {
            if (menuEntityToCheck.getActive() == null) {
                return false;
            } else {
                return true;
            }
        }

        if (existMenuEntity.getActive() == true) {
            if (menuEntityToCheck.getActive() == false) {
                return false;
            }

            int compareResult = pluginPackageMenuOrderComparator.compare(menuEntityToCheck, existMenuEntity);
            if (compareResult > 0) {
                return true;
            } else {
                return false;
            }
        } else {
            if (menuEntityToCheck.getActive() == true) {
                return true;
            }

            int compareResult = pluginPackageMenuOrderComparator.compare(menuEntityToCheck, existMenuEntity);
            if (compareResult > 0) {
                return true;
            } else {
                return false;
            }
        }

    }

    private static class PluginPackageMenuOrderComparator implements Comparator<PluginPackageMenus> {

        @Override
        public int compare(PluginPackageMenus o1, PluginPackageMenus o2) {
            if (o1.getMenuOrder() == null) {
                if (o2.getMenuOrder() == null) {
                    return 0;
                } else {
                    return -1;
                }
            }

            if (o2.getMenuOrder() == null) {
                return 1;
            }

            return (o1.getMenuOrder() - o2.getMenuOrder());
        }

    }

}
