//package com.webank.wecube.platform.core.entity.workflow;
//
//import java.util.Date;
//
//import javax.persistence.Column;
//import javax.persistence.MappedSuperclass;
//
//@MappedSuperclass
//public abstract class AbstractTraceableEntity {
//    @Column(name = "CREATED_BY")
//    private String createdBy;
//
//    @Column(name = "UPDATED_BY")
//    private String updatedBy;
//
//    @Column(name = "CREATED_TIME")
//    private Date createdTime = new Date();
//
//    @Column(name = "UPDATED_TIME")
//    private Date updatedTime;
//
//    public String getCreatedBy() {
//        return createdBy;
//    }
//
//    public void setCreatedBy(String createdBy) {
//        this.createdBy = createdBy;
//    }
//
//    public String getUpdatedBy() {
//        return updatedBy;
//    }
//
//    public void setUpdatedBy(String updatedBy) {
//        this.updatedBy = updatedBy;
//    }
//
//    public Date getCreatedTime() {
//        return createdTime;
//    }
//
//    public void setCreatedTime(Date createdTime) {
//        this.createdTime = createdTime;
//    }
//
//    public Date getUpdatedTime() {
//        return updatedTime;
//    }
//
//    public void setUpdatedTime(Date updatedTime) {
//        this.updatedTime = updatedTime;
//    }
//
//}
