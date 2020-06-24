package com.webank.wecube.platform.auth.server.boot;

public class ApplicationInitializeException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = -1628721381090457001L;

    public ApplicationInitializeException() {
        super();
    }

    public ApplicationInitializeException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ApplicationInitializeException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApplicationInitializeException(String message) {
        super(message);
    }

    public ApplicationInitializeException(Throwable cause) {
        super(cause);
    }

}
