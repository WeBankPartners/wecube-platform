package com.webank.wecube.platform.core.service.dme;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 
 * @author gavinli
 *
 */
@Service("standardEntityQueryExecutor")
public class StandardEntityQueryExecutor implements EntityQueryExecutor {
    private static final Logger log = LoggerFactory.getLogger(StandardEntityQueryExecutor.class);

    @Override
    public EntityTreeNodesOverview generateEntityLinkOverview(EntityOperationContext ctx) {
        doExecuteQuery(ctx);

        List<StandardEntityDataNode> hierarchicalEntityNodes = generateHierarchicalEntityTreeNodes(ctx);
        List<StandardEntityDataNode> leafNodeEntityNodes = generateLeafNodeEntityNodes(ctx);

        return buildEntityTreeNodesOverview(hierarchicalEntityNodes, leafNodeEntityNodes);
    }

    @Override
    public List<StandardEntityDataNode> generatePreviewTree(EntityOperationContext ctx) {
        doExecuteQuery(ctx);
        return generateHierarchicalEntityTreeNodes(ctx);
    }

    private void pupolateTreeNodeWithLinkNode(List<StandardEntityDataNode> result, EntityQueryLinkNode linkNode) {
        for (EntityDataDelegate delegate : linkNode.getEntityDataDelegates()) {

            StandardEntityDataNode currTreeNode = findTreeNode(result, delegate.getPackageName(), delegate.getEntityName(),
                    delegate.getId());
            if (currTreeNode == null) {
                currTreeNode = new StandardEntityDataNode();
                currTreeNode.setId(delegate.getId());
                currTreeNode.setDisplayName(delegate.getDisplayName());
                currTreeNode.setEntityName(delegate.getEntityName());
                currTreeNode.setPackageName(delegate.getPackageName());
                currTreeNode.setFullId(delegate.getFullId());

                result.add(currTreeNode);
            }

            EntityDataDelegate prevDelegate = delegate.getPreviousEntity();
            if (prevDelegate != null) {
                StandardEntityDataNode prevTreeNode = findTreeNode(result, prevDelegate.getPackageName(),
                        prevDelegate.getEntityName(), prevDelegate.getId());
                if (prevTreeNode == null) {
                    prevTreeNode = new StandardEntityDataNode();
                    prevTreeNode.setId(prevDelegate.getId());
                    prevTreeNode.setFullId(prevDelegate.getFullId());
                    prevTreeNode.setDisplayName(prevDelegate.getDisplayName());
                    prevTreeNode.setEntityName(prevDelegate.getEntityName());
                    prevTreeNode.setPackageName(prevDelegate.getPackageName());

                    result.add(prevTreeNode);
                }

                currTreeNode.setParent(prevTreeNode);
                prevTreeNode.addChildren(currTreeNode);
            }

            for (EntityDataDelegate succeedingDelegate : delegate.getSucceedingEntities()) {
                StandardEntityDataNode succeedingTreeNode = findTreeNode(result, succeedingDelegate.getPackageName(),
                        succeedingDelegate.getEntityName(), succeedingDelegate.getId());
                if (succeedingTreeNode == null) {
                    succeedingTreeNode = new StandardEntityDataNode();
                    succeedingTreeNode.setId(succeedingDelegate.getId());
                    succeedingTreeNode.setFullId(succeedingDelegate.getFullId());
                    succeedingTreeNode.setDisplayName(succeedingDelegate.getDisplayName());
                    succeedingTreeNode.setEntityName(succeedingDelegate.getEntityName());
                    succeedingTreeNode.setPackageName(succeedingDelegate.getPackageName());

                    result.add(succeedingTreeNode);
                }

                succeedingTreeNode.setParent(currTreeNode);
                currTreeNode.addChildren(succeedingTreeNode);
            }
        }
    }

    private StandardEntityDataNode findTreeNode(List<StandardEntityDataNode> nodes, String packageName, String entityName, String id) {
        for (StandardEntityDataNode n : nodes) {
            if (n.getPackageName().equals(packageName) && n.getEntityName().equals(entityName)
                    && n.getId().equals(id)) {
                return n;
            }
        }

        return null;
    }
    
