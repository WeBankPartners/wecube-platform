package com.webank.wecube.platform.core.entity.workflow;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "CORE_RE_TASK_NODE_PARAM")
public class TaskNodeParamEntity extends AbstractStatusFeaturedEntity {

    public static final String DRAFT_STATUS = "draft";
    public static final String DEPLOYED_STATUS = "deployed";
    public static final String PREDEPLOY_STATUS = "predeploy";
    
    public static final String BIND_TYPE_CONTEXT = "context";
    public static final String BIND_TYPE_CONSTANT = "constant";
    

    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "PROC_DEF_ID")
    private String procDefId;

    @Column(name = "NODE_ID")
    private String nodeId;

    @Column(name = "TASK_NODE_DEF_ID")
    private String taskNodeDefId;

    @Column(name = "PARAM_NAME")
    private String paramName;

    @Column(name = "BIND_NODE_ID")
    private String bindNodeId;

    @Column(name = "BIND_PARAM_TYPE")
    private String bindParamType; // Input, Output

    @Column(name = "BIND_PARAM_NAME")
    private String bindParamName;
    
    @Column(name = "BIND_TYPE")
    private String bindType;
    
    @Column(name = "BIND_VAL")
    private String bindValue;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProcDefId() {
        return procDefId;
    }

    public void setProcDefId(String procDefId) {
        this.procDefId = procDefId;
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

    public String getBindNodeId() {
        return bindNodeId;
    }

    public void setBindNodeId(String bindNodeId) {
        this.bindNodeId = bindNodeId;
    }

    public String getBindParamType() {
        return bindParamType;
    }

    public void setBindParamType(String bindParamType) {
        this.bindParamType = bindParamType;
    }

    public String getBindParamName() {
        return bindParamName;
    }

    public void setBindParamName(String bindParamName) {
        this.bindParamName = bindParamName;
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

    public String getBindValue() {
        return bindValue;
    }

    public void setBindValue(String bindValue) {
        this.bindValue = bindValue;
    }
}
