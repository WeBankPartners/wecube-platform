package com.webank.wecube.platform.core.model.workflow;

import java.util.ArrayList;
import java.util.List;

import com.webank.wecube.platform.core.entity.plugin.PluginConfigInterfaces;
import com.webank.wecube.platform.core.entity.workflow.ProcDefInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.ProcInstInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeDefInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeInstInfoEntity;

public class ContextCalculationParamCollection {
    private PluginConfigInterfaces pluginConfigInterface;
    private ProcDefInfoEntity procDefInfoEntity;
    private ProcInstInfoEntity procInstEntity;
    private TaskNodeInstInfoEntity currTaskNodeInstEntity;
    private TaskNodeDefInfoEntity currTaskNodeDefEntity;
    
    private List<ContextCalculationParam> contextCalculationParams = new ArrayList<>();

    public PluginConfigInterfaces getPluginConfigInterface() {
        return pluginConfigInterface;
    }

    public void setPluginConfigInterface(PluginConfigInterfaces pluginConfigInterface) {
        this.pluginConfigInterface = pluginConfigInterface;
    }

    public ProcDefInfoEntity getProcDefInfoEntity() {
        return procDefInfoEntity;
    }

    public void setProcDefInfoEntity(ProcDefInfoEntity procDefInfoEntity) {
        this.procDefInfoEntity = procDefInfoEntity;
    }

    public ProcInstInfoEntity getProcInstEntity() {
        return procInstEntity;
    }

    public void setProcInstEntity(ProcInstInfoEntity procInstEntity) {
        this.procInstEntity = procInstEntity;
    }

    public TaskNodeInstInfoEntity getCurrTaskNodeInstEntity() {
        return currTaskNodeInstEntity;
    }

    public void setCurrTaskNodeInstEntity(TaskNodeInstInfoEntity currTaskNodeInstEntity) {
        this.currTaskNodeInstEntity = currTaskNodeInstEntity;
    }

    public TaskNodeDefInfoEntity getCurrTaskNodeDefEntity() {
        return currTaskNodeDefEntity;
    }

    public void setCurrTaskNodeDefEntity(TaskNodeDefInfoEntity currTaskNodeDefEntity) {
        this.currTaskNodeDefEntity = currTaskNodeDefEntity;
    }

    public List<ContextCalculationParam> getContextCalculationParams() {
        return contextCalculationParams;
    }

    public void setContextCalculationParams(List<ContextCalculationParam> contextCalculationParams) {
        this.contextCalculationParams = contextCalculationParams;
    }
    
    public void addContextCalculationParam(ContextCalculationParam contextCalculationParam) {
        this.contextCalculationParams.add(contextCalculationParam);
    }
}
