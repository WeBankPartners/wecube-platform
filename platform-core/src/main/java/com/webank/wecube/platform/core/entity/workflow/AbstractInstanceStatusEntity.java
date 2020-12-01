//package com.webank.wecube.platform.core.entity.workflow;
//
//import javax.persistence.Column;
//import javax.persistence.GeneratedValue;
//import javax.persistence.Id;
//import javax.persistence.MappedSuperclass;
//
//@MappedSuperclass
//public abstract class AbstractInstanceStatusEntity extends AbstractTraceableEntity {
//    public static final String NOT_STARTED_STATUS = "NotStarted";
//    public static final String IN_PROGRESS_STATUS = "InProgress";
//    public static final String COMPLETED_STATUS = "Completed";
//    public static final String FAULTED_STATUS = "Faulted";
//    public static final String TIMEOUTED_STATUS = "Timeouted";
//
//    @Id
//    @Column(name = "ID")
//    @GeneratedValue
//    private Integer id;
//
//    @Column(name = "REV")
//    private int revision;
//
//    @Column(name = "STATUS")
//    private String status;
//    
//    @Column(name = "OPER")
//    private String operator;
//
//    @Column(name = "OPER_GRP")
//    private String operatorGroup;
//
//    public int getRevision() {
//        return revision;
//    }
//
//    public void setRevision(int revision) {
//        this.revision = revision;
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
//    public Integer getId() {
//        return id;
//    }
//
//    public void setId(Integer id) {
//        this.id = id;
//    }
//
//    public String getOperator() {
//        return operator;
//    }
//
//    public void setOperator(String operator) {
//        this.operator = operator;
//    }
//
//    public String getOperatorGroup() {
//        return operatorGroup;
//    }
//
//    public void setOperatorGroup(String operatorGroup) {
//        this.operatorGroup = operatorGroup;
//    }
//}
