package com.webank.wecube.platform.core.domain.workflow;

import java.util.ArrayList;
import java.util.List;

public class ProcessInstanceOutline {
	private String processDefinitionId;
	private String processInstanceId;
	private String executionId;
	private String status;

	private String startTime;
	private String endTime;

	private transient boolean ended;

	private List<FlowNodeVO> flowNodes = new ArrayList<FlowNodeVO>();

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

	public List<FlowNodeVO> getFlowNodes() {
		return flowNodes;
	}

	public void setFlowNodes(List<FlowNodeVO> flowNodes) {
		this.flowNodes = flowNodes;
	}

	public boolean isEnded() {
		return ended;
	}

	public void setEnded(boolean ended) {
		this.ended = ended;
	}

	public void addFlowNode(FlowNodeVO flowNode) {
		getFlowNodes().add(flowNode);
	}

	public FlowNodeVO findFlowNodeVOById(String id) {
		for (FlowNodeVO node : getFlowNodes()) {
			if (node.getId().equals(id)) {
				return node;
			}
		}

		return null;
	}

	@Override
	public String toString() {
		return "ProcessInstanceOutline [processDefinitionId=" + processDefinitionId + ", processInstanceId="
				+ processInstanceId + ", executionId=" + executionId + ", status=" + status + ", startTime=" + startTime
				+ ", endTime=" + endTime + ", flowNodes=" + flowNodes + "]";
	}

}
