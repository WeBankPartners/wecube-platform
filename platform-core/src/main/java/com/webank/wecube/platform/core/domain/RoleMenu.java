package com.webank.wecube.platform.core.domain;

import com.webank.wecube.platform.core.utils.Constants;
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

    @Column(name = "menu_code")
    private String menuCode;

    public RoleMenu(Long roleId, String menuCode) {
        this.roleId = roleId;
        this.menuCode = menuCode;
    }

    public RoleMenu() {
    }

    @PrePersist
    public void initGuid() {
        if (this.id == null | "".equals(this.id)) {
            this.id = Objects.requireNonNull(this.roleId, "The [roleId] cannot be NULL while persisting [role_menu]").toString()
                    + Constants.KEY_COLUMN_DELIMITER
                    + Objects.requireNonNull(this.menuCode, "The [menuItem] cannot be NULL while persisting [role_menu]");
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

    public String getMenuCode() {
        return menuCode;
    }

    public void setMenuCode(String menuCode) {
        this.menuCode = menuCode;
    }
}
