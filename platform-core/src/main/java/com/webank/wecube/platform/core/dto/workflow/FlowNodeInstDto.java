package com.webank.wecube.platform.core.dto.workflow;

public class FlowNodeInstDto extends FlowNodeDefDto{

    private Integer procInstId;
    private String procInstKey;

    public Integer getProcInstId() {
        return procInstId;
    }

    public void setProcInstId(Integer procInstId) {
        this.procInstId = procInstId;
    }

    public String getProcInstKey() {
        return procInstKey;
    }

    public void setProcInstKey(String procInstKey) {
        this.procInstKey = procInstKey;
    }
   
}
