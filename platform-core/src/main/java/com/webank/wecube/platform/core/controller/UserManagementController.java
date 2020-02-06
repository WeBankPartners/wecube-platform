package com.webank.wecube.platform.core.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webank.wecube.platform.core.commons.AuthenticationContextHolder;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.CommonResponseDto;
import com.webank.wecube.platform.core.dto.user.RoleDto;
import com.webank.wecube.platform.core.dto.user.RoleMenuDto;
import com.webank.wecube.platform.core.dto.user.UserDto;
import com.webank.wecube.platform.core.service.user.RoleMenuServiceImpl;
import com.webank.wecube.platform.core.service.user.UserManagementService;

/**
 * @author howechen
 */
@RestController
@RequestMapping("/v1")
public class UserManagementController {
    @Autowired
    private UserManagementService userManagementService;
    @Autowired
    private RoleMenuServiceImpl roleMenuService;

    @PostMapping("/users/create")
    public CommonResponseDto registerUser(@RequestBody UserDto userDto) {
        UserDto result = userManagementService.registerUser(userDto);
        return CommonResponseDto.okayWithData(result);

    }

    @GetMapping("/users/retrieve")
    public CommonResponseDto retrieveAllUserAccounts() {
        try {
            List<UserDto> result = userManagementService.retrieveAllUserAccounts();
            return CommonResponseDto.okayWithData(result);
        } catch (WecubeCoreException ex) {
            return CommonResponseDto.error(ex.getMessage());
        }
    }

    @DeleteMapping("/users/{user-id}/delete")
    public CommonResponseDto deleteUserByUserId(@PathVariable("user-id") String userId) {
        try {
            userManagementService.deleteUserByUserId(userId);
            return CommonResponseDto.okay();
        } catch (WecubeCoreException e) {
            return CommonResponseDto.error(e.getMessage());
        }
    }

    @GetMapping("/users/roles")
    public CommonResponseDto getRolesOfCurrentUser() {
        try {
            List<RoleDto> grantedRoles = userManagementService
                    .getGrantedRolesByUsername(AuthenticationContextHolder.getCurrentUsername());
            return CommonResponseDto.okayWithData(grantedRoles);
        } catch (WecubeCoreException ex) {
            return CommonResponseDto.error(ex.getMessage());
        }
    }

    @GetMapping("/users/{username}/roles")
    public CommonResponseDto getRolesByUsername(@PathVariable(value = "username") String username) {
        try {
            List<RoleDto> grantedRoles = userManagementService.getGrantedRolesByUsername(username);
            return CommonResponseDto.okayWithData(grantedRoles);
        } catch (WecubeCoreException ex) {
            return CommonResponseDto.error(ex.getMessage());
        }
    }

    @GetMapping("/users/{username}/menus")
    public CommonResponseDto getMenusByUsername(@PathVariable(value = "username") String username) {
        try {
            List<RoleMenuDto> result = this.roleMenuService.getMenusByUsername(username);
            return CommonResponseDto.okayWithData(result);
        } catch (WecubeCoreException ex) {
            return CommonResponseDto.error(ex.getMessage());
        }
    }

}
