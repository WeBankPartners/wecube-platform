package com.webank.wecube.platform.core.commons;

public class WecubeCoreException extends RuntimeException {
    private static final long serialVersionUID = -308888278441260314L;

    private String errorCode;
    private String messageKey;
    private boolean applyMessage = false;
    private Object[] args;

    public WecubeCoreException(String message) {
        super(message);
        this.applyMessage = true;
    }

    public WecubeCoreException(String message, Throwable cause) {
        super(message, cause);
        this.applyMessage = true;
    }

    public WecubeCoreException(String errorCode, String messageKey, Object... objects) {
        super();
        this.errorCode = errorCode;
        this.messageKey = messageKey;
        if (objects != null && (this.args == null)) {
            this.args = new Object[objects.length];
            int index = 0;
            for (Object object : objects) {
                this.args[index] = object;
                index++;
            }
        }
        this.applyMessage = false;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public boolean isApplyMessage() {
        return applyMessage;
    }

    public Object[] getArgs() {
        return args;
    }

}
