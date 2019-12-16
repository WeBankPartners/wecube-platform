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
    private Long roleId;


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

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
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
    @PreUpdate
    public void prePersistAndUpdate(ProcRoleBindingEntity entity) {
        String id = entity.getProcId() + Constants.KEY_COLUMN_DELIMITER + entity.getRoleId();
        entity.setId(id);
    }
}
