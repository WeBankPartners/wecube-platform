package com.webank.wecube.platform.core.domain.workflow;

public class ProcessTaskVO {

	private Integer id;
	private String operator;
	private String operatorGroup;
	private String processDefinitionId;
	private String processDefinitionKey;
	private Integer processDefinitionVersion;

	private String processInstanceId;
	private String processInstanceKey;
	private Integer rootCiTypeId;
	private String rootCiDataId;
	private String status;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getOperatorGroup() {
		return operatorGroup;
	}

	public void setOperatorGroup(String operatorGroup) {
		this.operatorGroup = operatorGroup;
	}

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

	public String getProcessInstanceKey() {
		return processInstanceKey;
	}

	public void setProcessInstanceKey(String processInstanceKey) {
		this.processInstanceKey = processInstanceKey;
	}

	public Integer getRootCiTypeId() {
		return rootCiTypeId;
	}

	public void setRootCiTypeId(Integer rootCiTypeId) {
		this.rootCiTypeId = rootCiTypeId;
	}

	public String getRootCiDataId() {
		return rootCiDataId;
	}

	public void setRootCiDataId(String rootCiDataId) {
		this.rootCiDataId = rootCiDataId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
