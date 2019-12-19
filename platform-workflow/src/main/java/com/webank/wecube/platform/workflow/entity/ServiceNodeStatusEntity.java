package com.webank.wecube.platform.workflow.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "ACT_RU_SRVNODE_STATUS")
public class ServiceNodeStatusEntity extends AbstractTraceableEntity {

    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "PROC_INST_ID")
    private String procInstanceId;

    @Column(name = "PROC_INST_KEY")
    private String procInstanceBizKey;

    @Column(name = "NODE_ID")
    private String nodeId;

    @Column(name = "NODE_NAME")
    private String nodeName;

    @Column(name = "NODE_TYPE")
    private String nodeType;

    @Column(name = "NODE_INST_ID")
    private String nodeInstanceId;

    @Column(name = "TRY_TIMES")
    private int tryTimes;

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

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getNodeInstanceId() {
        return nodeInstanceId;
    }

    public void setNodeInstanceId(String nodeInstanceId) {
        this.nodeInstanceId = nodeInstanceId;
    }

    public int getTryTimes() {
        return tryTimes;
    }

    public void setTryTimes(int tryTimes) {
        this.tryTimes = tryTimes;
    }

}
