package com.webank.wecube.platform.core.dto.workflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestObjectDto {
    private String callbackParameter;
    private List<Map<String, String>> inputs = new ArrayList<>();
    private List<Map<String, String>> outputs = new ArrayList<>();

    public String getCallbackParameter() {
        return callbackParameter;
    }

    public void setCallbackParameter(String callbackParameter) {
        this.callbackParameter = callbackParameter;
    }

    public List<Map<String, String>> getInputs() {
        return inputs;
    }

    public void setInputs(List<Map<String, String>> inputs) {
        this.inputs = inputs;
    }

    public List<Map<String, String>> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<Map<String, String>> outputs) {
        this.outputs = outputs;
    }

    public void addInput(Map<String, String> dtoMap) {
        this.inputs.add(dtoMap);
    }

    public void addOutput(Map<String, String> dtoMap) {
        this.outputs.add(dtoMap);
    }

    public static class RequestParamObjectDto {
        private String objectId;
        private String callbackParameter;
        private Map<String, String> paramAttrs = new HashMap<>();

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

        public Map<String, String> getParamAttrs() {
            return paramAttrs;
        }

        public void setParamAttrs(Map<String, String> paramAttrs) {
            this.paramAttrs = paramAttrs;
        }

        public void addParamAttr(String paramName, String paramValAsStr) {
            this.paramAttrs.put(paramName, paramValAsStr);
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
