package com.webank.wecube.platform.core.service.dme;

import java.util.List;

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

    public List<Object> query(EntityOperationRootCondition condition) {
        if (log.isInfoEnabled()) {
            log.info("query entity with condition {}", condition);
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

    public List<TreeNode> generatePreviewTree(EntityOperationRootCondition condition) {
        return null;
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
        }

        return headLinkNode;
    }

    public RestTemplate getRestTemplate() {
        return jwtSsoRestTemplate;
    }

}
