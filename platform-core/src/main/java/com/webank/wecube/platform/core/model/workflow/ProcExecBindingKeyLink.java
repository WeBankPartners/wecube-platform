package com.webank.wecube.platform.core.model.workflow;

import java.util.ArrayList;
import java.util.List;

import com.webank.wecube.platform.core.entity.workflow.ProcExecBindingEntity;

public class ProcExecBindingKeyLink {

    private List<ProcExecBindingEntity> procExecBindingKeys = new ArrayList<>();

    public List<ProcExecBindingEntity> getProcExecBindingKeys() {
        return procExecBindingKeys;
    }

    public void setProcExecBindingKeys(List<ProcExecBindingEntity> procExecBindingKeys) {
        this.procExecBindingKeys = procExecBindingKeys;
    }
    
    public void addProcExecBindingKey(ProcExecBindingEntity procExecBindingKey) {
        this.procExecBindingKeys.add(procExecBindingKey);
    }
}
