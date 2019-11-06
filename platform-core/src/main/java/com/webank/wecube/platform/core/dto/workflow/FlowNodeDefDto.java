package com.webank.wecube.platform.core.dto.workflow;

import java.util.ArrayList;
import java.util.List;

public class FlowNodeDefDto extends BaseNodeDefDto {
    private String nodeDefId;
    private String status;
    private String orderedNo;

    private String procDefId;
    private String procDefKey;

    private List<String> previousNodeIds = new ArrayList<>();
    private List<String> succeedingNodeIds = new ArrayList<>();
    
    public String getNodeDefId() {
        return nodeDefId;
    }

    public void setNodeDefId(String nodeDefId) {
        this.nodeDefId = nodeDefId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOrderedNo() {
        return orderedNo;
    }

    public void setOrderedNo(String orderedNo) {
        this.orderedNo = orderedNo;
    }

    public String getProcDefId() {
        return procDefId;
    }

    public void setProcDefId(String procDefId) {
        this.procDefId = procDefId;
    }

    public String getProcDefKey() {
        return procDefKey;
    }

    public void setProcDefKey(String procDefKey) {
        this.procDefKey = procDefKey;
    }

    public List<String> getPreviousNodeIds() {
        return previousNodeIds;
    }

    public void setPreviousNodeIds(List<String> previousNodeIds) {
        this.previousNodeIds = previousNodeIds;
    }

    public List<String> getSucceedingNodeIds() {
        return succeedingNodeIds;
    }

    public void setSucceedingNodeIds(List<String> succeedingNodeIds) {
        this.succeedingNodeIds = succeedingNodeIds;
    }

    public void addSucceedingNodes(FlowNodeDefDto... nodes) {
        if (this.succeedingNodeIds == null) {
            this.succeedingNodeIds = new ArrayList<>();
        }

        for (FlowNodeDefDto n : nodes) {
            if (n == null) {
                continue;
            }

            if (!this.succeedingNodeIds.contains(n.getNodeId())) {
                this.succeedingNodeIds.add(n.getNodeId());
            }
        }
    }

    public void addPreviousNodes(FlowNodeDefDto... nodes) {
        if (this.previousNodeIds == null) {
            this.previousNodeIds = new ArrayList<>();
        }

        for (FlowNodeDefDto n : nodes) {
            if (n == null) {
                continue;
            }

            if (!this.previousNodeIds.contains(n.getNodeId())) {
                this.previousNodeIds.add(n.getNodeId());
            }
        }
    }

    public void addSucceedingNodeIds(String... nodeIds) {
        if (this.succeedingNodeIds == null) {
            this.succeedingNodeIds = new ArrayList<>();
        }

        for (String nodeId : nodeIds) {
            if (!this.succeedingNodeIds.contains(nodeId)) {
                this.succeedingNodeIds.add(nodeId);
            }
        }
    }

    public void addPreviousNodeIds(String... nodeIds) {
        if (this.previousNodeIds == null) {
            this.previousNodeIds = new ArrayList<>();
        }

        for (String nodeId : nodeIds) {
            if (!this.previousNodeIds.contains(nodeId)) {
                this.previousNodeIds.add(nodeId);
            }
        }
    }

    @Override
    public String toString() {
        return "FlowNodeDefDto [procDefId=" + procDefId + ", procDefKey=" + procDefKey + ", previousNodeIds="
                + previousNodeIds + ", succeedingNodeIds=" + succeedingNodeIds + ", getNodeId()=" + getNodeId()
                + ", getNodeName()=" + getNodeName() + ", getNodeType()=" + getNodeType() + "]";
    }

}
