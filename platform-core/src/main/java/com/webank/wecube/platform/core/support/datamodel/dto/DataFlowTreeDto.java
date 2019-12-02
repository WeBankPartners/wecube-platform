package com.webank.wecube.platform.core.support.datamodel.dto;

import java.util.ArrayList;
import java.util.List;

public class DataFlowTreeDto {
    private TreeNode treeNode;
    private List<TreeNode> anchorTreeNodeList = new ArrayList<>(); // stands for latest tree's most bottom leaves

    public TreeNode getTreeNode() {
        return treeNode;
    }

    public void setTreeNode(TreeNode treeNode) {
        this.treeNode = treeNode;
    }

    public List<TreeNode> getAnchorTreeNodeList() {
        return anchorTreeNodeList;
    }

    public void setAnchorTreeNodeList(List<TreeNode> anchorTreeNodeList) {
        this.anchorTreeNodeList = anchorTreeNodeList;
    }
}
