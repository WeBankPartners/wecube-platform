package com.webank.wecube.platform.core.dto.workflow;

public class ProcessDraftResultDto {
    public static String STATUS_CONFIRM = "CONFIRM";
    public static String STATUS_OK = "OK";

    private String status;
    private String message;
    private ContinueTokenInfoDto continueToken;
    private ProcDefInfoDto result;

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

    public ProcDefInfoDto getResult() {
        return result;
    }

    public void setResult(ProcDefInfoDto result) {
        this.result = result;
    }

    public boolean isConfirm() {
        return STATUS_CONFIRM.equalsIgnoreCase(this.status);
    }

    public ContinueTokenInfoDto getContinueToken() {
        return continueToken;
    }

    public void setContinueToken(ContinueTokenInfoDto continueToken) {
        this.continueToken = continueToken;
    }

}
