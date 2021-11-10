package com.webank.wecube.platform.core.support.plugin.dto;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class DynamicWorkflowInstCreationInfoDto {
    private String procDefId;
    private String procDefKey;

    private String rootEntityOid;

    private List<DynamicEntityValueDto> entities = new ArrayList<>();
    private List<TaskNodeBindInfoDto> bindings = new ArrayList<>();

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

    public String getRootEntityOid() {
        return rootEntityOid;
    }

    public void setRootEntityOid(String rootEntityOid) {
        this.rootEntityOid = rootEntityOid;
    }

    public List<DynamicEntityValueDto> getEntities() {
        return entities;
    }

    public void setEntities(List<DynamicEntityValueDto> entities) {
        this.entities = entities;
    }

    public List<TaskNodeBindInfoDto> getBindings() {
        return bindings;
    }

    public void setBindings(List<TaskNodeBindInfoDto> bindings) {
        this.bindings = bindings;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DynamicWorkflowInstCreationInfoDto [procDefId=");
        builder.append(procDefId);
        builder.append(", procDefKey=");
        builder.append(procDefKey);
        builder.append(", rootEntityOid=");
        builder.append(rootEntityOid);
        builder.append(", entities=");
        builder.append(entities);
        builder.append(", bindings=");
        builder.append(bindings);
        builder.append("]");
        return builder.toString();
    }

    public DynamicEntityValueDto findByOid(String oid){
        if(StringUtils.isBlank(oid)) {
            return null;
        }
        
        if(entities == null ) {
            return null;
        }
        
        for(DynamicEntityValueDto e : entities){
            if(e.getOid().equals(oid)){
                return e;
            }
        }
        
        return null;
    }
}
