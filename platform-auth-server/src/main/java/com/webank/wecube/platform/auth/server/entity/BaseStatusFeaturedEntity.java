package com.webank.wecube.platform.auth.server.entity;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class BaseStatusFeaturedEntity extends BaseUUIDFeaturedEntity {
    @Column(name = "IS_ACTIVE")
    private boolean active = true;
    @Column(name = "IS_DELETED")
    private boolean deleted = false;

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
