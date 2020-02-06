package com.webank.wecube.platform.core.support.authserver;

public class AuthServerRestResponseDto<DATATYPE> {
    public final static String STATUS_OK = "OK";
    public final static String STATUS_ERROR = "ERROR";

    private String status;
    private String message;
    private DATATYPE data;

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

    public DATATYPE getData() {
        return data;
    }

    public void setData(DATATYPE data) {
        this.data = data;
    }

}
