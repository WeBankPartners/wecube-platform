package com.webank.wecube.platform.core.service.dme;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service("standardEntityOperationService")
public class StandardEntityOperationService {
    private static final Logger log = LoggerFactory.getLogger(StandardEntityOperationService.class);

    @Autowired
    private EntityQueryExpressionParser entityQueryExpressionParser;

    @Autowired
    @Qualifier(value = "jwtSsoRestTemplate")
    private RestTemplate jwtSsoRestTemplate;

    @Autowired
    private EntityQueryExecutor standardEntityQueryExcutor;
    
    @Autowired
    private EntityDataRouteFactory entityDataRouteFactory;
    
    public List<Map<String,Object>> queryAttributeValuesOfLeafNode(EntityOperationRootCondition condition){
    	if(log.isDebugEnabled()) {
    		log.debug("query attribute values of leaf node for condition {}", condition);
    	}
    	
    	EntityOperationContext ctx = buildEntityOperationContext(condition);
        ctx.setEntityOperationType(EntityOperationType.QUERY);
        
        List<EntityDataDelegate> entityDelegates = standardEntityQueryExcutor.executeQueryLeafEntity(ctx);
        List<Map<String,Object>> result = new ArrayList<>();
        if(entityDelegates == null) {
        	return result;
        }
        
        entityDelegates.forEach(entity -> {
        	Map<String, Object> entityData = entity.getEntityData();
        	Map<String, Object> recordMap = new HashMap<String, Object>();
        	recordMap.putAll(entityData);
        	
        	result.add(recordMap);
        });
        
        return result;
    }

    public List<Object> queryAttributeValues(EntityOperationRootCondition condition) {
        if (log.isDebugEnabled()) {
            log.debug("query entity with condition {}", condition);
        }

        EntityOperationContext ctx = buildEntityOperationContext(condition);
        ctx.setEntityOperationType(EntityOperationType.QUERY);
        return standardEntityQueryExcutor.executeQueryLeafAttributes(ctx);
    }

    public void update(EntityOperationRootCondition condition, Object attrValueToUpdate) {
        if (log.isInfoEnabled()) {
            log.info("update entity with condition {} and data {}", condition, attrValueToUpdate);
        }

        EntityOperationContext ctx = buildEntityOperationContext(condition);
        ctx.setEntityOperationType(EntityOperationType.UPDATE);

        standardEntityQueryExcutor.executeUpdate(ctx, attrValueToUpdate);

        return;
    }
    
    public EntityTreeNodesOverview generateEntityLinkOverview(EntityOperationRootCondition condition) {
    	if(log.isInfoEnabled()) {
    		log.info("generate entity link overview with condition {}", condition);
    	}
    	
    	EntityOperationContext ctx = buildEntityOperationContext(condition);
        ctx.setEntityOperationType(EntityOperationType.QUERY);
        
        return standardEntityQueryExcutor.generateEntityLinkOverview(ctx);
    }

    public List<TreeNode> generatePreviewTree(EntityOperationRootCondition condition) {
        if(log.isInfoEnabled()){
            log.info("generate preview tree with condition {}", condition);
        }
        
        EntityOperationContext ctx = buildEntityOperationContext(condition);
        ctx.setEntityOperationType(EntityOperationType.UPDATE);
        return standardEntityQueryExcutor.generatePreviewTree(ctx);
    }

    protected EntityOperationContext buildEntityOperationContext(EntityOperationRootCondition condition) {
        List<EntityQueryExprNodeInfo> exprNodeInfos = entityQueryExpressionParser.parse(condition.getEntityLinkExpr());

        EntityOperationContext ctx = new EntityOperationContext();
        ctx.setEntityQueryExprNodeInfos(exprNodeInfos);
        ctx.setOriginalEntityLinkExpression(condition.getEntityLinkExpr());
        ctx.setOriginalEntityData(condition.getEntityIdentity());
        ctx.setStandardEntityOperationRestClient(new StandardEntityOperationRestClient(jwtSsoRestTemplate));
        ctx.setHeadEntityQueryLinkNode(buildEntityQueryLinkNode(exprNodeInfos));
        ctx.setEntityDataRouteFactory(entityDataRouteFactory);

        return ctx;
    }

    protected EntityQueryLinkNode buildEntityQueryLinkNode(List<EntityQueryExprNodeInfo> exprNodeInfos) {
        if (exprNodeInfos == null || exprNodeInfos.isEmpty()) {
            return null;
        }
        EntityQueryExprNodeInfo nodeInfo = exprNodeInfos.get(0);
        EntityQueryLinkNode headLinkNode = new EntityQueryLinkNode();
        headLinkNode.setIndex(0);
        headLinkNode.setExprNodeInfo(nodeInfo);
        headLinkNode.setHead(true);
        headLinkNode.setPreviousNode(null);

        EntityQueryLinkNode previousLinkNode = headLinkNode;
        for (int i = 1; i < exprNodeInfos.size(); i++) {
            EntityQueryExprNodeInfo ni = exprNodeInfos.get(i);
            EntityQueryLinkNode linkNode = new EntityQueryLinkNode();
            linkNode.setIndex(i);
            linkNode.setExprNodeInfo(ni);
            linkNode.setHead(false);
            linkNode.setPreviousNode(previousLinkNode);
            linkNode.setSucceedingNode(null);
            
            previousLinkNode = linkNode;
        }

        return headLinkNode;
    }

    public RestTemplate getRestTemplate() {
        return jwtSsoRestTemplate;
    }

}
