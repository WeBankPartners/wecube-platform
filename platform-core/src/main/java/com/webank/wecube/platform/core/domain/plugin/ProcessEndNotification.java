package com.webank.wecube.platform.core.domain.plugin;

public class ProcessEndNotification {
    private String processDefinitionId;
    private String processDefinitionKey;
    private Integer processDefinitionVersion;

    private String processInstanceId;
    private String processInstanceBizKey;

    private String processExecutionId;

    private String processEndNodeId;
    private String processEndNodeName;

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

    public Integer getProcessDefinitionVersion() {
        return processDefinitionVersion;
    }

    public void setProcessDefinitionVersion(Integer processDefinitionVersion) {
        this.processDefinitionVersion = processDefinitionVersion;
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

    public String getProcessEndNodeId() {
        return processEndNodeId;
    }

    public void setProcessEndNodeId(String processEndNodeId) {
        this.processEndNodeId = processEndNodeId;
    }

    public String getProcessEndNodeName() {
        return processEndNodeName;
    }

    public void setProcessEndNodeName(String processEndNodeName) {
        this.processEndNodeName = processEndNodeName;
    }
}
