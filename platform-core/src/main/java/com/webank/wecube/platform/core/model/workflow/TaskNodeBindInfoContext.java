package com.webank.wecube.platform.core.model.workflow;

import java.io.Serializable;

public class TaskNodeBindInfoContext implements Serializable{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private String nodeId;// Physical node id from BPMN.
    private String nodeDefId;// Node record id from platform database.
    private String oid;
    private String entityDataId;

    private String bindFlag;

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeDefId() {
        return nodeDefId;
    }

    public void setNodeDefId(String nodeDefId) {
        this.nodeDefId = nodeDefId;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getEntityDataId() {
        return entityDataId;
    }

    public void setEntityDataId(String entityDataId) {
        this.entityDataId = entityDataId;
    }

    public String getBindFlag() {
        return bindFlag;
    }

    public void setBindFlag(String bindFlag) {
        this.bindFlag = bindFlag;
    }

}
