package com.webank.wecube.platform.core.entity.workflow;

import java.util.Date;

public class ProcInstInfoEntity {
    public static final String NOT_STARTED_STATUS = "NotStarted";
    public static final String IN_PROGRESS_STATUS = "InProgress";
    public static final String COMPLETED_STATUS = "Completed";
    public static final String FAULTED_STATUS = "Faulted";
    public static final String TIMEOUTED_STATUS = "Timeouted";
    public static final String INTERNALLY_TERMINATED_STATUS = "InternallyTerminated";

    private Integer id;

    private String createdBy;

    private Date createdTime;

    private String updatedBy;

    private Date updatedTime;

    private String oper;

    private String operGrp;

    private Integer rev;

    private String status;

    private String procDefId;

    private String procDefKey;

    private String procDefName;

    private String procInstKernelId;

    private String procInstKey;

    public String getProcInstKey() {
        return procInstKey;
    }

    public void setProcInstKey(String procInstKey) {
        this.procInstKey = procInstKey;
    }

    public String getProcInstKernelId() {
        return procInstKernelId;
    }

    public void setProcInstKernelId(String procInstKernelId) {
        this.procInstKernelId = procInstKernelId;
    }

    public String getProcDefId() {
        return procDefId;
    }

    public void setProcDefId(String procDefId) {
        this.procDefId = procDefId;
    }

    public String getProcDefKey() {
        return procDefKey;
    }

    public void setProcDefKey(String procDefKey) {
        this.procDefKey = procDefKey;
    }

    public String getProcDefName() {
        return procDefName;
    }

    public void setProcDefName(String procDefName) {
        this.procDefName = procDefName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

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

    public String getOper() {
        return oper;
    }

    public void setOper(String oper) {
        this.oper = oper;
    }

    public String getOperGrp() {
        return operGrp;
    }

    public void setOperGrp(String operGrp) {
        this.operGrp = operGrp;
    }

    public Integer getRev() {
        return rev;
    }

    public void setRev(Integer rev) {
        this.rev = rev;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
