package com.webank.wecube.platform.core.entity.plugin;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BatchExecutionJobs {
    private String id;

    private Date createTimestamp;

    private Date completeTimestamp;

    private String creator;
    
    private transient List<ExecutionJobs> jobs = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public Date getCreateTimestamp() {
        return createTimestamp;
    }

    public void setCreateTimestamp(Date createTimestamp) {
        this.createTimestamp = createTimestamp;
    }

    public Date getCompleteTimestamp() {
        return completeTimestamp;
    }

    public void setCompleteTimestamp(Date completeTimestamp) {
        this.completeTimestamp = completeTimestamp;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator == null ? null : creator.trim();
    }

    public List<ExecutionJobs> getJobs() {
        return jobs;
    }

    public void setJobs(List<ExecutionJobs> jobs) {
        this.jobs = jobs;
    }
    
    public void addJobs(ExecutionJobs job){
        if(job == null){
            return;
        }
        
        if(this.jobs == null){
            this.jobs = new ArrayList<>();
        }
        
        this.jobs.add(job);
    }
}