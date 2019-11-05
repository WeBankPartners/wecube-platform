package com.webank.wecube.platform.core.support.plugin.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

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

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }

    public ResultData<DATATYPE> getResultData() {
        return resultData;
    }

    public void setResultData(ResultData<DATATYPE> resultData) {
        this.resultData = resultData;
    }

    public static class ResultData<DATATYPE> {
        private List<DATATYPE> outputs;

        public List<DATATYPE> getOutputs() {
            return outputs;
        }

        public void setOutputs(List<DATATYPE> outputs) {
            this.outputs = outputs;
        }

    }

    public static class DefaultPluginResponse extends PluginResponse<Object> {
    }

    public static class PluginRunScriptResponse extends PluginResponse<PluginRunScriptOutput> {
    }
}
