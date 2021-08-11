package com.webank.wecube.platform.core.model.workflow;

import java.util.ArrayList;
import java.util.List;

public class ProcExecBindingKeyLink {

    private List<ProcExecBindingKey> procExecBindingKeys = new ArrayList<>();

    public List<ProcExecBindingKey> getProcExecBindingKeys() {
        return procExecBindingKeys;
    }

    public void setProcExecBindingKeys(List<ProcExecBindingKey> procExecBindingKeys) {
        this.procExecBindingKeys = procExecBindingKeys;
    }
    
    public void addProcExecBindingKey(ProcExecBindingKey procExecBindingKey) {
        this.procExecBindingKeys.add(procExecBindingKey);
    }
}
