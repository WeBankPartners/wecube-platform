package com.webank.wecube.platform.core.support.plugin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class PluginResponse<DATATYPE> {
    public static final String RESULT_CODE_OK = "0";

    @JsonProperty("result_code")
    private String resultCode;
    @JsonProperty("result_message")
    private String resultMessage;
    @JsonProperty("results")
    private ResultData<DATATYPE> resultData;

    public List<DATATYPE> getOutputs() {
        return (resultData == null) ? null : resultData.getOutputs();
    }

    @Data
    public static class ResultData<DATATYPE> {
        private List<DATATYPE> outputs;
    }

    public static class DefaultPluginResponse extends PluginResponse<Object> {
    }

    public static class PluginRunScriptResponse extends PluginResponse<PluginRunScriptOutput> {
    }
}
