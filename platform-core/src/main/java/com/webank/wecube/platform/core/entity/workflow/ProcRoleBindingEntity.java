package com.webank.wecube.platform.core.entity.workflow;

import com.webank.wecube.platform.core.utils.Constants;

import javax.persistence.*;

/**
 * @author howechen
 */
@Entity
@Table(name = "core_ru_proc_role_binding")
@EntityListeners(value = ProcRoleBindingEntityListener.class)
public class ProcRoleBindingEntity {
    @Id
    @Column(name = "id")
    private String id;
    @Column(name = "proc_id")
    private String procId;
    @Column(name = "permission")
    @Enumerated(EnumType.STRING)
    private permissionEnum permission;
    @Column(name = "role_id")
    private String roleId;
    @Column(name = "role_name")
    private String roleName;


    public ProcRoleBindingEntity() {
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProcId() {
        return procId;
    }

    public void setProcId(String procId) {
        this.procId = procId;
    }

    public permissionEnum getPermission() {
        return permission;
    }

    public void setPermission(permissionEnum permission) {
        this.permission = permission;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    /**
     * Process's permission enum
     */
    public enum permissionEnum {
        /**
         * Manage a process
         */
        MGMT,
        /**
         * Use a process only
         */
        USE
    }

}

class ProcRoleBindingEntityListener {
    @PrePersist
    public void prePersistAndUpdate(ProcRoleBindingEntity entity) {
        String id = entity.getProcId()
                + Constants.KEY_COLUMN_DELIMITER + entity.getRoleId()
                + Constants.KEY_COLUMN_DELIMITER + entity.getPermission().toString();
        entity.setId(id);
    }
}
