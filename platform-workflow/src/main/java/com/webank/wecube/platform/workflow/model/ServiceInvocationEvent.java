package com.webank.wecube.platform.workflow.model;

import java.util.List;

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

    List<String> getAllowedOptions();

    public enum EventType {
        SERVICE_INVOCATION, SERVICE_INVOCATION_RESULT, PROCESS_END_NOTIFICATION, PROCESS_FAULTED_END_NOTIFICATION;
    }
}
