package com.webank.wecube.platform.core.controller;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.CommonResponseDto;
import com.webank.wecube.platform.core.service.user.UserManagementServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    public CommonResponseDto createUser(@RequestHeader("Authorization") String token,
                                        @RequestBody Map<String, Object> requestBody) {
        try {
            return userManagementService.createUser(token, requestBody);
        } catch (WecubeCoreException ex) {
            return CommonResponseDto.error(ex.getMessage());
        }

    }

    @GetMapping("/users/retrieve")
    @ResponseBody
    public CommonResponseDto retrieveUser(@RequestHeader("Authorization") String token) {
        try {
            return userManagementService.retrieveUser(token);
        } catch (WecubeCoreException ex) {
            return CommonResponseDto.error(ex.getMessage());
        }
    }

    @DeleteMapping("/users/{user-id}/delete")
    @ResponseBody
    public CommonResponseDto deleteUser(@RequestHeader("Authorization") String token,
                                        @PathVariable("user-id") Long id) {
        try {
            return userManagementService.deleteUser(token, id);
        } catch (WecubeCoreException e) {
            return CommonResponseDto.error(e.getMessage());
        }
    }

    @PostMapping("/roles/create")
    @ResponseBody
    public CommonResponseDto createRole(@RequestHeader("Authorization") String token,
                                        @RequestBody Map<String, Object> requestBody) {
        try {
            return userManagementService.createRole(token, requestBody);
        } catch (WecubeCoreException ex) {
            return CommonResponseDto.error(ex.getMessage());
        }

    }

    @GetMapping("/roles/retrieve")
    @ResponseBody
    public CommonResponseDto retrieveRole(@RequestHeader("Authorization") String token) {
        try {
            return userManagementService.retrieveRole(token);
        } catch (WecubeCoreException ex) {
            return CommonResponseDto.error(ex.getMessage());
        }
    }

    @DeleteMapping("/roles/{role-id}/delete")
    @ResponseBody
    public CommonResponseDto deleteRole(@RequestHeader("Authorization") String token,
                                        @PathVariable("role-id") Long id) {
        try {
            return userManagementService.deleteRole(token, id);
        } catch (WecubeCoreException e) {
            return CommonResponseDto.error(e.getMessage());
        }
    }

    @GetMapping("/users/{user-name}/roles")
    @ResponseBody
    public CommonResponseDto getRolesByUsername(@RequestHeader("Authorization") String token,
                                                @PathVariable(value = "user-name") String userName) {
        try {
            return userManagementService.getRolesByUserName(token, userName);
        } catch (WecubeCoreException ex) {
            return CommonResponseDto.error(ex.getMessage());
        }
    }

    @GetMapping("/roles/{role-id}/users")
    @ResponseBody
    public CommonResponseDto getUsersByRoleId(@RequestHeader("Authorization") String token,
                                              @PathVariable(value = "role-id") Long roleId) {
        try {
            return userManagementService.getUsersByRoleId(token, roleId);
        } catch (WecubeCoreException ex) {
            return CommonResponseDto.error(ex.getMessage());
        }

    }

    @PostMapping("/roles/{role-id}/users/grant")
    @ResponseBody
    public CommonResponseDto grantRoleToUsers(@RequestHeader("Authorization") String token,
                                              @PathVariable(value = "role-id") Long roleId,
                                              @RequestBody List<Object> userIdList) {
        try {
            return userManagementService.grantRoleToUsers(token, roleId, userIdList);
        } catch (WecubeCoreException ex) {
            return CommonResponseDto.error(ex.getMessage());
        }
    }

    @DeleteMapping("/roles/{role-id}/users/revoke")
    @ResponseBody
    public CommonResponseDto revokeRoleFromUsers(@RequestHeader("Authorization") String token,
                                                 @PathVariable(value = "role-id") Long roleId,
                                                 @RequestBody List<Object> requestBody) {
        try {
            return userManagementService.revokeRoleFromUsers(token, roleId, requestBody);
        } catch (WecubeCoreException ex) {
            return CommonResponseDto.error(ex.getMessage());
        }
    }


}
