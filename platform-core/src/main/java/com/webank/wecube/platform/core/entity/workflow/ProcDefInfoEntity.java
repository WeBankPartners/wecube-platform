package com.webank.wecube.platform.core.entity.workflow;

import java.util.Date;

public class ProcDefInfoEntity {

    public static final String DRAFT_STATUS = "draft";
    public static final String DEPLOYED_STATUS = "deployed";
    public static final String DELETED_STATUS = "deleted";
    public static final String TEMPLATE_STATUS = "template";
    public static final String PREDEPLOY_STATUS = "predeploy";

    public static final String PROC_DATA_FORMAT_XML = "xml";
    public static final String PROC_DATA_FORMAT_JSON = "json";

    public static final String DYNAMIC_BIND_YES = "Y";
    public static final String DYNAMIC_BIND_NO = "N";

    public static final String EXCLUDE_MODE_YES = "Y";
    public static final String EXCLUDE_MODE_NO = "N";

    private String id;

    private String createdBy;

    private Date createdTime;

    private String updatedBy;

    private Date updatedTime;

    private Boolean active = true;

    private Integer rev;

    private String status;

    private String procDefDataFmt;

    private String procDefKernelId;

    private String procDefKey;

    private String procDefName;

    private Integer procDefVer;

    private String rootEntity;

    private Boolean isDeleted = false;

    private String owner;

    private String ownerGrp;

    private String procDefData;

    private String excludeMode;
    
    private String tags;

    public String getId() {
        return id;
    }

    public void setId(String id) {
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
        this.status = status;
    }

    public String getProcDefDataFmt() {
        return procDefDataFmt;
    }

    public void setProcDefDataFmt(String procDefDataFmt) {
        this.procDefDataFmt = procDefDataFmt;
    }

    public String getProcDefKernelId() {
        return procDefKernelId;
    }

    public void setProcDefKernelId(String procDefKernelId) {
        this.procDefKernelId = procDefKernelId;
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
        this.rootEntity = rootEntity;
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
        this.owner = owner;
    }

    public String getOwnerGrp() {
        return ownerGrp;
    }

    public void setOwnerGrp(String ownerGrp) {
        this.ownerGrp = ownerGrp;
    }

    public String getProcDefData() {
        return procDefData;
    }

    public void setProcDefData(String procDefData) {
        this.procDefData = procDefData;
    }

    public String getExcludeMode() {
        return excludeMode;
    }

    public void setExcludeMode(String excludeMode) {
        this.excludeMode = excludeMode;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }
    
    

}
