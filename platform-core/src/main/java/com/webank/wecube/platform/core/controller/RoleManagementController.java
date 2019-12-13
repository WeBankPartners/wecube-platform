package com.webank.wecube.platform.core.controller;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.CommonResponseDto;
import com.webank.wecube.platform.core.service.user.RoleMenuServiceImpl;
import com.webank.wecube.platform.core.service.user.UserManagementServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author howechen
 */
@RestController
@RequestMapping("v1/")
public class RoleManagementController {

    private UserManagementServiceImpl userManagementService;
    private RoleMenuServiceImpl roleMenuService;

    @Autowired
    public RoleManagementController(UserManagementServiceImpl userManagementService, RoleMenuServiceImpl roleMenuService) {
        this.userManagementService = userManagementService;
        this.roleMenuService = roleMenuService;
    }

    @PostMapping("/roles/create")
    @ResponseBody
    public CommonResponseDto createRole(@RequestHeader(value = "Authorization") String token,
                                        @RequestBody Map<String, Object> requestBody) {
        try {
            return userManagementService.createRole(token, requestBody);
        } catch (WecubeCoreException ex) {
            return CommonResponseDto.error(ex.getMessage());
        }

    }

    @GetMapping("/roles/retrieve")
    @ResponseBody
    public CommonResponseDto retrieveRole(@RequestHeader(value = "Authorization") String token) {
        try {
            return userManagementService.retrieveRole(token);
        } catch (WecubeCoreException ex) {
            return CommonResponseDto.error(ex.getMessage());
        }
    }

    @DeleteMapping("/roles/{role-id}/delete")
    @ResponseBody
    public CommonResponseDto deleteRole(@RequestHeader(value = "Authorization") String token,
                                        @PathVariable("role-id") Long id) {
        try {
            return userManagementService.deleteRole(token, id);
        } catch (WecubeCoreException e) {
            return CommonResponseDto.error(e.getMessage());
        }
    }

    @GetMapping("/roles/{role-id}/users")
    @ResponseBody
    public CommonResponseDto getUsersByRoleId(@RequestHeader(value = "Authorization") String token,
                                              @PathVariable(value = "role-id") Long roleId) {
        try {
            return userManagementService.getUsersByRoleId(token, roleId);
        } catch (WecubeCoreException ex) {
            return CommonResponseDto.error(ex.getMessage());
        }

    }

    @PostMapping("/roles/{role-id}/users/grant")
    @ResponseBody
    public CommonResponseDto grantRoleToUsers(@RequestHeader(value = "Authorization") String token,
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
    public CommonResponseDto revokeRoleFromUsers(@RequestHeader(value = "Authorization") String token,
                                                 @PathVariable(value = "role-id") Long roleId,
                                                 @RequestBody List<Object> requestBody) {
        try {
            return userManagementService.revokeRoleFromUsers(token, roleId, requestBody);
        } catch (WecubeCoreException ex) {
            return CommonResponseDto.error(ex.getMessage());
        }
    }

    @GetMapping("/roles/{role-id}/menus")
    @ResponseBody
    public CommonResponseDto retrieveMenusByRoleId(@PathVariable(value = "role-id") Long roleId) {
        try {
            return CommonResponseDto.okayWithData(this.roleMenuService.retrieveMenusByRoleId(roleId));
        } catch (WecubeCoreException ex) {
            return CommonResponseDto.error(ex.getMessage());
        }

    }

    @PostMapping("/roles/{role-id}/menus")
    @ResponseBody
    public CommonResponseDto updateRoleToMenusByRoleId(@PathVariable(value = "role-id") Long roleId,
                                                       @RequestBody List<String> menuCodeList) {
        try {
            this.roleMenuService.updateRoleToMenusByRoleId(roleId, menuCodeList);
        } catch (WecubeCoreException ex) {
            return CommonResponseDto.error(ex.getMessage());
        }
        return CommonResponseDto.okay();

    }
}
