package com.webank.wecube.platform.core.entity.workflow;

import java.util.Date;

public class CoreReProcDefInfo {
    private String id;

    private String createdBy;

    private Date createdTime;

    private String updatedBy;

    private Date updatedTime;

    private Boolean active;

    private Integer rev;

    private String status;

    private String procDefDataFmt;

    private String procDefKernelId;

    private String procDefKey;

    private String procDefName;

    private Integer procDefVer;

    private String rootEntity;

    private Boolean isDeleted;

    private String owner;

    private String ownerGrp;

    private String procDefData;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy == null ? null : createdBy.trim();
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
        this.updatedBy = updatedBy == null ? null : updatedBy.trim();
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
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
        this.status = status == null ? null : status.trim();
    }

    public String getProcDefDataFmt() {
        return procDefDataFmt;
    }

    public void setProcDefDataFmt(String procDefDataFmt) {
        this.procDefDataFmt = procDefDataFmt == null ? null : procDefDataFmt.trim();
    }

    public String getProcDefKernelId() {
        return procDefKernelId;
    }

    public void setProcDefKernelId(String procDefKernelId) {
        this.procDefKernelId = procDefKernelId == null ? null : procDefKernelId.trim();
    }

    public String getProcDefKey() {
        return procDefKey;
    }

    public void setProcDefKey(String procDefKey) {
        this.procDefKey = procDefKey == null ? null : procDefKey.trim();
    }

    public String getProcDefName() {
        return procDefName;
    }

    public void setProcDefName(String procDefName) {
        this.procDefName = procDefName == null ? null : procDefName.trim();
    }

    public Integer getProcDefVer() {
        return procDefVer;
    }

    public void setProcDefVer(Integer procDefVer) {
        this.procDefVer = procDefVer;
    }

    public String getRootEntity() {
        return rootEntity;
    }

    public void setRootEntity(String rootEntity) {
        this.rootEntity = rootEntity == null ? null : rootEntity.trim();
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner == null ? null : owner.trim();
    }

    public String getOwnerGrp() {
        return ownerGrp;
    }

    public void setOwnerGrp(String ownerGrp) {
        this.ownerGrp = ownerGrp == null ? null : ownerGrp.trim();
    }

    public String getProcDefData() {
        return procDefData;
    }

    public void setProcDefData(String procDefData) {
        this.procDefData = procDefData == null ? null : procDefData.trim();
    }
}