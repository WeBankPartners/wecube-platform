package com.webank.wecube.platform.core.entity.workflow;

public class StandardEntityDataKey {
    private String dataTypeId;
    private String dataId;

    public StandardEntityDataKey() {

    }

    public StandardEntityDataKey(String dataTypeId, String dataId) {
        this.dataTypeId = dataTypeId;
        this.dataId = dataId;
    }

    public String getDataTypeId() {
        return dataTypeId;
    }

    public void setDataTypeId(String dataTypeId) {
        this.dataTypeId = dataTypeId;
    }

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dataId == null) ? 0 : dataId.hashCode());
        result = prime * result + ((dataTypeId == null) ? 0 : dataTypeId.hashCode());
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
        StandardEntityDataKey other = (StandardEntityDataKey) obj;
        if (dataId == null) {
            if (other.dataId != null) {
                return false;
            }
        } else if (!dataId.equals(other.dataId)) {
            return false;
        }
        if (dataTypeId == null) {
            if (other.dataTypeId != null) {
                return false;
            }
        } else if (!dataTypeId.equals(other.dataTypeId)) {
            return false;
        }
        return true;
    }

}
