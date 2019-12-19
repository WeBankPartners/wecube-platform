package com.webank.wecube.platform.core.controller;

import com.webank.wecube.platform.core.commons.AuthenticationContextHolder;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.CommonResponseDto;
import com.webank.wecube.platform.core.service.user.UserManagementServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author howechen
 */
@RestController
@RequestMapping("v1/")
public class UserManagementController {
    private UserManagementServiceImpl userManagementService;

    @Autowired
    public UserManagementController(UserManagementServiceImpl userManagementService) {
        this.userManagementService = userManagementService;
    }

    @PostMapping("/users/create")
    @ResponseBody
    public CommonResponseDto createUser(@RequestHeader(value = "Authorization") String token,
                                        @RequestBody Map<String, Object> requestBody) {
        try {
            return userManagementService.createUser(token, requestBody);
        } catch (WecubeCoreException ex) {
            return CommonResponseDto.error(ex.getMessage());
        }

    }

    @GetMapping("/users/retrieve")
    @ResponseBody
    public CommonResponseDto retrieveUser(@RequestHeader(value = "Authorization") String token) {
        try {
            return userManagementService.retrieveUser(token);
        } catch (WecubeCoreException ex) {
            return CommonResponseDto.error(ex.getMessage());
        }
    }

    @DeleteMapping("/users/{user-id}/delete")
    @ResponseBody
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
    @ResponseBody
    public CommonResponseDto getRolesByUsername(@RequestHeader(value = "Authorization") String token,
                                                @PathVariable(value = "user-name") String userName) {
        try {
            return userManagementService.getRolesByUserName(token, userName);
        } catch (WecubeCoreException ex) {
            return CommonResponseDto.error(ex.getMessage());
        }
    }

    @GetMapping("/users/{user-name}/menus")
    @ResponseBody
    public CommonResponseDto getMenusByUsername(@RequestHeader(value = "Authorization") String token,
                                                @PathVariable(value = "user-name") String userName) {
        try {
            return CommonResponseDto.okayWithData(this.userManagementService.getMenusByUserName(token, userName));
        } catch (WecubeCoreException ex) {
            return CommonResponseDto.error(ex.getMessage());
        }
    }


}
