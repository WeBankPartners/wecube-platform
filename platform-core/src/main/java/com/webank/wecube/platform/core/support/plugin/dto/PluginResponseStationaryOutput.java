package com.webank.wecube.platform.core.support.plugin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PluginResponseStationaryOutput {
    public static final String ERROR_CODE_SUCCESSFUL = "0";
    public static final String ERROR_CODE_FAILED = "1";

    private String errorCode;
    private String errorMessage;
    private String callbackParameter;

    public PluginResponseStationaryOutput() {
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public String getCallbackParameter() {
		return callbackParameter;
	}

	public void setCallbackParameter(String callbackParameter) {
		this.callbackParameter = callbackParameter;
	}

	public PluginResponseStationaryOutput(String errorCode, String errorMessage, String callbackParameter) {
        super();
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return "CallbackRequestOutputsDto [errorCode=" + errorCode + ", errorMessage=" + errorMessage
                + ", callbackParameter=" + callbackParameter + "]";
    }

    public void setError(String errorMessage) {
        this.setErrorCode(ERROR_CODE_FAILED);
        this.setErrorMessage(errorMessage);
    }

    public static PluginResponseStationaryOutput withError(String errorMessage) {
        PluginResponseStationaryOutput output = new PluginResponseStationaryOutput();
        output.setErrorCode(ERROR_CODE_FAILED);
        output.setErrorMessage(errorMessage);
        return output;
    }
}
