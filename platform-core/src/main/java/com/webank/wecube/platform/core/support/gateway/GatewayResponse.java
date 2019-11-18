package com.webank.wecube.platform.core.support.gateway;

public class GatewayResponse {
    public static final String STATUS_CODE_OK = "OK";

    private String status;
    private String message;
    private Object data;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public static String getStatusCodeOk() {
        return STATUS_CODE_OK;
    }

}
