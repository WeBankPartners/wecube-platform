package com.webank.wecube.platform.core.dto.workflow;

public class TaskNodeDefParamDto {
    private String id;
    private String nodeId;

    private String paramName;
    private String bindNodeId;
    private String bindParamType; // Input,Output
    private String bindParamName;
    
    private String bindType; //context,constant
    private String bindValue;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    @Override
    public String toString() {
        return "TaskNodeDefParamDto [id=" + id + ", nodeId=" + nodeId + ", paramName=" + paramName + ", bindNodeId="
                + bindNodeId + ", bindParamType=" + bindParamType + ", bindParamName=" + bindParamName + "]";
    }

}
