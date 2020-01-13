package com.webank.wecube.platform.auth.server.common;

public class AuthServerException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 6968480222421420715L;

    public AuthServerException() {
        super();
    }

    public AuthServerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public AuthServerException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthServerException(String message) {
        super(message);
    }

    public AuthServerException(Throwable cause) {
        super(cause);
    }

}
