package com.webank.wecube.platform.core.dto.event;

public class OperationEventDto {

    private String eventSeqNo;
    private String eventType;
    private String sourceSubSystem;
    private String operationKey;
    private String operationData;
    private String notifyRequired;
    private String notifyEndpoint;
    private String operationUser;

    public String getEventSeqNo() {
        return eventSeqNo;
    }

    public void setEventSeqNo(String eventSeqNo) {
        this.eventSeqNo = eventSeqNo;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getOperationKey() {
        return operationKey;
    }

    public void setOperationKey(String operationKey) {
        this.operationKey = operationKey;
    }

    public String getSourceSubSystem() {
        return sourceSubSystem;
    }

    public void setSourceSubSystem(String sourceSubSystem) {
        this.sourceSubSystem = sourceSubSystem;
    }

    public String getOperationData() {
        return operationData;
    }

    public void setOperationData(String operationData) {
        this.operationData = operationData;
    }

    public String getNotifyRequired() {
        return notifyRequired;
    }

    public void setNotifyRequired(String notifyRequired) {
        this.notifyRequired = notifyRequired;
    }

    public String getNotifyEndpoint() {
        return notifyEndpoint;
    }

    public void setNotifyEndpoint(String notifyEndpoint) {
        this.notifyEndpoint = notifyEndpoint;
    }

    public String getOperationUser() {
        return operationUser;
    }

    public void setOperationUser(String operationUser) {
        this.operationUser = operationUser;
    }

    @Override
    public String toString() {
        return "[eventSeqNo=" + eventSeqNo + ", eventType=" + eventType + ", sourceSubSystem=" + sourceSubSystem
                + ", operationKey=" + operationKey + ", operationData=" + operationData + ", notifyRequired="
                + notifyRequired + ", notifyEndpoint=" + notifyEndpoint + ", operationUser=" + operationUser + "]";
    }
}
