package com.webank.wecube.platform.core.dto.workflow;

public class ProcInstTerminationRequestDto {
    private String procInstId;
    private String procInstKey;

    public String getProcInstId() {
        return procInstId;
    }

    public void setProcInstId(String procInstId) {
        this.procInstId = procInstId;
    }

    public String getProcInstKey() {
        return procInstKey;
    }

    public void setProcInstKey(String procInstKey) {
        this.procInstKey = procInstKey;
    }

}
