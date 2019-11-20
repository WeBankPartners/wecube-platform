package com.webank.wecube.platform.core.dto.workflow;

public class ProceedProcInstRequestDto {
    private Integer procInstId;

    private Integer nodeInstId;

    private String act;

    public Integer getProcInstId() {
        return procInstId;
    }

    public void setProcInstId(Integer procInstId) {
        this.procInstId = procInstId;
    }

    public Integer getNodeInstId() {
        return nodeInstId;
    }

    public void setNodeInstId(Integer nodeInstId) {
        this.nodeInstId = nodeInstId;
    }

    public String getAct() {
        return act;
    }

    public void setAct(String act) {
        this.act = act;
    }
    
    
}
