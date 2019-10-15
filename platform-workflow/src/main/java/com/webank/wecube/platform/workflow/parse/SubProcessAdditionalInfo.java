package com.webank.wecube.platform.workflow.parse;

public class SubProcessAdditionalInfo {
    private String subProcessNodeId;
    private String subProcessNodeName;
    private String timeoutExpression;

    public String getSubProcessNodeId() {
        return subProcessNodeId;
    }

    public void setSubProcessNodeId(String subProcessNodeId) {
        this.subProcessNodeId = subProcessNodeId;
    }

    public String getTimeoutExpression() {
        return timeoutExpression;
    }

    public void setTimeoutExpression(String timeoutExpression) {
        this.timeoutExpression = timeoutExpression;
    }
        
    public String getSubProcessNodeName() {
        return subProcessNodeName;
    }

    public void setSubProcessNodeName(String subProcessNodeName) {
        this.subProcessNodeName = subProcessNodeName;
    }

    public SubProcessAdditionalInfo withSubProcessNodeId(String subProcessNodeId) {
        this.subProcessNodeId = subProcessNodeId;
        return this;
    }
    
    public SubProcessAdditionalInfo withTimeoutExpression(String timeoutExpression) {
        this.timeoutExpression = timeoutExpression;
        return this;
    }

    public SubProcessAdditionalInfo withSubProcessNodeName(String subProcessNodeName) {
        this.subProcessNodeName = subProcessNodeName;
        return this;
    } 

}
