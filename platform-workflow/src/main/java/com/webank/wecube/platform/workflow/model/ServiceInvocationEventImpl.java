package com.webank.wecube.platform.workflow.model;

import java.util.ArrayList;
import java.util.List;

public class ServiceInvocationEventImpl implements ServiceInvocationEvent {

    private String eventId;
    private String requestId;
    private String instanceId;
    private String executionId;
    private String serviceCode;

    private String callbackUrl;

    private int retryTimes;

    private int direction;
    private String result;
    private String msg;

    private String eventSourceId;
    private String eventSourceName;

    private String definitionId;
    private String definitionKey;
    private int definitionVersion;
    private String businessKey;

    private EventType eventType;
    
    private List<String> allowedOptions = new ArrayList<String>();

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

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
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
    
    public List<String> getAllowedOptions() {
        return allowedOptions;
    }

    public void setAllowedOptions(List<String> allowedOptions) {
        this.allowedOptions = allowedOptions;
    }
    
    public void addAllowedOption(String option) {
        if(option == null){
            return;
        }
        this.allowedOptions.add(option);
    }

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ServiceInvocationEventImpl [eventId=");
		builder.append(eventId);
		builder.append(", requestId=");
		builder.append(requestId);
		builder.append(", instanceId=");
		builder.append(instanceId);
		builder.append(", executionId=");
		builder.append(executionId);
		builder.append(", serviceCode=");
		builder.append(serviceCode);
		builder.append(", callbackUrl=");
		builder.append(callbackUrl);
		builder.append(", retryTimes=");
		builder.append(retryTimes);
		builder.append(", direction=");
		builder.append(direction);
		builder.append(", result=");
		builder.append(result);
		builder.append(", msg=");
		builder.append(msg);
		builder.append(", eventSourceId=");
		builder.append(eventSourceId);
		builder.append(", eventSourceName=");
		builder.append(eventSourceName);
		builder.append(", definitionId=");
		builder.append(definitionId);
		builder.append(", definitionKey=");
		builder.append(definitionKey);
		builder.append(", definitionVersion=");
		builder.append(definitionVersion);
		builder.append(", businessKey=");
		builder.append(businessKey);
		builder.append(", eventType=");
		builder.append(eventType);
		builder.append(", allowedOptions=");
		builder.append(allowedOptions);
		builder.append("]");
		return builder.toString();
	}
}
