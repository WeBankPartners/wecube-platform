package com.webank.wecube.platform.core.service.workflow;

import java.io.Serializable;

public class WfBindEntityDataInfo implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 5477741870697568703L;
    private String entityTypeId;
    private String entityDataId;

    public WfBindEntityDataInfo() {
        super();
    }

    public WfBindEntityDataInfo(String entityTypeId, String entityDataId) {
        super();
        this.entityTypeId = entityTypeId;
        this.entityDataId = entityDataId;
    }

    public String getEntityTypeId() {
        return entityTypeId;
    }

    public void setEntityTypeId(String entityTypeId) {
        this.entityTypeId = entityTypeId;
    }

    public String getEntityDataId() {
        return entityDataId;
    }

    public void setEntityDataId(String entityDataId) {
        this.entityDataId = entityDataId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((entityDataId == null) ? 0 : entityDataId.hashCode());
        result = prime * result + ((entityTypeId == null) ? 0 : entityTypeId.hashCode());
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
        WfBindEntityDataInfo other = (WfBindEntityDataInfo) obj;
        if (entityDataId == null) {
            if (other.entityDataId != null) {
                return false;
            }
        } else if (!entityDataId.equals(other.entityDataId)) {
            return false;
        }
        if (entityTypeId == null) {
            if (other.entityTypeId != null) {
                return false;
            }
        } else if (!entityTypeId.equals(other.entityTypeId)) {
            return false;
        }
        return true;
    }

}
