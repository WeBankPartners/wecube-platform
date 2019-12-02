package com.webank.wecube.platform.core.support.datamodel;

import com.webank.wecube.platform.core.model.datamodel.DataModelExpressionToRootData;

import java.util.ArrayList;
import java.util.List;

public class ChainRequestDto {
    private String requestActualUrl = "";
    private TreeNode treeNode;
    private List<TreeNode> anchorTreeNodeList = new ArrayList<>(); // stands for latest tree's most bottom leaves
    private DataModelExpressionToRootData dataModelExpressionToRootData;

    public ChainRequestDto(DataModelExpressionToRootData dataModelExpressionToRootData) {
        this.dataModelExpressionToRootData = dataModelExpressionToRootData;
    }

    public String getRequestActualUrl() {
        return requestActualUrl;
    }

    public void setRequestActualUrl(String requestActualUrl) {
        this.requestActualUrl = requestActualUrl;
    }

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

    public DataModelExpressionToRootData getDataModelExpressionToRootData() {
        return dataModelExpressionToRootData;
    }

    public void setDataModelExpressionToRootData(DataModelExpressionToRootData dataModelExpressionToRootData) {
        this.dataModelExpressionToRootData = dataModelExpressionToRootData;
    }
}
