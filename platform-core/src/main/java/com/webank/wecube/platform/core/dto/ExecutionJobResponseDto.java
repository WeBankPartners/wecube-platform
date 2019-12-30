package com.webank.wecube.platform.core.dto;

public class ExecutionJobResponseDto {
    private String errorCode;
    private Object result;

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public ExecutionJobResponseDto() {
        super();
    }

    public ExecutionJobResponseDto(String errorCode, Object result) {
        super();
        this.errorCode = errorCode;
        this.result = result;
    }

}