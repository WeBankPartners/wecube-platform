package com.webank.wecube.platform.core.service.data;

import java.util.ArrayList;
import java.util.Collections;
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

import com.webank.wecube.platform.core.dto.data.EntityQueryFilterDto;
import com.webank.wecube.platform.core.dto.data.EntityQuerySpecDto;
import com.webank.wecube.platform.core.dto.plugin.PluginPackageAttributeDto;
import com.webank.wecube.platform.core.entity.workflow.ProcInstInfoEntity;
import com.webank.wecube.platform.core.model.workflow.WorkflowInstCreationContext;
import com.webank.wecube.platform.core.repository.workflow.ProcInstInfoMapper;
import com.webank.wecube.platform.core.service.dme.EntityDataRouteFactory;
import com.webank.wecube.platform.core.service.dme.EntityQueryFilter;
import com.webank.wecube.platform.core.service.dme.EntityQuerySpecification;
import com.webank.wecube.platform.core.service.dme.EntityRouteDescription;
import com.webank.wecube.platform.core.service.dme.StandardEntityOperationResponseDto;
import com.webank.wecube.platform.core.service.dme.StandardEntityOperationRestClient;
import com.webank.wecube.platform.core.service.plugin.PluginPackageDataModelService;
import com.webank.wecube.platform.core.service.workflow.WorkflowDataService;
import com.webank.wecube.platform.core.support.plugin.dto.DynamicEntityAttrValueDto;
import com.webank.wecube.platform.core.support.plugin.dto.DynamicEntityValueDto;
import com.webank.wecube.platform.core.utils.Constants;

@Service
public class EntityDataCenterService {
    private static final Logger log = LoggerFactory.getLogger(EntityDataCenterService.class);

    @Autowired
    protected EntityDataRouteFactory entityDataRouteFactory;

    @Autowired
    @Qualifier("userJwtSsoTokenRestTemplate")
    protected RestTemplate userJwtSsoTokenRestTemplate;

    @Autowired
    protected WorkflowDataService workflowDataService;

    @Autowired
    protected ProcInstInfoMapper procInstInfoMapper;

    @Autowired
    protected PluginPackageDataModelService pluginPackageDataModelService;

    public List<Map<String, Object>> retieveEntities(String packageName, String entityName,
            EntityQuerySpecDto querySpecDto) {
        
        String procInstId = querySpecDto.getProcInstId();
        String nodeInstId = querySpecDto.getNodeInstId();

        List<Map<String, Object>> entityDataList = tryRetrieveEntitiesFromPlugin(packageName, entityName, querySpecDto);

        if ((entityDataList != null) && (!entityDataList.isEmpty())) {
            return entityDataList;
        }

        entityDataList = tryRetrieveEntitiesFromTemporary(packageName, entityName, querySpecDto, procInstId,
                nodeInstId);

        if (entityDataList == null) {
            entityDataList = Collections.emptyList();
        }

        return entityDataList;
    }

    protected List<Map<String, Object>> tryRetrieveEntitiesFromTemporary(String packageName, String entityName,
            EntityQuerySpecDto querySpecDto, String procInstIdStr, String nodeInstIdStr) {
        List<Map<String, Object>> entityDataList = new ArrayList<Map<String, Object>>();
        if (StringUtils.isBlank(procInstIdStr)) {
            return entityDataList;
        }

        if (querySpecDto == null) {
            return entityDataList;
        }

        EntityQueryFilterDto idFilterDto = querySpecDto.findOutIdFilter();
        if (idFilterDto == null) {
            return entityDataList;
        }

        String targetEntityOid = (String) idFilterDto.getCondition();
        if (StringUtils.isBlank(targetEntityOid)) {
            return entityDataList;
        }

        if (targetEntityOid.startsWith(Constants.TEMPORARY_ENTITY_ID_PREFIX)) {
            targetEntityOid = targetEntityOid.substring(Constants.TEMPORARY_ENTITY_ID_PREFIX.length());
        }

        int procInstId = Integer.parseInt(procInstIdStr);
        ProcInstInfoEntity procInstInfo = procInstInfoMapper.selectByPrimaryKey(procInstId);
        if (procInstInfo == null) {
            return entityDataList;
        }

        WorkflowInstCreationContext workflowInstCreationCtx = workflowDataService
                .tryFetchWorkflowInstCreationContext(procInstInfo);
        if (workflowInstCreationCtx == null) {
            return entityDataList;
        }

        DynamicEntityValueDto targetEntityValueDto = workflowInstCreationCtx.findByOid(targetEntityOid);
        if (targetEntityValueDto == null) {
            return entityDataList;
        }

        Map<String, Object> targetEntityValueAsMap = convertDynamicEntityValueDto(targetEntityValueDto);
        entityDataList.add(targetEntityValueAsMap);
        return entityDataList;
    }

