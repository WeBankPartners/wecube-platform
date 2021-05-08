package com.webank.wecube.platform.core.model.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.webank.wecube.platform.core.support.plugin.dto.DynamicEntityValueDto;

public class WorkflowInstCreationContext implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private String procDefId;
    private String procDefKey;

    private String rootEntityOid;

    private List<DynamicEntityValueDto> entities = new ArrayList<>();
    private List<TaskNodeBindInfoContext> bindings = new ArrayList<>();

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

    public List<TaskNodeBindInfoContext> getBindings() {
        return bindings;
    }

    public void setBindings(List<TaskNodeBindInfoContext> bindings) {
        this.bindings = bindings;
    }

    public void addBinding(TaskNodeBindInfoContext binding) {
        if(binding == null){
            return;
        }
        this.bindings.add(binding);
    }
    
    public DynamicEntityValueDto findByEntityDataIdOrOid(String id){
        for(DynamicEntityValueDto e : entities){
            if(StringUtils.isNoneBlank(e.getEntityDataId()) && e.getEntityDataId().equals(id)){
                return e;
            }
            
            if(e.getOid().equals(id)){
                return e;
            }
        }
        
        return null;
    }
    
    public DynamicEntityValueDto findByOid(String oid){
        for(DynamicEntityValueDto e : entities){
            if(e.getOid().equals(oid)){
                return e;
            }
        }
        
        return null;
    }
    
    public boolean entityExists(DynamicEntityValueDto entity){
        for(DynamicEntityValueDto e : entities){
            if(e.getOid().equals(entity.getOid())){
                return true;
            }
        }
        
        return false;
    }
    
    public void addEntity(DynamicEntityValueDto entity) {
        if(entity == null){
            return;
        }
        
        if(entityExists(entity)){
            return;
        }
        this.entities.add(entity);
    }
}
