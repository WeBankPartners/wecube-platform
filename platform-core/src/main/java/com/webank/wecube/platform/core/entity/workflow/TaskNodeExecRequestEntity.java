package com.webank.wecube.platform.core.entity.workflow;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.webank.wecube.platform.core.entity.BaseTraceableEntity;

@Entity
@Table(name = "CORE_RU_TASK_NODE_EXEC_REQ")
public class TaskNodeExecRequestEntity extends BaseTraceableEntity {
    @Id
    @Column(name = "REQ_ID")
    private String requestId;

    @Column(name = "NODE_INST_ID")
    private Integer nodeInstId;

    @Column(name = "REQ_URL")
    private String requestUrl;

    @Column(name = "ERR_CODE")
    private String errorCode;

    @Column(name = "ERR_MSG")
    private String errorMessage;

    @Column(name = "IS_CURRENT")
    private Boolean current = true;

    @Column(name = "IS_COMPLETED")
    private Boolean completed = false;

    @Column(name = "PROC_DEF_KERNEL_ID")
    private String procDefKernelId;
    
    @Column(name = "PROC_DEF_KERNEL_KEY")
    private String procDefKernelKey;
    
    @Column(name = "PROC_DEF_VER")
    private Integer procDefVersion;
    
    @Column(name = "PROC_INST_KERNEL_ID")
    private String procInstKernelId;

    @Column(name = "PROC_INST_KERNEL_KEY")
    private String procInstKernelKey;

    @Column(name = "NODE_ID")
    private String nodeId;
    
    @Column(name = "NODE_NAME")
    private String nodeName;
    
    @Column(name = "EXECUTION_ID")
    private String executionId;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Integer getNodeInstId() {
        return nodeInstId;
    }

    public void setNodeInstId(Integer nodeInstId) {
        this.nodeInstId = nodeInstId;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
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

    public Boolean isCurrent() {
        return current;
    }

    public void setCurrent(Boolean current) {
        this.current = current;
    }

    public Boolean isCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public String getProcDefKernelId() {
        return procDefKernelId;
    }

    public void setProcDefKernelId(String procDefKernelId) {
        this.procDefKernelId = procDefKernelId;
    }

    public String getProcDefKernelKey() {
        return procDefKernelKey;
    }

    public void setProcDefKernelKey(String procDefKernelKey) {
        this.procDefKernelKey = procDefKernelKey;
    }

    public Integer getProcDefVersion() {
        return procDefVersion;
    }

    public void setProcDefVersion(Integer procDefVersion) {
        this.procDefVersion = procDefVersion;
    }

    public String getProcInstKernelId() {
        return procInstKernelId;
    }

    public void setProcInstKernelId(String procInstKernelId) {
        this.procInstKernelId = procInstKernelId;
    }

    public String getProcInstKernelKey() {
        return procInstKernelKey;
    }

    public void setProcInstKernelKey(String procInstKernelKey) {
        this.procInstKernelKey = procInstKernelKey;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public Boolean getCurrent() {
        return current;
    }

    public Boolean getCompleted() {
        return completed;
    }

}
