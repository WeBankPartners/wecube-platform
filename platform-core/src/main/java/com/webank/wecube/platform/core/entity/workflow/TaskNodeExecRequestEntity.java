package com.webank.wecube.platform.core.entity.workflow;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

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

}
