package com.webank.wecube.platform.core.service.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.MenuItem;
import com.webank.wecube.platform.core.domain.RoleMenu;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageMenu;
import com.webank.wecube.platform.core.dto.MenuItemDto;
import com.webank.wecube.platform.core.dto.user.RoleDto;
import com.webank.wecube.platform.core.dto.user.RoleMenuDto;
import com.webank.wecube.platform.core.jpa.MenuItemRepository;
import com.webank.wecube.platform.core.jpa.PluginPackageMenuRepository;
import com.webank.wecube.platform.core.jpa.user.RoleMenuRepository;
import com.webank.wecube.platform.core.support.authserver.AsAuthorityDto;
import com.webank.wecube.platform.core.support.authserver.AuthServerRestClient;

/**
 * @author howechen
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class RoleMenuServiceImpl implements RoleMenuService {

    private static final Logger logger = LoggerFactory.getLogger(RoleMenuServiceImpl.class);
    @Autowired
    private RoleMenuRepository roleMenuRepository;
    @Autowired
    private MenuItemRepository menuItemRepository;
    @Autowired
    private PluginPackageMenuRepository pluginPackageMenuRepository;

    @Autowired
    private UserManagementService userManagementService;

    @Autowired
    private AuthServerRestClient authServerRestClient;


    /**
     * Retrieve role_menu table by given roleId
     *
     * @param roleId the id of role
     * @return role2MenuDto
     */
    @Override
    public RoleMenuDto retrieveMenusByRoleId(String roleId) throws WecubeCoreException {
        logger.info(String.format("Fetching all menus by role ID: [%s]", roleId));
        List<RoleMenu> roleMenuList;
        try {
            roleMenuList = this.roleMenuRepository.findAllByRoleId(roleId);
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            throw new WecubeCoreException(ex.getMessage());
        }

        List<MenuItemDto> menuCodeList = new ArrayList<>();
        roleMenuList.forEach(roleMenu -> {
            String menuCode = roleMenu.getMenuCode();
            // sys menu
            MenuItem sysMenu = this.menuItemRepository.findByCode(menuCode);
            // use {if sysMenu is null} to judge if this is a sys menu or package menu
            if (null != sysMenu) {
                logger.info(String.format("System menu was found. The menu code is: [%s]", menuCode));
                menuCodeList.add(MenuItemDto.fromSystemMenuItem(sysMenu));
            } else {
                // package menu
                Optional<List<PluginPackageMenu>> allActivatePackageMenuByCode = this.pluginPackageMenuRepository.findAllActiveMenuByCode(menuCode);
                allActivatePackageMenuByCode.ifPresent(pluginPackageMenus -> {
                    logger.info(String.format("Plugin package menu was found. The menu code is: [%s]", menuCode));
                    pluginPackageMenus.forEach(pluginPackageMenu -> menuCodeList.add(this.transferPackageMenuToMenuItemDto(pluginPackageMenu)));
                });
            }

        });
        return new RoleMenuDto(roleId, menuCodeList);
    }

    /**
     * Update role_menu table
     *
     * @param roleId       given roleId
     * @param menuCodeList given total amount of the menuCode list
     */
    @Override
    public void updateRoleToMenusByRoleId(String roleId, List<String> menuCodeList) throws WecubeCoreException {
        List<RoleMenu> roleMenuList = this.roleMenuRepository.findAllByRoleId(roleId);
        RoleDto roleDto = userManagementService.retrieveRoleById(roleId);
        String roleName = roleDto.getName();

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
        for(RoleMenu rm : needToDeleteList){
        	AsAuthorityDto authorityToRevoke = new AsAuthorityDto();
        	authorityToRevoke.setCode(rm.getMenuCode());
        	authoritiesToRevoke.add(authorityToRevoke);
        }
        
        authServerRestClient.revokeAuthoritiesFromRole(roleId, authoritiesToRevoke);

        // new menuCodeList - current menuCodeList = needToCreateList
        List<String> needToCreateList;
        List<String> currentMenuCodeList = roleMenuList.stream().map(RoleMenu::getMenuCode).collect(Collectors.toList());
        needToCreateList = menuCodeList.stream().filter(menuCode -> !currentMenuCodeList.contains(menuCode)).collect(Collectors.toList());

        if (!needToCreateList.isEmpty()) {
            logger.info(String.format("Creating menus: [%s]", needToCreateList));
            List<RoleMenu> batchUpdateList = new ArrayList<>();
            needToCreateList.forEach(menuCode -> batchUpdateList.add(new RoleMenu(roleId, roleName, menuCode)));
            try {
                this.roleMenuRepository.saveAll(batchUpdateList);
            } catch (Exception ex) {
                logger.error(ex.getMessage());
                throw new WecubeCoreException(ex.getMessage());
            }
            
            List<AsAuthorityDto> authoritiesToGrant = new ArrayList<>();
            for(RoleMenu rm : batchUpdateList){
            	AsAuthorityDto authorityToGrant = new AsAuthorityDto();
            	authorityToGrant.setCode(rm.getMenuCode());
            	authoritiesToGrant.add(authorityToGrant);
            }
            
            authServerRestClient.configureRoleAuthorities(roleId, authoritiesToGrant);
        }
    }

    @Override
    public List<RoleMenuDto> getMenusByUsername(String username) {
        List<RoleDto> roleDtoList = this.userManagementService.getGrantedRolesByUsername(username);
        return roleDtoList.stream().map(roleDto -> this.retrieveMenusByRoleId(roleDto.getId())).collect(Collectors.toList());
    }

    @Override
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
            throw new WecubeCoreException(msg);
        }
        return MenuItemDto.fromPackageMenuItem(packageMenu, menuItem);
    }
}
