package com.webank.wecube.platform.workflow.model;

import java.util.ArrayList;
import java.util.List;

public class ProcDefOutline {
    private String id;
    private String procDefKey;
    private String procDefName;

    private List<ProcFlowNode> flowNodes = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProcDefKey() {
        return procDefKey;
    }

    public void setProcDefKey(String procDefKey) {
        this.procDefKey = procDefKey;
    }

    public String getProcDefName() {
        return procDefName;
    }

    public void setProcDefName(String procDefName) {
        this.procDefName = procDefName;
    }

    public List<ProcFlowNode> getFlowNodes() {
        return flowNodes;
    }

    public void setFlowNodes(List<ProcFlowNode> flowNodes) {
        this.flowNodes = flowNodes;
    }

    public ProcDefOutline addFlowNodes(ProcFlowNode... flowNodes) {
        if (this.flowNodes == null) {
            this.flowNodes = new ArrayList<>();
        }

        for (ProcFlowNode flowNode : flowNodes) {
            if (flowNode == null) {
                continue;
            }
            if (contains(flowNode.getId())) {
                continue;
            }

            this.flowNodes.add(flowNode);
        }

        return this;
    }

    public boolean contains(String nodeId) {
        for (ProcFlowNode f : this.flowNodes) {
            if (f.getId().equals(nodeId)) {
                return true;
            }
        }

        return false;
    }

    public ProcFlowNode findFlowNode(String nodeId) {
        if (this.flowNodes == null) {
            this.flowNodes = new ArrayList<>();
        }

        for (ProcFlowNode f : this.flowNodes) {
            if (f.getId().equals(nodeId)) {
                return f;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return "ProcDefOutline [id=" + id + ", procDefKey=" + procDefKey + ", procDefName=" + procDefName
                + ", flowNodes=" + flowNodes + "]";
    }

    
}
