package com.webank.wecube.platform.core.entity.workflow;

import java.util.Date;

public class TaskNodeExecRequestEntity  {
    private String reqId;

    private String createdBy;

    private Date createdTime;

    private String updatedBy;

    private Date updatedTime;

    private Boolean isCompleted = false;

    private Boolean isCurrent;

    private String errCode;

    private String errMsg;

    private Integer nodeInstId;

    private String reqUrl;

    private String executionId;

    private String nodeId;

    private String nodeName;

    private String procDefKernelId;

    private String procDefKernelKey;

    private Integer procDefVer;

    private String procInstKernelId;

    private String procInstKernelKey;
    
  //#2233
    private String contextData;
  //#2233
    private Integer reqObjectAmount;
  //#2233
    private Integer respObjectAmount;

    public String getReqId() {
        return reqId;
    }

    public void setReqId(String reqId) {
        this.reqId = reqId;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }

    public Boolean getIsCompleted() {
        return isCompleted;
    }

    public void setIsCompleted(Boolean isCompleted) {
        this.isCompleted = isCompleted;
    }

    public Boolean getIsCurrent() {
        return isCurrent;
    }

    public void setIsCurrent(Boolean isCurrent) {
        this.isCurrent = isCurrent;
    }

    public String getErrCode() {
        return errCode;
    }

    public void setErrCode(String errCode) {
        this.errCode = errCode;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public Integer getNodeInstId() {
        return nodeInstId;
    }

    public void setNodeInstId(Integer nodeInstId) {
        this.nodeInstId = nodeInstId;
    }

    public String getReqUrl() {
        return reqUrl;
    }

    public void setReqUrl(String reqUrl) {
        this.reqUrl = reqUrl;
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
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

    public Integer getProcDefVer() {
        return procDefVer;
    }

    public void setProcDefVer(Integer procDefVer) {
        this.procDefVer = procDefVer;
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

    public String getContextData() {
        return contextData;
    }

    public void setContextData(String contextData) {
        this.contextData = contextData;
    }

    public Integer getReqObjectAmount() {
        return reqObjectAmount;
    }

    public void setReqObjectAmount(Integer reqObjectAmount) {
        this.reqObjectAmount = reqObjectAmount;
    }

    public Integer getRespObjectAmount() {
        return respObjectAmount;
    }

    public void setRespObjectAmount(Integer respObjectAmount) {
        this.respObjectAmount = respObjectAmount;
    }

    
}
