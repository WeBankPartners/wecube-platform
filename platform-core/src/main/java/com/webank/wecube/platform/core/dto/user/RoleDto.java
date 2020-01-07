package com.webank.wecube.platform.core.dto.user;

/**
 * @author howechen
 */
public class RoleDto {

    private String createdBy;
    private String updatedBy;
    private String createdTime;
    private String updatedTime;
    private String id;
    private String displayName;
    private String name;
    private String email;


    public RoleDto() {
    }

    public RoleDto(String displayName, String name) {
        this.displayName = displayName;
        this.name = name;
    }

    public RoleDto(String createdBy, String updatedBy, String createdTime, String updatedTime, String id, String displayName, String name) {
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
        this.createdTime = createdTime;
        this.updatedTime = updatedTime;
        this.id = id;
        this.displayName = displayName;
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(String updatedTime) {
        this.updatedTime = updatedTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
