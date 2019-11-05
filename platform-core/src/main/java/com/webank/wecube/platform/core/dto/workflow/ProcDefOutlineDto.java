package com.webank.wecube.platform.core.dto.workflow;

import java.util.ArrayList;
import java.util.List;

public class ProcDefOutlineDto extends BaseProcDefDto {
    private List<FlowNodeDefDto> flowNodes = new ArrayList<>();
    
    public FlowNodeDefDto findFlowNodeDefDto(String nodeId){
        for(FlowNodeDefDto f : flowNodes){
            if(f.getNodeId().equals(nodeId)){
                return f;
            }
        }
        
        return null;
    }

    public List<FlowNodeDefDto> getFlowNodes() {
        return flowNodes;
    }

    public void setFlowNodes(List<FlowNodeDefDto> flowNodes) {
        this.flowNodes = flowNodes;
    }

    public void addFlowNodes(FlowNodeDefDto... flowNodes) {
        if (this.flowNodes == null) {
            this.flowNodes = new ArrayList<>();
        }

        for (FlowNodeDefDto f : flowNodes) {
            if (f == null) {
                continue;
            }

            if (!this.flowNodes.contains(f)) {
                this.flowNodes.add(f);
            }
        }
    }
}
