package com.webank.wecube.platform.core.support.plugin.dto;

import java.util.ArrayList;
import java.util.List;

public class DynamicEntityValueDto {
    private String entityDefId;// Entity definition id from platform.
    private String packageName;
    private String entityName;
    private String entityDataId;// Existing data id,such as guid in cmdb.

    private String fullEntityDataId;
    private String oid;// Equals to dataId once dataId presents,or a temporary
                       // assigned.

    private String entityDataState;// NotCreated,Created,Deleted
    private String entityDataOp;// create,update,delete

    private List<String> previousOids = new ArrayList<>();
    private List<String> succeedingOids = new ArrayList<>();

    private List<DynamicEntityAttrValueDto> attrValues = new ArrayList<>();

    private String bindFlag;// Y,N
    private boolean processed = false;

    public String getEntityDefId() {
        return entityDefId;
    }

    public void setEntityDefId(String entityDefId) {
        this.entityDefId = entityDefId;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getEntityDataId() {
        return entityDataId;
    }

    public void setEntityDataId(String entityDataId) {
        this.entityDataId = entityDataId;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public List<String> getPreviousOids() {
        return previousOids;
    }

    public void setPreviousOids(List<String> previousOids) {
        this.previousOids = previousOids;
    }

    public List<String> getSucceedingOids() {
        return succeedingOids;
    }

    public void setSucceedingOids(List<String> succeedingOids) {
        this.succeedingOids = succeedingOids;
    }

    public List<DynamicEntityAttrValueDto> getAttrValues() {
        return attrValues;
    }

    public void setAttrValues(List<DynamicEntityAttrValueDto> attrValues) {
        this.attrValues = attrValues;
    }
    
    public void addAttrValue(DynamicEntityAttrValueDto attrValue) {
        this.attrValues.add(attrValue);
    }
    
    public DynamicEntityAttrValueDto findAttrValue(String attrName){
        for(DynamicEntityAttrValueDto attrValue : attrValues){
            if(attrName.equals(attrValue.getAttrName())){
                return attrValue;
            }
        }
        
        return null;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DynamicEntityValueDto [entityDefId=");
        builder.append(entityDefId);
        builder.append(", packageName=");
        builder.append(packageName);
        builder.append(", entityName=");
        builder.append(entityName);
        builder.append(", entityDataId=");
        builder.append(entityDataId);
        builder.append(", oid=");
        builder.append(oid);
        builder.append(", previousOids=");
        builder.append(previousOids);
        builder.append(", succeedingOids=");
        builder.append(succeedingOids);
        builder.append(", attrValues=");
        builder.append(attrValues);
        builder.append("]");
        return builder.toString();
    }

    public String getFullEntityDataId() {
        return fullEntityDataId;
    }

    public void setFullEntityDataId(String fullEntityDataId) {
        this.fullEntityDataId = fullEntityDataId;
    }

    public String getEntityDataState() {
        return entityDataState;
    }

    public void setEntityDataState(String entityDataState) {
        this.entityDataState = entityDataState;
    }

    public String getEntityDataOp() {
        return entityDataOp;
    }

    public void setEntityDataOp(String entityDataOp) {
        this.entityDataOp = entityDataOp;
    }

    public String getBindFlag() {
        return bindFlag;
    }

    public void setBindFlag(String bindFlag) {
        this.bindFlag = bindFlag;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

}
