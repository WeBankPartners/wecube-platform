package com.webank.wecube.platform.core.entity.workflow;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class ProcInstInfoQueryEntity {
    @Id
    private Integer id;
    @Column(name = "PROC_INST_KEY")
    private String procInstKey;
    @Column(name = "PROC_DEF_NAME")
    private String procDefName;
    @Column(name = "CREATED_TIME")
    private Date createdTime;
    @Column(name = "OPER")
    private String operator;
    @Column(name = "STATUS")
    private String status;
    @Column(name = "PROC_DEF_ID")
    private String procDefId;

    @Column(name = "ENTITY_TYPE_ID")
    private String entityTypeId;
    @Column(name = "ENTITY_DATA_ID")
    private String entityDataId;
    @Column(name = "ENTITY_DATA_NAME")
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
