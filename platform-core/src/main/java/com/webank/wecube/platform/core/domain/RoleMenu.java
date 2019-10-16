package com.webank.wecube.platform.core.domain;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity @Table(name = "role_menu")
public class RoleMenu {

    @Id @GeneratedValue
    private Integer id;

    @Column(name = "role_id")
    private Integer roleId;

    @ManyToOne @JoinColumn(name = "menu_id")
    private MenuItem menuItem;

    public RoleMenu() {
        this(null);
    }

    public RoleMenu(Integer id) {
        this.setId(id);
    }

}
