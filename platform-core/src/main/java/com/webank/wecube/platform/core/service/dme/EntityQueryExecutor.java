package com.webank.wecube.platform.core.service.dme;

import java.util.List;
import java.util.Map;

/**
 * 
 * @author gavin
 *
 */
public interface EntityQueryExecutor {
    /**
     * 
     * @param ctx
     * @param valueToUpdate
     */
    void executeUpdate(EntityOperationContext ctx, Object valueToUpdate);

    /**
     * 
     * @param ctx
     * @param entityDef
     * @param recordsToCreate
     * @return
     */
    Map<String, Object> executeCreate(EntityOperationContext ctx, EntityRouteDescription entityDef,
            List<EntityDataRecord> recordsToCreate);

    /**
     * 
     * @param ctx
     * @return
     */
    List<Object> executeQueryLeafAttributes(EntityOperationContext ctx);

    /**
     * 
     * @param ctx
     * @return
     */
    List<EntityDataDelegate> executeQueryLeafEntity(EntityOperationContext ctx);

    /**
     * 
     * @param ctx
     * @param linkNode
     */
    void performQuery(EntityOperationContext ctx, EntityQueryLinkNode linkNode);

    /**
     * 
     * @param ctx
     * @return
     */
    List<StandardEntityDataNode> generatePreviewTree(EntityOperationContext ctx);

    /**
     * 
     * @param ctx
     * @return
     */
    EntityTreeNodesOverview generateEntityLinkOverview(EntityOperationContext ctx);

    /**
     * 
     * @param exprNodeInfos
     * @return
     */
    EntityQueryLinkNode buildEntityQueryLinkNode(List<EntityQueryExprNodeInfo> exprNodeInfos);
}