    public Map<String, Object> executeCreate(EntityOperationContext ctx, EntityRouteDescription entityDef, List<EntityDataRecord> recordsToCreate){
        StandardEntityOperationRestClient restClient = ctx.getStandardEntityOperationRestClient();
        StandardEntityOperationResponseDto responseDto = restClient.create(entityDef, recordsToCreate);
        
        if (StandardEntityOperationResponseDto.STATUS_OK.equalsIgnoreCase(responseDto.getStatus())) {
            List<Map<String, Object>> recordMapList = extractEntityDataFromResponse(responseDto.getData());
            if(recordMapList == null || recordMapList.isEmpty()){
                return null;
            }
            
            return recordMapList.get(0);
            
        } else {
            log.error("Error status met {} with message {}", responseDto.getStatus(), responseDto.getMessage());
            String msg = String.format("Errors met while creating data from %s due to status %s.",
                    entityDef.getPackageName(), responseDto.getStatus());
            throw new EntityOperationException("3309", msg, entityDef.getPackageName(), responseDto.getStatus());
        }
    }

    public void executeUpdate(EntityOperationContext ctx, Object valueToUpdate) {
        List<EntityDataDelegate> entitiesToUpdate = executeQueryLeafEntity(ctx);
        List<EntityDataRecord> entityDataRecordsToUpdate = buildEntityDataRecords(entitiesToUpdate, valueToUpdate);

        EntityQueryLinkNode leafLinkNode = ctx.getTailEntityQueryLinkNode();
        EntityQueryExprNodeInfo nodeInfo = leafLinkNode.getExprNodeInfo();
        EntityRouteDescription entityDef = ctx.getEntityDataRouteFactory()
                .deduceEntityDescription(nodeInfo.getPackageName(), nodeInfo.getEntityName());

        StandardEntityOperationRestClient restClient = ctx.getStandardEntityOperationRestClient();
        restClient.update(entityDef, entityDataRecordsToUpdate);
    }

