package com.webank.wecube.platform.core.model.workflow;

import com.webank.wecube.platform.core.entity.plugin.PluginConfigInterfaceParameters;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeExecParamEntity;

public class BoundTaskNodeExecParamWrapper {
    private TaskNodeExecParamEntity boundTaskNodeExecParamEntity;
    private PluginConfigInterfaceParameters boundParam;

    public TaskNodeExecParamEntity getBoundTaskNodeExecParamEntity() {
        return boundTaskNodeExecParamEntity;
    }

    public void setBoundTaskNodeExecParamEntity(TaskNodeExecParamEntity boundTaskNodeExecParamEntity) {
        this.boundTaskNodeExecParamEntity = boundTaskNodeExecParamEntity;
    }

    public PluginConfigInterfaceParameters getBoundParam() {
        return boundParam;
    }

    public void setBoundParam(PluginConfigInterfaceParameters boundParam) {
        this.boundParam = boundParam;
    }

}
