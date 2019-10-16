package com.webank.wecube.platform.workflow.parse;

public class BpmnCustomizationException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public BpmnCustomizationException() {
        super();
    }

    public BpmnCustomizationException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public BpmnCustomizationException(String message, Throwable cause) {
        super(message, cause);
    }

    public BpmnCustomizationException(String message) {
        super(message);
    }

    public BpmnCustomizationException(Throwable cause) {
        super(cause);
    }
}
