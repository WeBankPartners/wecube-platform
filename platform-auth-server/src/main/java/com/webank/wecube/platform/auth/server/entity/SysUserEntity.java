package com.webank.wecube.platform.auth.server.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "AUTH_SYS_USER")
public class SysUserEntity extends BaseUUIDFeaturedEntity {
    @Column(name = "USERNAME")
    private String username;

    @Column(name = "ENGLISH_NAME")
    private String englishName;

    @Column(name = "LOCAL_NAME")
    private String localName;

    @Column(name = "DEPT")
    private String department;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "EMAIL_ADDR")
    private String emailAddr;

    @Column(name = "OFFICE_TEL_NO")
    private String officeTelNo;

    @Column(name = "CELL_PHONE_NO")
    private String cellPhoneNo;

    @Column(name = "PASSWORD")
    private String password;

    @Column(name = "IS_ACTIVE")
    private boolean active = true;

    @Column(name = "IS_BLOCKED")
    private boolean blocked = false;

    @Column(name = "IS_DELETED")
    private boolean deleted = false;
    
    @Column(name = "AUTH_SRC")
    private String authSource;
    
    @Column(name = "AUTH_CTX")
    private String authContext;

    public SysUserEntity() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEnglishName() {
        return englishName;
    }

    public void setEnglishName(String englishName) {
        this.englishName = englishName;
    }

    public String getLocalName() {
        return localName;
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

	public String getAuthSource() {
		return authSource;
	}

	public void setAuthSource(String authSource) {
		this.authSource = authSource;
	}

	public String getAuthContext() {
		return authContext;
	}

	public void setAuthContext(String authContext) {
		this.authContext = authContext;
	}
	
}
