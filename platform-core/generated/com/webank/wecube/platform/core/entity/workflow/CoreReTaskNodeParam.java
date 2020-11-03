package com.webank.wecube.platform.core.entity.workflow;

import java.util.Date;

public class CoreReTaskNodeParam {
    private String id;

    private String createdBy;

    private Date createdTime;

    private String updatedBy;

    private Date updatedTime;

    private Boolean active;

    private Integer rev;

    private String status;

    private String bindNodeId;

    private String bindParamName;

    private String bindParamType;

    private String nodeId;

    private String paramName;

    private String procDefId;

    private String taskNodeDefId;

    private String bindType;

    private String bindVal;

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

    public String getBindNodeId() {
        return bindNodeId;
    }

    public void setBindNodeId(String bindNodeId) {
        this.bindNodeId = bindNodeId == null ? null : bindNodeId.trim();
    }

    public String getBindParamName() {
        return bindParamName;
    }

    public void setBindParamName(String bindParamName) {
        this.bindParamName = bindParamName == null ? null : bindParamName.trim();
    }

    public String getBindParamType() {
        return bindParamType;
    }

    public void setBindParamType(String bindParamType) {
        this.bindParamType = bindParamType == null ? null : bindParamType.trim();
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId == null ? null : nodeId.trim();
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName == null ? null : paramName.trim();
    }

    public String getProcDefId() {
        return procDefId;
    }

    public void setProcDefId(String procDefId) {
        this.procDefId = procDefId == null ? null : procDefId.trim();
    }

    public String getTaskNodeDefId() {
        return taskNodeDefId;
    }

    public void setTaskNodeDefId(String taskNodeDefId) {
        this.taskNodeDefId = taskNodeDefId == null ? null : taskNodeDefId.trim();
    }

    public String getBindType() {
        return bindType;
    }

    public void setBindType(String bindType) {
        this.bindType = bindType == null ? null : bindType.trim();
    }

    public String getBindVal() {
        return bindVal;
    }

    public void setBindVal(String bindVal) {
        this.bindVal = bindVal == null ? null : bindVal.trim();
    }
}