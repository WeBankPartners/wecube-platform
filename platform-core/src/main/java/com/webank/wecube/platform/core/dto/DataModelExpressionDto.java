package com.webank.wecube.platform.core.dto;

import java.util.List;
import java.util.Map;

public class DataModelExpressionDto {
    private String expression;
    private String fromUrl;
    private String toUrl;
    private List<Map<String, String>> returnedJson;

    public DataModelExpressionDto(String expression, String fromUrl, String toUrl, List<Map<String, String>> returnedJson) {
        this.expression = expression;
        this.fromUrl = fromUrl;
        this.toUrl = toUrl;
        this.returnedJson = returnedJson;
    }

    public DataModelExpressionDto() {
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public List<Map<String, String>> getReturnedJson() {
        return returnedJson;
    }

    public void setReturnedJson(List<Map<String, String>> returnedJson) {
        this.returnedJson = returnedJson;
    }

    public String getFromUrl() {
        return fromUrl;
    }

    public void setFromUrl(String fromUrl) {
        this.fromUrl = fromUrl;
    }

    public String getToUrl() {
        return toUrl;
    }

    public void setToUrl(String toUrl) {
        this.toUrl = toUrl;
    }
}
