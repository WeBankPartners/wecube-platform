//package com.webank.wecube.platform.core.domain.plugin;
//
//import java.util.Date;
//
//import javax.persistence.Column;
//import javax.persistence.Entity;
//import javax.persistence.GeneratedValue;
//import javax.persistence.Id;
//import javax.persistence.Table;
//
//import org.hibernate.annotations.GenericGenerator;
//
//@Entity
//@Table(name = "plugin_artifact_pull_req")
//public class PluginArtifactPullRequestEntity {
//    public static final String STATE_IN_PROGRESS = "InProgress";
//    public static final String STATE_COMPLETED = "Completed";
//    public static final String STATE_FAULTED = "Faulted";
//
//    @Id
//    @GenericGenerator(name = "jpa-uuid", strategy = "uuid")
//    @GeneratedValue(generator = "jpa-uuid")
//    @Column(name = "ID")
//    private String id;
//
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
//    @Column(name = "BUCKET_NAME")
//    private String bucketName;
//
//    @Column(name = "KEY_NAME")
//    private String keyName;
//
//    @Column(name = "TOTAL_SIZE")
//    private Long totalSize;
//
//    @Column(name = "STATE")
//    private String state;
//
//    @Column(name = "ERR_MSG")
//    private String errorMsg;
//
//    @Column(name = "REV")
//    private Integer rev;
//
//    @Column(name = "PKG_ID")
//    private String packageId;
//
//    public String getId() {
//        return id;
//    }
//
//    public void setId(String id) {
//        this.id = id;
//    }
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
//    public String getBucketName() {
//        return bucketName;
//    }
//
//    public void setBucketName(String bucketName) {
//        this.bucketName = bucketName;
//    }
//
//    public String getKeyName() {
//        return keyName;
//    }
//
//    public void setKeyName(String keyName) {
//        this.keyName = keyName;
//    }
//
//    public Long getTotalSize() {
//        return totalSize;
//    }
//
//    public void setTotalSize(Long totalSize) {
//        this.totalSize = totalSize;
//    }
//
//    public String getState() {
//        return state;
//    }
//
//    public void setState(String state) {
//        this.state = state;
//    }
//
//    public String getErrorMsg() {
//        return errorMsg;
//    }
//
//    public void setErrorMsg(String errorMsg) {
//        this.errorMsg = errorMsg;
//    }
//
//    public Integer getRev() {
//        return rev;
//    }
//
//    public void setRev(Integer rev) {
//        this.rev = rev;
//    }
//
//    public String getPackageId() {
//        return packageId;
//    }
//
//    public void setPackageId(String packageId) {
//        this.packageId = packageId;
//    }
//
//}
