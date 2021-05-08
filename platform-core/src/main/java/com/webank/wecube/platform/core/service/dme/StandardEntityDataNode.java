package com.webank.wecube.platform.core.service.dme;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class StandardEntityDataNode {
    private String packageName;
    private String entityName;
    private String id;
    private String displayName;
    private String fullId;
    private StandardEntityDataNode parent;
    private List<StandardEntityDataNode> children = new ArrayList<>();

    public StandardEntityDataNode() {
    }

    public StandardEntityDataNode(String packageName, String entityName, String id, String displayName,
            StandardEntityDataNode parent, List<StandardEntityDataNode> children) {
        this.packageName = packageName;
        this.entityName = entityName;
        this.id = id;
        this.displayName = displayName;
        this.parent = parent;
        this.children = children;
    }

    public StandardEntityDataNode(String packageName, String entityName, String rootId) {
        this.packageName = packageName;
        this.entityName = entityName;
        this.id = rootId;
    }

    public StandardEntityDataNode(String packageName, String entityName, String rootId, String displayName) {
        this.packageName = packageName;
        this.entityName = entityName;
        this.id = rootId;
        this.displayName = displayName;
    }

    public String getFullId() {
        if (StringUtils.isNoneBlank(this.fullId)) {
            return this.fullId;
        }

        if (parent == null) {
            return id;
        }

        return String.format("%s::%s", parent.getFullId(), id);
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

    public String getId() {
        return id;
    }

    public void setId(String rootId) {
        this.id = rootId;
    }

    public StandardEntityDataNode getParent() {
        return parent;
    }

    public void setParent(StandardEntityDataNode parent) {
        this.parent = parent;
    }

    public List<StandardEntityDataNode> getChildren() {
        return children;
    }

    public void setChildren(List<StandardEntityDataNode> children) {
        this.children = children;
    }

    public void addChildren(StandardEntityDataNode node) {
        if (node == null) {
            return;
        }

        for (StandardEntityDataNode n : children) {
            if (n.equals(node)) {
                return;
            }
        }
        this.children.add(node);
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
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

        StandardEntityDataNode treeNode = (StandardEntityDataNode) o;

        return new EqualsBuilder().append(getPackageName(), treeNode.getPackageName())
                .append(getEntityName(), treeNode.getEntityName()).append(getId(), treeNode.getId()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getPackageName()).append(getEntityName()).append(getId())
                .toHashCode();
    }

    public void setFullId(String fullId) {
        this.fullId = fullId;
    }
}
