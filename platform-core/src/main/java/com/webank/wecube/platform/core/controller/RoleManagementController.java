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

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.CommonResponseDto;
import com.webank.wecube.platform.core.dto.user.RoleDto;
import com.webank.wecube.platform.core.dto.user.RoleMenuDto;
import com.webank.wecube.platform.core.dto.user.UserDto;
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
    public CommonResponseDto registerLocalRole(@RequestBody RoleDto roleDto) {
        try {
            RoleDto result = userManagementService.registerLocalRole(roleDto);
            return CommonResponseDto.okayWithData(result);
        } catch (WecubeCoreException ex) {
            return CommonResponseDto.error(ex.getMessage());
        }

    }

    @GetMapping("/roles/retrieve")
    public CommonResponseDto retrieveAllRoles() {
        try {
            List<RoleDto> result = userManagementService.retrieveAllRoles();
            return CommonResponseDto.okayWithData(result);
        } catch (WecubeCoreException ex) {
            return CommonResponseDto.error(ex.getMessage());
        }
    }

    @DeleteMapping("/roles/{role-id}/delete")
    public CommonResponseDto unregisterLocalRoleById(@PathVariable("role-id") String roleId) {
        try {
            userManagementService.unregisterLocalRoleById(roleId);
            return CommonResponseDto.okay();
        } catch (WecubeCoreException e) {
            return CommonResponseDto.error(e.getMessage());
        }
    }

    @GetMapping("/roles/{role-id}/users")
    public CommonResponseDto getUsersByRoleId(@PathVariable(value = "role-id") String roleId) {
        try {
            List<UserDto> users = userManagementService.getUsersByRoleId(roleId);
            return CommonResponseDto.okayWithData(users);
        } catch (WecubeCoreException ex) {
            return CommonResponseDto.error(ex.getMessage());
        }

    }

    @PostMapping("/roles/{role-id}/users/grant")
    public CommonResponseDto grantRoleToUsers(@PathVariable(value = "role-id") String roleId,
            @RequestBody List<Object> userIds) {
        try {
            userManagementService.grantRoleToUsers(roleId, userIds);
            return CommonResponseDto.okay();
        } catch (WecubeCoreException ex) {
            return CommonResponseDto.error(ex.getMessage());
        }
    }

    @DeleteMapping("/roles/{role-id}/users/revoke")
    public CommonResponseDto revokeRoleFromUsers(@PathVariable(value = "role-id") String roleId,
            @RequestBody List<Object> requestBody) {
        try {
            userManagementService.revokeRoleFromUsers(roleId, requestBody);
            return CommonResponseDto.okay();
        } catch (WecubeCoreException ex) {
            return CommonResponseDto.error(ex.getMessage());
        }
    }

    @GetMapping("/roles/{role-id}/menus")
    public CommonResponseDto retrieveMenusByRoleId(@PathVariable(value = "role-id") String roleId) {
        try {
            RoleMenuDto result = this.roleMenuService.retrieveMenusByRoleId(roleId);
            return CommonResponseDto.okayWithData(result);
        } catch (WecubeCoreException ex) {
            return CommonResponseDto.error(ex.getMessage());
        }

    }

    @PostMapping("/roles/{role-id}/menus")
    public CommonResponseDto updateRoleToMenusByRoleId(@PathVariable(value = "role-id") String roleId,
            @RequestBody List<String> menuCodeList) {
        try {
            this.roleMenuService.updateRoleToMenusByRoleId(roleId, menuCodeList);
            return CommonResponseDto.okay();
        } catch (WecubeCoreException ex) {
            return CommonResponseDto.error(ex.getMessage());
        }

    }
}
