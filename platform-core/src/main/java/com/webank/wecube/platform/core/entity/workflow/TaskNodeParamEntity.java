package com.webank.wecube.platform.core.entity.workflow;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "CORE_RE_TASK_NODE_PARAM")
public class TaskNodeParamEntity extends BaseStatusFeaturedEntity {

    public static final String DRAFT_STATUS = "draft";
    public static final String DEPLOYED_STATUS = "deployed";
    public static final String PREDEPLOY_STATUS = "predeploy";

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

    @Column(name = "PARAM_EXP")
    private String paramExpression;

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

    public String getParamExpression() {
        return paramExpression;
    }

    public void setParamExpression(String paramExpression) {
        this.paramExpression = paramExpression;
    }

    public String getTaskNodeDefId() {
        return taskNodeDefId;
    }

    public void setTaskNodeDefId(String taskNodeDefId) {
        this.taskNodeDefId = taskNodeDefId;
    }

}
