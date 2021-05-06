package com.webank.wecube.platform.core.support.plugin.dto;

public class TaskFormMetaResponseDto {
    public final static String STATUS_OK = "OK";
    public final static String STATUS_ERROR = "ERROR";
    private String status;
    private String message;
    private TaskFormMetaDto data;

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

    public TaskFormMetaDto getData() {
        return data;
    }

    public void setData(TaskFormMetaDto data) {
        this.data = data;
    }

}
