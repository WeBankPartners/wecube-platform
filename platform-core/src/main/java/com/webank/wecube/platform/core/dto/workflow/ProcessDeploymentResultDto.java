package com.webank.wecube.platform.core.dto.workflow;

public class ProcessDeploymentResultDto {
    public static String STATUS_CONFIRM = "CONFIRM";
    public static String STATUS_OK = "OK";

    private String status;
    private String message;
    private ContinueTokenInfoDto continueToken;
    private ProcDefOutlineDto result;

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

    public ProcDefOutlineDto getResult() {
        return result;
    }

    public void setResult(ProcDefOutlineDto result) {
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
