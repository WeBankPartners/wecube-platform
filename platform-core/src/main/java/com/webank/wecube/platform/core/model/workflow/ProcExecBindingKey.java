package com.webank.wecube.platform.core.model.workflow;

import com.webank.wecube.platform.core.entity.workflow.ProcExecBindingEntity;

public class ProcExecBindingKey {
    private String taskNodeId;
    private ProcExecBindingEntity procExecBinding;

    public String getTaskNodeId() {
        return taskNodeId;
    }

    public void setTaskNodeId(String taskNodeId) {
        this.taskNodeId = taskNodeId;
    }

    public ProcExecBindingEntity getProcExecBinding() {
        return procExecBinding;
    }

    public void setProcExecBinding(ProcExecBindingEntity procExecBinding) {
        this.procExecBinding = procExecBinding;
    }

}
