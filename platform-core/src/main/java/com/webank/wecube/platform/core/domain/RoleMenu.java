//package com.webank.wecube.platform.core.domain;
//
//import com.webank.wecube.platform.core.utils.Constants;
//
//import javax.persistence.*;
//import java.util.Objects;
//
//@Entity
//@Table(name = "role_menu")
//public class RoleMenu {
//
//    @Id
//    private String id;
//
//    @Column(name = "role_name")
//    private String roleName;
//
//    @Column(name = "menu_code")
//    private String menuCode;
//
//    public RoleMenu(String roleName, String menuCode) {
//        this.roleName = roleName;
//        this.menuCode = menuCode;
//    }
//
//    public RoleMenu() {
//    }
//
//    @PrePersist
//    public void initGuid() {
//        if (this.id == null || "".equals(this.id)) {
//            this.id = Objects.requireNonNull(this.roleName, "The [roleName] cannot be NULL while persisting [role_menu]")
//                    + Constants.KEY_COLUMN_DELIMITER
//                    + Objects.requireNonNull(this.menuCode, "The [menuItem] cannot be NULL while persisting [role_menu]");
//        }
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
//    public String getMenuCode() {
//        return menuCode;
//    }
//
//    public void setMenuCode(String menuCode) {
//        this.menuCode = menuCode;
//    }
//
//    public String getRoleName() {
//        return roleName;
//    }
//
//    public void setRoleName(String roleName) {
//        this.roleName = roleName;
//    }
//}
