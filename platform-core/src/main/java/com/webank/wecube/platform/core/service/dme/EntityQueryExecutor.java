package com.webank.wecube.platform.core.service.dme;

import java.util.List;

public interface EntityQueryExecutor {
    void executeUpdate(EntityOperationContext ctx, Object valueToUpdate);
    List<Object> executeQueryLeafAttributes(EntityOperationContext ctx);
    List<EntityDataDelegate> executeQueryLeafEntity(EntityOperationContext ctx);
    void performQuery(EntityOperationContext ctx, EntityQueryLinkNode linkNode);
    List<TreeNode> generatePreviewTree(EntityOperationContext ctx);
    EntityTreeNodesOverview generateEntityLinkOverview(EntityOperationContext ctx);
    EntityQueryLinkNode buildEntityQueryLinkNode(List<EntityQueryExprNodeInfo> exprNodeInfos);
}

