package com.webank.wecube.platform.core.dto.workflow;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DynamicEntityValueDto {
    @Nullable
    private String entityDefId;//Entity definition id from platform.
    @Nonnull
    private String packageName;
    @Nonnull
    private String entityName;
    @Nullable
    private String dataId;//Existing data id,such as guid in cmdb.
    @Nonnull
    private String oid;//Equals to dataId once dataId presents,or a temporary assigned.
    
    //TODO delete?

    private List<String> previousOids = new ArrayList<>();
    private List<String> succeedingOids = new ArrayList<>();
    
    private List<DynamicEntityAttrValueDto> attrValues = new ArrayList<>();

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

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
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

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DynamicEntityValueDto [entityDefId=");
        builder.append(entityDefId);
        builder.append(", packageName=");
        builder.append(packageName);
        builder.append(", entityName=");
        builder.append(entityName);
        builder.append(", dataId=");
        builder.append(dataId);
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
    
    
}
