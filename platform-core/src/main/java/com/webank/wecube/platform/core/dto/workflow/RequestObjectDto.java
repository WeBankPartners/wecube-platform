package com.webank.wecube.platform.core.dto.workflow;

import java.util.ArrayList;
import java.util.List;

public class RequestObjectDto {
    private String callbackParameter;
    private List<RequestParamObjectDto> inputs = new ArrayList<>();
    private List<RequestParamObjectDto> outputs = new ArrayList<>();

    public String getCallbackParameter() {
        return callbackParameter;
    }

    public void setCallbackParameter(String callbackParameter) {
        this.callbackParameter = callbackParameter;
    }

    public List<RequestParamObjectDto> getInputs() {
        return inputs;
    }

    public void setInputs(List<RequestParamObjectDto> inputs) {
        this.inputs = inputs;
    }

    public List<RequestParamObjectDto> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<RequestParamObjectDto> outputs) {
        this.outputs = outputs;
    }

    public void addInput(RequestParamObjectDto dto) {
        this.inputs.add(dto);
    }

    public void addOutput(RequestParamObjectDto dto) {
        this.outputs.add(dto);
    }

    public static class RequestParamObjectDto {
        private String objectId;
        private String callbackParameter;
        private List<RequestParamAttrDto> paramAttrs = new ArrayList<>();

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

        public List<RequestParamAttrDto> getParamAttrs() {
            return paramAttrs;
        }

        public void setParamAttrs(List<RequestParamAttrDto> paramAttrs) {
            this.paramAttrs = paramAttrs;
        }

        public void addParamAttr(String paramName, String paramValAsStr) {
            RequestParamAttrDto dto = new RequestParamAttrDto(paramName, paramValAsStr);
            this.paramAttrs.add(dto);
        }

        public void addParamAttr(RequestParamAttrDto dto) {
            this.paramAttrs.add(dto);
        }

    }

    public static class RequestParamAttrDto {
        private String paramName;
        private String paramValue;

        public RequestParamAttrDto() {

        }

        public RequestParamAttrDto(String paramName, String paramValue) {
            this.paramName = paramName;
            this.paramValue = paramValue;
        }

        public String getParamName() {
            return paramName;
        }

        public void setParamName(String paramName) {
            this.paramName = paramName;
        }

        public String getParamValue() {
            return paramValue;
        }

        public void setParamValue(String paramValue) {
            this.paramValue = paramValue;
        }

    }

}
