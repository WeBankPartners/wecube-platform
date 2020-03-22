package com.webank.wecube.platform.core.dto.workflow;

import java.util.ArrayList;
import java.util.List;

public class ProcessDataPreviewDto {
	private List<GraphNodeDto> entityTreeNodes = new ArrayList<>();
	private String processSessionId;

	public List<GraphNodeDto> getEntityTreeNodes() {
		return entityTreeNodes;
	}

	public void setEntityTreeNodes(List<GraphNodeDto> entityTreeNodes) {
		this.entityTreeNodes = entityTreeNodes;
	}

	public void addAllEntityTreeNodes(List<GraphNodeDto> entityTreeNodes) {
		if (entityTreeNodes == null) {
			return;
		}
		this.entityTreeNodes.addAll(entityTreeNodes);
	}

	public String getProcessSessionId() {
		return processSessionId;
	}

	public void setProcessSessionId(String processSessionId) {
		this.processSessionId = processSessionId;
	}

}
