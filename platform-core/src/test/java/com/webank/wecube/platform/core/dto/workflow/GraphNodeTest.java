package com.webank.wecube.platform.core.dto.workflow;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecube.platform.core.model.workflow.GraphNode;

public class GraphNodeTest {
    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testGetGraphNodeById() throws JsonProcessingException {
        GraphNode rootNode = buildGraphNode();
//        String json = objectMapper.writeValueAsString(rootNode);
//        System.out.println(json);
        
        List<GraphNodeDto> list = convertToGraphNodeDtoList(rootNode);
        String json = objectMapper.writeValueAsString(list);
        
        System.out.println("JSON:"+json);
        
        System.out.println("=====================");
        String graphNodeId = "DISK25";
        
        GraphNode g = GraphNode.getGraphNodeFromRootNodeById(rootNode, graphNodeId);
        System.out.println("=====================");
        System.out.println(g);
    }
    
    private List<GraphNodeDto> convertToGraphNodeDtoList(GraphNode rootGraphNode){
        List<GraphNodeDto> dtoList = new ArrayList<>();
        
        addDto(rootGraphNode, dtoList);
        rootGraphNode.resetVisitedAll();
        
        return dtoList;
    }
    
    private void addDto(GraphNode node, List<GraphNodeDto> dtoList){
        if(node.isVisited()){
            return;
        }
        GraphNodeDto dto = new GraphNodeDto(node.getId());
        dtoList.add(dto);
        for(GraphNode n : node.getSrcGraphNodes()){
            dto.addSrcGraphNodeIds(n.getId());
        }
        
        for(GraphNode n: node.getToGraphNodes()){
            dto.addToGraphNodeIds(n.getId());
        }
        
        node.visited();
        
        for(GraphNode n : node.getToGraphNodes()){
            addDto(n, dtoList);
        }
        
    }
    
    
    
    
    private GraphNode buildGraphNode(){
        GraphNode root = new GraphNode("UNIT_APP");
        GraphNode ins1 = new GraphNode("INS1");
        GraphNode ins2 = new GraphNode("INS2");
        
//        root.addToGraphNodes(ins1, ins2);
        ins1.addSrcGraphNodes(root);
        ins2.addSrcGraphNodes(root);
        
        
        GraphNode host1 = new GraphNode("HOST1");
        host1.addSrcGraphNodes(ins1);
        
        GraphNode host2 = new GraphNode("HOST2");
        host2.addSrcGraphNodes(ins2);
        GraphNode packV1 = new GraphNode("PACK_V1");
        packV1.addSrcGraphNodes(ins1,root,ins2);
//        root.addToGraphNodes(packV1);
        
//        ins1.addToGraphNodes(host1, packV1);
//        ins2.addToGraphNodes(host2, packV1);
        
        GraphNode disk1 = new GraphNode("DISK1");
        disk1.addSrcGraphNodes(host1);
        
        GraphNode ip1 = new GraphNode("IP1");
        ip1.addSrcGraphNodes(host1);
        
        GraphNode user = new GraphNode("USER");
        user.addSrcGraphNodes(packV1);
        GraphNode ip2 = new GraphNode("IP2");
        ip2.addSrcGraphNodes(host2);
        GraphNode disk2 = new GraphNode("DISK2");
        disk2.addSrcGraphNodes(host2);
        
        return root;
        
    }

}
