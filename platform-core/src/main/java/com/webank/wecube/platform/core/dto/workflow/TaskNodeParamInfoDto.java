package com.webank.wecube.platform.core.dto.workflow;

public class TaskNodeParamInfoDto {
    private String id;
    private String nodeId;

    private String paramName;
    private String paramExpression;

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

    public String getParamExpression() {
        return paramExpression;
    }

    public void setParamExpression(String paramExpression) {
        this.paramExpression = paramExpression;
    }

    @Override
    public String toString() {
        return "TaskNodeParamInfoDto [id=" + id + ", nodeId=" + nodeId + ", paramName=" + paramName
                + ", paramExpression=" + paramExpression + "]";
    }

    
}
