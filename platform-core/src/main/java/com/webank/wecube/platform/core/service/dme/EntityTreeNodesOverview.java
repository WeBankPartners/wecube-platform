package com.webank.wecube.platform.core.service.dme;

import java.util.ArrayList;
import java.util.List;

public class EntityTreeNodesOverview {
	private List<StandardEntityDataNode> hierarchicalEntityNodes = new ArrayList<>();
	private List<StandardEntityDataNode> leafNodeEntityNodes = new ArrayList<>();

	public List<StandardEntityDataNode> getHierarchicalEntityNodes() {
		return hierarchicalEntityNodes;
	}

	public void setHierarchicalEntityNodes(List<StandardEntityDataNode> hierarchicalEntityNodes) {
		this.hierarchicalEntityNodes = hierarchicalEntityNodes;
	}

	public List<StandardEntityDataNode> getLeafNodeEntityNodes() {
		return leafNodeEntityNodes;
	}

	public void setLeafNodeEntityNodes(List<StandardEntityDataNode> leafNodeEntityNodes) {
		this.leafNodeEntityNodes = leafNodeEntityNodes;
	}

	public void addLeafNodeEntityNodes(StandardEntityDataNode... leafNodeEntityNodes) {
		for (StandardEntityDataNode tn : leafNodeEntityNodes) {
			this.leafNodeEntityNodes.add(tn);
		}
	}

	public void addHierarchicalEntityNodes(StandardEntityDataNode... hierarchicalEntityNodes) {
		for (StandardEntityDataNode tn : hierarchicalEntityNodes) {
			this.hierarchicalEntityNodes.add(tn);
		}
	}
}
