package com.webank.wecube.platform.core.dto.workflow;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PluginAsyncInvocationResultDto {
    public static final String RESULT_CODE_OK = "0";

    @JsonProperty("resultCode")
    private String resultCode;
    @JsonProperty("resultMessage")
    private String resultMessage;
    @JsonProperty("results")
    private ResultData resultData;

    public List<Object> getOutputs() {
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

    public ResultData getResultData() {
        return resultData;
    }

    public void setResultData(ResultData resultData) {
        this.resultData = resultData;
    }
    
    @Override
    public String toString() {
        return "[resultCode=" + resultCode + ", resultMessage=" + resultMessage
                + ", resultData=" + resultData + "]";
    }

    public static class ResultData {
        private String requestId;
        private List<Object> outputs;
        
        public String getRequestId() {
            return requestId;
        }

        public void setRequestId(String requestId) {
            this.requestId = requestId;
        }

        public List<Object> getOutputs() {
            return outputs;
        }

        public void setOutputs(List<Object> outputs) {
            this.outputs = outputs;
        }

        @Override
        public String toString() {
            return "[requestId=" + requestId + ", outputs=" + outputs + "]";
        }
    }

}
