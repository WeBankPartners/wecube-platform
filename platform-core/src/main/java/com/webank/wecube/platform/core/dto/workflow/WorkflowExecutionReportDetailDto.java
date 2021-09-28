package com.webank.wecube.platform.core.dto.workflow;

import java.util.ArrayList;
import java.util.List;

public class WorkflowExecutionReportDetailDto {
    private String procDefId;
    private String procDefName;
    private String procExecDate;
    private String procExecOper;
    private String procStatus;
    private String nodeDefId;
    private String nodeDefName;
    private String nodeExecDate;
    private String nodeStatus;

    private String serviceId;

    private String entityDataId;
    private String reqId;
    
    private String execDate;

    private List<TaskNodeExecParamDto> execParams = new ArrayList<>();

    public String getProcDefId() {
        return procDefId;
    }

    public void setProcDefId(String procDefId) {
        this.procDefId = procDefId;
    }

    public String getProcDefName() {
        return procDefName;
    }

    public void setProcDefName(String procDefName) {
        this.procDefName = procDefName;
    }

    public String getProcExecDate() {
        return procExecDate;
    }

    public void setProcExecDate(String procExecDate) {
        this.procExecDate = procExecDate;
    }

    public String getProcExecOper() {
        return procExecOper;
    }

    public void setProcExecOper(String procExecOper) {
        this.procExecOper = procExecOper;
    }

    public String getProcStatus() {
        return procStatus;
    }

    public void setProcStatus(String procStatus) {
        this.procStatus = procStatus;
    }

    public String getNodeDefId() {
        return nodeDefId;
    }

    public void setNodeDefId(String nodeDefId) {
        this.nodeDefId = nodeDefId;
    }

    public String getNodeDefName() {
        return nodeDefName;
    }

    public void setNodeDefName(String nodeDefName) {
        this.nodeDefName = nodeDefName;
    }

    public String getNodeExecDate() {
        return nodeExecDate;
    }

    public void setNodeExecDate(String nodeExecDate) {
        this.nodeExecDate = nodeExecDate;
    }

    public String getNodeStatus() {
        return nodeStatus;
    }

    public void setNodeStatus(String nodeStatus) {
        this.nodeStatus = nodeStatus;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getEntityDataId() {
        return entityDataId;
    }

    public void setEntityDataId(String entityDataId) {
        this.entityDataId = entityDataId;
    }

    public List<TaskNodeExecParamDto> getExecParams() {
        return execParams;
    }

    public void setExecParams(List<TaskNodeExecParamDto> execParams) {
        this.execParams = execParams;
    }

    public String getReqId() {
        return reqId;
    }

    public void setReqId(String reqId) {
        this.reqId = reqId;
    }

    public String getExecDate() {
        return execDate;
    }

    public void setExecDate(String execDate) {
        this.execDate = execDate;
    }

    
}
