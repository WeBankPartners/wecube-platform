package com.webank.wecube.platform.core.dto.event;

public class OperationEventNotificationDto {
    private String eventSeqNo;
    private String eventType;
    private String sourceSubSystem;
    
    private String status;

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

    public String getSourceSubSystem() {
        return sourceSubSystem;
    }

    public void setSourceSubSystem(String sourceSubSystem) {
        this.sourceSubSystem = sourceSubSystem;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "[eventSeqNo=" + eventSeqNo + ", eventType=" + eventType
                + ", sourceSubSystem=" + sourceSubSystem + ", status=" + status + "]";
    }
    
    
}
