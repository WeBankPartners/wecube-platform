package com.webank.wecube.platform.core.entity.workflow;

public class CoreRuGraphNodeWithBLOBs extends CoreRuGraphNode {
    private String prevIds;

    private String succIds;

    public String getPrevIds() {
        return prevIds;
    }

    public void setPrevIds(String prevIds) {
        this.prevIds = prevIds == null ? null : prevIds.trim();
    }

    public String getSuccIds() {
        return succIds;
    }

    public void setSuccIds(String succIds) {
        this.succIds = succIds == null ? null : succIds.trim();
    }
}