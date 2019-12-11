package com.webank.wecube.platform.core.service.user;

import com.webank.wecube.platform.core.domain.RoleMenu;
import com.webank.wecube.platform.core.dto.user.RoleMenuDto;
import com.webank.wecube.platform.core.jpa.MenuItemRepository;
import com.webank.wecube.platform.core.jpa.user.RoleMenuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author howechen
 */
@Service
@Transactional
public class RoleMenuServiceImpl implements RoleMenuService {

    private RoleMenuRepository roleMenuRepository;
    private MenuItemRepository menuItemRepository;

    @Autowired
    public RoleMenuServiceImpl(RoleMenuRepository roleMenuRepository, MenuItemRepository menuItemRepository) {
        this.roleMenuRepository = roleMenuRepository;
        this.menuItemRepository = menuItemRepository;
    }

    /**
     * Retrieve role_menu table by given roleId
     *
     * @param roleId the id of role
     * @return role2MenuDto
     */
    @Override
    public RoleMenuDto retrieveMenusByRoleId(Long roleId) {
        List<RoleMenu> roleMenuList = this.roleMenuRepository.findAllByRoleId(roleId);
        List<String> menuCodeList = new ArrayList<>();
        roleMenuList.forEach(roleMenu -> menuCodeList.add(roleMenu.getMenuItem().getCode()));
        return new RoleMenuDto(roleId, menuCodeList);
    }

    /**
     * Update role_menu table
     *
     * @param roleId       given roleId
     * @param menuCodeList given total amount of the menuCode list
     * @return role2MenuDto
     */
    @Override
    public RoleMenuDto updateRoleToMenusByRoleId(Long roleId, List<String> menuCodeList) {
        List<RoleMenu> roleMenuList = this.roleMenuRepository.findAllByRoleId(roleId);

        //
        // current menuCodeList - new menuCodeList = needToDeleteList
        List<RoleMenu> needToDeleteList = roleMenuList.stream().filter(roleMenu -> {
            String code = roleMenu.getMenuItem().getCode();
            return !menuCodeList.contains(code);
        }).collect(Collectors.toList());
        if (!needToDeleteList.isEmpty()) {
            for (RoleMenu roleMenu : needToDeleteList) {
                this.roleMenuRepository.deleteById(roleMenu.getId());
            }
        }

        // new menuCodeList - current menuCodeList = needToCreateList
        List<String> needToCreateList;
        List<String> currentMenuCodeList = roleMenuList.stream().map(roleMenu -> roleMenu.getMenuItem().getCode()).collect(Collectors.toList());
        needToCreateList = menuCodeList.stream().filter(menuCode -> !currentMenuCodeList.contains(menuCode)).collect(Collectors.toList());

        if (!needToCreateList.isEmpty()) {
            List<RoleMenu> batchUpdateList = new ArrayList<>();
            needToCreateList.forEach(menuCode -> batchUpdateList.add(new RoleMenu(roleId, this.menuItemRepository.findByCode(menuCode))));
            this.roleMenuRepository.saveAll(batchUpdateList);
        }

        return retrieveMenusByRoleId(roleId);
    }
}
