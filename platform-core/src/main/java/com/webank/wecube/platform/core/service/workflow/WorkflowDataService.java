package com.webank.wecube.platform.core.service.workflow;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.workflow.FlowNodeDefDto;
import com.webank.wecube.platform.core.dto.workflow.GraphNodeDto;
import com.webank.wecube.platform.core.dto.workflow.ProcDefOutlineDto;
import com.webank.wecube.platform.core.model.datamodel.DataModelExpressionToRootData;
import com.webank.wecube.platform.core.service.DataModelExpressionService;
import com.webank.wecube.platform.core.support.datamodel.TreeNode;

@Service
public class WorkflowDataService {
    private static final Logger log = LoggerFactory.getLogger(WorkflowDataService.class);

    @Autowired
    private WorkflowProcDefService workflowProcDefService;

    @Autowired
    private DataModelExpressionService dataModelExpressionService;

    public List<GraphNodeDto> getProcessDataPreview(String procDefId, String dataId) {
        if (StringUtils.isBlank(procDefId) || StringUtils.isBlank(dataId)) {
            throw new WecubeCoreException("Process definition ID or entity ID is not provided.");
        }

        ProcDefOutlineDto procDefOutline = workflowProcDefService.getProcessDefinitionOutline(procDefId);

        if (procDefOutline == null) {
            log.debug("process definition with id {} does not exist.", procDefId);
            throw new WecubeCoreException(String.format("Such process definition {%s} does not exist.", procDefId));
        }

        return doFetchProcessPreviewData(procDefOutline, dataId);

    }

    protected List<GraphNodeDto> doFetchProcessPreviewData(ProcDefOutlineDto outline, String dataId) {

        List<GraphNodeDto> result = new ArrayList<>();
        
        for(FlowNodeDefDto f : outline.getFlowNodes()){
            String nodeType = f.getNodeType();
            
            if(!"subProcess".equals(nodeType)){
                continue;
            }
            
            log.debug("About to fetch data with {} and {}", f.getRoutineExpression(), dataId);
            DataModelExpressionToRootData expr = new DataModelExpressionToRootData(f.getRoutineExpression(), dataId);
            List<TreeNode> nodes = dataModelExpressionService.getPreviewTree(expr);
            
            if(nodes == null || nodes.isEmpty()){
                log.debug("None data returned for {} and {}", f.getRoutineExpression(), dataId);
                continue;
            }
            
            for(TreeNode tn : nodes){
                String treeNodeId = buildId(tn);
                GraphNodeDto currNode = findGraphNodeDtoById(result, treeNodeId);
                if(currNode == null){
                    currNode = new GraphNodeDto();
                    currNode.setDataId(tn.getRootId().toString());
                    currNode.setPackageName(tn.getPackageName());
                    currNode.setEntityName(tn.getEntityName());
                    
                    addToResult(result, currNode);
                }
                
                TreeNode parentTreeNode = tn.getParent();
                if(parentTreeNode != null){
                    String parentTreeNodeId = buildId(parentTreeNode);
                    currNode.addPreviousIds(parentTreeNodeId);
                }
                
                List<TreeNode> childrenTreeNodes = tn.getChildren();
                if(childrenTreeNodes != null){
                    for(TreeNode ctn : childrenTreeNodes){
                        String ctnId = buildId(ctn);
                        currNode.addSucceedingIds(ctnId);
                    }
                }
            }
        }
        
        return result;
        
        
    }
    
    private void addToResult(List<GraphNodeDto> result, GraphNodeDto...nodes){
        for(GraphNodeDto n : nodes){
            if(result.contains(n)){
                continue;
            }
            
            GraphNodeDto exist = findGraphNodeDtoById(result, n.getId());
            if(exist == null){
                result.add(n);
            }
        }
    }
    
    private GraphNodeDto findGraphNodeDtoById(List<GraphNodeDto> result, String id){
        for(GraphNodeDto n : result){
            if(n.getId().equals(id)){
                return n;
            }
        }
        
        return null;
    }
    
    private String buildId(TreeNode n){
        return String.format("%s|%s|%s", n.getPackageName(), n.getEntityName(), n.getRootId());
    }

}
