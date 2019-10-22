package com.webank.wecube.platform.core.domain.workflow;

public class ProcessDefinitionPreviewVO {
    private int ciTypeId;
    private String ciGuid;
    private int definitionKey;

    public int getCiTypeId() {
        return ciTypeId;
    }

    public void setCiTypeId(int ciTypeId) {
        this.ciTypeId = ciTypeId;
    }

    public String getCiGuid() {
        return ciGuid;
    }

    public void setCiGuid(String ciGuid) {
        this.ciGuid = ciGuid;
    }

    public void setDefinitionKey(int definitionKey) {
        this.definitionKey = definitionKey;
    }

    public int getDefinitionKey() {
        return definitionKey;
    }

}
