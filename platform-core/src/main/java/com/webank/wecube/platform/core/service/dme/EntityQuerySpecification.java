package com.webank.wecube.platform.core.service.dme;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 
 * @author gavin
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EntityQuerySpecification {
    

    private EntityQueryCriteria criteria;
    private List<EntityQueryFilter> additionalFilters = new ArrayList<>();

    public EntityQueryCriteria getCriteria() {
        return criteria;
    }

    public void setCriteria(EntityQueryCriteria criteria) {
        this.criteria = criteria;
    }

    public List<EntityQueryFilter> getAdditionalFilters() {
        return additionalFilters;
    }

    public void setAdditionalFilters(List<EntityQueryFilter> additionalFilters) {
        this.additionalFilters = additionalFilters;
    }

    public EntityQuerySpecification addAdditionalFilters(EntityQueryFilter... additionalFilters) {
        if (this.additionalFilters == null) {
            this.additionalFilters = new ArrayList<>();
        }

        for (EntityQueryFilter f : additionalFilters) {
            this.additionalFilters.add(f);
        }

        return this;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[criteria=");
        builder.append(criteria);
        builder.append(", additionalFilters=");
        builder.append(additionalFilters);
        builder.append("]");
        return builder.toString();
    }
    
    
}
