package com.webank.wecube.platform.core.domain;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.webank.wecube.platform.core.utils.JsonUtils;

@Entity
@Table(name = "execution_jobs")
public class ExecutionJob {
    public static final String ERROR_CODE_SUCCESSFUL = "0";
    public static final String ERROR_CODE_FAILED = "1";

    @Id
    @GeneratedValue
    private Integer id;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "batch_execution_job_id")
    private BatchExecutionJob batchExecutionJob;

    @JsonManagedReference
    @OneToMany(mappedBy = "executionJob", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ExecutionJobParameter> parameters;

    @Column
    private String pluginConfigInterfaceId;
    @Column
    private String packageName;
    @Column
    private String entityName;

    @Column
    private String businessKey;
    @Column
    private String rootEntityId;

    @Column
    private String executeTime;

    @Column
    private String completeTime;
    @Column
    private String errorCode;
    @Column
    private String errorMessage;
    @Column
    private String returnJson;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPluginConfigInterfaceId() {
        return pluginConfigInterfaceId;
    }

    public void setPluginConfigInterfaceId(String pluginConfigInterfaceId) {
        this.pluginConfigInterfaceId = pluginConfigInterfaceId;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getExecuteTime() {
        return executeTime;
    }

    public void setExecuteTime(String executeTime) {
        this.executeTime = executeTime;
    }

    public String getCompleteTime() {
        return completeTime;
    }

    public void setCompleteTime(String completeTime) {
        this.completeTime = completeTime;
    }

    public BatchExecutionJob getBatchExecutionJob() {
        return batchExecutionJob;
    }

    public void setBatchExecutionJob(BatchExecutionJob batchExecutionJob) {
        this.batchExecutionJob = batchExecutionJob;
    }

    public List<ExecutionJobParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<ExecutionJobParameter> parameters) {
        this.parameters = parameters;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public void setBusinessKey(String businessKey) {
        this.businessKey = businessKey;
    }

    public ExecutionJob(String rootEntityid, String pluginConfigInterfaceId, String packageName, String entityName,
            String businessKey) {
        super();
        this.pluginConfigInterfaceId = pluginConfigInterfaceId;
        this.packageName = packageName;
        this.entityName = entityName;
        this.businessKey = businessKey;
        this.rootEntityId = rootEntityid;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getReturnJson() {
        return returnJson;
    }

    public void setReturnJson(String returnJson) {
        this.returnJson = returnJson;
    }

    public void setErrorWithMessage(String errorMessage) {
        this.errorCode = ERROR_CODE_FAILED;
        this.errorMessage = errorMessage;
        this.returnJson = JsonUtils.toJsonString(this);
    }

    @Override
    public String toString() {
        return "ExecutionJob [id=" + id + ", batchExecutionJob=" + batchExecutionJob + ", parameters=" + parameters
                + ", pluginConfigInterfaceId=" + pluginConfigInterfaceId + ", packageName=" + packageName
                + ", entityName=" + entityName + ", rootEntityId=" + businessKey + ", executeTime=" + executeTime
                + ", completeTime=" + completeTime + ", errorCode=" + errorCode + ", errorMessage=" + errorMessage
                + ", returnJson=" + returnJson + "]";
    }

    public String getRootEntityId() {
        return rootEntityId;
    }

    public void setRootEntityId(String rootEntityId) {
        this.rootEntityId = rootEntityId;
    }

}
