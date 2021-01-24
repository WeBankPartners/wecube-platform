package com.webank.wecube.platform.core.entity.workflow;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TaskNodeInstInfoEntity {

    public static final String NOT_STARTED_STATUS = "NotStarted";
    public static final String IN_PROGRESS_STATUS = "InProgress";
    public static final String COMPLETED_STATUS = "Completed";
    public static final String FAULTED_STATUS = "Faulted";
    public static final String TIMEOUTED_STATUS = "Timeouted";
    public static final String RISKY_STATUS = "Risky";
    public static final String INTERNALLY_TERMINATED_STATUS = "InternallyTerminated";
    
    public static final String PRE_CHECK_RESULT_RISKY = "RISKY";
    public static final String PRE_CHECK_RESULT_NONE_RISK = "NONE_RISK";
    
    public static final String BIND_STATUS_BOUND = "BOUND";

    private Integer id;

    private String createdBy;

    private Date createdTime;

    private String updatedBy;

    private Date updatedTime;

    private String oper;

    private String operGrp;

    private Integer rev;

    private String status;

    private String nodeDefId;

    private String nodeId;

    private String nodeName;

    private String nodeType;

    private String orderedNo;

    private String procDefId;

    private String procDefKey;

    private Integer procInstId;

    private String procInstKey;

    private String errMsg;
    
    private String preCheckRet;
    
    private String bindStatus;
    
    
    private transient List<ProcExecBindingEntity> nodeBindEntities = new ArrayList<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public String getOper() {
        return oper;
    }

    public void setOper(String oper) {
        this.oper = oper;
    }

    public String getOperGrp() {
        return operGrp;
    }

    public void setOperGrp(String operGrp) {
        this.operGrp = operGrp;
    }

    public Integer getRev() {
        return rev;
    }

    public void setRev(Integer rev) {
        this.rev = rev;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNodeDefId() {
        return nodeDefId;
    }

    public void setNodeDefId(String nodeDefId) {
        this.nodeDefId = nodeDefId;
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

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getOrderedNo() {
        return orderedNo;
    }

    public void setOrderedNo(String orderedNo) {
        this.orderedNo = orderedNo;
    }

    public String getProcDefId() {
        return procDefId;
    }

    public void setProcDefId(String procDefId) {
        this.procDefId = procDefId;
    }

    public String getProcDefKey() {
        return procDefKey;
    }

    public void setProcDefKey(String procDefKey) {
        this.procDefKey = procDefKey;
    }

    public Integer getProcInstId() {
        return procInstId;
    }

    public void setProcInstId(Integer procInstId) {
        this.procInstId = procInstId;
    }

    public String getProcInstKey() {
        return procInstKey;
    }

    public void setProcInstKey(String procInstKey) {
        this.procInstKey = procInstKey;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public String getPreCheckRet() {
        return preCheckRet;
    }

    public void setPreCheckRet(String preCheckRet) {
        this.preCheckRet = preCheckRet;
    }

    public List<ProcExecBindingEntity> getNodeBindEntities() {
        return nodeBindEntities;
    }

    public void setNodeBindEntities(List<ProcExecBindingEntity> nodeBindEntities) {
        this.nodeBindEntities = nodeBindEntities;
    }
    
    public void addNodeBindEntity(ProcExecBindingEntity nodeBindEntity){
        if(nodeBindEntity == null){
            return;
        }
        
        if(this.nodeBindEntities == null){
            this.nodeBindEntities = new ArrayList<>();
        }
        
        this.nodeBindEntities.add(nodeBindEntity);
    }

    public String getBindStatus() {
        return bindStatus;
    }

    public void setBindStatus(String bindStatus) {
        this.bindStatus = bindStatus;
    }
    
    
}
