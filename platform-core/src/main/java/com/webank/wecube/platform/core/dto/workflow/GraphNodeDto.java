package com.webank.wecube.platform.core.dto.workflow;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class GraphNodeDto {

    private String packageName;
    private String entityName;
    private String dataId;
    private String displayName;

    //for taskman
    private Object entityData;

    private String id;
    private List<String> previousIds = new ArrayList<>();
    private List<String> succeedingIds = new ArrayList<>();

    public GraphNodeDto(String id) {
        super();
        this.id = id;
    }

    public GraphNodeDto() {
        super();
    }

    public String getId() {
        if (StringUtils.isBlank(id)) {
            this.id = buildId();
        }

        return id;
    }

    public String buildId() {
        return String.format("%s:%s:%s", packageName, entityName, dataId);
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getPreviousIds() {
        return previousIds;
    }

    public void setPreviousIds(List<String> previousIds) {
        this.previousIds = previousIds;
    }

    public List<String> getSucceedingIds() {
        return succeedingIds;
    }

    public void setSucceedingIds(List<String> succeedingIds) {
        this.succeedingIds = succeedingIds;
    }

    public GraphNodeDto addSucceedingIds(String... ids) {
        for (String gid : ids) {
            if (!this.getSucceedingIds().contains(gid)) {
                this.getSucceedingIds().add(gid);
            }
        }

        return this;
    }

    public GraphNodeDto addPreviousIds(String... ids) {
        for (String gid : ids) {
            if (!this.getPreviousIds().contains(gid)) {
                this.getPreviousIds().add(gid);
            }
        }

        return this;
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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Object getEntityData() {
        return entityData;
    }

    public void setEntityData(Object entityData) {
        this.entityData = entityData;
    }

}
