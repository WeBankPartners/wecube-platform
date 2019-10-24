package com.webank.wecube.platform.core.support.plugin;

import com.webank.wecube.platform.core.support.RemoteCallException;
import com.webank.wecube.platform.core.support.plugin.dto.PluginResponse;

public class PluginRemoteCallException extends RemoteCallException {

    private transient PluginResponse pluginResponse;

    public PluginRemoteCallException(String message) {
        super(message);
    }

    public PluginRemoteCallException(String message, PluginResponse pluginResponse) {
        super(message);
        this.pluginResponse = pluginResponse;
    }

    public PluginRemoteCallException(String message, PluginResponse pluginResponse, Throwable cause) {
        super(message, cause);
        this.pluginResponse = pluginResponse;
    }

    public PluginResponse getPluginResponse() {
        return pluginResponse;
    }

    @Override
    public String getErrorMessage() {
        return String.format("%s (PLUGIN_ERROR_CODE: %s)", this.getMessage(), getStatusCode(pluginResponse));
    }

    @Override
    public Object getErrorData() {
        return pluginResponse == null ? null : pluginResponse.getResultData();
    }

    private String getStatusCode(PluginResponse pluginResponse) {
        return pluginResponse == null ? null : pluginResponse.getResultCode();
    }
}
