package com.webank.wecube.platform.core.entity.workflow;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "CORE_RU_TASK_NODE_INST_INFO")
public class TaskNodeInstInfoEntity extends AbstractInstanceStatusEntity {

    @Column(name = "NODE_DEF_ID")
    private String nodeDefId;

    @Column(name = "NODE_ID")
    private String nodeId;

    @Column(name = "NODE_NAME")
    private String nodeName;

    @Column(name = "NODE_TYPE")
    private String nodeType;

    @Column(name = "ORDERED_NO")
    private String orderedNo;

    @Column(name = "PROC_INST_ID")
    private Integer procInstId;

    @Column(name = "PROC_INST_KEY")
    private String procInstKey;

    @Column(name = "PROC_DEF_ID")
    private String procDefId;

    @Column(name = "PROC_DEF_KEY")
    private String procDefKey;
    
    @Column(name = "ERR_MSG")
    private String errorMessage;

    public String getNodeDefId() {
        return nodeDefId;
    }

    public void setNodeDefId(String nodeDefId) {
        this.nodeDefId = nodeDefId;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public Integer getProcInstId() {
        return procInstId;
    }

    public void setProcInstId(Integer procInstId) {
        this.procInstId = procInstId;
    }

    public String getProcInstKey() {
        return procInstKey;
    }

    public void setProcInstKey(String procInstKey) {
        this.procInstKey = procInstKey;
    }

    public String getProcDefId() {
        return procDefId;
    }

    public void setProcDefId(String procDefId) {
        this.procDefId = procDefId;
    }

    public String getProcDefKey() {
        return procDefKey;
    }

    public void setProcDefKey(String procDefKey) {
        this.procDefKey = procDefKey;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getOrderedNo() {
        return orderedNo;
    }

    public void setOrderedNo(String orderedNo) {
        this.orderedNo = orderedNo;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
