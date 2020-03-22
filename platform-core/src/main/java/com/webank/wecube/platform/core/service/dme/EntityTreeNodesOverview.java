package com.webank.wecube.platform.core.service.dme;

import java.util.ArrayList;
import java.util.List;

public class EntityTreeNodesOverview {
	private List<TreeNode> hierarchicalEntityNodes = new ArrayList<>();
	private List<TreeNode> leafNodeEntityNodes = new ArrayList<>();

	public List<TreeNode> getHierarchicalEntityNodes() {
		return hierarchicalEntityNodes;
	}

	public void setHierarchicalEntityNodes(List<TreeNode> hierarchicalEntityNodes) {
		this.hierarchicalEntityNodes = hierarchicalEntityNodes;
	}

	public List<TreeNode> getLeafNodeEntityNodes() {
		return leafNodeEntityNodes;
	}

	public void setLeafNodeEntityNodes(List<TreeNode> leafNodeEntityNodes) {
		this.leafNodeEntityNodes = leafNodeEntityNodes;
	}

	public void addLeafNodeEntityNodes(TreeNode... leafNodeEntityNodes) {
		for (TreeNode tn : leafNodeEntityNodes) {
			this.leafNodeEntityNodes.add(tn);
		}
	}

	public void addHierarchicalEntityNodes(TreeNode... hierarchicalEntityNodes) {
		for (TreeNode tn : hierarchicalEntityNodes) {
			this.hierarchicalEntityNodes.add(tn);
		}
	}
}
