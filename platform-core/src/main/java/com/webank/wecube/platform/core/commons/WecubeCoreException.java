package com.webank.wecube.platform.core.commons;

public class WecubeCoreException extends RuntimeException {
    /**
     * 
     */
    private static final long serialVersionUID = -308888278441260314L;

    public WecubeCoreException(String message) {
        super(message);
    }

    public WecubeCoreException(String message, Throwable cause) {
        super(message, cause);
    }
}
