package com.webank.wecube.platform.core.entity.workflow;

import java.util.Date;

public class TaskNodeParamEntity {

    public static final String DRAFT_STATUS = "draft";
    public static final String DEPLOYED_STATUS = "deployed";
    public static final String PREDEPLOY_STATUS = "predeploy";

    public static final String BIND_TYPE_CONTEXT = "context";
    public static final String BIND_TYPE_CONSTANT = "constant";

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

    public String getBindNodeId() {
        return bindNodeId;
    }

    public void setBindNodeId(String bindNodeId) {
        this.bindNodeId = bindNodeId;
    }

    public String getBindParamName() {
        return bindParamName;
    }

    public void setBindParamName(String bindParamName) {
        this.bindParamName = bindParamName;
    }

    public String getBindParamType() {
        return bindParamType;
    }

    public void setBindParamType(String bindParamType) {
        this.bindParamType = bindParamType;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public String getProcDefId() {
        return procDefId;
    }

    public void setProcDefId(String procDefId) {
        this.procDefId = procDefId;
    }

    public String getTaskNodeDefId() {
        return taskNodeDefId;
    }

    public void setTaskNodeDefId(String taskNodeDefId) {
        this.taskNodeDefId = taskNodeDefId;
    }

    public String getBindType() {
        return bindType;
    }

    public void setBindType(String bindType) {
        this.bindType = bindType;
    }

    public String getBindVal() {
        return bindVal;
    }

    public void setBindVal(String bindVal) {
        this.bindVal = bindVal;
    }

}
