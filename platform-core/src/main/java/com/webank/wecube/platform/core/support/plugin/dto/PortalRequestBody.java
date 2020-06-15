package com.webank.wecube.platform.core.support.plugin.dto;

import java.util.List;

public class PortalRequestBody<DATATYPE> {
    private DATATYPE pluginRequest;

    private List<Integer> instanceIds;

    public DATATYPE getPluginRequest() {
        return pluginRequest;
    }

    public void setPluginRequest(DATATYPE pluginRequest) {
        this.pluginRequest = pluginRequest;
    }

    public List<Integer> getInstanceIds() {
        return instanceIds;
    }

    public void setInstanceIds(List<Integer> instanceIds) {
        this.instanceIds = instanceIds;
    }

}
