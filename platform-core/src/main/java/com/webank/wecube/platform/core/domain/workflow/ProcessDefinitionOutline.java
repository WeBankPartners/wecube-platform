package com.webank.wecube.platform.core.domain.workflow;

import java.util.ArrayList;
import java.util.List;

public class ProcessDefinitionOutline {
	private String definitionId;
	private String defintiionKey;
	private int version;
	private String definitionName;
	
	private List<FlowNodeVO> flowNodes = new ArrayList<FlowNodeVO>();

	public String getDefinitionId() {
		return definitionId;
	}

	public void setDefinitionId(String definitionId) {
		this.definitionId = definitionId;
	}

	public String getDefintiionKey() {
		return defintiionKey;
	}

	public void setDefintiionKey(String defintiionKey) {
		this.defintiionKey = defintiionKey;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getDefinitionName() {
		return definitionName;
	}

	public void setDefinitionName(String definitionName) {
		this.definitionName = definitionName;
	}

	public List<FlowNodeVO> getFlowNodes() {
		return flowNodes;
	}

	public void setFlowNodes(List<FlowNodeVO> flowNodes) {
		this.flowNodes = flowNodes;
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
}
