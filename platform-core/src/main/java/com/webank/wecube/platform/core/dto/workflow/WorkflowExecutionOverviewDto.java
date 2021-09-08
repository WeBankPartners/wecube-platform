package com.webank.wecube.platform.core.dto.workflow;

public class WorkflowExecutionOverviewDto {
    private String procDefId;
    private String procDefName;

    private int totalInstances;
    private int totalInProgressInstances;
    private int totalCompletedInstances;
    private int totalFaultedInstances;

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

    public int getTotalInstances() {
        return totalInstances;
    }

    public void setTotalInstances(int totalInstances) {
        this.totalInstances = totalInstances;
    }

    public int getTotalInProgressInstances() {
        return totalInProgressInstances;
    }

    public void setTotalInProgressInstances(int totalInProgressInstances) {
        this.totalInProgressInstances = totalInProgressInstances;
    }

    public int getTotalCompletedInstances() {
        return totalCompletedInstances;
    }

    public void setTotalCompletedInstances(int totalCompletedInstances) {
        this.totalCompletedInstances = totalCompletedInstances;
    }

    public int getTotalFaultedInstances() {
        return totalFaultedInstances;
    }

    public void setTotalFaultedInstances(int totalFaultedInstances) {
        this.totalFaultedInstances = totalFaultedInstances;
    }

}
