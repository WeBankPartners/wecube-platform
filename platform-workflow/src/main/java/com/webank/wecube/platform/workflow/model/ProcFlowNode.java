package com.webank.wecube.platform.workflow.model;

import java.util.ArrayList;
import java.util.List;

public class ProcFlowNode {
    private String id;
    private String nodeType;
    private String nodeName;

    private List<ProcFlowNode> previousFlowNodes = new ArrayList<>();
    private List<ProcFlowNode> succeedingFlowNodes = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public List<ProcFlowNode> getPreviousFlowNodes() {
        return previousFlowNodes;
    }

    public void setPreviousFlowNodes(List<ProcFlowNode> previousFlowNodes) {
        this.previousFlowNodes = previousFlowNodes;
    }

    public List<ProcFlowNode> getSucceedingFlowNodes() {
        return succeedingFlowNodes;
    }

    public void setSucceedingFlowNodes(List<ProcFlowNode> succeedingFlowNodes) {
        this.succeedingFlowNodes = succeedingFlowNodes;
    }

    public void addPreviousFlowNodes(ProcFlowNode... flowNodes) {
        if (this.previousFlowNodes == null) {
            this.previousFlowNodes = new ArrayList<>();
        }
        for (ProcFlowNode f : flowNodes) {
            if (f == null) {
                continue;
            }
            if (!this.previousFlowNodes.contains(f)) {
                this.previousFlowNodes.add(f);
            }

            if (f.getSucceedingFlowNodes() != null && !f.getSucceedingFlowNodes().contains(this)) {
                f.addSucceedingFlowNodes(this);
            }
        }
    }

    public void addSucceedingFlowNodes(ProcFlowNode... flowNodes) {
        if (this.succeedingFlowNodes == null) {
            this.succeedingFlowNodes = new ArrayList<>();
        }

        for (ProcFlowNode f : flowNodes) {
            if (f == null) {
                continue;
            }

            if (!this.succeedingFlowNodes.contains(f)) {
                this.succeedingFlowNodes.add(f);
            }

            if (f.getPreviousFlowNodes() != null && !f.getPreviousFlowNodes().contains(this)) {
                f.addPreviousFlowNodes(this);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder previousFlowNodesSb = new StringBuilder();
        previousFlowNodes.forEach(n -> {
            previousFlowNodesSb.append(n.getId()).append(" ");
        });
        StringBuilder succeedingFlowNodesSb = new StringBuilder();
        succeedingFlowNodes.forEach(n -> {
            succeedingFlowNodesSb.append(n.getId()).append(" ");
        });
        return "ProcFlowNode [id=" + id + ", nodeType=" + nodeType + ", nodeName=" + nodeName + ", previousFlowNodes="
                + previousFlowNodesSb.toString() + ", succeedingFlowNodes=" + succeedingFlowNodesSb.toString() + "]";
    }

}
