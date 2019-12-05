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
    public CommonResponseDto createUser(@RequestBody Map<String, Object> requestBody) {
        try {
            return userManagementService.createUser(requestBody);
        } catch (WecubeCoreException ex) {
            return CommonResponseDto.error(ex.getMessage());
        }

    }

    @GetMapping("/users/retrieve")
    @ResponseBody
    public CommonResponseDto retrieveUser() {
        try {
            return userManagementService.retrieveUser();
        } catch (WecubeCoreException ex) {
            return CommonResponseDto.error(ex.getMessage());
        }
    }

    @DeleteMapping("/users/{user-id}/delete")
    @ResponseBody
    public CommonResponseDto deleteUser(@PathVariable("user-id") Long id) {
        try {
            return userManagementService.deleteUser(id);
        } catch (WecubeCoreException e) {
            return CommonResponseDto.error(e.getMessage());
        }
    }

    @PostMapping("/roles/create")
    @ResponseBody
    public CommonResponseDto createRole(@RequestBody Map<String, Object> requestBody) {
        try {
            return userManagementService.createRole(requestBody);
        } catch (WecubeCoreException ex) {
            return CommonResponseDto.error(ex.getMessage());
        }

    }

    @GetMapping("/roles/retrieve")
    @ResponseBody
    public CommonResponseDto retrieveRole() {
        try {
            return userManagementService.retrieveRole();
        } catch (WecubeCoreException ex) {
            return CommonResponseDto.error(ex.getMessage());
        }
    }

    @DeleteMapping("/roles/{role-id}/delete")
    @ResponseBody
    public CommonResponseDto deleteRole(@PathVariable("role-id") Long id) {
        try {
            return userManagementService.deleteRole(id);
        } catch (WecubeCoreException e) {
            return CommonResponseDto.error(e.getMessage());
        }
    }

    @GetMapping("/users/{user-name}/roles")
    @ResponseBody
    public CommonResponseDto getRolesByUsername(@PathVariable(value = "user-name") String userName) {
        try {
            return userManagementService.getRolesByUserName(userName);
        } catch (WecubeCoreException ex) {
            return CommonResponseDto.error(ex.getMessage());
        }
    }

    @GetMapping("/roles/{role-id}/users")
    @ResponseBody
    public CommonResponseDto getUsersByRoleId(@PathVariable(value = "role-id") Long roleId) {
        try {
            return userManagementService.getUsersByRoleId(roleId);
        } catch (WecubeCoreException ex) {
            return CommonResponseDto.error(ex.getMessage());
        }

    }

    @PostMapping("/roles/{role-id}/users/grant")
    @ResponseBody
    public CommonResponseDto grantRoleToUsers(@PathVariable(value = "role-id") Long roleId,
                                              @RequestBody List<Object> userIdList) {
        try {
            return userManagementService.grantRoleToUsers(roleId, userIdList);
        } catch (WecubeCoreException ex) {
            return CommonResponseDto.error(ex.getMessage());
        }
    }

    @DeleteMapping("/roles/{role-id}/users/revoke")
    @ResponseBody
    public CommonResponseDto revokeRoleFromUsers(@PathVariable(value = "role-id") Long roleId,
                                                 @RequestBody List<Object> requestBody) {
        try {
            return userManagementService.revokeRoleFromUsers(roleId, requestBody);
        } catch (WecubeCoreException ex) {
            return CommonResponseDto.error(ex.getMessage());
        }
    }


}
