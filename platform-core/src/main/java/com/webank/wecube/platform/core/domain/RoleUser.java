package com.webank.wecube.platform.core.domain;

public class RoleUser {
    private Integer roleUserId;
    private Integer roleId;
    private String userId;

    public RoleUser(Integer roleId, String userId) {
        this.roleId = roleId;
        this.userId = userId;
    }

    public RoleUser(Integer roleUserId, Integer roleId, String userId) {
        super();
        this.roleUserId = roleUserId;
        this.roleId = roleId;
        this.userId = userId;
    }

    public RoleUser() {
        super();
    }

    public Integer getRoleUserId() {
        return roleUserId;
    }

    public void setRoleUserId(Integer roleUserId) {
        this.roleUserId = roleUserId;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

}
