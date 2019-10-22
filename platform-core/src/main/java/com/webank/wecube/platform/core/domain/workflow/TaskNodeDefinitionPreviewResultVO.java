package com.webank.wecube.platform.core.domain.workflow;

import java.util.ArrayList;
import java.util.List;

public class TaskNodeDefinitionPreviewResultVO {
    private String taskNodeId;
    private String serviceName;
    private String procDefKey;
    private int procDefVersion;

    private String rootCiDataId;
    private String rootCiTypeId;

    private String bindCiTypeId;

    private List<HeaderItem> headerItems = new ArrayList<HeaderItem>();
    private List<CiDataItem> ciDataItems = new ArrayList<CiDataItem>();

    public String getTaskNodeId() {
        return taskNodeId;
    }

    public void setTaskNodeId(String taskNodeId) {
        this.taskNodeId = taskNodeId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getProcDefKey() {
        return procDefKey;
    }

    public void setProcDefKey(String procDefKey) {
        this.procDefKey = procDefKey;
    }

    public int getProcDefVersion() {
        return procDefVersion;
    }

    public void setProcDefVersion(int procDefVersion) {
        this.procDefVersion = procDefVersion;
    }

    public String getRootCiDataId() {
        return rootCiDataId;
    }

    public void setRootCiDataId(String rootCiDataId) {
        this.rootCiDataId = rootCiDataId;
    }

    public String getRootCiTypeId() {
        return rootCiTypeId;
    }

    public void setRootCiTypeId(String rootCiTypeId) {
        this.rootCiTypeId = rootCiTypeId;
    }

    public List<HeaderItem> getHeaderItems() {
        return headerItems;
    }

    public void setHeaderItems(List<HeaderItem> headerItems) {
        this.headerItems = headerItems;
    }

    public List<CiDataItem> getCiDataItems() {
        return ciDataItems;
    }

    public void setCiDataItems(List<CiDataItem> ciDataItems) {
        this.ciDataItems = ciDataItems;
    }

    public String getBindCiTypeId() {
        return bindCiTypeId;
    }

    public void setBindCiTypeId(String bindCiTypeId) {
        this.bindCiTypeId = bindCiTypeId;
    }

    public TaskNodeDefinitionPreviewResultVO addCiDataItem(CiDataItem ciDataItem) {
        if (ciDataItem == null) {
            return this;
        }

        if (this.ciDataItems == null) {
            this.ciDataItems = new ArrayList<CiDataItem>();
        }

        this.ciDataItems.add(ciDataItem);

        return this;
    }

    public TaskNodeDefinitionPreviewResultVO addHeaderItem(HeaderItem headerItem) {
        if (headerItem == null) {
            return this;
        }

        if (this.headerItems == null) {
            this.headerItems = new ArrayList<HeaderItem>();
        }

        this.headerItems.add(headerItem);

        return this;
    }

    public static class CiDataItem {
        private int ciTypeId;
        private String ciDataId;
        private String name;

        private List<CiDataAttrItem> attrItems = new ArrayList<CiDataAttrItem>();

        public int getCiTypeId() {
            return ciTypeId;
        }

        public void setCiTypeId(int ciTypeId) {
            this.ciTypeId = ciTypeId;
        }

        public String getCiDataId() {
            return ciDataId;
        }

        public void setCiDataId(String ciDataId) {
            this.ciDataId = ciDataId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<CiDataAttrItem> getAttrItems() {
            return attrItems;
        }

        public void setAttrItems(List<CiDataAttrItem> attrItems) {
            this.attrItems = attrItems;
        }

        public CiDataItem addAttrItem(CiDataAttrItem attrItem) {
            if (attrItem == null) {
                return this;
            }

            if (this.attrItems == null) {
                this.attrItems = new ArrayList<CiDataAttrItem>();
            }

            this.attrItems.add(attrItem);
            return this;
        }
    }

    public static class CiDataAttrItem {
        private int order;
        private int ciTypeId;
        private int ciAttrId;
        private String propertyName;
        private Object propertyVal;

        public int getOrder() {
            return order;
        }

        public void setOrder(int order) {
            this.order = order;
        }

        public int getCiTypeId() {
            return ciTypeId;
        }

        public void setCiTypeId(int ciTypeId) {
            this.ciTypeId = ciTypeId;
        }

        public int getCiAttrId() {
            return ciAttrId;
        }

        public void setCiAttrId(int ciAttrId) {
            this.ciAttrId = ciAttrId;
        }

        public String getPropertyName() {
            return propertyName;
        }

        public void setPropertyName(String propertyName) {
            this.propertyName = propertyName;
        }

        public Object getPropertyVal() {
            return propertyVal;
        }

        public void setPropertyVal(Object propertyVal) {
            this.propertyVal = propertyVal;
        }
    }

    public static class HeaderItem {
        private int order;
        private int ciTypeId;
        private int ciAttrId;
        private String propertyName;
        private String name;
        private String description;

        public int getCiTypeId() {
            return ciTypeId;
        }

        public void setCiTypeId(int ciTypeId) {
            this.ciTypeId = ciTypeId;
        }

        public int getCiAttrId() {
            return ciAttrId;
        }

        public void setCiAttrId(int ciAttrId) {
            this.ciAttrId = ciAttrId;
        }

        public String getPropertyName() {
            return propertyName;
        }

        public void setPropertyName(String propertyName) {
            this.propertyName = propertyName;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public int getOrder() {
            return order;
        }

        public void setOrder(int order) {
            this.order = order;
        }

    }

}
