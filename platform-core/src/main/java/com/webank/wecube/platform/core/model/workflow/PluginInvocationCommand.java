package com.webank.wecube.platform.core.model.workflow;

public class PluginInvocationCommand {
    private String procDefId;
    private String procDefKey;
    private Integer procDefVersion;

    private String procInstId;
    private String procInstKey;

    private String nodeId;
    private String nodeName;
    
    private String executionId;

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

    public Integer getProcDefVersion() {
        return procDefVersion;
    }

    public void setProcDefVersion(Integer procDefVersion) {
        this.procDefVersion = procDefVersion;
    }

    public String getProcInstId() {
        return procInstId;
    }

    public void setProcInstId(String procInstId) {
        this.procInstId = procInstId;
    }

    public String getProcInstKey() {
        return procInstKey;
    }

    public void setProcInstKey(String procInstKey) {
        this.procInstKey = procInstKey;
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
    
    

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    @Override
    public String toString() {
        return "PluginInvocationCommand [procDefId=" + procDefId + ", procDefKey=" + procDefKey + ", procDefVersion="
                + procDefVersion + ", procInstId=" + procInstId + ", procInstKey=" + procInstKey + ", nodeId=" + nodeId
                + ", nodeName=" + nodeName + ", executionId=" + executionId + "]";
    }

}
