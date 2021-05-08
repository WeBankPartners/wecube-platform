package com.webank.wecube.platform.core.service.dme;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
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

    public List<Map<String, Object>> queryAttributeValuesOfLeafNode(EntityOperationRootCondition condition, Map<Object, Object> externalCacheMap) {
        return queryAttributeValuesOfLeafNode(condition, jwtSsoRestTemplate, externalCacheMap);
    }

    public List<Map<String, Object>> queryAttributeValuesOfLeafNode(EntityOperationRootCondition condition,
            RestTemplate restTemplate, Map<Object, Object> externalCacheMap) {
        if (log.isDebugEnabled()) {
            log.debug("query attribute values of leaf node for condition {}", condition);
        }

        EntityOperationContext ctx = buildEntityOperationContext(condition, restTemplate, externalCacheMap);
        ctx.setEntityOperationType(EntityOperationType.QUERY);

        List<EntityDataDelegate> entityDelegates = standardEntityQueryExcutor.executeQueryLeafEntity(ctx);
        List<Map<String, Object>> result = new ArrayList<>();
        if (entityDelegates == null) {
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

    public List<Object> queryAttributeValues(EntityOperationRootCondition condition, Map<Object, Object> externalCacheMap) {
        if (log.isDebugEnabled()) {
            log.debug("query entity with condition {}", condition);
        }

        EntityOperationContext ctx = buildEntityOperationContext(condition, jwtSsoRestTemplate, externalCacheMap);
        ctx.setEntityOperationType(EntityOperationType.QUERY);
        return standardEntityQueryExcutor.executeQueryLeafAttributes(ctx);
    }

    public List<Object> queryAttributeValues(EntityOperationRootCondition condition, RestTemplate restTemplate, Map<Object, Object> externalCacheMap) {
        if (log.isDebugEnabled()) {
            log.debug("query entity with condition {}", condition);
        }

        EntityOperationContext ctx = buildEntityOperationContext(condition, restTemplate, externalCacheMap);
        ctx.setEntityOperationType(EntityOperationType.QUERY);
        return standardEntityQueryExcutor.executeQueryLeafAttributes(ctx);
    }

    public void update(EntityOperationRootCondition condition, Object attrValueToUpdate, RestTemplate restTemplate, Map<Object, Object> externalCacheMap) {
        if (log.isInfoEnabled()) {
            log.info("update entity with condition {} and data {}", condition, attrValueToUpdate);
        }

        EntityOperationContext ctx = buildEntityOperationContext(condition, restTemplate, externalCacheMap);
        ctx.setEntityOperationType(EntityOperationType.UPDATE);

        standardEntityQueryExcutor.executeUpdate(ctx, attrValueToUpdate);

        return;
    }

    public void update(EntityOperationRootCondition condition, Object attrValueToUpdate, Map<Object, Object> externalCacheMap) {
        update(condition, attrValueToUpdate, jwtSsoRestTemplate, externalCacheMap);
        return;
    }
    
    public Map<String,Object> create(String packageName,String entityName, Map<String, Object> objDataMap){
        if(objDataMap == null || objDataMap.isEmpty()){
            return null;
        }
        
        if(StringUtils.isBlank(packageName) || StringUtils.isBlank(entityName)){
            throw new IllegalArgumentException("package name and entity name must provide.");
        }
        
        EntityRouteDescription entityDef = entityDataRouteFactory.deduceEntityDescription(packageName, entityName);
        
        List<EntityDataRecord> recordsToCreate = new ArrayList<>();
        EntityDataRecord newEntityDataRecord = new EntityDataRecord();
        for(Map.Entry<String, Object> attrEntry : objDataMap.entrySet()){
            EntityDataAttr attr = new EntityDataAttr();
            attr.setAttrName(attrEntry.getKey());
            attr.setAttrValue(attrEntry.getValue());
            
            newEntityDataRecord.addAttrs(attr);
        }
        
        recordsToCreate.add(newEntityDataRecord);
        
        EntityOperationContext ctx = new EntityOperationContext();
        ctx.setEntityQueryExprNodeInfos(null);
        ctx.setOriginalEntityLinkExpression(null);
        ctx.setOriginalEntityData(null);
        ctx.setStandardEntityOperationRestClient(new StandardEntityOperationRestClient(this.getRestTemplate()));
        ctx.setHeadEntityQueryLinkNode(null);
        ctx.setEntityDataRouteFactory(entityDataRouteFactory);
        
        Map<String, Object> createdDataMap = standardEntityQueryExcutor.executeCreate(ctx, entityDef, recordsToCreate);
        
        return createdDataMap;
    }
    
    public boolean delete(String packageName, String entityName, String entityDataId){
        return false;
    }

    public EntityTreeNodesOverview generateEntityLinkOverview(EntityOperationRootCondition condition, Map<Object, Object> externalCacheMap) {
        return generateEntityLinkOverview(condition, jwtSsoRestTemplate, externalCacheMap);
    }

    public EntityTreeNodesOverview generateEntityLinkOverview(EntityOperationRootCondition condition,
            RestTemplate restTemplate, Map<Object, Object> externalCacheMap) {
        if (log.isInfoEnabled()) {
            log.info("generate entity link overview with condition {}", condition);
        }

        EntityOperationContext ctx = buildEntityOperationContext(condition, restTemplate, externalCacheMap);
        ctx.setEntityOperationType(EntityOperationType.QUERY);

        return standardEntityQueryExcutor.generateEntityLinkOverview(ctx);
    }

    public List<StandardEntityDataNode> generatePreviewTree(EntityOperationRootCondition condition, Map<Object, Object> externalCacheMap) {
        if (log.isInfoEnabled()) {
            log.info("generate preview tree with condition {}", condition);
        }

        EntityOperationContext ctx = buildEntityOperationContext(condition, jwtSsoRestTemplate, externalCacheMap);
        ctx.setEntityOperationType(EntityOperationType.UPDATE);
        return standardEntityQueryExcutor.generatePreviewTree(ctx);
    }

    protected EntityOperationContext buildEntityOperationContext(EntityOperationRootCondition condition,
            RestTemplate restTemplate, Map<Object, Object> externalCacheMap) {
        List<EntityQueryExprNodeInfo> exprNodeInfos = entityQueryExpressionParser.parse(condition.getEntityLinkExpr());

        EntityOperationContext ctx = new EntityOperationContext();
        ctx.setEntityQueryExprNodeInfos(exprNodeInfos);
        ctx.setOriginalEntityLinkExpression(condition.getEntityLinkExpr());
        ctx.setOriginalEntityData(condition.getEntityIdentity());
        ctx.setStandardEntityOperationRestClient(new StandardEntityOperationRestClient(restTemplate));
        ctx.setHeadEntityQueryLinkNode(standardEntityQueryExcutor.buildEntityQueryLinkNode(exprNodeInfos));
        ctx.setEntityDataRouteFactory(entityDataRouteFactory);
        
        if(externalCacheMap != null){
            ctx.setExternalCacheMap(externalCacheMap);
        }

        return ctx;
    }

    public RestTemplate getRestTemplate() {
        return jwtSsoRestTemplate;
    }

}
