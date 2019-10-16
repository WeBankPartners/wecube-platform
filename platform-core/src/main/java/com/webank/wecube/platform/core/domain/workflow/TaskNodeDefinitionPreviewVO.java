package com.webank.wecube.platform.core.domain.workflow;

public class TaskNodeDefinitionPreviewVO {
    private String rootCiTypeId;
    private String rootCiDataId;

    private String taskNodeId;
    private String procDefKey;

    public String getRootCiTypeId() {
        return rootCiTypeId;
    }

    public void setRootCiTypeId(String rootCiTypeId) {
        this.rootCiTypeId = rootCiTypeId;
    }

    public String getRootCiDataId() {
        return rootCiDataId;
    }

    public void setRootCiDataId(String rootCiDataId) {
        this.rootCiDataId = rootCiDataId;
    }

    public String getTaskNodeId() {
        return taskNodeId;
    }

    public void setTaskNodeId(String taskNodeId) {
        this.taskNodeId = taskNodeId;
    }

    public String getProcDefKey() {
        return procDefKey;
    }

    public void setProcDefKey(String procDefKey) {
        this.procDefKey = procDefKey;
    }

}
