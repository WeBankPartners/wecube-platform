package com.webank.wecube.platform.core.dto.plugin;

public class CommonResponseDto {
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

    public CommonResponseDto withData(Object data){
        this.data = data;
        return this;
    }

    public static CommonResponseDto okay() {
        CommonResponseDto result = new CommonResponseDto();
        result.setStatus(STATUS_OK);
        result.setMessage("Success");
        return result;
    }

    public static CommonResponseDto okayWithData(Object data) {
        return okay().withData(data);
    }

    public static CommonResponseDto error(String errorMessage) {
        CommonResponseDto result = new CommonResponseDto();
        result.setStatus(STATUS_ERROR);
        result.setMessage(errorMessage);
        return result;
    }
}
