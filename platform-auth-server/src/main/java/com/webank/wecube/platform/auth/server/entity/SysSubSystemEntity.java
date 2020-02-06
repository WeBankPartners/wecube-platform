package com.webank.wecube.platform.auth.server.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "AUTH_SYS_SUB_SYSTEM")
public class SysSubSystemEntity extends BaseUUIDFeaturedEntity {

    @Column(name = "NAME")
    private String name;

    @Column(name = "SYSTEM_CODE")
    private String systemCode;

    @Column(name = "API_KEY", length = 500)
    private String apiKey;

    @Column(name = "PUB_API_KEY", length = 500)
    private String pubApiKey;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "IS_ACTIVE")
    private boolean active = true;

    @Column(name = "IS_BLOCKED")
    private boolean blocked = false;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSystemCode() {
        return systemCode;
    }

    public void setSystemCode(String systemCode) {
        this.systemCode = systemCode;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getPubApiKey() {
        return pubApiKey;
    }

    public void setPubApiKey(String pubApiKey) {
        this.pubApiKey = pubApiKey;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
