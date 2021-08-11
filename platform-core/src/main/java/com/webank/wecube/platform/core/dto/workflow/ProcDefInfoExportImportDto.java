package com.webank.wecube.platform.core.dto.workflow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProcDefInfoExportImportDto {
    private String procDefId;
    private String procDefKey;
    private String procDefName;
    private String procDefVersion;
    private String status;

    private String procDefData;
    private String rootEntity;

    private String createdTime;

    private String excludeMode;

    private Map<String, List<Long>> permissionToRole;

    private List<TaskNodeDefInfoDto> taskNodeInfos = new ArrayList<>();
    
    private String tags;

    public String getProcDefId() {
        return procDefId;
    }

    public void setProcDefId(String procDefId) {
        this.procDefId = procDefId;
    }

    public String getProcDefKey() {
        return procDefKey;
    }

    public void setProcDefKey(String procDefKey) {
        this.procDefKey = procDefKey;
    }

    public String getProcDefName() {
        return procDefName;
    }

    public void setProcDefName(String procDefName) {
        this.procDefName = procDefName;
    }

    public String getProcDefVersion() {
        return procDefVersion;
    }

    public void setProcDefVersion(String procDefVersion) {
        this.procDefVersion = procDefVersion;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProcDefData() {
        return procDefData;
    }

    public void setProcDefData(String procDefData) {
        this.procDefData = procDefData;
    }

    public String getRootEntity() {
        return rootEntity;
    }

    public void setRootEntity(String rootEntity) {
        this.rootEntity = rootEntity;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public Map<String, List<Long>> getPermissionToRole() {
        return permissionToRole;
    }

    public void setPermissionToRole(Map<String, List<Long>> permissionToRole) {
        this.permissionToRole = permissionToRole;
    }

    public List<TaskNodeDefInfoDto> getTaskNodeInfos() {
        return taskNodeInfos;
    }

    public void setTaskNodeInfos(List<TaskNodeDefInfoDto> taskNodeInfos) {
        this.taskNodeInfos = taskNodeInfos;
    }

    public ProcDefInfoExportImportDto addTaskNodeInfos(TaskNodeDefInfoDto... infos) {
        if (infos == null) {
            return this;
        }

        for (TaskNodeDefInfoDto info : infos) {
            this.taskNodeInfos.add(info);
        }

        return this;
    }

    public String getExcludeMode() {
        return excludeMode;
    }

    public void setExcludeMode(String excludeMode) {
        this.excludeMode = excludeMode;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }
    
    

}
