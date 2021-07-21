package com.webank.wecube.platform.core.model.workflow;

import java.util.ArrayList;
import java.util.List;

import com.webank.wecube.platform.core.entity.plugin.PluginConfigInterfaceParameters;
import com.webank.wecube.platform.core.entity.plugin.PluginConfigInterfaces;
import com.webank.wecube.platform.core.entity.workflow.ProcInstInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeDefInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeExecParamEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeExecRequestEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeInstInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeParamEntity;

public class ContextCalculationParam {
    private String paramName;
    private String paramType;
    private PluginConfigInterfaceParameters param;
    private ProcInstInfoEntity procInstEntity;
    private TaskNodeInstInfoEntity currTaskNodeInstEntity;
    private TaskNodeDefInfoEntity currTaskNodeDefEntity;
    private PluginConfigInterfaces pluginConfigInterface;
    private TaskNodeParamEntity nodeParamEntity;
    private TaskNodeInstInfoEntity boundNodeInstEntity;
    private TaskNodeDefInfoEntity boundNodeDefEntity;
    private TaskNodeExecRequestEntity boundNodeRequestEntity;
    private List<TaskNodeExecParamEntity> boundExecParamEntities = new ArrayList<>();
    public String getParamName() {
        return paramName;
    }
    public void setParamName(String paramName) {
        this.paramName = paramName;
    }
    public String getParamType() {
        return paramType;
    }
    public void setParamType(String paramType) {
        this.paramType = paramType;
    }
    public PluginConfigInterfaceParameters getParam() {
        return param;
    }
    public void setParam(PluginConfigInterfaceParameters param) {
        this.param = param;
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
    public PluginConfigInterfaces getPluginConfigInterface() {
        return pluginConfigInterface;
    }
    public void setPluginConfigInterface(PluginConfigInterfaces pluginConfigInterface) {
        this.pluginConfigInterface = pluginConfigInterface;
    }
    public TaskNodeParamEntity getNodeParamEntity() {
        return nodeParamEntity;
    }
    public void setNodeParamEntity(TaskNodeParamEntity nodeParamEntity) {
        this.nodeParamEntity = nodeParamEntity;
    }
    public TaskNodeInstInfoEntity getBoundNodeInstEntity() {
        return boundNodeInstEntity;
    }
    public void setBoundNodeInstEntity(TaskNodeInstInfoEntity boundNodeInstEntity) {
        this.boundNodeInstEntity = boundNodeInstEntity;
    }
    public TaskNodeDefInfoEntity getBoundNodeDefEntity() {
        return boundNodeDefEntity;
    }
    public void setBoundNodeDefEntity(TaskNodeDefInfoEntity boundNodeDefEntity) {
        this.boundNodeDefEntity = boundNodeDefEntity;
    }
    public TaskNodeExecRequestEntity getBoundNodeRequestEntity() {
        return boundNodeRequestEntity;
    }
    public void setBoundNodeRequestEntity(TaskNodeExecRequestEntity boundNodeRequestEntity) {
        this.boundNodeRequestEntity = boundNodeRequestEntity;
    }
    public List<TaskNodeExecParamEntity> getBoundExecParamEntities() {
        return boundExecParamEntities;
    }
    public void setBoundExecParamEntities(List<TaskNodeExecParamEntity> boundExecParamEntities) {
        this.boundExecParamEntities = boundExecParamEntities;
    }
    
    
}
