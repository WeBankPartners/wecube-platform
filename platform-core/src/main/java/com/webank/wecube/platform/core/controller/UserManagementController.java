package com.webank.wecube.platform.core.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.webank.wecube.platform.core.commons.AuthenticationContextHolder;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.CommonResponseDto;
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
    public CommonResponseDto retrieveUser(@RequestHeader(value = "Authorization") String token) {
        try {
            return userManagementService.retrieveUser(token);
        } catch (WecubeCoreException ex) {
            return CommonResponseDto.error(ex.getMessage());
        }
    }

    @DeleteMapping("/users/{user-id}/delete")
    public CommonResponseDto deleteUser(@RequestHeader(value = "Authorization") String token,
                                        @PathVariable("user-id") Long id) {
        try {
            return userManagementService.deleteUser(token, id);
        } catch (WecubeCoreException e) {
            return CommonResponseDto.error(e.getMessage());
        }
    }

    @GetMapping("/users/roles")
    @ResponseBody
    public CommonResponseDto getRolesByCurrentUserName(@RequestHeader(value = "Authorization") String token) {
        try {
            return userManagementService.getRolesByUserName(token, AuthenticationContextHolder.getCurrentUsername());
        } catch (WecubeCoreException ex) {
            return CommonResponseDto.error(ex.getMessage());
        }
    }

    @GetMapping("/users/{user-name}/roles")
    public CommonResponseDto getRolesByUsername(@RequestHeader(value = "Authorization") String token,
                                                @PathVariable(value = "user-name") String userName) {
        try {
            return userManagementService.getRolesByUserName(token, userName);
        } catch (WecubeCoreException ex) {
            return CommonResponseDto.error(ex.getMessage());
        }
    }

    @GetMapping("/users/{user-name}/menus")
    public CommonResponseDto getMenusByUsername(@RequestHeader(value = "Authorization") String token,
                                                @PathVariable(value = "user-name") String userName) {
        try {
            return CommonResponseDto.okayWithData(this.roleMenuService.getMenusByUserName(token, userName));
        } catch (WecubeCoreException ex) {
            return CommonResponseDto.error(ex.getMessage());
        }
    }


}
