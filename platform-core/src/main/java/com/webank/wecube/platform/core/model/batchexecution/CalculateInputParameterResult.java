package com.webank.wecube.platform.core.model.batchexecution;

import java.util.Map;

import com.webank.wecube.platform.core.support.plugin.dto.PluginResponseStationaryOutput;

public class CalculateInputParameterResult {

    private Map<String, PluginResponseStationaryOutput> failedResult;
    private Map<String, Map<String, Object>> successfulResult;

    public Map<String, PluginResponseStationaryOutput> getFailedResult() {
        return failedResult;
    }

    public void setFailedResult(Map<String, PluginResponseStationaryOutput> failedResult) {
        this.failedResult = failedResult;
    }

    public Map<String, Map<String, Object>> getSuccessfulResult() {
        return successfulResult;
    }

    public void setSuccessfulResult(Map<String, Map<String, Object>> successfulResult) {
        this.successfulResult = successfulResult;
    }

    public CalculateInputParameterResult(Map<String, PluginResponseStationaryOutput> failedResult,
            Map<String, Map<String, Object>> successfulResult) {
        super();
        this.failedResult = failedResult;
        this.successfulResult = successfulResult;
    }

    public CalculateInputParameterResult() {
    }
}
