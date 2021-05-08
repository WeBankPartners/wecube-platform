package com.webank.wecube.platform.core.entity.workflow;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//core_ru_graph_node
public class GraphNodeEntity {
    private static final String IDS_DELIMITER = ",";

    private Integer id;

    private String createdBy;

    private Date createdTime;

    private String updatedBy;

    private Date updatedTime;

    private String dataId;

    private String displayName;

    private String entityName;

    private String graphNodeId;

    private String pkgName;

    private Integer procInstId;

    private String procSessId;

    private String prevIds;

    private String succIds;
    
    private String fullDataId;

    public static String convertIdsListToString(List<String> ids) {
        if (ids == null) {
            return null;
        }

        if (ids.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (String id : ids) {
            sb.append(id).append(IDS_DELIMITER);
        }

        return sb.toString();
    }

    public static List<String> convertIdsStringToList(String idString) {
        List<String> ids = new ArrayList<>();
        if (idString == null || idString.trim().length() <= 0) {
            return ids;
        }

        String[] idStringParts = idString.split(IDS_DELIMITER);
        for (String idStringPart : idStringParts) {
            ids.add(idStringPart);
        }

        return ids;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getGraphNodeId() {
        return graphNodeId;
    }

    public void setGraphNodeId(String graphNodeId) {
        this.graphNodeId = graphNodeId;
    }

    public String getPkgName() {
        return pkgName;
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }

    public Integer getProcInstId() {
        return procInstId;
    }

    public void setProcInstId(Integer procInstId) {
        this.procInstId = procInstId;
    }

    public String getProcSessId() {
        return procSessId;
    }

    public void setProcSessId(String procSessId) {
        this.procSessId = procSessId;
    }

    public String getPrevIds() {
        return prevIds;
    }

    public void setPrevIds(String prevIds) {
        this.prevIds = prevIds;
    }

    public String getSuccIds() {
        return succIds;
    }

    public void setSuccIds(String succIds) {
        this.succIds = succIds;
    }

    public String getFullDataId() {
        return fullDataId;
    }

    public void setFullDataId(String fullDataId) {
        this.fullDataId = fullDataId;
    }

}
