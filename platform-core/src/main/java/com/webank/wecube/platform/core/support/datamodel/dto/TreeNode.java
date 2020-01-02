package com.webank.wecube.platform.core.support.datamodel.dto;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;

public class TreeNode {
    private String packageName;
    private String entityName;
    private Object rootId;
    private Object displayName;
    private TreeNode parent;
    private List<TreeNode> children;

    public TreeNode() {
    }

    public TreeNode(String packageName, String entityName, Object rootId, Object displayName, TreeNode parent, List<TreeNode> children) {
        this.packageName = packageName;
        this.entityName = entityName;
        this.rootId = rootId;
        this.displayName = displayName;
        this.parent = parent;
        this.children = children;
    }

    public TreeNode(String packageName, String entityName, Object rootId) {
        this.packageName = packageName;
        this.entityName = entityName;
        this.rootId = rootId;
    }

    public TreeNode(String packageName, String entityName, Object rootId, Object displayName) {
        this.packageName = packageName;
        this.entityName = entityName;
        this.rootId = rootId;
        this.displayName = displayName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public Object getRootId() {
        return rootId;
    }

    public void setRootId(Object rootId) {
        this.rootId = rootId;
    }

    public TreeNode getParent() {
        return parent;
    }

    public void setParent(TreeNode parent) {
        this.parent = parent;
    }

    public List<TreeNode> getChildren() {
        return children;
    }

    public void setChildren(List<TreeNode> children) {
        this.children = children;
    }

    public Object getDisplayName() {
        return displayName;
    }

    public void setDisplayName(Object displayName) {
        this.displayName = displayName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TreeNode treeNode = (TreeNode) o;

        return new EqualsBuilder()
                .append(getPackageName(), treeNode.getPackageName())
                .append(getEntityName(), treeNode.getEntityName())
                .append(getRootId(), treeNode.getRootId())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getPackageName())
                .append(getEntityName())
                .append(getRootId())
                .toHashCode();
    }
}
