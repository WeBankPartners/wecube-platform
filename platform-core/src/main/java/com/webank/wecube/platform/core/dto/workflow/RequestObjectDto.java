package com.webank.wecube.platform.core.dto.workflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestObjectDto {
    private String callbackParameter;
    private List<Map<String, Object>> inputs = new ArrayList<>();
    private List<Map<String, Object>> outputs = new ArrayList<>();

    public String getCallbackParameter() {
        return callbackParameter;
    }

    public void setCallbackParameter(String callbackParameter) {
        this.callbackParameter = callbackParameter;
    }

    public List<Map<String, Object>> getInputs() {
        return inputs;
    }

    public void setInputs(List<Map<String, Object>> inputs) {
        this.inputs = inputs;
    }

    public List<Map<String, Object>> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<Map<String, Object>> outputs) {
        this.outputs = outputs;
    }

    public void addInput(Map<String, Object> dtoMap) {
        this.inputs.add(dtoMap);
    }

    public void addOutput(Map<String, Object> dtoMap) {
        this.outputs.add(dtoMap);
    }

    public static class RequestParamObjectDto {
        private String objectId;
        private String callbackParameter;
        private Map<String, Object> paramAttrs = new HashMap<>();

        public String getObjectId() {
            return objectId;
        }

        public void setObjectId(String objectId) {
            this.objectId = objectId;
        }

        public String getCallbackParameter() {
            return callbackParameter;
        }

        public void setCallbackParameter(String callbackParameter) {
            this.callbackParameter = callbackParameter;
        }

        public Map<String, Object> getParamAttrs() {
            return paramAttrs;
        }

        public void setParamAttrs(Map<String, Object> paramAttrs) {
            this.paramAttrs = paramAttrs;
        }

        public void addParamAttr(String paramName, Object paramVal) {
            this.paramAttrs.put(paramName, paramVal);
        }

    }

    // public static class RequestParamAttrDto {
    // private String paramName;
    // private String paramValue;
    //
    // public RequestParamAttrDto() {
    //
    // }
    //
    // public RequestParamAttrDto(String paramName, String paramValue) {
    // this.paramName = paramName;
    // this.paramValue = paramValue;
    // }
    //
    // public String getParamName() {
    // return paramName;
    // }
    //
    // public void setParamName(String paramName) {
    // this.paramName = paramName;
    // }
    //
    // public String getParamValue() {
    // return paramValue;
    // }
    //
    // public void setParamValue(String paramValue) {
    // this.paramValue = paramValue;
    // }
    //
    // }

}
