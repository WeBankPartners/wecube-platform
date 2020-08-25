package com.webank.wecube.platform.auth.server.common;

public class AuthServerException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 6968480222421420715L;
    
    private String errorCode;
    private String messageKey;
    private boolean applyMessage = false;
    private Object[] args;

    public AuthServerException() {
        super();
    }

    public AuthServerException(String message) {
        super(message);
    }

    public AuthServerException(String message, Throwable cause) {
        super(message, cause);
    }

    
    public AuthServerException(String errorCode, String message, Object... objects) {
        this(errorCode, null, message, null, false, objects);
    }

    private AuthServerException(String errorCode, String messageKey, String message, Throwable cause,
            boolean applyMessage, Object... objects) {
        super(message, cause);
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
        this.applyMessage = applyMessage;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
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
    
    public AuthServerException withErrorCode(String errorCode) {
        this.errorCode = errorCode;
        return this;
    }

    public AuthServerException withErrorCode(String errorCode, Object... objects) {
        this.errorCode = errorCode;
        if (objects != null && (this.args == null)) {
            this.args = new Object[objects.length];
            int index = 0;
            for (Object object : objects) {
                this.args[index] = object;
                index++;
            }
        }
        return this;
    }

    public AuthServerException withErrorCodeAndArgs(String errorCode, Object[] objects) {
        this.errorCode = errorCode;
        if (objects != null && (this.args == null)) {
            this.args = new Object[objects.length];
            int index = 0;
            for (Object object : objects) {
                this.args[index] = object;
                index++;
            }
        }
        return this;
    }

    public AuthServerException withMessageKey(String msgKey) {
        this.messageKey = msgKey;
        return this;
    }

    public AuthServerException withMessageKey(String msgKey, Object... objects) {
        this.messageKey = msgKey;
        if (objects != null && (this.args == null)) {
            this.args = new Object[objects.length];
            int index = 0;
            for (Object object : objects) {
                this.args[index] = object;
                index++;
            }
        }

        return this;
    }

}
