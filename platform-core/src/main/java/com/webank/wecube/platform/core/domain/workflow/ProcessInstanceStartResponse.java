package com.webank.wecube.platform.core.domain.workflow;

public class ProcessInstanceStartResponse {
	private String processDefinitionId;
	private String processInstanceId;
	private String processExecutionId;

	private String businessKey;

	private String processDefinitionKey;
	private int processDefinitionVersion;

	public String getProcessInstanceId() {
		return processInstanceId;
	}

	public void setProcessInstanceId(String processInstanceId) {
		this.processInstanceId = processInstanceId;
	}

	public String getProcessDefinitionId() {
		return processDefinitionId;
	}

	public void setProcessDefinitionId(String processDefinitionId) {
		this.processDefinitionId = processDefinitionId;
	}

	public String getProcessExecutionId() {
		return processExecutionId;
	}

	public void setProcessExecutionId(String processExecutionId) {
		this.processExecutionId = processExecutionId;
	}

	public String getBusinessKey() {
		return businessKey;
	}

	public void setBusinessKey(String businessKey) {
		this.businessKey = businessKey;
	}

	public String getProcessDefinitionKey() {
		return processDefinitionKey;
	}

	public void setProcessDefinitionKey(String processDefinitionKey) {
		this.processDefinitionKey = processDefinitionKey;
	}

	public int getProcessDefinitionVersion() {
		return processDefinitionVersion;
	}

	public void setProcessDefinitionVersion(int processDefinitionVersion) {
		this.processDefinitionVersion = processDefinitionVersion;
	}

}
