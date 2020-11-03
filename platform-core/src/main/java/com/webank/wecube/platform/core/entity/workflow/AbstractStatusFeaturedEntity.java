package com.webank.wecube.platform.core.entity.workflow;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class AbstractStatusFeaturedEntity extends AbstractTraceableEntity {
    @Column(name = "ACTIVE")
    private boolean active = true;
    @Column(name = "REV")
    private int revision;

    @Column(name = "STATUS")
    private String status;

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getRevision() {
        return revision;
    }

    public void setRevision(int revision) {
        this.revision = revision;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
