//package com.webank.wecube.platform.core.domain;
//
//import java.sql.Timestamp;
//import java.text.ParseException;
//import java.util.List;
//
//import javax.persistence.*;
//
//import com.fasterxml.jackson.annotation.JsonManagedReference;
//import com.webank.wecube.platform.core.support.DomainIdBuilder;
//
//@Entity
//@Table(name = "batch_execution_jobs")
//public class BatchExecutionJob {
//    @Id
//    private String id;
//
//    @JsonManagedReference
//    @OneToMany(mappedBy = "batchExecutionJob", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
//    private List<ExecutionJob> jobs;
//
//    @Column
//    private Timestamp createTimestamp;
//
//    @Column
//    private String creator;
//
//    @Column
//    private Timestamp completeTimestamp;
//
//    @PrePersist
//    public void initId() throws ParseException {
//        this.id = DomainIdBuilder.buildDomainId(this);
//    }
//
//    public String getId() {
//        return id;
//    }
//
//    public void setId(String id) {
//        this.id = id;
//    }
//
//    public Timestamp getCreateTimestamp() {
//        return createTimestamp;
//    }
//
//    public void setCreateTimestamp(Timestamp createTimestamp) {
//        this.createTimestamp = createTimestamp;
//    }
//
//    public Timestamp getCompleteTimestamp() {
//        return completeTimestamp;
//    }
//
//    public void setCompleteTimestamp(Timestamp completeTimestamp) {
//        this.completeTimestamp = completeTimestamp;
//    }
//
//    public List<ExecutionJob> getJobs() {
//        return jobs;
//    }
//
//    public void setJobs(List<ExecutionJob> jobs) {
//        this.jobs = jobs;
//    }
//
//    public String getCreator() {
//        return creator;
//    }
//
//    public void setCreator(String creator) {
//        this.creator = creator;
//    }
//
//    public BatchExecutionJob(List<ExecutionJob> jobs) {
//        super();
//        this.jobs = jobs;
//    }
//
//    public BatchExecutionJob() {
//    }
//
//	@Override
//	public String toString() {
//		StringBuilder builder = new StringBuilder();
//		builder.append("BatchExecutionJob [id=");
//		builder.append(id);
//		builder.append(", createTimestamp=");
//		builder.append(createTimestamp);
//		builder.append(", creator=");
//		builder.append(creator);
//		builder.append(", completeTimestamp=");
//		builder.append(completeTimestamp);
//		builder.append("]");
//		return builder.toString();
//	}
//    
//    
//}
