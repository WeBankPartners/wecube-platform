package com.webank.wecube.platform.core.support;

public class RestClientException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = -8115036466524016690L;

    private String errorStatus;
    private String errorMessage;

    public RestClientException() {
        super();
    }

    public RestClientException(String errorStatus, String errorMessage, Throwable cause) {
        super(errorMessage, cause);
        this.errorStatus = errorStatus;
        this.errorMessage = errorMessage;
    }
    
    public RestClientException(String errorStatus, String errorMessage){
        super(errorMessage);
        this.errorStatus = errorStatus;
        this.errorMessage = errorMessage;
    }

    public RestClientException(String errorStatus) {
        this.errorStatus = errorStatus;
    }

    public RestClientException(Throwable cause) {
        super(cause);
    }

    public String getErrorStatus() {
        return errorStatus;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}
