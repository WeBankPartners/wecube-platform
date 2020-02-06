package com.webank.wecube.platform.core.support.authserver;

import java.io.Serializable;

public class AsAuthorityDto implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -2877918185362439084L;
    private String id;
    private String code;
    private String displayName;
    private String scope;
    private String description;

    private boolean active;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

}
