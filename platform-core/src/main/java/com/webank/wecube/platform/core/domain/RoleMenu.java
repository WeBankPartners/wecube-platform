package com.webank.wecube.platform.core.domain;

import lombok.Data;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "role_menu")
public class RoleMenu {

    @Id
    private String id;

    @Column(name = "role_id")
    private Long roleId;

    @ManyToOne
    @JoinColumn(name = "menu_id")
    private MenuItem menuItem;

    public RoleMenu(Long roleId, MenuItem menuItem) {
        this.roleId = roleId;
        this.menuItem = menuItem;
    }

    public RoleMenu() {
    }

    @PrePersist
    public void initGuid() {
        if (this.id == null | "".equals(this.id)) {
            this.id = Objects.requireNonNull(this.roleId, "The [roleId] cannot be NULL while persisting [role_menu]").toString()
                    + Objects.requireNonNull(this.menuItem, "The [menuItem] cannot be NULL while persisting [role_menu]").getCode();
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public MenuItem getMenuItem() {
        return menuItem;
    }

    public void setMenuItem(MenuItem menuItem) {
        this.menuItem = menuItem;
    }
}
