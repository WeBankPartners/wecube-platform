package com.webank.wecube.platform.core.dto.workflow;

public class WorkflowExecutionReportItemDto {

    private String procDefId;
    private String procDefName;
    private String nodeDefId;
    private String nodeDefName;

    private String serviceId;

    private String entityDataId;
    private String entityDataName;

    private int failureCount;
    private int successCount;

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

    public String getEntityDataName() {
        return entityDataName;
    }

    public void setEntityDataName(String entityDataName) {
        this.entityDataName = entityDataName;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(int failureCount) {
        this.failureCount = failureCount;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[procDefId=");
        builder.append(procDefId);
        builder.append(", procDefName=");
        builder.append(procDefName);
        builder.append(", nodeDefId=");
        builder.append(nodeDefId);
        builder.append(", nodeDefName=");
        builder.append(nodeDefName);
        builder.append(", serviceId=");
        builder.append(serviceId);
        builder.append(", entityDataId=");
        builder.append(entityDataId);
        builder.append(", entityDataName=");
        builder.append(entityDataName);
        builder.append(", failureCount=");
        builder.append(failureCount);
        builder.append(", successCount=");
        builder.append(successCount);
        builder.append("]");
        return builder.toString();
    }

    
}
