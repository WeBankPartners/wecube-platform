package com.webank.wecube.platform.core.controller.plugin;

import static com.webank.wecube.platform.core.dto.plugin.CommonResponseDto.okayWithData;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webank.wecube.platform.core.dto.plugin.CommonResponseDto;
import com.webank.wecube.platform.core.dto.plugin.MenuItemDto;
import com.webank.wecube.platform.core.service.plugin.MenuService;

@RestController
@RequestMapping("/v1")
public class ApplicationHomeController {

    @Autowired
    private MenuService menuService;

    @GetMapping("/my-menus")
    public CommonResponseDto getMyMenuItems() {
        List<MenuItemDto> currentUserAllMenus = menuService.getCurrentUserAllMenus();
        return okayWithData(currentUserAllMenus);
    }

    @GetMapping("/all-menus")
    public CommonResponseDto getAllMenuItems(Principal principal) {
        return okayWithData(menuService.getAllMenus());
    }
}
