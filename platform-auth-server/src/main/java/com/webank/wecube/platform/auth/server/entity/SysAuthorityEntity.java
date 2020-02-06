package com.webank.wecube.platform.auth.server.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "AUTH_SYS_AUTHORITY")
public class SysAuthorityEntity extends BaseStatusFeaturedEntity {
    
    public static final String SCOPE_GLOBAL = "GLOBAL";

    @Column(name = "CODE")
    private String code;

    @Column(name = "DISPLAY_NAME")
    private String displayName;

    @Column(name = "SCOPE")
    private String scope;
    
    @Column(name = "DESCRIPTION")
    private String description;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
