package com.webank.wecube.platform.core.dto.workflow;

import java.util.ArrayList;
import java.util.List;

public class GraphNodeDto {

    private String id;
    private List<String> srcGraphNodeIds = new ArrayList<>();
    private List<String> toGraphNodeIds = new ArrayList<>();

    public GraphNodeDto(String id) {
        super();
        this.id = id;
    }

    public GraphNodeDto() {
        super();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getSrcGraphNodeIds() {
        return srcGraphNodeIds;
    }

    public void setSrcGraphNodeIds(List<String> srcGraphNodeIds) {
        this.srcGraphNodeIds = srcGraphNodeIds;
    }

    public List<String> getToGraphNodeIds() {
        return toGraphNodeIds;
    }

    public void setToGraphNodeIds(List<String> toGraphNodeIds) {
        this.toGraphNodeIds = toGraphNodeIds;
    }

    public GraphNodeDto addToGraphNodeIds(String... graphNodeIds) {
        for (String gid : graphNodeIds) {
            if (!this.getToGraphNodeIds().contains(gid)) {
                this.getToGraphNodeIds().add(gid);
            }
        }

        return this;
    }

    public GraphNodeDto addSrcGraphNodeIds(String... graphNodeIds) {
        for (String gid : graphNodeIds) {
            if (!this.getSrcGraphNodeIds().contains(gid)) {
                this.getSrcGraphNodeIds().add(gid);
            }
        }

        return this;
    }

}
