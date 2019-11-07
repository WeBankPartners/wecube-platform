package com.webank.wecube.platform.core.service;

import com.webank.wecube.platform.core.DatabaseBasedTest;
import com.webank.wecube.platform.core.dto.MenuItemDto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class UserManagementServiceTest extends DatabaseBasedTest {
    @Autowired
    UserManagerService userManagerService;

    @Test
    public void sortMenusByCodeShouldSuccess() {
        boolean sortByMenuCodeFailed = false;
        for (int i = 0; i < 10; i++) {
            List<MenuItemDto> menusDtos = userManagerService.getAllMenus();
            if (menusDtos.get(menusDtos.size() - 1).getCode().compareTo(menusDtos.get(0).getCode()) < 0) {
                sortByMenuCodeFailed = true;
            }
        }
        assertThat(sortByMenuCodeFailed).isEqualTo(false);
    }
}
