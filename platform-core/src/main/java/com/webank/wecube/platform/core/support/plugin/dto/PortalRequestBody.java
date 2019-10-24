package com.webank.wecube.platform.core.support.plugin.dto;

import lombok.Data;

import java.util.List;

@Data
public class PortalRequestBody<DATATYPE> {
    private DATATYPE pluginRequest;

    private List<Integer> instanceIds;

    public static class SearchPluginLogRequest extends PortalRequestBody<PluginRequest.PluginLoggingInfoRequest> {
    }


}
