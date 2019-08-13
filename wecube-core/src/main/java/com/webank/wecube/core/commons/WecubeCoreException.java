package com.webank.wecube.core.commons;

public class WecubeCoreException extends RuntimeException {
    public WecubeCoreException(String message) {
        super(message);
    }

    public WecubeCoreException(String message, Throwable cause) {
        super(message, cause);
    }
}
