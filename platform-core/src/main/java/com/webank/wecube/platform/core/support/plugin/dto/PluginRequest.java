package com.webank.wecube.platform.core.support.plugin.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class PluginRequest<DATATYPE> {
    private List<DATATYPE> inputs;

    public PluginRequest<DATATYPE> withInputs(List<DATATYPE> inputs) {
        this.inputs = inputs;
        return this;
    }

    public static class DefaultPluginRequest extends PluginRequest<Map<String, Object>> {
    }

}
