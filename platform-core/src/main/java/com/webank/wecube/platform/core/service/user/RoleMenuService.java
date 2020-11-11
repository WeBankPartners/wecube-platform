package com.webank.wecube.platform.core.service.user;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.MenuItemDto;
import com.webank.wecube.platform.core.dto.user.RoleDto;
import com.webank.wecube.platform.core.dto.user.RoleMenuDto;
import com.webank.wecube.platform.core.entity.plugin.MenuItems;
import com.webank.wecube.platform.core.entity.plugin.PluginPackageMenus;
import com.webank.wecube.platform.core.entity.plugin.PluginPackages;
import com.webank.wecube.platform.core.entity.plugin.RoleMenu;
import com.webank.wecube.platform.core.repository.plugin.MenuItemsMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginPackageMenusMapper;
import com.webank.wecube.platform.core.repository.plugin.RoleMenuMapper;
import com.webank.wecube.platform.core.support.authserver.AsAuthorityDto;
import com.webank.wecube.platform.core.support.authserver.AuthServerRestClient;
import com.webank.wecube.platform.workflow.commons.LocalIdGenerator;

/**
 * @author howechen
 */
@Service
public class RoleMenuService {

    private static final Logger logger = LoggerFactory.getLogger(RoleMenuService.class);
    @Autowired
    private RoleMenuMapper roleMenuMapper;
    @Autowired
    private MenuItemsMapper menuItemsMapper;
    @Autowired
    private PluginPackageMenusMapper pluginPackageMenusMapper;

    @Autowired
    private UserManagementService userManagementService;

    @Autowired
    private AuthServerRestClient authServerRestClient;

    /**
     * Retrieve role_menu table by given roleId
     *
     * @param roleId
     *            the id of role
     * @return role2MenuDto
     */
    public RoleMenuDto retrieveMenusByRoleId(String roleId) throws WecubeCoreException {

        String roleName = validateAndFetchRoleName(roleId);

        List<RoleMenu> roleMenuEntities = roleMenuMapper.selectAllByRoleName(roleName);

        RoleMenuDto result = new RoleMenuDto();
        result.setRoleId(roleId);
        result.setRoleName(roleName);

        if (roleMenuEntities == null || roleMenuEntities.isEmpty()) {
            return result;
        }
        List<MenuItemDto> menuItemDtos = new ArrayList<>();

        for (RoleMenu roleMenuEntity : roleMenuEntities) {
            String menuCode = roleMenuEntity.getMenuCode();
            MenuItems menuItemsEntity = menuItemsMapper.selectByMenuCode(menuCode);
            if (menuItemsEntity != null) {
                logger.info(String.format("System menu was found. The menu code is: [%s]", menuCode));
                MenuItemDto menuItemDto = buildMenuItemDto(menuItemsEntity);
                menuItemDtos.add(menuItemDto);
            } else {
                List<String> pluginPackageActiveStatues = new ArrayList<String>();
                pluginPackageActiveStatues.addAll(PluginPackages.PLUGIN_PACKAGE_ACTIVE_STATUSES);
                List<PluginPackageMenus> pluginPackageMenusEntities = pluginPackageMenusMapper
                        .findAllActiveMenuByCode(menuCode, pluginPackageActiveStatues);

                if (pluginPackageMenusEntities != null) {
                    for (PluginPackageMenus pluginPackageMenusEntity : pluginPackageMenusEntities) {
                        logger.info(String.format("Plugin package menu was found. The menu code is: [%s]", menuCode));
                        MenuItemDto pluginPackageMenuItemDto = transferPackageMenuToMenuItemDto(
                                pluginPackageMenusEntity);
                        menuItemDtos.add(pluginPackageMenuItemDto);
                    }
                }
            }
        }

        result.setMenuList(menuItemDtos);

        return result;
    }

