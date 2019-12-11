package com.webank.wecube.platform.core.entity.workflow;

import com.webank.wecube.platform.core.utils.Constants;

import javax.persistence.*;

/**
 * @author howechen
 */
@Entity
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

    @PrePersist
    public void initEntity() {
        if (null == this.id || "".equals(this.id.trim())) {
            this.id = this.procId
                    + Constants.KEY_COLUMN_DELIMITER
                    + this.roleId;
        }
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
