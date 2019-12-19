package com.webank.wecube.platform.workflow.model;

public class ServiceInvocationEventImpl implements ServiceInvocationEvent {

    private String eventId;
    private String requestId;
    private String instanceId;
    private String executionId;
    private String serviceCode;

    private String callbackUrl;

    private int retryTimes;

    private int direction;
    private int result;
    private String msg;

    private String eventSourceId;
    private String eventSourceName;

    private String definitionId;
    private String definitionKey;
    private int definitionVersion;
    private String businessKey;

    private EventType eventType;

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public String getEventId() {
        return eventId;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public String getExecutionId() {
        return executionId;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public int getRetryTimes() {
        return this.retryTimes;
    }

    public void increaseRetryTimes() {
        ++this.retryTimes;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getEventSourceId() {
        return eventSourceId;
    }

    public void setEventSourceId(String eventSourceId) {
        this.eventSourceId = eventSourceId;
    }

    public String getEventSourceName() {
        return eventSourceName;
    }

    public void setEventSourceName(String eventSourceName) {
        this.eventSourceName = eventSourceName;
    }

    public String getDefinitionId() {
        return definitionId;
    }

    public void setDefinitionId(String definitionId) {
        this.definitionId = definitionId;
    }

    public String getDefinitionKey() {
        return definitionKey;
    }

    public void setDefinitionKey(String definitionKey) {
        this.definitionKey = definitionKey;
    }

    public int getDefinitionVersion() {
        return definitionVersion;
    }

    public void setDefinitionVersion(int definitionVersion) {
        this.definitionVersion = definitionVersion;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public void setBusinessKey(String businessKey) {
        this.businessKey = businessKey;
    }

    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    @Override
    public String toString() {
        return "ServiceInvocationEventImpl [eventId=" + eventId + ", requestId=" + requestId + ", instanceId="
                + instanceId + ", executionId=" + executionId + ", serviceCode=" + serviceCode + ", callbackUrl="
                + callbackUrl + ", retryTimes=" + retryTimes + ", direction=" + direction + ", result=" + result
                + ", msg=" + msg + ", eventSourceId=" + eventSourceId + ", eventSourceName=" + eventSourceName
                + ", definitionId=" + definitionId + ", definitionKey=" + definitionKey + ", definitionVersion="
                + definitionVersion + ", businessKey=" + businessKey + ", eventType=" + eventType + "]";
    }
}
