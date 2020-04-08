package com.webank.wecube.platform.core.entity.workflow;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import com.webank.wecube.platform.core.entity.BaseTraceableEntity;

@Entity
@Table(name = "CORE_RU_PROC_EXEC_BINDING")
public class ProcExecBindingEntity extends BaseTraceableEntity {

    public static final String BIND_TYPE_PROC_INSTANCE = "process";
    public static final String BIND_TYPE_TASK_NODE_INSTANCE = "taskNode";

    @Id
    @Column(name = "ID")
    @GeneratedValue
    private Integer id;

    @Column(name = "PROC_DEF_ID")
    private String procDefId;

    @Column(name = "NODE_DEF_ID")
    private String nodeDefId;

    @Column(name = "PROC_INST_ID")
    private Integer procInstId;

    @Column(name = "TASK_NODE_INST_ID")
    private Integer taskNodeInstId;

    @Column(name = "BIND_TYPE")
    private String bindType;

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

    public Integer getProcInstId() {
        return procInstId;
    }

    public void setProcInstId(Integer procInstId) {
        this.procInstId = procInstId;
    }

    public Integer getTaskNodeInstId() {
        return taskNodeInstId;
    }

    public void setTaskNodeInstId(Integer taskNodeInstId) {
        this.taskNodeInstId = taskNodeInstId;
    }

    public String getBindType() {
        return bindType;
    }

    public void setBindType(String bindType) {
        this.bindType = bindType;
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

    public String getProcDefId() {
        return procDefId;
    }

    public void setProcDefId(String procDefId) {
        this.procDefId = procDefId;
    }

    public String getNodeDefId() {
        return nodeDefId;
    }

    public void setNodeDefId(String nodeDefId) {
        this.nodeDefId = nodeDefId;
    }

	public String getEntityDataName() {
		return entityDataName;
	}

	public void setEntityDataName(String entityDataName) {
		this.entityDataName = entityDataName;
	}
}
