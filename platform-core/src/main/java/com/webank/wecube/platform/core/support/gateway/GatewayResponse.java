package com.webank.wecube.platform.core.support.gateway;

public class GatewayResponse {
    public static final String STATUS_CODE_OK = "OK";

    private String statusCode;
    private String statusMessage;

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public static String getStatusCodeOk() {
        return STATUS_CODE_OK;
    }

}
