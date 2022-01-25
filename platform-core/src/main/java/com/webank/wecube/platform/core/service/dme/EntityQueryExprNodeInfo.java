package com.webank.wecube.platform.core.service.dme;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class EntityQueryExprNodeInfo {
    private String entityQueryNodeExpr;
    private String entityInfoExpr;
    private String entityFilterExpr;
    private EntityLinkType entityLinkType;

    private String queryAttrName;
    private String refByAttrName;

    private String packageName;
    private String entityName;

    private boolean isHeadEntity;

    private List<EntityQueryFilter> additionalFilters = new ArrayList<>();

    public String getEntityQueryNodeExpr() {
        return entityQueryNodeExpr;
    }

    public void setEntityQueryNodeExpr(String entityQueryNodeExpr) {
        this.entityQueryNodeExpr = entityQueryNodeExpr;
    }

    public String getEntityInfoExpr() {
        return entityInfoExpr;
    }

    public void setEntityInfoExpr(String entityInfoExpr) {
        this.entityInfoExpr = entityInfoExpr;
    }

    public String getEntityFilterExpr() {
        return entityFilterExpr;
    }

    public void setEntityFilterExpr(String entityFilterExpr) {
        this.entityFilterExpr = entityFilterExpr;
    }

    public EntityLinkType getEntityLinkType() {
        return entityLinkType;
    }

    public void setEntityLinkType(EntityLinkType entityLinkType) {
        this.entityLinkType = entityLinkType;
    }

    public String getQueryAttrName() {
        return queryAttrName;
    }

    public void setQueryAttrName(String queryAttrName) {
        this.queryAttrName = queryAttrName;
    }

    public String getRefByAttrName() {
        return refByAttrName;
    }

    public void setRefByAttrName(String refByAttrName) {
        this.refByAttrName = refByAttrName;
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

    public boolean isHeadEntity() {
        return isHeadEntity;
    }

    public void setHeadEntity(boolean isHeadEntity) {
        this.isHeadEntity = isHeadEntity;
    }

    public List<EntityQueryFilter> getAdditionalFilters() {
        return additionalFilters;
    }
    
    public boolean hasQueryAttribute(){
        return StringUtils.isNoneBlank(this.getQueryAttrName());
    }

    public void setAdditionalFilters(List<EntityQueryFilter> additionalFilters) {
        this.additionalFilters = additionalFilters;
    }

    public EntityQueryExprNodeInfo addAdditionalFilters(EntityQueryFilter... additionalFilters) {
        for (EntityQueryFilter f : additionalFilters) {
            if (f != null) {
                this.additionalFilters.add(f);
            }
        }
        
        return this;
    }

    public boolean hasAdditionalFilters() {
        if (this.additionalFilters == null) {
            return false;
        }

        return !this.additionalFilters.isEmpty();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[entityQueryNodeExpr=");
        builder.append(entityQueryNodeExpr);
        builder.append(", entityInfoExpr=");
        builder.append(entityInfoExpr);
        builder.append(", entityFilterExpr=");
        builder.append(entityFilterExpr);
        builder.append(", entityLinkType=");
        builder.append(entityLinkType);
        builder.append(", queryAttrName=");
        builder.append(queryAttrName);
        builder.append(", refByAttrName=");
        builder.append(refByAttrName);
        builder.append(", packageName=");
        builder.append(packageName);
        builder.append(", entityName=");
        builder.append(entityName);
        builder.append(", isHeadEntity=");
        builder.append(isHeadEntity);
        builder.append("]");
        return builder.toString();
    }

}
