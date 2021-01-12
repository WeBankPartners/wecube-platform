package com.webank.wecube.platform.core.support.itsdanger;

public class ItsDangerCheckRespDto {
    private int code;
    private String status;
    private String message;
    private ItsDanerResultDataInfoDto data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

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

    public ItsDanerResultDataInfoDto getData() {
        return data;
    }

    public void setData(ItsDanerResultDataInfoDto data) {
        this.data = data;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ItsDangerCheckRespDto [code=");
        builder.append(code);
        builder.append(", status=");
        builder.append(status);
        builder.append(", message=");
        builder.append(message);
        builder.append(", data=");
        builder.append(data);
        builder.append("]");
        return builder.toString();
    }

    
}
