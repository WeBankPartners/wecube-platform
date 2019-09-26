package com.webank.wecube.platform.auth.server.dto;

public class BaseResponse {
    public final static String STATUS_OK = "OK";
    public final static String STATUS_ERROR = "ERROR";

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

    public BaseResponse withData(Object data){
        this.data = data;
        return this;
    }

    public static BaseResponse okay() {
        BaseResponse result = new BaseResponse();
        result.setStatus(STATUS_OK);
        result.setMessage("Success");
        return result;
    }

    public static BaseResponse okayWithData(Object data) {
        return okay().withData(data);
    }

    public static BaseResponse error(String errorMessage) {
        BaseResponse result = new BaseResponse();
        result.setStatus(STATUS_ERROR);
        result.setMessage(errorMessage);
        return result;
    }
}
