package com.webank.wecube.platform.core.dto;

public class ItsDangerConfirmResultDto {
    private String status = "CONFIRM";
    private ItsDangerTokenInfoDto data;

    private String message;

    public static class ItsDangerTokenInfoDto {
        private String continueToken;

        public String getContinueToken() {
            return continueToken;
        }

        public void setContinueToken(String continueToken) {
            this.continueToken = continueToken;
        }

    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ItsDangerTokenInfoDto getData() {
        return data;
    }

    public void setData(ItsDangerTokenInfoDto data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
