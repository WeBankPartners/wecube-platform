package com.webank.wecube.platform.core.service.dme;

public class EntityOperationException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = -827654002329929784L;

    private String errorCode;
    private Object[] args;

    public EntityOperationException(String errorCode, String message, Object... objects) {
        super(message);
        this.errorCode = errorCode;
        if (objects == null || objects.length < 1) {
            return;
        }
        if (this.args == null) {
            this.args = new Object[objects.length];
            int index = 0;
            for (Object object : objects) {
                this.args[index] = object;
                index++;
            }
        }
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

}
