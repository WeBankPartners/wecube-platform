package com.webank.wecube.platform.core.service.plugin;

import java.util.Map;

import com.webank.wecube.platform.core.entity.workflow.ProcDefInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.ProcInstInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeDefInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeInstInfoEntity;

public class CoreObjectVarCalculationContext {

    private ProcDefInfoEntity procDefInfo;
    private ProcInstInfoEntity procInstInfo;
    private TaskNodeDefInfoEntity taskNodeDefInfo;
    private TaskNodeInstInfoEntity taskNodeInstInfo;

    private String rootEntityTypeId;
    private String rootEntityDataId;
    private String rootEntityFullDataId;

    private Map<Object, Object> externalCacheMap;

    public Map<Object, Object> getExternalCacheMap() {
        return externalCacheMap;
    }

    public void setExternalCacheMap(Map<Object, Object> externalCacheMap) {
        this.externalCacheMap = externalCacheMap;
    }

    public CoreObjectVarCalculationContext withExternalCacheMap(Map<Object, Object> externalCacheMap) {
        this.externalCacheMap = externalCacheMap;
        return this;
    }

    public ProcDefInfoEntity getProcDefInfo() {
        return procDefInfo;
    }

    public void setProcDefInfo(ProcDefInfoEntity procDefInfo) {
        this.procDefInfo = procDefInfo;
    }

    public ProcInstInfoEntity getProcInstInfo() {
        return procInstInfo;
    }

    public void setProcInstInfo(ProcInstInfoEntity procInstInfo) {
        this.procInstInfo = procInstInfo;
    }

    public TaskNodeDefInfoEntity getTaskNodeDefInfo() {
        return taskNodeDefInfo;
    }

    public void setTaskNodeDefInfo(TaskNodeDefInfoEntity taskNodeDefInfo) {
        this.taskNodeDefInfo = taskNodeDefInfo;
    }

    public TaskNodeInstInfoEntity getTaskNodeInstInfo() {
        return taskNodeInstInfo;
    }

    public void setTaskNodeInstInfo(TaskNodeInstInfoEntity taskNodeInstInfo) {
        this.taskNodeInstInfo = taskNodeInstInfo;
    }

    public String getRootEntityTypeId() {
        return rootEntityTypeId;
    }

    public void setRootEntityTypeId(String rootEntityTypeId) {
        this.rootEntityTypeId = rootEntityTypeId;
    }

    public String getRootEntityDataId() {
        return rootEntityDataId;
    }

    public void setRootEntityDataId(String rootEntityDataId) {
        this.rootEntityDataId = rootEntityDataId;
    }

    public String getRootEntityFullDataId() {
        return rootEntityFullDataId;
    }

    public void setRootEntityFullDataId(String rootEntityFullDataId) {
        this.rootEntityFullDataId = rootEntityFullDataId;
    }

}
