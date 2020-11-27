package com.webank.wecube.platform.core.entity.plugin;

import java.util.Date;

public class BatchExecutionJobs {
    private String id;

    private Date createTimestamp;

    private Date completeTimestamp;

    private String creator;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public Date getCreateTimestamp() {
        return createTimestamp;
    }

    public void setCreateTimestamp(Date createTimestamp) {
        this.createTimestamp = createTimestamp;
    }

    public Date getCompleteTimestamp() {
        return completeTimestamp;
    }

    public void setCompleteTimestamp(Date completeTimestamp) {
        this.completeTimestamp = completeTimestamp;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator == null ? null : creator.trim();
    }
}