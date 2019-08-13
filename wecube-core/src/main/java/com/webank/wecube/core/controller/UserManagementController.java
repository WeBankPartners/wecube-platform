package com.webank.wecube.core.controller;

import com.webank.wecube.core.domain.JsonResponse;
import com.webank.wecube.core.domain.MenuItem;
import com.webank.wecube.core.domain.Role;
import com.webank.wecube.core.service.UserManagerService;
import com.webank.wecube.core.support.cmdb.CmdbServiceV2Stub;
import com.webank.wecube.core.support.cmdb.dto.v2.RoleCiTypeDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;
import java.security.Principal;
import java.util.List;
import java.util.Map;

import static com.webank.wecube.core.domain.JsonResponse.*;
import static com.webank.wecube.core.domain.MenuItem.MENU_ADMIN_PERMISSION_MANAGEMENT;

@RestController
@Slf4j
@RequestMapping("/admin")
@RolesAllowed({MENU_ADMIN_PERMISSION_MANAGEMENT})
public class UserManagementController {

    @Data
    @AllArgsConstructor
    class Permissions {
        List<String> menuPermissions;
        List<RoleCiTypeDto> ciTypePermissions;
    }

    @Autowired
    private UserManagerService userManagerService;

    @Autowired
    private CmdbServiceV2Stub cmdbServiceV2Stub;

    @GetMapping("/users")
    @ResponseBody
    public JsonResponse getAllUsers() {
        return okayWithData(cmdbServiceV2Stub.getAllUsers());
    }

    @GetMapping("/roles")
    @ResponseBody
    public JsonResponse getAllRoles() {
        return okayWithData(cmdbServiceV2Stub.getAllRoles());
    }

    @GetMapping("/menus")
    @ResponseBody
    public JsonResponse getAllMenuItems() {
        List<MenuItem> allMenuItems = userManagerService.getAllMenuItems();
        return okayWithData(allMenuItems);
    }

    @GetMapping("/users/{username}/roles")
    @ResponseBody
    public JsonResponse getRolesByUsername(@PathVariable(value = "username")String username) {
        return okayWithData(cmdbServiceV2Stub.getRolesByUsername(username));
    }

    @GetMapping("/roles/{role-id}/users")
    @ResponseBody
    public JsonResponse getUsersByRoleId(@PathVariable(value = "role-id")int roleId) {
        return okayWithData(cmdbServiceV2Stub.getUsersByRoleId(roleId));
    }

    @GetMapping("/roles/{role-id}/permissions")
    @ResponseBody
    public JsonResponse getPermissionsByRoleId(@PathVariable(value = "role-id")int roleId) {
        List<String> menuPermissions = userManagerService.getMenuItemsByRoleId(roleId);
        List<RoleCiTypeDto> ciTypePermissions = userManagerService.getRoleCiTypesByRoleId(roleId);
        return okayWithData(new Permissions(menuPermissions, ciTypePermissions));
    }

    @GetMapping("/roles/{role-id}/menu-permissions")
    @ResponseBody
    public JsonResponse getMenuPermissionsByRoleId(@PathVariable(value = "role-id")int roleId) {
        return okayWithData(userManagerService.getMenuItemsByRoleId(roleId));
    }

    @GetMapping("/roles/{role-id}/citype-permissions")
    @ResponseBody
    public JsonResponse getCiTypePermissionsByRoleId(@PathVariable(value = "role-id")int roleId) {
        return okayWithData(userManagerService.getRoleCiTypesByRoleId(roleId));
    }

    @GetMapping("/users/{username}/permissions")
    @ResponseBody
    public JsonResponse getPermissionsByUsername(@PathVariable(value = "username")String username) {
        List<String> menuPermissions = userManagerService.getMenuItemCodesByUsername(username);
        List<RoleCiTypeDto> ciTypePermissions = userManagerService.getRoleCiTypesByUsername(username);
        return okayWithData(new Permissions(menuPermissions, ciTypePermissions));
    }

    @GetMapping("/users/{username}/menu-permissions")
    @ResponseBody
    public JsonResponse getMenuPermissionsByUsername(@PathVariable(value = "username")String username) {
        return okayWithData(userManagerService.getMenuItemCodesByUsername(username));
    }

    @GetMapping("/users/{username}/citype-permissions")
    @ResponseBody
    public JsonResponse getCiTypePermissionsByUsername(@PathVariable(value = "username")String username) {
        return okayWithData(userManagerService.getRoleCiTypesByUsername(username));
    }


