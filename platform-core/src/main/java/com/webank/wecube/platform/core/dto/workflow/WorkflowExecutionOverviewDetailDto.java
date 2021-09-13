package com.webank.wecube.platform.core.dto.workflow;

public class WorkflowExecutionOverviewDetailDto {

    private String procDefId;
    private String procDefName;
    private String procInstId;
    private String status;
    private String execOper;
    private String execStartDate;
    private String execEndDate;
    private String rootEntityDataId;
    private String rootEntityDataName;

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

    public String getProcInstId() {
        return procInstId;
    }

    public void setProcInstId(String procInstId) {
        this.procInstId = procInstId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getExecOper() {
        return execOper;
    }

    public void setExecOper(String execOper) {
        this.execOper = execOper;
    }

    public String getExecStartDate() {
        return execStartDate;
    }

    public void setExecStartDate(String execStartDate) {
        this.execStartDate = execStartDate;
    }

    public String getExecEndDate() {
        return execEndDate;
    }

    public void setExecEndDate(String execEndDate) {
        this.execEndDate = execEndDate;
    }

    public String getRootEntityDataId() {
        return rootEntityDataId;
    }

    public void setRootEntityDataId(String rootEntityDataId) {
        this.rootEntityDataId = rootEntityDataId;
    }

    public String getRootEntityDataName() {
        return rootEntityDataName;
    }

    public void setRootEntityDataName(String rootEntityDataName) {
        this.rootEntityDataName = rootEntityDataName;
    }

}
