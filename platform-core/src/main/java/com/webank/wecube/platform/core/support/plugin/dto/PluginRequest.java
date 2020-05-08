package com.webank.wecube.platform.core.support.plugin.dto;

import java.util.List;
import java.util.Map;

public class PluginRequest<DATATYPE> {
    private String requestId;
    private List<String> allowedOptions;
    private List<DATATYPE> inputs;

    public PluginRequest<DATATYPE> withInputs(List<DATATYPE> inputs) {
        this.inputs = inputs;
        return this;
    }
    
    public PluginRequest<DATATYPE> withRequestId(String requestId){
        this.requestId = requestId;
        return this;
    }
    
    public PluginRequest<DATATYPE> withAllowedOptions(List<String> allowedOptions){
        this.allowedOptions = allowedOptions;
        return this;
    }
    
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public List<DATATYPE> getInputs() {
        return inputs;
    }

    public void setInputs(List<DATATYPE> inputs) {
        this.inputs = inputs;
    }
    
    public List<String> getAllowedOptions() {
        return allowedOptions;
    }

    public void setAllowedOptions(List<String> allowedOptions) {
        this.allowedOptions = allowedOptions;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PluginRequest [requestId=");
        builder.append(requestId);
        builder.append(", allowedOptions=");
        builder.append(allowedOptions);
        builder.append(", inputs=");
        builder.append(inputs);
        builder.append("]");
        return builder.toString();
    }
    
    public static class DefaultPluginRequest extends PluginRequest<Map<String, Object>> {
    }
    
    public static class PluginLoggingInfoRequest extends PluginRequest<PluginLoggingInfoRequestParameter> {
    }

    public static class PluginLoggingInfoSearchDetailRequest extends PluginRequest<PluginLoggingInfoSearchDetailRequestParameter> {
    }
}
