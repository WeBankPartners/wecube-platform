package com.webank.wecube.platform.core.service.dme;

import java.io.Serializable;

class StandardEntityQueryCacheKey implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 3646765980069532447L;
    private EntityRouteDescription entityDef;
    private EntityQuerySpecification querySpec;

    public StandardEntityQueryCacheKey() {
        super();
    }

    public StandardEntityQueryCacheKey(EntityRouteDescription entityDef, EntityQuerySpecification querySpec) {
        super();
        this.entityDef = entityDef;
        this.querySpec = querySpec;
    }

    public EntityRouteDescription getEntityDef() {
        return entityDef;
    }

    public void setEntityDef(EntityRouteDescription entityDef) {
        this.entityDef = entityDef;
    }

    public EntityQuerySpecification getQuerySpec() {
        return querySpec;
    }

    public void setQuerySpec(EntityQuerySpecification querySpec) {
        this.querySpec = querySpec;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((entityDef == null) ? 0 : entityDef.hashCode());
        result = prime * result + ((querySpec == null) ? 0 : querySpec.hashCode());
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
        StandardEntityQueryCacheKey other = (StandardEntityQueryCacheKey) obj;
        if (entityDef == null) {
            if (other.entityDef != null) {
                return false;
            }
        } else if (!entityDef.equals(other.entityDef)) {
            return false;
        }
        if (querySpec == null) {
            if (other.querySpec != null) {
                return false;
            }
        } else if (!querySpec.equals(other.querySpec)) {
            return false;
        }
        return true;
    }

}
