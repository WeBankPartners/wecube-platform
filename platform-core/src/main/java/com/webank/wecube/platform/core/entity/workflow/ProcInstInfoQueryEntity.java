package com.webank.wecube.platform.core.entity.workflow;

import java.util.Date;

public class ProcInstInfoQueryEntity {
    private Integer id;
    private String procInstKey;
    private String procDefName;
    private Date createdTime;
    private String operator;
    private String status;
    private String procDefId;

    private String entityTypeId;
    private String entityDataId;
    private String entityDataName;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getProcInstKey() {
        return procInstKey;
    }

    public void setProcInstKey(String procInstKey) {
        this.procInstKey = procInstKey;
    }

    public String getProcDefName() {
        return procDefName;
    }

    public void setProcDefName(String procDefName) {
        this.procDefName = procDefName;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProcDefId() {
        return procDefId;
    }

    public void setProcDefId(String procDefId) {
        this.procDefId = procDefId;
    }

    public String getEntityTypeId() {
        return entityTypeId;
    }

    public void setEntityTypeId(String entityTypeId) {
        this.entityTypeId = entityTypeId;
    }

    public String getEntityDataId() {
        return entityDataId;
    }

    public void setEntityDataId(String entityDataId) {
        this.entityDataId = entityDataId;
    }

    public String getEntityDataName() {
        return entityDataName;
    }

    public void setEntityDataName(String entityDataName) {
        this.entityDataName = entityDataName;
    }

}
