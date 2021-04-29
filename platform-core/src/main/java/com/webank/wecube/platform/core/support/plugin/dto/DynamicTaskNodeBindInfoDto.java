package com.webank.wecube.platform.core.support.plugin.dto;

import java.util.ArrayList;
import java.util.List;

public class DynamicTaskNodeBindInfoDto {
    private String nodeId;//Physical node id from BPMN.
    private String nodeDefId;//Node record id from platform database.
    
    private List<DynamicEntityValueDto> boundEntityValues = new ArrayList<>();

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeDefId() {
        return nodeDefId;
    }

    public void setNodeDefId(String nodeDefId) {
        this.nodeDefId = nodeDefId;
    }

    public List<DynamicEntityValueDto> getBoundEntityValues() {
        return boundEntityValues;
    }

    public void setBoundEntityValues(List<DynamicEntityValueDto> boundEntityValues) {
        this.boundEntityValues = boundEntityValues;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DynamicTaskNodeBindInfoDto [nodeId=");
        builder.append(nodeId);
        builder.append(", nodeDefId=");
        builder.append(nodeDefId);
        builder.append(", boundEntityValues=");
        builder.append(boundEntityValues);
        builder.append("]");
        return builder.toString();
    }
    
    
}
