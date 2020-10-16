package com.webank.wecube.platform.core.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webank.wecube.platform.core.commons.AuthenticationContextHolder;
import com.webank.wecube.platform.core.dto.CommonResponseDto;
import com.webank.wecube.platform.core.dto.user.RoleDto;
import com.webank.wecube.platform.core.dto.user.RoleMenuDto;
import com.webank.wecube.platform.core.dto.user.UserDto;
import com.webank.wecube.platform.core.dto.user.UserPasswordDto;
import com.webank.wecube.platform.core.dto.user.UserPasswordResetDto;
import com.webank.wecube.platform.core.service.user.RoleMenuServiceImpl;
import com.webank.wecube.platform.core.service.user.UserManagementService;

@RestController
@RequestMapping("/v1")
public class UserManagementController {
    @Autowired
    private UserManagementService userManagementService;
    @Autowired
    private RoleMenuServiceImpl roleMenuService;

    @PostMapping("/users/create")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN')")
    public CommonResponseDto registerUser(@RequestBody UserDto userDto) {
        UserDto result = userManagementService.registerUser(userDto);
        return CommonResponseDto.okayWithData(result);

    }
    
    @PostMapping("/users/change-password")
    public CommonResponseDto changeUserPassword(@RequestBody UserPasswordDto userPassDto){
        userManagementService.changeUserPassword(userPassDto);
        return CommonResponseDto.okay();
    }
    
    @PostMapping("/users/reset-password")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN')")
    public CommonResponseDto resetUserPassword(@RequestBody UserPasswordResetDto userPassResetDto){
        String password = userManagementService.resetUserPassword(userPassResetDto);
        return CommonResponseDto.okayWithData(password);
    }

    @GetMapping("/users/retrieve")
    public CommonResponseDto retrieveAllUserAccounts() {
        List<UserDto> result = userManagementService.retrieveAllUserAccounts();
        return CommonResponseDto.okayWithData(result);
    }

    @DeleteMapping("/users/{user-id}/delete")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN')")
    public CommonResponseDto deleteUserByUserId(@PathVariable("user-id") String userId) {
        userManagementService.deleteUserByUserId(userId);
        return CommonResponseDto.okay();
    }

    @GetMapping("/users/roles")
    public CommonResponseDto getRolesOfCurrentUser() {
        List<RoleDto> grantedRoles = userManagementService
                .getGrantedRolesByUsername(AuthenticationContextHolder.getCurrentUsername());
        return CommonResponseDto.okayWithData(grantedRoles);
    }

    @GetMapping("/users/{username}/roles")
    public CommonResponseDto getRolesByUsername(@PathVariable(value = "username") String username) {
        List<RoleDto> grantedRoles = userManagementService.getGrantedRolesByUsername(username);
        return CommonResponseDto.okayWithData(grantedRoles);
    }

    @GetMapping("/users/{username}/menus")
    public CommonResponseDto getMenusByUsername(@PathVariable(value = "username") String username) {
        List<RoleMenuDto> result = this.roleMenuService.getMenusByUsername(username);
        return CommonResponseDto.okayWithData(result);
    }

}
