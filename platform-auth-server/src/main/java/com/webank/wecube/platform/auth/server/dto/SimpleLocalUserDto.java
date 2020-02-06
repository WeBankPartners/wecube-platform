package com.webank.wecube.platform.auth.server.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SimpleLocalUserDto implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -7912286712449427306L;
    private String id;
    private String username;
    private String password;
    private String nativeName;
    private String title;
    private String emailAddr;
    private String officeTelNo;
    private String cellPhoneNo;
    private String department;
    private String englishName;
    private boolean active;
    private boolean blocked;
    private boolean deleted;

    private List<SimpleLocalRoleDto> roles = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNativeName() {
        return nativeName;
    }

    public void setNativeName(String nativeName) {
        this.nativeName = nativeName;
    }

    public String getEnglishName() {
        return englishName;
    }

    public void setEnglishName(String englishName) {
        this.englishName = englishName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getEmailAddr() {
        return emailAddr;
    }

    public void setEmailAddr(String emailAddr) {
        this.emailAddr = emailAddr;
    }

    public String getOfficeTelNo() {
        return officeTelNo;
    }

    public void setOfficeTelNo(String officeTelNo) {
        this.officeTelNo = officeTelNo;
    }

    public String getCellPhoneNo() {
        return cellPhoneNo;
    }

    public void setCellPhoneNo(String cellPhoneNo) {
        this.cellPhoneNo = cellPhoneNo;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public List<SimpleLocalRoleDto> getRoles() {
        return roles;
    }

    public void setRoles(List<SimpleLocalRoleDto> roles) {
        this.roles = roles;
    }
    
    public void addAllRoles(List<SimpleLocalRoleDto> roles) {
        for (SimpleLocalRoleDto role : roles) {
            if (role != null) {
                this.roles.add(role);
            }
        }
    }

    public void addRoles(SimpleLocalRoleDto... roles) {
        for (SimpleLocalRoleDto role : roles) {
            if (role != null) {
                this.roles.add(role);
            }
        }
    }
    
    
}
