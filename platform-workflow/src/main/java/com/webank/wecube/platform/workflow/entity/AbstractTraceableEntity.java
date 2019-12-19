package com.webank.wecube.platform.workflow.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;

import com.webank.wecube.platform.workflow.model.TraceStatus;

@MappedSuperclass
public abstract class AbstractTraceableEntity {
    
    @Column(name = "STATUS")
    @Enumerated(EnumType.STRING)
    private TraceStatus status;

    @Column(name = "START_TIME")
    private Date startTime;

    @Column(name = "END_TIME")
    private Date endTime;

    @Column(name="CREATED_BY")
    private String createdBy;
    
    @Column(name="CREATED_TIME")
    private Date createdTime;
    
    @Column(name="UPDATED_BY")
    private String updatedBy;
    
    @Column(name="UPDATED_TIME")
    private Date updatedTime;

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }
    
    public TraceStatus getStatus() {
        return status;
    }

    public void setStatus(TraceStatus status) {
        this.status = status;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
}
