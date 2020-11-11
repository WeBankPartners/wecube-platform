package com.webank.wecube.platform.core.service.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.MenuItem;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageMenu;
import com.webank.wecube.platform.core.dto.MenuItemDto;
import com.webank.wecube.platform.core.dto.user.RoleDto;
import com.webank.wecube.platform.core.dto.user.RoleMenuDto;
import com.webank.wecube.platform.core.entity.plugin.RoleMenu;
import com.webank.wecube.platform.core.jpa.MenuItemRepository;
import com.webank.wecube.platform.core.jpa.PluginPackageMenuRepository;
import com.webank.wecube.platform.core.lazyDomain.plugin.LazyPluginPackageMenu;
import com.webank.wecube.platform.core.repository.plugin.MenuItemsMapper;
import com.webank.wecube.platform.core.repository.plugin.RoleMenuMapper;
import com.webank.wecube.platform.core.support.authserver.AsAuthorityDto;
import com.webank.wecube.platform.core.support.authserver.AuthServerRestClient;

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
    private PluginPackageMenuRepository pluginPackageMenuRepository;

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
        
        if(roleMenuEntities == null || roleMenuEntities.isEmpty()){
            return result;
        }
        List<MenuItemDto> menuItemDtos = new ArrayList<>();
        
        for(RoleMenu roleMenuEntity : roleMenuEntities){
            String menuCode = roleMenuEntity.getMenuCode();
            MenuItem sysMenu = this.menuItemRepository.findByCode(menuCode);
            // use {if sysMenu is null} to judge if this is a sys menu or
            // package menu
            if (null != sysMenu) {
                logger.info(String.format("System menu was found. The menu code is: [%s]", menuCode));
                menuList.add(MenuItemDto.fromSystemMenuItem(sysMenu));
            } else {
                // package menu
                Optional<List<PluginPackageMenu>> allActivatePackageMenuByCode = this.pluginPackageMenuRepository
                        .findAllActiveMenuByCode(menuCode);
                allActivatePackageMenuByCode.ifPresent(pluginPackageMenus -> {
                    logger.info(String.format("Plugin package menu was found. The menu code is: [%s]", menuCode));
                    pluginPackageMenus.forEach(pluginPackageMenu -> menuList
                            .add(this.transferPackageMenuToMenuItemDto(pluginPackageMenu)));
                });
            }
        }
        
        roleMenuList.forEach(roleMenu -> {
            String menuCode = roleMenu.getMenuCode();
            // sys menu
            MenuItem sysMenu = this.menuItemRepository.findByCode(menuCode);
            // use {if sysMenu is null} to judge if this is a sys menu or
            // package menu
            if (null != sysMenu) {
                logger.info(String.format("System menu was found. The menu code is: [%s]", menuCode));
                menuList.add(MenuItemDto.fromSystemMenuItem(sysMenu));
            } else {
                // package menu
                Optional<List<PluginPackageMenu>> allActivatePackageMenuByCode = this.pluginPackageMenuRepository
                        .findAllActiveMenuByCode(menuCode);
                allActivatePackageMenuByCode.ifPresent(pluginPackageMenus -> {
                    logger.info(String.format("Plugin package menu was found. The menu code is: [%s]", menuCode));
                    pluginPackageMenus.forEach(pluginPackageMenu -> menuList
                            .add(this.transferPackageMenuToMenuItemDto(pluginPackageMenu)));
                });
            }

        });
        return new RoleMenuDto(roleId, menuList);
    }

    /**
     * Update role_menu table
     *
     * @param roleId
     *            given roleId
     * @param menuCodeList
     *            given total amount of the menuCode list
     */
    public void updateRoleToMenusByRoleId(String roleId, List<String> menuCodeList) throws WecubeCoreException {

        String roleName = validateAndFetchRoleName(roleId);

        List<RoleMenu> roleMenuList = new ArrayList<>();
        final Optional<List<RoleMenu>> roleMenuListOpt = this.roleMenuRepository.findAllByRoleName(roleName);
        if (roleMenuListOpt.isPresent()) {
            roleMenuList = roleMenuListOpt.get();
        }

        // current menuCodeList - new menuCodeList = needToDeleteList
        List<RoleMenu> needToDeleteList = roleMenuList.stream().filter(roleMenu -> {
            String code = roleMenu.getMenuCode();
            return !menuCodeList.contains(code);
        }).collect(Collectors.toList());
        if (!needToDeleteList.isEmpty()) {
            logger.info(String.format("Deleting menus: [%s]", needToDeleteList));
            for (RoleMenu roleMenu : needToDeleteList) {
                this.roleMenuRepository.deleteById(roleMenu.getId());

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
            List<RoleMenu> batchUpdateList = new ArrayList<>();
            needToCreateList.forEach(menuCode -> batchUpdateList.add(new RoleMenu(roleName, menuCode)));
            try {
                this.roleMenuRepository.saveAll(batchUpdateList);
            } catch (Exception ex) {
                logger.error(ex.getMessage());
                throw new WecubeCoreException(ex.getMessage());
            }

            List<AsAuthorityDto> authoritiesToGrant = new ArrayList<>();
            for (RoleMenu rm : batchUpdateList) {
                AsAuthorityDto authorityToGrant = new AsAuthorityDto();
                authorityToGrant.setCode(rm.getMenuCode());
                authoritiesToGrant.add(authorityToGrant);
            }

            authServerRestClient.configureRoleAuthorities(roleId, authoritiesToGrant);
        }
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
        final Boolean isRoleMenuBindingExists = this.roleMenuRepository.existsRoleMenuByRoleNameAndMenuCode(roleName,
                menuCode);
        if (!isRoleMenuBindingExists) {
            logger.info("Saving roleMenuBinding, role name: [{}], menu code: [{}]", roleName, menuCode);
            this.roleMenuRepository.save(new RoleMenu(roleName, menuCode));
        }
    }

    public List<RoleMenuDto> getMenusByUsername(String username) {
        List<RoleDto> roleDtoList = this.userManagementService.getGrantedRolesByUsername(username);
        return roleDtoList.stream().map(roleDto -> this.retrieveMenusByRoleId(roleDto.getId()))
                .collect(Collectors.toList());
    }

    public List<String> getMenuCodeListByRoleName(String roleName) {
        Optional<List<RoleMenu>> allByRoleName = this.roleMenuRepository.findAllByRoleName(roleName);
        List<String> result = new ArrayList<>();
        if (allByRoleName.isPresent()) {
            result = allByRoleName.get().stream().map(RoleMenu::getMenuCode).collect(Collectors.toList());
        }
        return result;
    }

    public MenuItemDto transferPackageMenuToMenuItemDto(PluginPackageMenu packageMenu) throws WecubeCoreException {
        MenuItem menuItem = menuItemRepository.findByCode(packageMenu.getCategory());
        if (null == menuItem) {
            String msg = String.format("Cannot find system menu item by package menu's category: [%s]",
                    packageMenu.getCategory());
            logger.error(msg);
            throw new WecubeCoreException("3267", msg, packageMenu.getCategory());
        }
        return MenuItemDto.fromPackageMenuItem(packageMenu, menuItem);
    }

    public MenuItemDto transferPackageMenuToMenuItemDto(LazyPluginPackageMenu packageMenu) throws WecubeCoreException {
        MenuItem menuItem = menuItemRepository.findByCode(packageMenu.getCategory());
        if (null == menuItem) {
            String msg = String.format("Cannot find system menu item by package menu's category: [%s]",
                    packageMenu.getCategory());
            logger.error(msg);
            throw new WecubeCoreException("3268", msg, packageMenu.getCategory());
        }
        return MenuItemDto.fromPackageMenuItem(packageMenu, menuItem);
    }

}