    protected Map<String, Object> convertDynamicEntityValueDto(DynamicEntityValueDto entityValueDto) {
        Map<String, Object> targetEntityValueAsMap = new HashMap<>();
        targetEntityValueAsMap.put("id", entityValueDto.getOid());
        targetEntityValueAsMap.put("dataId", entityValueDto.getEntityDataId());
        targetEntityValueAsMap.put("displayName", entityValueDto.getEntityDisplayName());
        
        List<DynamicEntityAttrValueDto> attrValues = entityValueDto.getAttrValues();
        if (attrValues == null || attrValues.isEmpty()) {
            return targetEntityValueAsMap;
        }

        for (DynamicEntityAttrValueDto attrValue : attrValues) {
            targetEntityValueAsMap.put(attrValue.getAttrName(), attrValue.getDataValue());
        }

        return targetEntityValueAsMap;

    }

    protected List<Map<String, Object>> tryRetrieveEntitiesFromPlugin(String packageName, String entityName,
            EntityQuerySpecDto querySpecDto) {
        StandardEntityOperationRestClient client = new StandardEntityOperationRestClient(userJwtSsoTokenRestTemplate);
        EntityQuerySpecification querySpec = buildEntityQuerySpecification(querySpecDto);

        List<Map<String, Object>> results = retrieveEntitiesFromPlugin(packageName, entityName, querySpec, client);

        return results;
    }

    protected List<Map<String, Object>> retrieveEntitiesFromPlugin(String packageName, String entityName,
            EntityQuerySpecification querySpec, StandardEntityOperationRestClient client) {
        EntityRouteDescription entityRoute = entityDataRouteFactory.deduceEntityDescription(packageName, entityName);

        StandardEntityOperationResponseDto respDto = client.query(entityRoute, querySpec);
        List<Map<String, Object>> resultMaps = extractEntitiesDataFromPluginResponse(respDto.getData());

        if (resultMaps == null || resultMaps.isEmpty()) {
            return resultMaps;
        }

        Map<String, PluginPackageAttributeDto> attrDefs = getAttrDefs(packageName, entityName);

        if (attrDefs == null || attrDefs.isEmpty()) {
            return resultMaps;
        }

        List<Map<String, Object>> enrichedResultMaps = new ArrayList<>();
        for (Map<String, Object> resultMap : resultMaps) {
            Map<String, Object> enrichedResultMap = new HashMap<>();
            for (String attrName : resultMap.keySet()) {
                PluginPackageAttributeDto attrDef = attrDefs.get(attrName);
                if (attrDef == null) {
                    enrichedResultMap.put(attrName, resultMap.get(attrName));
                } else {
                    String attrDataType = attrDef.getDataType();

                    if ("ref".equalsIgnoreCase(attrDataType)) {
                        Object refValue = tryCalRefValue(attrName, attrDef, resultMap.get(attrName), client);
                        enrichedResultMap.put(attrName, refValue);
                    } else {
                        enrichedResultMap.put(attrName, resultMap.get(attrName));
                    }
                }
            }

            enrichedResultMaps.add(enrichedResultMap);
        }

        return enrichedResultMaps;
    }

    protected Object tryCalRefValue(String attrName, PluginPackageAttributeDto attrDef, Object refValueAsObj,
            StandardEntityOperationRestClient client) {
        if (refValueAsObj == null) {
            return null;
        }
        String multiple = attrDef.getMultiple();
        
        if(Constants.DATA_MULTIPLE.equalsIgnoreCase(multiple)) {
            return tryCalMultipleRefValue( attrName,  attrDef,  refValueAsObj,
                     client);
        }else {
            return tryCalSingleRefValue( attrName,  attrDef,  refValueAsObj,
                     client);
        }

    }
    
