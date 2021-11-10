package com.webank.wecube.platform.core.dto.data;

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
public class EntityQuerySpecDto implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -1185278060442250785L;
    private List<EntityQueryFilterDto> additionalFilters = new ArrayList<>();

    public List<EntityQueryFilterDto> getAdditionalFilters() {
        return additionalFilters;
    }

    public void setAdditionalFilters(List<EntityQueryFilterDto> additionalFilters) {
        this.additionalFilters = additionalFilters;
    }

    public EntityQuerySpecDto addAdditionalFilters(EntityQueryFilterDto... additionalFilters) {
        if (this.additionalFilters == null) {
            this.additionalFilters = new ArrayList<>();
        }

        for (EntityQueryFilterDto f : additionalFilters) {
            this.additionalFilters.add(f);
        }

        return this;
    }

    public EntityQueryFilterDto findOutIdFilter() {
        if (additionalFilters == null) {
            return null;
        }

        if (additionalFilters.isEmpty()) {
            return null;
        }

        for (EntityQueryFilterDto f : additionalFilters) {
            if ("id".equals(f.getAttrName()) && EntityQueryFilterDto.OP_EQUALS.equalsIgnoreCase(f.getOp())) {
                return f;
            }
        }

        return null;
    }

}
