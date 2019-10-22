package com.webank.wecube.platform.core.domain.workflow;

public class ServiceTaskBindInfoVO {
	private String id;
	private String processDefinitionKey;
	private Integer version;
	private String nodeId;
	private String nodeName;
	private String serviceId;
	private String serviceName;

	private String ciRoutineExp;
	private String ciRoutineRaw;

	private String description;
	
	private String timeoutExpression;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getProcessDefinitionKey() {
		return processDefinitionKey;
	}

	public void setProcessDefinitionKey(String processDefinitionKey) {
		this.processDefinitionKey = processDefinitionKey;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
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

	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getCiRoutineExp() {
		return ciRoutineExp;
	}

	public void setCiRoutineExp(String ciRoutineExp) {
		this.ciRoutineExp = ciRoutineExp;
	}

	public String getCiRoutineRaw() {
		return ciRoutineRaw;
	}

	public void setCiRoutineRaw(String ciRoutineRaw) {
		this.ciRoutineRaw = ciRoutineRaw;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

    public String getTimeoutExpression() {
        return timeoutExpression;
    }

    public void setTimeoutExpression(String timeoutExpression) {
        this.timeoutExpression = timeoutExpression;
    }
	
}