    @PostMapping("/roles/create")
    @ResponseBody
    public JsonResponse createNewRole(@RequestBody Role role) {
        return okayWithData(cmdbServiceV2Stub.createRoles(role));
    }

    @DeleteMapping("/roles/{role-id}")
    @ResponseBody
    public JsonResponse deleteRole(@PathVariable(value = "role-id") int roleId) {
        userManagerService.deleteRole(roleId);
        return okay();
    }

    @PostMapping("/roles/{role-id}/users")
    @ResponseBody
    public JsonResponse grantRoleForUser(@PathVariable(value = "role-id")int roleId, @RequestBody List<String> userIds) {
        userManagerService.grantRoleForUsers(roleId, userIds);
        return okay();
    }

    @DeleteMapping("/roles/{role-id}/users")
    @ResponseBody
    public JsonResponse revokeRoleForUser(@PathVariable(value = "role-id")int roleId, @RequestBody List<String> usernames) {
        userManagerService.revokeRoleForUsers(roleId, usernames);
        return okay();
    }

    @PostMapping("/roles/{role-id}/menu-permissions")
    @ResponseBody
    public JsonResponse assignMenuPermissionForRole(@PathVariable(value = "role-id")int roleId, @RequestBody List<String> menuCodes) {
        userManagerService.assignMenuPermissionForRoles(roleId, menuCodes);
        return okay();
    }

    @DeleteMapping("/roles/{role-id}/menu-permissions")
    @ResponseBody
    public JsonResponse removeMenuPermissionForRole(@PathVariable(value = "role-id")int roleId, @RequestBody List<String> menuCodes) {
        userManagerService.removeMenuPermissionForRoles(roleId, menuCodes);
        return okay();
    }

    @PostMapping("/roles/{role-id}/citypes/{citype-id}/actions/{action-code}")
    @ResponseBody
    public JsonResponse assignCiTypePermissionForRole(@PathVariable(value = "role-id")int roleId, @PathVariable(value = "citype-id")int ciTypeId, @PathVariable(value = "action-code")String actionCode) {
        userManagerService.assignCiTypePermissionForRole(roleId, ciTypeId, actionCode);
        return okay();
    }

    @DeleteMapping("/roles/{role-id}/citypes/{citype-id}/actions/{action-code}")
    @ResponseBody
    public JsonResponse removeCiTypePermissionForRole(@PathVariable(value = "role-id")int roleId, @PathVariable(value = "citype-id")int ciTypeId, @PathVariable(value = "action-code")String actionCode) {
        userManagerService.removeCiTypePermissionForRole(roleId, ciTypeId, actionCode);
        return okay();
    }

    @GetMapping("/role-citypes/{role-citype-id}/ctrl-attributes")
    @ResponseBody
    public JsonResponse getRoleCiTypeCtrlAttributesByRoleCiTypeId(@PathVariable(value = "role-citype-id")int roleCiTypeId) {
        return okayWithData(userManagerService.getRoleCiTypeCtrlAttributesByRoleCiTypeId(roleCiTypeId));
    }

    @PostMapping("/role-citypes/{role-citype-id}/ctrl-attributes/create")
    @ResponseBody
    public JsonResponse createRoleCiTypeCtrlAttributes(@PathVariable(value = "role-citype-id")int roleCiTypeId, @RequestBody List<Map<String,Object>> roleCiTypeCtrlAttributes) {
        return okayWithData(userManagerService.createRoleCiTypeCtrlAttributes(roleCiTypeId, roleCiTypeCtrlAttributes));
    }

    @PostMapping("/role-citypes/{role-citype-id}/ctrl-attributes/update")
    @ResponseBody
    public JsonResponse updateRoleCiTypeCtrlAttributes(@PathVariable(value = "role-citype-id")int roleCiTypeId, @RequestBody List<Map<String,Object>> roleCiTypeCtrlAttributes) {
        return okayWithData(userManagerService.updateRoleCiTypeCtrlAttributes(roleCiTypeId, roleCiTypeCtrlAttributes));
    }

    @PostMapping("/role-citypes/{role-citype-id}/ctrl-attributes/delete")
    @ResponseBody
    public JsonResponse deleteRoleCiTypeCtrlAttributes(@PathVariable(value = "role-citype-id")int roleCiTypeId, @RequestBody Integer[] roleCiTypeCtrlAttrIds) {
        cmdbServiceV2Stub.deleteRoleCiTypeCtrlAttributes(roleCiTypeCtrlAttrIds);
        return okay();
    }

}



