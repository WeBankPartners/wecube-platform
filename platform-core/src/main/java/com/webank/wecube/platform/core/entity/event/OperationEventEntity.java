//package com.webank.wecube.platform.core.entity.event;
//
//import java.util.Date;
//
//import javax.persistence.Column;
//import javax.persistence.Entity;
//import javax.persistence.GeneratedValue;
//import javax.persistence.Id;
//import javax.persistence.Table;
//
//import com.webank.wecube.platform.core.entity.BaseTraceableEntity;
//
//@Entity
//@Table(name = "CORE_OPERATION_EVENT")
//public class OperationEventEntity extends BaseTraceableEntity {
//
//    public static final String STATUS_NEW = "New";
//    public static final String STATUS_IN_PROGRESS = "InProgress";
//    public static final String STATUS_COMPLETED = "Completed";
//
//    @Id
//    @Column(name = "ID")
//    @GeneratedValue
//    private Long id;
//
//    @Column(name = "STATUS")
//    private String status;
//
//    @Column(name = "EVENT_SEQ_NO")
//    private String eventSeqNo;
//
//    @Column(name = "EVENT_TYPE")
//    private String eventType;
//
//    @Column(name = "SRC_SUB_SYSTEM")
//    private String sourceSubSystem;
//
//    @Column(name = "OPER_KEY")
//    private String operationKey;
//
//    @Column(name = "OPER_DATA")
//    private String operationData;
//
//    @Column(name = "IS_NOTIFY_REQUIRED")
//    private Boolean notifyRequired;
//
//    @Column(name = "NOTIFY_ENDPOINT")
//    private String notifyEndpoint;
//
//    @Column(name = "IS_NOTIFIED")
//    private Boolean notified;
//
//    @Column(name = "OPER_USER")
//    private String operationUser;
//
//    @Column(name = "PROC_DEF_ID")
//    private String procDefId;
//
//    @Column(name = "PROC_INST_ID")
//    private String procInstId;
//    
//    @Column(name = "PROC_INST_KEY")
//    private String procInstKey;
//
//    @Column(name = "PRIORITY")
//    private int priority;
//
//    @Column(name = "START_TIME")
//    private Date startTime;
//
//    @Column(name = "END_TIME")
//    private Date endTime;
//
//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//    public String getStatus() {
//        return status;
//    }
//
//    public void setStatus(String status) {
//        this.status = status;
//    }
//
//    public String getEventSeqNo() {
//        return eventSeqNo;
//    }
//
//    public void setEventSeqNo(String eventSeqNo) {
//        this.eventSeqNo = eventSeqNo;
//    }
//
//    public String getEventType() {
//        return eventType;
//    }
//
//    public void setEventType(String eventType) {
//        this.eventType = eventType;
//    }
//
//    public String getSourceSubSystem() {
//        return sourceSubSystem;
//    }
//
//    public void setSourceSubSystem(String sourceSubSystem) {
//        this.sourceSubSystem = sourceSubSystem;
//    }
//
//    public String getOperationKey() {
//        return operationKey;
//    }
//
//    public void setOperationKey(String operationKey) {
//        this.operationKey = operationKey;
//    }
//
//    public String getOperationData() {
//        return operationData;
//    }
//
//    public void setOperationData(String operationData) {
//        this.operationData = operationData;
//    }
//
//    public Boolean getNotifyRequired() {
//        return notifyRequired;
//    }
//
//    public void setNotifyRequired(Boolean notifyRequired) {
//        this.notifyRequired = notifyRequired;
//    }
//
//    public String getNotifyEndpoint() {
//        return notifyEndpoint;
//    }
//
//    public void setNotifyEndpoint(String notifyEndpoint) {
//        this.notifyEndpoint = notifyEndpoint;
//    }
//
//    public Boolean getNotified() {
//        return notified;
//    }
//
//    public void setNotified(Boolean notified) {
//        this.notified = notified;
//    }
//
//    public String getOperationUser() {
//        return operationUser;
//    }
//
//    public void setOperationUser(String operationUser) {
//        this.operationUser = operationUser;
//    }
//
//    public String getProcDefId() {
//        return procDefId;
//    }
//
//    public void setProcDefId(String procDefId) {
//        this.procDefId = procDefId;
//    }
//
//    public String getProcInstId() {
//        return procInstId;
//    }
//
//    public void setProcInstId(String procInstId) {
//        this.procInstId = procInstId;
//    }
//
//    public int getPriority() {
//        return priority;
//    }
//
//    public void setPriority(int priority) {
//        this.priority = priority;
//    }
//
//    public Date getStartTime() {
//        return startTime;
//    }
//
//    public void setStartTime(Date startTime) {
//        this.startTime = startTime;
//    }
//
//    public Date getEndTime() {
//        return endTime;
//    }
//
//    public void setEndTime(Date endTime) {
//        this.endTime = endTime;
//    }
//
//    public String getProcInstKey() {
//        return procInstKey;
//    }
//
//    public void setProcInstKey(String procInstKey) {
//        this.procInstKey = procInstKey;
//    }
//    
//    
//}
