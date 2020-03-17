package com.webank.wecube.platform.core.dto;

import com.webank.wecube.platform.core.entity.workflow.FavoritesRoleEntity;

public class FavoritesRoleDto {
    private String favoritesId;
    private String permission;
    private String roleId;
    private String roleName;

    public static FavoritesRoleDto fromDomain(FavoritesRoleEntity favoritesRoleEntity) {
        FavoritesRoleDto result = new FavoritesRoleDto();
        result.setFavoritesId(favoritesRoleEntity.getFavoritesId());
        result.setPermission(favoritesRoleEntity.getPermission().toString());
        result.setRoleId(favoritesRoleEntity.getRoleId());
        result.setRoleName(favoritesRoleEntity.getRoleName());
        return result;
    }

    public static FavoritesRoleEntity toDomain(String id, String favoritesId, String roleId, FavoritesRoleEntity.permissionEnum permissionEnum, String roleName) {
        FavoritesRoleEntity result = new FavoritesRoleEntity();
        result.setId(id);
        result.setFavoritesId(favoritesId);
        result.setRoleId(roleId);
        result.setPermission(permissionEnum);
        result.setRoleName(roleName);
        return result;

    }
    public FavoritesRoleDto() {
    }

    public FavoritesRoleDto(String id, String favoritesId, String permission, String roleId, String roleName) {
        this.favoritesId = favoritesId;
        this.permission = permission;
        this.roleId = roleId;
        this.roleName = roleName;
    }

    public String getFavoritesId() {
        return favoritesId;
    }

    public void setFavoritesId(String favoritesId) {
        this.favoritesId = favoritesId;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}