    protected Object tryCalSingleRefValue(String attrName, PluginPackageAttributeDto attrDef, Object refValueAsObj,
            StandardEntityOperationRestClient client){
        String refValueAsStr = null;
        if(refValueAsObj instanceof String) {
            refValueAsStr = (String)refValueAsObj;
        }else {
            return refValueAsObj;
        }
        
        String refPackageName = attrDef.getRefPackageName();
        String refEntityName = attrDef.getRefEntityName();
        String refAttrName = attrDef.getRefAttributeName();
        
        List<Map<String, Object>> resultMaps = queryEntityData( refPackageName,  refEntityName,  refAttrName, refValueAsStr,  client);
        
        if(resultMaps == null || resultMaps.isEmpty()) {
            return refValueAsObj;
        }
        
        Map<String, Object> resultMap = resultMaps.get(0);
        return resultMap;
    }
    
    protected List<Map<String, Object>> queryEntityData(String packageName, String entityName, String attrName,Object condition, StandardEntityOperationRestClient client){
        EntityQuerySpecification querySpec = new EntityQuerySpecification();
        

        EntityQueryFilter f = new EntityQueryFilter();
        f.setAttrName(attrName);
        f.setCondition(condition);
        f.setOp(EntityQueryFilter.OP_EQUALS);

        querySpec.addAdditionalFilters(f);
        
        EntityRouteDescription entityRoute = entityDataRouteFactory.deduceEntityDescription(packageName, entityName);

        StandardEntityOperationResponseDto respDto = client.query(entityRoute, querySpec);
        List<Map<String, Object>> resultMaps = extractEntitiesDataFromPluginResponse(respDto.getData());

        return resultMaps;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected List<Object> tryCalMultipleRefValue(String attrName, PluginPackageAttributeDto attrDef, Object refValueAsObj,
            StandardEntityOperationRestClient client){
        String refPackageName = attrDef.getRefPackageName();
        String refEntityName = attrDef.getRefEntityName();
        String refAttrName = attrDef.getRefAttributeName();
        
        List<Object> results = new ArrayList<>();
        List<String> conditions = new ArrayList<>();
        boolean unknownRefValue = false;
        if(refValueAsObj instanceof String) {
            String refValueAsStr = (String)refValueAsObj;
            String [] parts = refValueAsStr.split(",");
            for(String part : parts) {
                conditions.add(part);
            }
        }else if(refValueAsObj instanceof List) {
            List<Object> refValueAsList = (List)refValueAsObj;
            for(Object refObj : refValueAsList) {
                if(refObj instanceof String) {
                    conditions.add((String)refObj);
                }else {
                    unknownRefValue = true;
                }
            }
        }else {
            results.add(refValueAsObj);
            return results;
        }
        
        if(unknownRefValue) {
            results.add(refValueAsObj);
            return results;
        }
        
        for(String condition : conditions) {
            List<Map<String, Object>> dataMaps = queryEntityData(refPackageName, refEntityName, refAttrName, condition,  client);
            if(dataMaps == null) {
                Map<String, Object> blankDataMap = new HashMap<>();
                results.add(blankDataMap);
            }else {
                for(Map<String, Object> dataMap : dataMaps) {
                    results.add(dataMap);
                }
            }
        }
        
        return results;
    }

    protected Map<String, PluginPackageAttributeDto> getAttrDefs(String packageName, String entityName) {
        Map<String, PluginPackageAttributeDto> attrDefMap = new HashMap<>();
        List<PluginPackageAttributeDto> attrDefs = pluginPackageDataModelService.entityView(packageName, entityName);

        if (attrDefs == null || attrDefs.isEmpty()) {
            return attrDefMap;
        }

        for (PluginPackageAttributeDto attrDef : attrDefs) {
            attrDefMap.put(attrDef.getName(), attrDef);
        }

        return attrDefMap;
    }

    protected EntityQuerySpecification buildEntityQuerySpecification(EntityQuerySpecDto querySpecDto) {
        EntityQuerySpecification spec = new EntityQuerySpecification();
        if (querySpecDto == null) {
            return spec;
        }

        List<EntityQueryFilterDto> additionalFilters = querySpecDto.getAdditionalFilters();
        if (additionalFilters == null || additionalFilters.isEmpty()) {
            return spec;
        }

        for (EntityQueryFilterDto filterDto : additionalFilters) {
            EntityQueryFilter f = new EntityQueryFilter();
            f.setAttrName(filterDto.getAttrName());
            f.setCondition(filterDto.getCondition());
            f.setOp(filterDto.getOp());

            spec.addAdditionalFilters(f);
        }

        return spec;
    }

    @SuppressWarnings("unchecked")
    protected List<Map<String, Object>> extractEntitiesDataFromPluginResponse(Object responseData) {
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

}
