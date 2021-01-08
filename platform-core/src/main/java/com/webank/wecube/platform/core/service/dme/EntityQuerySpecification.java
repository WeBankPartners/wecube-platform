package com.webank.wecube.platform.core.service.dme;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 
 * @author gavin
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EntityQuerySpecification implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -1185278060442250785L;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((additionalFilters == null) ? 0 : additionalFilters.hashCode());
        result = prime * result + ((criteria == null) ? 0 : criteria.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        EntityQuerySpecification other = (EntityQuerySpecification) obj;
        if (additionalFilters == null) {
            if (other.additionalFilters != null) {
                return false;
            }
        } else if (!additionalFilters.equals(other.additionalFilters)) {
            return false;
        }
        if (criteria == null) {
            if (other.criteria != null) {
                return false;
            }
        } else if (!criteria.equals(other.criteria)) {
            return false;
        }
        return true;
    }

}
