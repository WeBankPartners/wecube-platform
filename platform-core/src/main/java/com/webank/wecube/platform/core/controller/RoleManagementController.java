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

import com.webank.wecube.platform.core.dto.CommonResponseDto;
import com.webank.wecube.platform.core.dto.FavoritesDto;
import com.webank.wecube.platform.core.dto.user.RoleDto;
import com.webank.wecube.platform.core.dto.user.RoleMenuDto;
import com.webank.wecube.platform.core.dto.user.UserDto;
import com.webank.wecube.platform.core.dto.workflow.ProcRoleRequestDto;
import com.webank.wecube.platform.core.service.user.RoleFavoritesService;
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
    @Autowired
    private RoleFavoritesService roleFavoritesService;

    @PostMapping("/roles/create")
    public CommonResponseDto registerLocalRole(@RequestBody RoleDto roleDto) {
        RoleDto result = userManagementService.registerLocalRole(roleDto);
        return CommonResponseDto.okayWithData(result);

    }

    @GetMapping("/roles/retrieve")
    public CommonResponseDto retrieveAllRoles() {
        List<RoleDto> result = userManagementService.retrieveAllRoles();
        return CommonResponseDto.okayWithData(result);
    }

    @DeleteMapping("/roles/{role-id}/delete")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN')")
    public CommonResponseDto unregisterLocalRoleById(@PathVariable("role-id") String roleId) {
        userManagementService.unregisterLocalRoleById(roleId);
        return CommonResponseDto.okay();
    }

    @GetMapping("/roles/{role-id}/users")
    public CommonResponseDto getUsersByRoleId(@PathVariable(value = "role-id") String roleId) {
        List<UserDto> users = userManagementService.getUsersByRoleId(roleId);
        return CommonResponseDto.okayWithData(users);

    }

    @PostMapping("/roles/{role-id}/users/grant")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN')")
    public CommonResponseDto grantRoleToUsers(@PathVariable(value = "role-id") String roleId,
            @RequestBody List<String> userIds) {
        userManagementService.grantRoleToUsers(roleId, userIds);
        return CommonResponseDto.okay();
    }

    @DeleteMapping("/roles/{role-id}/users/revoke")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN')")
    public CommonResponseDto revokeRoleFromUsers(@PathVariable(value = "role-id") String roleId,
            @RequestBody List<String> userIds) {
        userManagementService.revokeRoleFromUsers(roleId, userIds);
        return CommonResponseDto.okay();
    }

    @GetMapping("/roles/{role-id}/menus")
    public CommonResponseDto retrieveMenusByRoleId(@PathVariable(value = "role-id") String roleId) {
        RoleMenuDto result = this.roleMenuService.retrieveMenusByRoleId(roleId);
        return CommonResponseDto.okayWithData(result);

    }

    @PostMapping("/roles/{role-id}/menus")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN')")
    public CommonResponseDto updateRoleToMenusByRoleId(@PathVariable(value = "role-id") String roleId,
            @RequestBody List<String> menuCodeList) {
        this.roleMenuService.updateRoleToMenusByRoleId(roleId, menuCodeList);
        return CommonResponseDto.okay();
    }

    @GetMapping("/roles/favorites/retrieve")
    public CommonResponseDto retrieveAllCollections() {
        List<FavoritesDto> result = roleFavoritesService.retrieveAllCollections();
        return CommonResponseDto.okayWithData(result);
    }

    @PostMapping("/roles/favorites/create")
    public CommonResponseDto createCollectionByRole(@RequestBody FavoritesDto favoritesDto) {
        roleFavoritesService.createCollectionByRole(favoritesDto);
        return CommonResponseDto.okay();
    }

    @PostMapping("/roles/{favorites-id}/favorites")
    public CommonResponseDto updateCollectionByRole(@PathVariable("favorites-id") String favoritesId,
            @RequestBody ProcRoleRequestDto favoritesRoleRequestDto) {
        roleFavoritesService.updateFavoritesRoleBinding(favoritesId, favoritesRoleRequestDto);
        return CommonResponseDto.okay();
    }

    @DeleteMapping("/roles/{favorites-id}/favorites")
    public CommonResponseDto deleteProcRoleBinding(@PathVariable("favorites-id") String favoritesId,
            @RequestBody ProcRoleRequestDto favoritesRoleRequestDto) {
        roleFavoritesService.deleteFavoritesRoleBinding(favoritesId, favoritesRoleRequestDto);
        return CommonResponseDto.okay();
    }

    @DeleteMapping("/roles/favorites/{favorites-id}/delete")
    public CommonResponseDto deleteCollectionByRole(@PathVariable("favorites-id") String favoritesId) {
        this.roleFavoritesService.deleteCollectionById(favoritesId);
        // this.roleMenuService.updateRoleToMenusByRoleId(roleId, menuCodeList);
        return CommonResponseDto.okay();
    }
}
