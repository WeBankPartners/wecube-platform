package com.webank.wecube.platform.core.service.dme;

import java.util.List;

public class EntityOperationContext {

    protected EntityOperationType entityOperationType;
    protected StandardEntityOperationRestClient standardEntityOperationRestClient;
    protected String originalEntityLinkExpression;
    protected String originalEntityData;
    protected List<EntityQueryExprNodeInfo> entityQueryExprNodeInfos;
    protected EntityQueryLinkNode headEntityQueryLinkNode;

    protected EntityQueryLinkNode currentEntityQueryLinkNode;
    
    protected EntityDataRouter entityDataRouter;
    
    public EntityQueryLinkNode getHeadEntityQueryLinkNode(){
        return headEntityQueryLinkNode;
    }
    
    public EntityQueryLinkNode getTailEntityQueryLinkNode(){
        EntityQueryLinkNode start = null;
        if(currentEntityQueryLinkNode != null){
            start = currentEntityQueryLinkNode;
        }else{
            start = getHeadEntityQueryLinkNode();
        }
        
        EntityQueryLinkNode visitNode = start;
        while(visitNode.getSucceedingNode() != null){
            visitNode = visitNode.getSucceedingNode();
        }
        
        return visitNode;
    }

    public EntityOperationType getEntityOperationType() {
        return entityOperationType;
    }

    public void setEntityOperationType(EntityOperationType entityOperationType) {
        this.entityOperationType = entityOperationType;
    }

    public StandardEntityOperationRestClient getStandardEntityOperationRestClient() {
        return standardEntityOperationRestClient;
    }

    public void setStandardEntityOperationRestClient(
            StandardEntityOperationRestClient standardEntityOperationRestClient) {
        this.standardEntityOperationRestClient = standardEntityOperationRestClient;
    }

    public String getOriginalEntityLinkExpression() {
        return originalEntityLinkExpression;
    }

    public void setOriginalEntityLinkExpression(String originalEntityLinkExpression) {
        this.originalEntityLinkExpression = originalEntityLinkExpression;
    }

    public List<EntityQueryExprNodeInfo> getEntityQueryExprNodeInfos() {
        return entityQueryExprNodeInfos;
    }

    public void setEntityQueryExprNodeInfos(List<EntityQueryExprNodeInfo> entityQueryExprNodeInfos) {
        this.entityQueryExprNodeInfos = entityQueryExprNodeInfos;
    }


    public void setHeadEntityQueryLinkNode(EntityQueryLinkNode entityQueryLinkNode) {
        this.headEntityQueryLinkNode = entityQueryLinkNode;
    }

    public String getOriginalEntityData() {
        return originalEntityData;
    }

    public void setOriginalEntityData(String originalEntityData) {
        this.originalEntityData = originalEntityData;
    }

    public EntityQueryLinkNode getCurrentEntityQueryLinkNode() {
        return currentEntityQueryLinkNode;
    }

    public void setCurrentEntityQueryLinkNode(EntityQueryLinkNode currentEntityQueryLinkNode) {
        this.currentEntityQueryLinkNode = currentEntityQueryLinkNode;
    }

    public EntityDataRouter getEntityDataRouter() {
        return entityDataRouter;
    }

    public void setEntityDataRouter(EntityDataRouter entityDataRouter) {
        this.entityDataRouter = entityDataRouter;
    }
}
