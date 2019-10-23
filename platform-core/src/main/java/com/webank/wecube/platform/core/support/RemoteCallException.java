package com.webank.wecube.platform.core.support;

public abstract class RemoteCallException extends RuntimeException {

    public RemoteCallException(String message) {
        super(message);
    }

    public RemoteCallException(String message, Throwable cause) {
        super(message, cause);
    }

    public abstract String getErrorMessage();

    public abstract Object getErrorData();
}