    /**
     * Update role_menu table
     *
     * @param roleId
     *            given roleId
     * @param menuCodeList
     *            given total amount of the menuCode list
     */
    public void updateRoleToMenusByRoleId(String roleId, List<String> menuCodeList) {

        String roleName = validateAndFetchRoleName(roleId);

        List<RoleMenu> roleMenuList = roleMenuMapper.selectAllByRoleName(roleName);

        // current menuCodeList - new menuCodeList = needToDeleteList
        List<RoleMenu> needToDeleteList = roleMenuList.stream().filter(roleMenu -> {
            String code = roleMenu.getMenuCode();
            return !menuCodeList.contains(code);
        }).collect(Collectors.toList());
        
        if (!needToDeleteList.isEmpty()) {
            logger.info(String.format("Deleting menus: [%s]", needToDeleteList));
            for (RoleMenu roleMenu : needToDeleteList) {
                roleMenuMapper.deleteByPrimaryKey(roleMenu.getId());

            }
        }

        List<AsAuthorityDto> authoritiesToRevoke = new ArrayList<>();
        for (RoleMenu rm : needToDeleteList) {
            AsAuthorityDto authorityToRevoke = new AsAuthorityDto();
            authorityToRevoke.setCode(rm.getMenuCode());
            authoritiesToRevoke.add(authorityToRevoke);
        }

        authServerRestClient.revokeAuthoritiesFromRole(roleId, authoritiesToRevoke);

        // new menuCodeList - current menuCodeList = needToCreateList
        List<String> needToCreateList;
        List<String> currentMenuCodeList = roleMenuList.stream().map(RoleMenu::getMenuCode)
                .collect(Collectors.toList());
        needToCreateList = menuCodeList.stream().filter(menuCode -> !currentMenuCodeList.contains(menuCode))
                .collect(Collectors.toList());

        if (!needToCreateList.isEmpty()) {
            logger.info(String.format("Creating menus: [%s]", needToCreateList));
            List<AsAuthorityDto> authoritiesToGrant = new ArrayList<>();
            for(String menuCode : needToCreateList){
                RoleMenu newRoleMenuEntity = new RoleMenu();
                newRoleMenuEntity.setId(LocalIdGenerator.generateId());
                newRoleMenuEntity.setMenuCode(menuCode);
                newRoleMenuEntity.setRoleName(roleName);
                
                roleMenuMapper.insert(newRoleMenuEntity);
                
                AsAuthorityDto authorityToGrant = new AsAuthorityDto();
                authorityToGrant.setCode(menuCode);
                authoritiesToGrant.add(authorityToGrant);
            }

            authServerRestClient.configureRoleAuthorities(roleId, authoritiesToGrant);
        }
    }

    private MenuItemDto buildMenuItemDto(MenuItems entity) {
        MenuItemDto dto = new MenuItemDto();
        dto.setId(entity.getId());
        String category = entity.getParentCode();
        if (category != null) {
            dto.setCategory(category);
        }
        dto.setCode(entity.getCode());
        dto.setSource(entity.getSource());
        dto.setMenuOrder(entity.getMenuOrder());
        dto.setDisplayName(entity.getDescription());
        dto.setLocalDisplayName(entity.getLocalDisplayName());
        dto.setPath(null);
        dto.setActive(true);
        return dto;
    }

    private String validateAndFetchRoleName(String roleId) {
        String roleName = this.userManagementService.retrieveRoleById(roleId).getName();

        if (StringUtils.isEmpty(roleName)) {
            String msg = String.format("Cannot validate role id: [%s] from auth server.", roleId);
            logger.error(msg);
            throw new WecubeCoreException("3266", msg, roleId);
        }
        return roleName;
    }

    public void createRoleMenuBinding(String roleName, String menuCode) {
        List<RoleMenu> existRoleMenuEntities = roleMenuMapper.selectAllByRoleNameAndMenuCode(roleName, menuCode);
        if(existRoleMenuEntities != null && !existRoleMenuEntities.isEmpty()){
            return;
        }
        
        RoleMenu newRoleMenuEntity = new RoleMenu();
        newRoleMenuEntity.setId(LocalIdGenerator.generateId());
        newRoleMenuEntity.setMenuCode(menuCode);
        newRoleMenuEntity.setRoleName(roleName);
        
        roleMenuMapper.insert(newRoleMenuEntity);
        logger.info("Saving roleMenuBinding, role name: [{}], menu code: [{}]", roleName, menuCode);
        
    }

    public List<RoleMenuDto> getMenusByUsername(String username) {
        List<RoleDto> roleDtoList = this.userManagementService.getGrantedRolesByUsername(username);
        return roleDtoList.stream().map(roleDto -> this.retrieveMenusByRoleId(roleDto.getId()))
                .collect(Collectors.toList());
    }

    public List<String> getMenuCodeListByRoleName(String roleName) {
        List<RoleMenu> roleMenuEntities = roleMenuMapper.selectAllByRoleName(roleName);
        List<String> result = new ArrayList<>();
        if(roleMenuEntities == null || roleMenuEntities.isEmpty()){
            return result;
        }
        result = roleMenuEntities.stream().map(RoleMenu::getMenuCode).collect(Collectors.toList());
        return result;
    }

    public MenuItemDto transferPackageMenuToMenuItemDto(PluginPackageMenus packageMenu) {
        MenuItems menuItemsEntity = menuItemsMapper.selectByMenuCode(packageMenu.getCategory());
        if (menuItemsEntity == null) {
            String msg = String.format("Cannot find system menu item by package menu's category: [%s]",
                    packageMenu.getCategory());
            logger.error(msg);
            throw new WecubeCoreException("3267", msg, packageMenu.getCategory());
        }
        return buildMenuItemDtoFromPackageMenuItem(packageMenu, menuItemsEntity);
    }

    private MenuItemDto buildMenuItemDtoFromPackageMenuItem(PluginPackageMenus packageMenu, MenuItems menuItem) {
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

}
