package com.webank.wecube.platform.auth.server.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

@Entity
@Table(name = "AUTH_SYS_MENU")
public class SysMenuEntity extends AbstractTraceableEntity {

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private Long id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "SYSTEMID")
    private Long systemId;

    @Column(name = "SYSTEM_NAME")
    private String systemName;

    @Column(name = "SYSTEM_CODE")
    private String systemCode;

    @Column(name = "IS_ACTIVE")
    private Boolean active;

    @Column(name = "IS_BLOCKED")
    private Boolean blocked;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "AUTH_SYS_MENU_ROLE", joinColumns = { @JoinColumn(name = "MENU_ID") }, inverseJoinColumns = {
            @JoinColumn(name = "ROLE_ID") })
    private List<SysRoleEntity> boundRoles = new ArrayList<SysRoleEntity>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "AUTH_SYS_MENU_API", joinColumns = { @JoinColumn(name = "MENU_ID") }, inverseJoinColumns = {
            @JoinColumn(name = "API_ID") })
    private List<SysApiEntity> boundApis = new ArrayList<SysApiEntity>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getSystemId() {
        return systemId;
    }

    public void setSystemId(Long systemId) {
        this.systemId = systemId;
    }

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public String getSystemCode() {
        return systemCode;
    }

    public void setSystemCode(String systemCode) {
        this.systemCode = systemCode;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getBlocked() {
        return blocked;
    }

    public void setBlocked(Boolean blocked) {
        this.blocked = blocked;
    }

    public List<SysRoleEntity> getBoundRoles() {
        return boundRoles;
    }

    public void setBoundRoles(List<SysRoleEntity> boundRoles) {
        this.boundRoles = boundRoles;
    }

    public List<SysApiEntity> getBoundApis() {
        return boundApis;
    }

    public void setBoundApis(List<SysApiEntity> boundApis) {
        this.boundApis = boundApis;
    }

}
