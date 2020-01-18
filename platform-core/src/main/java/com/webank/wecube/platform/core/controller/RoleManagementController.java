package com.webank.wecube.platform.core.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.CommonResponseDto;
import com.webank.wecube.platform.core.dto.user.RoleDto;
import com.webank.wecube.platform.core.service.user.RoleMenuService;
import com.webank.wecube.platform.core.service.user.UserManagementService;

/**
 * @author howechen
 */
@RestController
@RequestMapping("/v1")
public class RoleManagementController {

    @Autowired
    private UserManagementService userManagementService;
    @Autowired
    private RoleMenuService roleMenuService;

    @PostMapping("/roles/create")
    public CommonResponseDto registerLocalRole(
                                       @RequestBody RoleDto roleDto) {
        try {
            RoleDto result = userManagementService.registerLocalRole(roleDto);
            return CommonResponseDto.okayWithData(result);
        } catch (WecubeCoreException ex) {
            return CommonResponseDto.error(ex.getMessage());
        }

    }

    @GetMapping("/roles/retrieve")
    public CommonResponseDto retrieveRole(@RequestHeader(value = "Authorization") String token) {
        try {
            return userManagementService.retrieveRole(token);
        } catch (WecubeCoreException ex) {
            return CommonResponseDto.error(ex.getMessage());
        }
    }

    @DeleteMapping("/roles/{role-id}/delete")
    public CommonResponseDto deleteRole(@RequestHeader(value = "Authorization") String token,
                                        @PathVariable("role-id") String roleName) {
        try {
            return userManagementService.deleteRole(token, roleName);
        } catch (WecubeCoreException e) {
            return CommonResponseDto.error(e.getMessage());
        }
    }

    @GetMapping("/roles/{role-id}/users")
    public CommonResponseDto getUsersByRoleId(@RequestHeader(value = "Authorization") String token,
                                              @PathVariable(value = "role-id") String roleId) {
        try {
            return userManagementService.getUsersByRoleId(token, roleId);
        } catch (WecubeCoreException ex) {
            return CommonResponseDto.error(ex.getMessage());
        }

    }

    @PostMapping("/roles/{role-id}/users/grant")
    public CommonResponseDto grantRoleToUsers(@RequestHeader(value = "Authorization") String token,
                                              @PathVariable(value = "role-id") String roleId,
                                              @RequestBody List<Object> userIdList) {
        try {
            return userManagementService.grantRoleToUsers(token, roleId, userIdList);
        } catch (WecubeCoreException ex) {
            return CommonResponseDto.error(ex.getMessage());
        }
    }

    @DeleteMapping("/roles/{role-id}/users/revoke")
    public CommonResponseDto revokeRoleFromUsers(@RequestHeader(value = "Authorization") String token,
                                                 @PathVariable(value = "role-id") String roleId,
                                                 @RequestBody List<Object> requestBody) {
        try {
            return userManagementService.revokeRoleFromUsers(token, roleId, requestBody);
        } catch (WecubeCoreException ex) {
            return CommonResponseDto.error(ex.getMessage());
        }
    }

    @GetMapping("/roles/{role-id}/menus")
    public CommonResponseDto retrieveMenusByRoleId(@PathVariable(value = "role-id") String roleId) {
        try {
            return CommonResponseDto.okayWithData(this.roleMenuService.retrieveMenusByRoleId(roleId));
        } catch (WecubeCoreException ex) {
            return CommonResponseDto.error(ex.getMessage());
        }

    }

    @PostMapping("/roles/{role-id}/menus")
    public CommonResponseDto updateRoleToMenusByRoleId(
                                                       @PathVariable(value = "role-id") String roleId,
                                                       @RequestBody List<String> menuCodeList) {
        try {
            this.roleMenuService.updateRoleToMenusByRoleId(roleId, menuCodeList);
        } catch (WecubeCoreException ex) {
            return CommonResponseDto.error(ex.getMessage());
        }
        return CommonResponseDto.okay();

    }
}
