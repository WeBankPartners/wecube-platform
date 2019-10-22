package com.webank.wecube.platform.core.entity.workflow;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "CORE_RU_TASK_NODE_EXEC_LOG")
public class TaskNodeExecLogEntity {
    public static final String ERR_CODE_OK = "0";
    public static final String ERR_CODE_ERR = "1";

    @Id
    @Column(name = "ID")
    @GeneratedValue
    private Integer id;

    @Column(name = "INST_ID")
    private String instanceId;

    @Column(name = "INST_KEY")
    private String instanceBusinessKey;

    @Column(name = "EXEC_ID")
    private String executionId;

    @Column(name = "NODE_ID")
    private String taskNodeId;

    @Column(name = "SERV_NAME")
    private String serviceName;
    
    @Column(name = "ROOT_CI_TYPE")
    private Integer rootCiTypeId;

    @Column(name = "NODE_STATUS")
    private String taskNodeStatus;

    @Column(name = "ERR_CODE")
    private String errCode;

    @Column(name = "ERR_MSG")
    private String errMsg;
    @Column(name = "PRE_STATUS")
    private String preStatus;
    @Column(name = "POST_STATUS")
    private String postStatus;

    @Column(name = "CREATED_BY")
    private String createdBy;
    @Column(name = "CREATED_TIME")
    private Date createdTime;
    @Column(name = "UPDATED_BY")
    private String updatedBy;
    @Column(name = "UPDATED_TIME")
    private Date updatedTime;

    @Column(name = "REQ_DATA")
    private String requestData;
    @Column(name = "RESP_DATA")
    private String responseData;

    @Column(name = "REQ_URL")
    private String requestUrl;

    @OneToMany(mappedBy = "taskNodeExecLog", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<TaskNodeExecVariableEntity> taskNodeExecVariables = new ArrayList<TaskNodeExecVariableEntity>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getInstanceBusinessKey() {
        return instanceBusinessKey;
    }

    public void setInstanceBusinessKey(String instanceBusinessKey) {
        this.instanceBusinessKey = instanceBusinessKey;
    }

    public String getTaskNodeId() {
        return taskNodeId;
    }

    public void setTaskNodeId(String taskNodeId) {
        this.taskNodeId = taskNodeId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getTaskNodeStatus() {
        return taskNodeStatus;
    }

    public void setTaskNodeStatus(String taskNodeStatus) {
        this.taskNodeStatus = taskNodeStatus;
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

    public String getPreStatus() {
        return preStatus;
    }

    public void setPreStatus(String preStatus) {
        this.preStatus = preStatus;
    }

    public String getPostStatus() {
        return postStatus;
    }

    public void setPostStatus(String postStatus) {
        this.postStatus = postStatus;
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

    public String getRequestData() {
        return requestData;
    }

    public void setRequestData(String requestData) {
        this.requestData = requestData;
    }

    public String getResponseData() {
        return responseData;
    }

    public void setResponseData(String responseData) {
        this.responseData = responseData;
    }

    public List<TaskNodeExecVariableEntity> getTaskNodeExecVariables() {
        return taskNodeExecVariables;
    }

    public void setTaskNodeExecVariables(List<TaskNodeExecVariableEntity> taskNodeExecVariables) {
        this.taskNodeExecVariables = taskNodeExecVariables;
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public Integer getRootCiTypeId() {
        return rootCiTypeId;
    }

    public void setRootCiTypeId(Integer rootCiTypeId) {
        this.rootCiTypeId = rootCiTypeId;
    }
}