    public EntityQueryLinkNode buildEntityQueryLinkNode(List<EntityQueryExprNodeInfo> exprNodeInfos) {
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

    public List<Object> executeQueryLeafAttributes(EntityOperationContext ctx) {
        doExecuteQuery(ctx);
        return extractAttrValues(ctx);
    }

    public List<EntityDataDelegate> executeQueryLeafEntity(EntityOperationContext ctx) {
        doExecuteQuery(ctx);
        return extractLeafEntityData(ctx);
    }

    public void performQuery(EntityOperationContext ctx, EntityQueryLinkNode linkNode) {
        if (log.isInfoEnabled()) {
            log.info("performing query for {} {}", linkNode.getIndex(),
                    linkNode.getExprNodeInfo().getEntityQueryNodeExpr());
        }

        EntityQueryExprNodeInfo nodeInfo = linkNode.getExprNodeInfo();
        EntityRouteDescription entityDef = ctx.getEntityDataRouteFactory()
                .deduceEntityDescription(nodeInfo.getPackageName(), nodeInfo.getEntityName());

        doPerformQuery(ctx, linkNode, entityDef);
    }

    protected List<EntityDataRecord> buildEntityDataRecords(List<EntityDataDelegate> entitiesToUpdate,
            Object valueToUpdate) {
        List<EntityDataRecord> dataRecords = new ArrayList<>();
        for (EntityDataDelegate delegate : entitiesToUpdate) {
            if (delegate.getQueryAttrName() == null || delegate.getQueryAttrName().trim().length() < 1) {
                log.warn("Unknown field to update for {} {}, probably the expression is not valid. ",
                        delegate.getPackageName(), delegate.getEntityName());
                throw new IllegalStateException(String.format("Unknown field to update for %s %s",
                        delegate.getPackageName(), delegate.getEntityName()));
            }
            if (log.isInfoEnabled()) {
                log.info("UPDATE entity:id={} name={} attrName={} oldValue={} newValue={}", delegate.getId(),
                        delegate.getDisplayName(), delegate.getQueryAttrName(), delegate.getQueryAttrValue(),
                        valueToUpdate);
            }
            EntityDataRecord record = new EntityDataRecord();
            record.setId(delegate.getId());
            EntityDataAttr attr = new EntityDataAttr();
            attr.setAttrName(delegate.getQueryAttrName());
            attr.setAttrValue(valueToUpdate);
            record.addAttrs(attr);

            dataRecords.add(record);
        }

        return dataRecords;
    }

    private void doPerformQuery(EntityOperationContext ctx, EntityQueryLinkNode linkNode,
            EntityRouteDescription entityDef) {

        if (linkNode.isHeadLinkNode()) {
            doPerformHeadEntityLinkNodeQuery(ctx, linkNode, entityDef);
            return;
        }

        if (linkNode.getExprNodeInfo().getEntityLinkType() == EntityLinkType.REF_TO) {
            doPerformRefToEntityLinkNodeQuery(ctx, linkNode, entityDef);
            return;
        }

        if (linkNode.getExprNodeInfo().getEntityLinkType() == EntityLinkType.REF_BY) {
            doPerformRefByEntityLinkNodeQuery(ctx, linkNode, entityDef);
            return;
        }

        log.error("Such entity link type {} is not supported currently",
                linkNode.getExprNodeInfo().getEntityLinkType());
        throw new UnsupportedOperationException("Such entity link type is not supported.");

    }

    private void doPerformRefByEntityLinkNodeQuery(EntityOperationContext ctx, EntityQueryLinkNode linkNode,
            EntityRouteDescription entityDef) {
        if (log.isDebugEnabled()) {
            log.debug("perform query for RefBy entity link  node with {}",
                    linkNode.getExprNodeInfo().getEntityQueryNodeExpr());
        }

        EntityQueryExprNodeInfo exprNodeInfo = linkNode.getExprNodeInfo();

        EntityQueryLinkNode previousLinkNode = linkNode.getPreviousNode();
        List<EntityDataDelegate> prevEntityDataDelegates = previousLinkNode.getEntityDataDelegates();

        if (prevEntityDataDelegates != null) {
            for (EntityDataDelegate prevEntityDataDelegate : prevEntityDataDelegates) {
                EntityQuerySpecification querySpec = buildRefByEntityQuerySpecification(ctx, linkNode, entityDef,
                        exprNodeInfo, previousLinkNode, prevEntityDataDelegate);

                performRestOperation(ctx, linkNode, entityDef, prevEntityDataDelegate, querySpec);
            }
        }
    }

    protected void doExecuteQuery(EntityOperationContext ctx) {
        EntityQueryLinkNode linkNode = ctx.getHeadEntityQueryLinkNode();
        while (linkNode != null) {
            linkNode.executeQuery(this, ctx);
            linkNode = linkNode.getSucceedingNode();
        }
    }

    private EntityQuerySpecification buildRefByEntityQuerySpecification(EntityOperationContext ctx,
            EntityQueryLinkNode linkNode, EntityRouteDescription entityDef, EntityQueryExprNodeInfo exprNodeInfo,
            EntityQueryLinkNode previousLinkNode, EntityDataDelegate prevEntityDataDelegate) {
        EntityQuerySpecification querySpec = new EntityQuerySpecification();
        EntityQueryCriteria criteria = new EntityQueryCriteria();
        criteria.setAttrName(exprNodeInfo.getRefByAttrName());
        criteria.setCondition(String.valueOf(prevEntityDataDelegate.getId()));

        if (exprNodeInfo.getAdditionalFilters() != null) {
            for (EntityQueryFilter f : exprNodeInfo.getAdditionalFilters()) {
                EntityQueryFilter queryFilter = new EntityQueryFilter();
                queryFilter.setAttrName(f.getAttrName());
                queryFilter.setOp(f.getOp());
                queryFilter.setCondition(f.getCondition());
                querySpec.addAdditionalFilters(queryFilter);
            }
        }

        querySpec.setCriteria(criteria);

        return querySpec;
    }

    private void doPerformRefToEntityLinkNodeQuery(EntityOperationContext ctx, EntityQueryLinkNode linkNode,
            EntityRouteDescription entityDef) {
        if (log.isDebugEnabled()) {
            log.debug("perform query for RefTo entity link  node with {}",
                    linkNode.getExprNodeInfo().getEntityQueryNodeExpr());
        }

        EntityQueryLinkNode previousLinkNode = linkNode.getPreviousNode();
        List<EntityDataDelegate> prevEntityDataDelegates = previousLinkNode.getEntityDataDelegates();
        if (prevEntityDataDelegates != null) {
            for (EntityDataDelegate prevEntityDataDelegate : prevEntityDataDelegates) {
                if (prevEntityDataDelegate == null) {
                    continue;
                }

                performRefToEntityDataDelegate(ctx, linkNode, entityDef, prevEntityDataDelegate, previousLinkNode);
            }
        }
    }

    private void performRefToEntityDataDelegate(EntityOperationContext ctx, EntityQueryLinkNode linkNode,
            EntityRouteDescription entityDef, EntityDataDelegate prevEntityDataDelegate,
            EntityQueryLinkNode previousLinkNode) {
        List<EntityQuerySpecification> querySpecs = buildRefToEntityQuerySpecifications(ctx, linkNode, entityDef,
                linkNode.getExprNodeInfo(), previousLinkNode, prevEntityDataDelegate);

        if (log.isInfoEnabled() && (querySpecs.size() > 1)) {
            log.info("performing multi-ref-to querying for {} {}", linkNode.getExprNodeInfo().getPackageName(),
                    linkNode.getExprNodeInfo().getEntityName());
        }

        for (EntityQuerySpecification querySpec : querySpecs) {
            performRestOperation(ctx, linkNode, entityDef, prevEntityDataDelegate, querySpec);
        }

    }

    private List<EntityQuerySpecification> buildRefToEntityQuerySpecifications(EntityOperationContext ctx,
            EntityQueryLinkNode linkNode, EntityRouteDescription entityDef, EntityQueryExprNodeInfo exprNodeInfo,
            EntityQueryLinkNode previousLinkNode, EntityDataDelegate prevEntityDataDelegate) {
        List<EntityQuerySpecification> specs = new ArrayList<EntityQuerySpecification>();
        if (prevEntityDataDelegate.getQueryAttrValue() == null) {
            return specs;
        }

        String queryAttrValueStr = String.valueOf(prevEntityDataDelegate.getQueryAttrValue());
        if (queryAttrValueStr.trim().length() <= 0) {
            return specs;
        }

        queryAttrValueStr = stripHeadAndTailChar(queryAttrValueStr, "[");
        queryAttrValueStr = stripHeadAndTailChar(queryAttrValueStr, "]");

        String[] queryAttrValueParts = queryAttrValueStr.split(",");

        for (String queryAttrValuePart : queryAttrValueParts) {
            EntityQuerySpecification spec = buildRefToEntityQuerySpecification(ctx, linkNode, entityDef, exprNodeInfo,
                    previousLinkNode, prevEntityDataDelegate, queryAttrValuePart);
            specs.add(spec);
        }

        return specs;
    }

    private EntityQuerySpecification buildRefToEntityQuerySpecification(EntityOperationContext ctx,
            EntityQueryLinkNode linkNode, EntityRouteDescription entityDef, EntityQueryExprNodeInfo exprNodeInfo,
            EntityQueryLinkNode previousLinkNode, EntityDataDelegate prevEntityDataDelegate, String queryAttrValueStr) {
        EntityQuerySpecification querySpec = new EntityQuerySpecification();
        EntityQueryCriteria criteria = new EntityQueryCriteria();
        criteria.setAttrName(EntityDataDelegate.UNIQUE_IDENTIFIER);
        criteria.setCondition(queryAttrValueStr.trim());

        if (exprNodeInfo.getAdditionalFilters() != null) {
            for (EntityQueryFilter f : exprNodeInfo.getAdditionalFilters()) {
                EntityQueryFilter queryFilter = new EntityQueryFilter();
                queryFilter.setAttrName(f.getAttrName());
                queryFilter.setOp(f.getOp());
                queryFilter.setCondition(f.getCondition());
                querySpec.addAdditionalFilters(queryFilter);
            }
        }

        querySpec.setCriteria(criteria);

        return querySpec;
    }

    private void doPerformHeadEntityLinkNodeQuery(EntityOperationContext ctx, EntityQueryLinkNode linkNode,
            EntityRouteDescription entityDef) {
        if (log.isDebugEnabled()) {
            log.debug("perform query for head entity link  node with {}",
                    linkNode.getExprNodeInfo().getEntityQueryNodeExpr());
        }
        EntityQueryExprNodeInfo exprNodeInfo = linkNode.getExprNodeInfo();
        EntityQuerySpecification querySpec = new EntityQuerySpecification();

        EntityQueryCriteria criteria = null;
        if (ctx.getOriginalEntityData() != null && ctx.getOriginalEntityData().trim().length() > 0) {
            criteria = new EntityQueryCriteria();
            criteria.setAttrName(EntityDataDelegate.UNIQUE_IDENTIFIER);
            criteria.setCondition(ctx.getOriginalEntityData());
        }

        if (exprNodeInfo.getAdditionalFilters() != null) {
            for (EntityQueryFilter f : exprNodeInfo.getAdditionalFilters()) {
                EntityQueryFilter queryFilter = new EntityQueryFilter();
                queryFilter.setAttrName(f.getAttrName());
                queryFilter.setOp(f.getOp());
                queryFilter.setCondition(f.getCondition());
                querySpec.addAdditionalFilters(queryFilter);
            }
        }

        if (criteria != null) {
            querySpec.setCriteria(criteria);
        }

        performRestOperation(ctx, linkNode, entityDef, null, querySpec);

    }

    private void performRestOperation(EntityOperationContext ctx, EntityQueryLinkNode linkNode,
            EntityRouteDescription entityDef, EntityDataDelegate prevEntityDataDelegate,
            EntityQuerySpecification querySpec) {
        List<Map<String, Object>> cachedRecordMapList = trySearchFromCache(ctx, entityDef, querySpec);
        if (cachedRecordMapList != null) {
            log.debug("picked out query result from cache for {}", linkNode.getExprNodeInfo());
            performEntityDataExtractionFromCachedResultData(ctx, linkNode, prevEntityDataDelegate, cachedRecordMapList);
            return;
        }
        StandardEntityOperationRestClient restClient = ctx.getStandardEntityOperationRestClient();
        StandardEntityOperationResponseDto responseDto = restClient.query(entityDef, querySpec);

        if (StandardEntityOperationResponseDto.STATUS_OK.equalsIgnoreCase(responseDto.getStatus())) {
            List<Map<String, Object>> recordMapList = performEntityDataExtraction(ctx, linkNode, prevEntityDataDelegate,
                    responseDto.getData());
            tryCacheQueryResultData(ctx, entityDef, querySpec, recordMapList);
        } else {
            log.error("Error status met {} with message {}", responseDto.getStatus(), responseDto.getMessage());
            String msg = String.format("Errors met while fetching data from %s due to status %s.",
                    entityDef.getPackageName(), responseDto.getStatus());
            throw new EntityOperationException("3309", msg, entityDef.getPackageName(), responseDto.getStatus());
        }
    }

    protected void tryCacheQueryResultData(EntityOperationContext ctx, EntityRouteDescription entityDef,
            EntityQuerySpecification querySpec, List<Map<String, Object>> recordMapList) {
        Map<Object, Object> externalCacheMap = ctx.getExternalCacheMap();
        if (externalCacheMap == null) {
            log.debug("There is no external cache provided to cache.");
            return;
        }
        StandardEntityQueryCacheKey key = new StandardEntityQueryCacheKey(entityDef, querySpec);
        StandardEntityQueryCacheData data = new StandardEntityQueryCacheData(recordMapList, System.currentTimeMillis());
        
        externalCacheMap.put(key, data);
    }

    @SuppressWarnings("unchecked")
    protected List<Map<String, Object>> trySearchFromCache(EntityOperationContext ctx, EntityRouteDescription entityDef,
            EntityQuerySpecification querySpec) {
        Map<Object, Object> externalCacheMap = ctx.getExternalCacheMap();
        if (externalCacheMap == null) {
            log.debug("There is no external cache provided to search.");
            return null;
        }
        
        StandardEntityQueryCacheKey key = new StandardEntityQueryCacheKey(entityDef, querySpec);
        Object objData = externalCacheMap.get(key);
        if(objData == null){
            return null;
        }
        if(!(objData instanceof StandardEntityQueryCacheData)){
            return null;
        }
        StandardEntityQueryCacheData data = (StandardEntityQueryCacheData)objData;
        Object cachedObjData = data.getData();
        if(cachedObjData == null){
            return null;
        }
        
        return (List<Map<String, Object>>)cachedObjData;
    }

    protected List<Map<String, Object>> performEntityDataExtractionFromCachedResultData(EntityOperationContext ctx,
            EntityQueryLinkNode linkNode, EntityDataDelegate prevEntityDataDelegate,
            List<Map<String, Object>> recordMapList) {
        for (Map<String, Object> recordMap : recordMapList) {
            EntityDataDelegate delegate = buildEntityDataDelegate(prevEntityDataDelegate, recordMap, linkNode);
            linkNode.addEntityDataDelegates(delegate);
        }

        return recordMapList;

    }

    protected List<Map<String, Object>> performEntityDataExtraction(EntityOperationContext ctx,
            EntityQueryLinkNode linkNode, EntityDataDelegate prevEntityDataDelegate, Object responseData) {
        List<Map<String, Object>> recordMapList = extractEntityDataFromResponse(responseData);
        for (Map<String, Object> recordMap : recordMapList) {
            EntityDataDelegate delegate = buildEntityDataDelegate(prevEntityDataDelegate, recordMap, linkNode);
            linkNode.addEntityDataDelegates(delegate);
        }

        return recordMapList;
    }

    private EntityDataDelegate buildEntityDataDelegate(EntityDataDelegate prevEntityDataDelegate,
            Map<String, Object> recordMap, EntityQueryLinkNode linkNode) {
        EntityDataDelegate entity = new EntityDataDelegate();
        entity.setEntityData(recordMap);
        entity.setDisplayName((String) recordMap.get(EntityDataDelegate.VISUAL_FIELD));
        entity.setId((String) recordMap.get(EntityDataDelegate.UNIQUE_IDENTIFIER));
        entity.setPackageName(linkNode.getExprNodeInfo().getPackageName());
        entity.setEntityName(linkNode.getExprNodeInfo().getEntityName());

        if (prevEntityDataDelegate != null) {
            entity.setPreviousEntity(prevEntityDataDelegate);
        }

        if (linkNode.hasQueryAttribute()) {
            entity.setQueryAttrName(linkNode.getQueryAttributeName());
            entity.setQueryAttrValue(recordMap.get(linkNode.getQueryAttributeName()));
        }

        return entity;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractEntityDataFromResponse(Object responseData) {
        List<Map<String, Object>> recordMapList = new ArrayList<Map<String, Object>>();
        if (responseData == null) {
            log.info("response data is empty");
            return recordMapList;
        }

        if (responseData instanceof List) {
            List<?> dataList = ((List<Map<String, Object>>) responseData);
            for (Object m : dataList) {
                if (m == null) {
                    continue;
                }
                if (m instanceof Map) {
                    Map<String, Object> dataMap = (Map<String, Object>) m;
                    recordMapList.add(dataMap);
                }
            }
        } else if (responseData instanceof Map) {
            Map<String, Object> dataMap = ((Map<String, Object>) responseData);
            recordMapList.add(dataMap);
        }

        return recordMapList;
    }

    protected List<EntityDataDelegate> extractLeafEntityData(EntityOperationContext ctx) {
        EntityQueryLinkNode tailEntityQueryLinkNode = ctx.getTailEntityQueryLinkNode();
        List<EntityDataDelegate> retDataDelegates = new ArrayList<>();
        for (EntityDataDelegate e : tailEntityQueryLinkNode.getEntityDataDelegates()) {
            EntityDataDelegate ret = new EntityDataDelegate();
            ret.setDisplayName(e.getDisplayName());
            ret.setEntityName(e.getEntityName());
            ret.setId(e.getId());
            ret.setPackageName(e.getPackageName());
            ret.setQueryAttrName(e.getQueryAttrName());
            ret.setQueryAttrValue(e.getQueryAttrValue());
            ret.setEntityData(e.getEntityData());

            retDataDelegates.add(ret);

        }
        return retDataDelegates;
    }

    protected List<Object> extractAttrValues(EntityOperationContext ctx) {
        EntityQueryLinkNode tailEntityQueryLinkNode = ctx.getTailEntityQueryLinkNode();
        return Collections.unmodifiableList(tailEntityQueryLinkNode.extractFinalAttributeValues());
    }

    protected List<StandardEntityDataNode> generateHierarchicalEntityTreeNodes(EntityOperationContext ctx) {
        List<StandardEntityDataNode> result = new ArrayList<>();

        EntityQueryLinkNode headEntityQueryLinkNode = ctx.getHeadEntityQueryLinkNode();
        EntityQueryLinkNode linkNode = headEntityQueryLinkNode;

        while (linkNode != null) {
            pupolateTreeNodeWithLinkNode(result, linkNode);
            linkNode = linkNode.getSucceedingNode();
        }

        return result;
    }

    protected EntityTreeNodesOverview buildEntityTreeNodesOverview(List<StandardEntityDataNode> hierarchicalEntityNodes,
            List<StandardEntityDataNode> leafNodeEntityNodes) {
        EntityTreeNodesOverview result = new EntityTreeNodesOverview();
        if (hierarchicalEntityNodes != null) {
            for (StandardEntityDataNode tn : hierarchicalEntityNodes) {
                result.addHierarchicalEntityNodes(tn);
            }
        }

        if (leafNodeEntityNodes != null) {
            for (StandardEntityDataNode tn : leafNodeEntityNodes) {
                result.addLeafNodeEntityNodes(tn);
            }
        }

        return result;
    }

    protected List<StandardEntityDataNode> generateLeafNodeEntityNodes(EntityOperationContext ctx) {
        EntityQueryLinkNode leafLinkNode = ctx.getTailEntityQueryLinkNode();
        List<EntityDataDelegate> entityDataDelegates = leafLinkNode.getEntityDataDelegates();
        List<StandardEntityDataNode> result = new ArrayList<>();

        for (EntityDataDelegate delegate : entityDataDelegates) {
            if (delegate == null) {
                continue;
            }

            StandardEntityDataNode tn = new StandardEntityDataNode();
            tn.setPackageName(leafLinkNode.getExprNodeInfo().getPackageName());
            tn.setEntityName(leafLinkNode.getExprNodeInfo().getEntityName());
            tn.setDisplayName(delegate.getDisplayName());
            tn.setId(delegate.getId());
            tn.setFullId(delegate.getFullId());

            result.add(tn);
        }
        return result;
    }

    private String stripHeadAndTailChar(String s, String specialChar) {
        String data = s;
        if (data.startsWith(specialChar)) {
            data = data.substring(1);
        }

        if (data.endsWith(specialChar)) {
            data = data.substring(0, data.length() - 1);
        }

        return data;
    }

}
