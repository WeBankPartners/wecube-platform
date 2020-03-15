package com.webank.wecube.platform.core.service.dme;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StandardEntityQueryExecutor {
    private static final Logger log = LoggerFactory.getLogger(StandardEntityQueryExecutor.class);

    public void executeUpdate(EntityOperationContext ctx, Object valueToUpdate) {
        List<EntityDataDelegate> entitiesToUpdate = executeQueryLeafEntity(ctx);
        List<EntityDataRecord> entityDataRecordsToUpdate = buildEntityDataRecords(entitiesToUpdate, valueToUpdate);
        
        EntityQueryLinkNode leafLinkNode = ctx.getTailEntityQueryLinkNode();
        EntityQueryExprNodeInfo nodeInfo = leafLinkNode.getExprNodeInfo();
        EntityDescription entityDef = ctx.getEntityDataRouter().deduceEntityDescription(nodeInfo.getEntityName(),
                nodeInfo.getPackageName());
        
        StandardEntityOperationRestClient restClient = ctx.getStandardEntityOperationRestClient();
        restClient.update(entityDef, entityDataRecordsToUpdate);
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
            log.info("perform query for {}", linkNode.getExprNodeInfo().getEntityQueryNodeExpr());
        }

        EntityQueryExprNodeInfo nodeInfo = linkNode.getExprNodeInfo();
        EntityDescription entityDef = ctx.getEntityDataRouter().deduceEntityDescription(nodeInfo.getEntityName(),
                nodeInfo.getPackageName());

        doPerformQuery(ctx, linkNode, entityDef);
    }

    protected List<EntityDataRecord> buildEntityDataRecords(List<EntityDataDelegate> entitiesToUpdate,
            Object valueToUpdate) {
        List<EntityDataRecord> dataRecords = new ArrayList<>();
        for (EntityDataDelegate delegate : entitiesToUpdate) {
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

    private void doPerformQuery(EntityOperationContext ctx, EntityQueryLinkNode linkNode, EntityDescription entityDef) {

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
            EntityDescription entityDef) {
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

                performRestOperation(ctx, linkNode, entityDef, querySpec);
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
            EntityQueryLinkNode linkNode, EntityDescription entityDef, EntityQueryExprNodeInfo exprNodeInfo,
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
            EntityDescription entityDef) {
        if (log.isDebugEnabled()) {
            log.debug("perform query for RefTo entity link  node with {}",
                    linkNode.getExprNodeInfo().getEntityQueryNodeExpr());
        }

        EntityQueryExprNodeInfo exprNodeInfo = linkNode.getExprNodeInfo();

        EntityQueryLinkNode previousLinkNode = linkNode.getPreviousNode();
        List<EntityDataDelegate> prevEntityDataDelegates = previousLinkNode.getEntityDataDelegates();
        if (prevEntityDataDelegates != null) {
            for (EntityDataDelegate prevEntityDataDelegate : prevEntityDataDelegates) {
                EntityQuerySpecification querySpec = buildRefToEntityQuerySpecification(ctx, linkNode, entityDef,
                        exprNodeInfo, previousLinkNode, prevEntityDataDelegate);

                performRestOperation(ctx, linkNode, entityDef, querySpec);

            }
        }
    }

    private EntityQuerySpecification buildRefToEntityQuerySpecification(EntityOperationContext ctx,
            EntityQueryLinkNode linkNode, EntityDescription entityDef, EntityQueryExprNodeInfo exprNodeInfo,
            EntityQueryLinkNode previousLinkNode, EntityDataDelegate prevEntityDataDelegate) {
        EntityQuerySpecification querySpec = new EntityQuerySpecification();
        EntityQueryCriteria criteria = new EntityQueryCriteria();
        criteria.setAttrName(EntityDataDelegate.UNIQUE_IDENTIFIER);
        criteria.setCondition(String.valueOf(prevEntityDataDelegate.getQueryAttrValue()));

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
            EntityDescription entityDef) {
        if (log.isDebugEnabled()) {
            log.debug("perform query for head entity link  node with {}",
                    linkNode.getExprNodeInfo().getEntityQueryNodeExpr());
        }
        EntityQueryExprNodeInfo exprNodeInfo = linkNode.getExprNodeInfo();
        EntityQuerySpecification querySpec = new EntityQuerySpecification();
        EntityQueryCriteria criteria = new EntityQueryCriteria();
        criteria.setAttrName(EntityDataDelegate.UNIQUE_IDENTIFIER);
        criteria.setCondition(ctx.getOriginalEntityData());

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

        performRestOperation(ctx, linkNode, entityDef, querySpec);

    }

    private void performRestOperation(EntityOperationContext ctx, EntityQueryLinkNode linkNode,
            EntityDescription entityDef, EntityQuerySpecification querySpec) {
        StandardEntityOperationRestClient restClient = ctx.getStandardEntityOperationRestClient();
        StandardEntityOperationResponseDto responseDto = restClient.query(entityDef, querySpec);

        if (StandardEntityOperationResponseDto.STATUS_OK.equalsIgnoreCase(responseDto.getStatus())) {
            performEntityDataExtraction(ctx, linkNode, responseDto.getData());
        } else {
            log.error("Error status met {} with message {}", responseDto.getStatus(), responseDto.getMessage());
            throw new IllegalStateException("Error status met.");
        }
    }

    protected void performEntityDataExtraction(EntityOperationContext ctx, EntityQueryLinkNode linkNode,
            Object responseData) {
        List<Map<String, Object>> recordMapList = extractEntityDataFromResponse(responseData);
        for (Map<String, Object> recordMap : recordMapList) {
            linkNode.addEntityDataDelegates(buildEntityDataDelegate(recordMap, linkNode));
        }

    }

    private EntityDataDelegate buildEntityDataDelegate(Map<String, Object> recordMap, EntityQueryLinkNode linkNode) {
        EntityDataDelegate entity = new EntityDataDelegate();
        entity.setEntityData(recordMap);
        entity.setDisplayName((String) recordMap.get(EntityDataDelegate.VISUAL_FIELD));
        entity.setId((String) recordMap.get(EntityDataDelegate.UNIQUE_IDENTIFIER));
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
        return tailEntityQueryLinkNode.getEntityDataDelegates();
    }

    protected List<Object> extractAttrValues(EntityOperationContext ctx) {
        EntityQueryLinkNode tailEntityQueryLinkNode = ctx.getTailEntityQueryLinkNode();
        return tailEntityQueryLinkNode.extractFinalAttributeValues();
    }
}
