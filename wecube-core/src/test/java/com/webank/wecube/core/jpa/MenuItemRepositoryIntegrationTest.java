package com.webank.wecube.core.jpa;

import com.google.common.collect.Lists;
import com.webank.wecube.core.DatabaseBasedTest;
import com.webank.wecube.core.domain.MenuItem;
import com.webank.wecube.core.domain.RoleMenu;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

public class MenuItemRepositoryIntegrationTest extends DatabaseBasedTest {

    @Autowired
    MenuItemRepository menuItemRepository;

    @Test
    public void findSavedMenuItemById() {
        MenuItem menuItem = mockMenuItem("MOCK_ADMIN");
        menuItem = menuItemRepository.save(menuItem);

        assertThat(menuItemRepository.findById(menuItem.getId())).hasValue(menuItem);
    }

    @Test
    public void findSavedMenuItemByCode() {
        MenuItem menuItem = mockMenuItem("MOCK_ADMIN");
        menuItem = menuItemRepository.save(menuItem);

        assertThat(menuItemRepository.findByCode("MOCK_ADMIN")).isEqualTo(menuItem);
    }

    @Test
    public void findMenuItemsByRole() {
        MenuItem menuItem1 = mockMenuItem("MOCK_ADMIN_1");
        MenuItem menuItem2 = mockMenuItem("MOCK_ADMIN_2");
        MenuItem menuItem3 = mockMenuItem("MOCK_ADMIN_3");
        RoleMenu roleMenu11 = mockRoleMenu(menuItem1, 1001);
        RoleMenu roleMenu12 = mockRoleMenu(menuItem1, 1002);
        RoleMenu roleMenu21 = mockRoleMenu(menuItem2, 1001);
        RoleMenu roleMenu23 = mockRoleMenu(menuItem2, 1003);
        menuItem1.setAssignedRoles(Lists.newArrayList(roleMenu11, roleMenu12));
        menuItem2.setAssignedRoles(Lists.newArrayList(roleMenu21, roleMenu23));
        menuItemRepository.save(menuItem1);
        menuItemRepository.save(menuItem2);
        menuItemRepository.save(menuItem3);

        assertThat(menuItemRepository.findMenuItemsByRoles(1001))
                .extracting("code").containsExactly("MOCK_ADMIN_1", "MOCK_ADMIN_2");
        assertThat(menuItemRepository.findMenuItemsByRoles(1002, 1003))
                .extracting("code").containsExactly("MOCK_ADMIN_1", "MOCK_ADMIN_2");
    }

    @Test
    public void assignAndRemoveMenuForRole() {
        MenuItem mockMenuItem = mockMenuItem("MOCK_ADMIN_1");
        RoleMenu mockRoleMenu = mockRoleMenu(mockMenuItem, 1001);
        mockMenuItem.setAssignedRoles(Lists.newArrayList(mockRoleMenu));
        mockMenuItem = menuItemRepository.save(mockMenuItem);

        System.out.println(mockMenuItem);
        mockMenuItem.getAssignedRoles().clear();
        menuItemRepository.save(mockMenuItem);

        assertThat(menuItemRepository.findByCode("MOCK_ADMIN_1").getAssignedRoles()).isEmpty();
    }

    private MenuItem mockMenuItem(String code) {
        MenuItem menuItem = new MenuItem();
        menuItem.setCode(code);
        menuItem.setDescription("mock desc");
        return menuItem;
    }

    private RoleMenu mockRoleMenu(MenuItem menuItem, int roleId) {
        RoleMenu roleMenu = new RoleMenu();
        roleMenu.setMenuItem(menuItem);
        roleMenu.setRoleId(roleId);
        return roleMenu;
    }
}
