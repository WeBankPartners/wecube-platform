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
            return CommonResponseDto.okayWithData(userManagementService.createUser(requestBody));
        } catch (WecubeCoreException ex) {
            return CommonResponseDto.error(ex.getMessage());
        }

    }

    @GetMapping("/users/retrieve")
    @ResponseBody
    public CommonResponseDto retrieveUser() {
        try {
            return CommonResponseDto.okayWithData(userManagementService.retrieveUser());
        } catch (WecubeCoreException ex) {
            return CommonResponseDto.error(ex.getMessage());
        }
    }

    @DeleteMapping("/users/{user-id}/delete")
    @ResponseBody
    public CommonResponseDto deleteUser(@PathVariable("user-id") Long id) {
        try {
            return CommonResponseDto.okayWithData(userManagementService.deleteUser(id));
        } catch (WecubeCoreException e) {
            return CommonResponseDto.error(e.getMessage());
        }
    }

    @PostMapping("/roles/create")
    @ResponseBody
    public CommonResponseDto createRole(@RequestBody Map<String, Object> requestBody) {
        try {
            return CommonResponseDto.okayWithData(userManagementService.createRole(requestBody));
        } catch (WecubeCoreException ex) {
            return CommonResponseDto.error(ex.getMessage());
        }

    }

    @GetMapping("/roles/retrieve")
    @ResponseBody
    public CommonResponseDto retrieveRole() {
        try {
            return CommonResponseDto.okayWithData(userManagementService.retrieveRole());
        } catch (WecubeCoreException ex) {
            return CommonResponseDto.error(ex.getMessage());
        }
    }

    @DeleteMapping("/roles/{role-id}/delete")
    @ResponseBody
    public CommonResponseDto deleteRole(@PathVariable("role-id") Long id) {
        try {
            return CommonResponseDto.okayWithData(userManagementService.deleteRole(id));
        } catch (WecubeCoreException e) {
            return CommonResponseDto.error(e.getMessage());
        }
    }

    @GetMapping("/users/{user-name}/roles")
    @ResponseBody
    public CommonResponseDto getRolesByUsername(@PathVariable(value = "user-name") String userName) {
        return CommonResponseDto.okayWithData(userManagementService.getRolesByUserName(userName));
    }

    @GetMapping("/roles/{role-id}/users")
    @ResponseBody
    public CommonResponseDto getUsersByRoleId(@PathVariable(value = "role-id") Long roleId) {
        return CommonResponseDto.okayWithData(userManagementService.getUsersByRoleId(roleId));
    }

    @PostMapping("/roles/{role-id}/users/grant")
    @ResponseBody
    public CommonResponseDto grantRoleForUsers(@PathVariable(value = "role-id") Long roleId,
                                               @RequestBody List<Object> userIdList) {
        try {
            return CommonResponseDto.okayWithData(userManagementService.grantRoleForUsers(roleId, userIdList));
        } catch (WecubeCoreException ex) {
            return CommonResponseDto.error(ex.getMessage());
        }
    }

    @DeleteMapping("/roles/{role-id}/users/revoke")
    @ResponseBody
    public CommonResponseDto revokeRoleForUsers(@PathVariable(value = "role-id") Long roleId,
                                                @RequestBody List<Object> requestBody) {
        try {
            return CommonResponseDto.okayWithData(userManagementService.revokeRoleFromUser(roleId, requestBody));
        } catch (WecubeCoreException ex) {
            return CommonResponseDto.error(ex.getMessage());
        }
    }


}
