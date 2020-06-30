package com.webank.wecube.platform.auth.server.boot;

public class UpdateSqlFailureException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = -909206473681676312L;

    public UpdateSqlFailureException() {
        super();
    }

    public UpdateSqlFailureException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public UpdateSqlFailureException(String message, Throwable cause) {
        super(message, cause);
    }

    public UpdateSqlFailureException(String message) {
        super(message);
    }

    public UpdateSqlFailureException(Throwable cause) {
        super(cause);
    }

}
