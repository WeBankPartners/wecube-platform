package com.webank.wecube.platform.core.utils;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.*;

public class TreeNode {
    String name;

    @JsonBackReference
    TreeNode parent;

    @JsonManagedReference
    List<TreeNode> children;

    public TreeNode() {
    }

    public TreeNode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public void addChild(TreeNode child) {
        if (children == null)
            children = new ArrayList<TreeNode>();
        children.add(child);
    }
}
