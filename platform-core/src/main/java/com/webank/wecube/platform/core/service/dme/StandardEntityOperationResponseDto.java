package com.webank.wecube.platform.core.service.dme;

public class StandardEntityOperationResponseDto {
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

    public StandardEntityOperationResponseDto withData(Object data){
        this.data = data;
        return this;
    }

    public static StandardEntityOperationResponseDto okay() {
        StandardEntityOperationResponseDto result = new StandardEntityOperationResponseDto();
        result.setStatus(STATUS_OK);
        result.setMessage("Success");
        return result;
    }

    public static StandardEntityOperationResponseDto okayWithData(Object data) {
        return okay().withData(data);
    }

    public static StandardEntityOperationResponseDto error(String errorMessage) {
        StandardEntityOperationResponseDto result = new StandardEntityOperationResponseDto();
        result.setStatus(STATUS_ERROR);
        result.setMessage(errorMessage);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[status=");
        builder.append(status);
        builder.append(", message=");
        builder.append(message);
        builder.append(", data=");
        builder.append(data);
        builder.append("]");
        return builder.toString();
    }
    
    
}
