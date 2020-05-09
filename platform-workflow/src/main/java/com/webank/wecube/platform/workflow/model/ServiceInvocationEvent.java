package com.webank.wecube.platform.workflow.model;

public interface ServiceInvocationEvent {

    String getEventId();

    String getRequestId();

    String getInstanceId();

    String getBusinessKey();

    String getDefinitionId();

    String getDefinitionKey();

    int getDefinitionVersion();

    String getExecutionId();

    String getEventSourceId();

    String getEventSourceName();

    String getServiceCode();

    String getCallbackUrl();

    int getDirection();

    int getRetryTimes();

    void increaseRetryTimes();

    String getResult();

    String getMsg();

    EventType getEventType();

    public enum EventType {
        SERVICE_INVOCATION, SERVICE_INVOCATION_RESULT, PROCESS_END_NOTIFICATION;
    }
}
