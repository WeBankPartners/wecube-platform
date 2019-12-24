package com.webank.wecube.platform.core.support.plugin.dto;

import java.util.List;

public class PluginResponseResultData<DATATYPE> {
    private List<DATATYPE> outputs;

    public List<DATATYPE> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<DATATYPE> outputs) {
        this.outputs = outputs;
    }

    public static class StationaryResultData extends PluginResponseResultData<PluginResponseStationaryOutput> {
    }
}
