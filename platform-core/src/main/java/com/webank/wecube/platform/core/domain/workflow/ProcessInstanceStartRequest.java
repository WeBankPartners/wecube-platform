package com.webank.wecube.platform.core.domain.workflow;

public class ProcessInstanceStartRequest {
	private String processDefinitionId;
	private String comment;
	
	private String processDefinitionKey;

	public String getProcessDefinitionId() {
		return processDefinitionId;
	}

	public void setProcessDefinitionId(String processDefinitionId) {
		this.processDefinitionId = processDefinitionId;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public String getProcessDefinitionKey() {
		return processDefinitionKey;
	}

	public void setProcessDefinitionKey(String processDefinitionKey) {
		this.processDefinitionKey = processDefinitionKey;
	}

	@Override
	public String toString() {
		return "ProcessInstanceStartRequest [processDefinitionId=" + processDefinitionId + ", comment=" + comment + "]";
	}

}
