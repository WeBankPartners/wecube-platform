package com.webank.wecube.platform.core.domain.plugin;

public class PluginTriggerCommand {
    private String processDefinitionId;
    private String processDefinitionKey;
    private Integer processDefinitionVersion;

    private String processInstanceId;
    private String processInstanceBizKey;

    private String processExecutionId;

    private String serviceTaskNodeId;
    private String serviceTaskNodeName;

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    public void setProcessDefinitionKey(String processDefinitionKey) {
        this.processDefinitionKey = processDefinitionKey;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getProcessInstanceBizKey() {
        return processInstanceBizKey;
    }

    public void setProcessInstanceBizKey(String processInstanceBizKey) {
        this.processInstanceBizKey = processInstanceBizKey;
    }

    public String getProcessExecutionId() {
        return processExecutionId;
    }

    public void setProcessExecutionId(String processExecutionId) {
        this.processExecutionId = processExecutionId;
    }

    public String getServiceTaskNodeId() {
        return serviceTaskNodeId;
    }

    public void setServiceTaskNodeId(String serviceTaskNodeId) {
        this.serviceTaskNodeId = serviceTaskNodeId;
    }

    public String getServiceTaskNodeName() {
        return serviceTaskNodeName;
    }

    public void setServiceTaskNodeName(String serviceTaskNodeName) {
        this.serviceTaskNodeName = serviceTaskNodeName;
    }

    public Integer getProcessDefinitionVersion() {
        return processDefinitionVersion;
    }

    public void setProcessDefinitionVersion(Integer processDefinitionVersion) {
        this.processDefinitionVersion = processDefinitionVersion;
    }

    @Override
    public String toString() {
        return "PluginTriggerCommand [processDefinitionId=" + processDefinitionId + ", processDefinitionKey="
                + processDefinitionKey + ", processInstanceId=" + processInstanceId + ", processInstanceBizKey="
                + processInstanceBizKey + ", processExecutionId=" + processExecutionId + ", serviceTaskNodeId="
                + serviceTaskNodeId + ", serviceTaskNodeName=" + serviceTaskNodeName + "]";
    }

}
