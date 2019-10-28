package com.webank.wecube.platform.workflow.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProcInstOutline {
    private String id;
    private String procInstKey;

    private String procDefKernelId;
    private String procDefKey;
    private String procDefName;

    private String status;

    private Date startTime;

    private Date endTime;

    private List<ProcFlowNodeInst> nodeInsts = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProcInstKey() {
        return procInstKey;
    }

    public void setProcInstKey(String procInstKey) {
        this.procInstKey = procInstKey;
    }

    public String getProcDefKernelId() {
        return procDefKernelId;
    }

    public void setProcDefKernelId(String procDefKernelId) {
        this.procDefKernelId = procDefKernelId;
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

    public List<ProcFlowNodeInst> getNodeInsts() {
        return nodeInsts;
    }

    public void setNodeInsts(List<ProcFlowNodeInst> nodeInsts) {
        this.nodeInsts = nodeInsts;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public ProcFlowNodeInst findProcFlowNodeInstByNodeId(String nodeId) {
        for (ProcFlowNodeInst n : this.getNodeInsts()) {
            if (n.getId().equals(nodeId)) {
                return n;
            }
        }

        return null;
    }

    public void addNodeInsts(ProcFlowNodeInst... nodeInsts) {
        if (this.nodeInsts == null) {
            this.nodeInsts = new ArrayList<>();
        }

        for (ProcFlowNodeInst n : nodeInsts) {
            if (n == null) {
                continue;
            }

            if (n.getId() == null) {
                throw new IllegalArgumentException("Node id is null.");
            }
            if (!this.nodeInsts.contains(n)) {
                this.nodeInsts.add(n);
            }
        }
    }
}
