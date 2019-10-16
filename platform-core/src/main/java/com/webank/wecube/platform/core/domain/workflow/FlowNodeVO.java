package com.webank.wecube.platform.core.domain.workflow;

import java.util.ArrayList;
import java.util.List;

public class FlowNodeVO {
	private String id;
	private String name;
	private String status;
	private transient String nodeTypeName;
	private String processDefinitionId;
	private String processInstanceId;
	private String executionId;

	private String startTime;
	private String endTime;

	private List<String> fromNodeIds = new ArrayList<String>();
	private List<String> toNodeIds = new ArrayList<String>();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getProcessDefinitionId() {
		return processDefinitionId;
	}

	public void setProcessDefinitionId(String processDefinitionId) {
		this.processDefinitionId = processDefinitionId;
	}

	public String getProcessInstanceId() {
		return processInstanceId;
	}

	public void setProcessInstanceId(String processInstanceId) {
		this.processInstanceId = processInstanceId;
	}

	public String getExecutionId() {
		return executionId;
	}

	public void setExecutionId(String executionId) {
		this.executionId = executionId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public List<String> getFromNodeIds() {
		return fromNodeIds;
	}

	public void setFromNodeIds(List<String> fromNodeIds) {
		this.fromNodeIds = fromNodeIds;
	}

	public List<String> getToNodeIds() {
		return toNodeIds;
	}

	public void setToNodeIds(List<String> toNodeIds) {
		this.toNodeIds = toNodeIds;
	}

	public void addFromNode(FlowNodeVO node) {
		getFromNodeIds().add(node.getId());
	}

	public void addToNode(FlowNodeVO node) {
		getToNodeIds().add(node.getId());
	}

	public String getNodeTypeName() {
		return nodeTypeName;
	}

	public void setNodeTypeName(String nodeTypeName) {
		this.nodeTypeName = nodeTypeName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "\nFlowNodeVO [id=" + id + ", name=" + name + ", status=" + status + ", nodeTypeName=" + nodeTypeName
				+ ", processDefinitionId=" + processDefinitionId + ", processInstanceId=" + processInstanceId
				+ ", executionId=" + executionId + ", startTime=" + startTime + ", endTime=" + endTime
				+ ", fromNodeIds=" + fromNodeIds + ", toNodeIds=" + toNodeIds + "]";
	}
}
