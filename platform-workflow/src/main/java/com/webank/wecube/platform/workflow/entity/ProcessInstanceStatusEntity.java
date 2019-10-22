package com.webank.wecube.platform.workflow.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "ACT_RU_PROCINST_STATUS")
public class ProcessInstanceStatusEntity extends AbstractTraceableEntity {

    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "PROC_INST_ID")
    private String procInstanceId;

    @Column(name = "PROC_INST_KEY")
    private String procInstanceBizKey;

    @Column(name = "PROC_DEF_ID")
    private String procDefinitionId;

    @Column(name = "PROC_DEF_KEY")
    private String procDefinitionKey;

    @Column(name = "PROC_DEF_NAME")
    private String procDefinitionName;

    

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProcInstanceId() {
        return procInstanceId;
    }

    public void setProcInstanceId(String procInstanceId) {
        this.procInstanceId = procInstanceId;
    }

    public String getProcInstanceBizKey() {
        return procInstanceBizKey;
    }

    public void setProcInstanceBizKey(String procInstanceBizKey) {
        this.procInstanceBizKey = procInstanceBizKey;
    }

    public String getProcDefinitionId() {
        return procDefinitionId;
    }

    public void setProcDefinitionId(String procDefinitionId) {
        this.procDefinitionId = procDefinitionId;
    }

    public String getProcDefinitionKey() {
        return procDefinitionKey;
    }

    public void setProcDefinitionKey(String procDefinitionKey) {
        this.procDefinitionKey = procDefinitionKey;
    }

    public String getProcDefinitionName() {
        return procDefinitionName;
    }

    public void setProcDefinitionName(String procDefinitionName) {
        this.procDefinitionName = procDefinitionName;
    }

   

}
