//package com.webank.wecube.platform.core.entity.workflow;
//
//import com.webank.wecube.platform.core.dto.FavoritesRoleDto;
//import com.webank.wecube.platform.core.dto.workflow.ProcRoleDto;
//
//import javax.persistence.*;
//
///**
// * @author howechen
// */
//@Entity
//@Table(name = "FAVORITES_ROLE")
//public class FavoritesRoleEntity {
//    @Id
//    private String id;
//    @Column(name = "favorites_id")
//    private String favoritesId;
//    @Column(name = "permission")
//    @Enumerated(EnumType.STRING)
//    private permissionEnum permission;
//    @Column(name = "role_id")
//    private String roleId;
//    @Column(name = "role_name")
//    private String roleName;
//
//    public FavoritesRoleEntity() {
//    }
//
//    public String getId() {
//        return id;
//    }
//
//    public void setId(String id) {
//        this.id = id;
//    }
//
//    public String getFavoritesId() {
//        return favoritesId;
//    }
//
//    public void setFavoritesId(String favoritesId) {
//        this.favoritesId = favoritesId;
//    }
//
//    public permissionEnum getPermission() {
//        return permission;
//    }
//
//    public void setPermission(permissionEnum permission) {
//        this.permission = permission;
//    }
//
//    public String getRoleId() {
//        return roleId;
//    }
//
//    public void setRoleId(String roleId) {
//        this.roleId = roleId;
//    }
//
//    public String getRoleName() {
//        return roleName;
//    }
//
//    public void setRoleName(String roleName) {
//        this.roleName = roleName;
//    }
//
//    /**
//     * Process's permission enum
//     */
//    public enum permissionEnum {
//        /**
//         * Manage a process
//         */
//        MGMT,
//        /**
//         * Use a process only
//         */
//        USE
//    }
//}
//
