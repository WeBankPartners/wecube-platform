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
import com.webank.wecube.platform.core.entity.workflow.ProcInstInfoEntity;
import com.webank.wecube.platform.core.model.workflow.WorkflowInstCreationContext;
import com.webank.wecube.platform.core.repository.workflow.ProcInstInfoMapper;
import com.webank.wecube.platform.core.service.dme.EntityDataRouteFactory;
import com.webank.wecube.platform.core.service.dme.EntityQueryFilter;
import com.webank.wecube.platform.core.service.dme.EntityQuerySpecification;
import com.webank.wecube.platform.core.service.dme.EntityRouteDescription;
import com.webank.wecube.platform.core.service.dme.StandardEntityOperationResponseDto;
import com.webank.wecube.platform.core.service.dme.StandardEntityOperationRestClient;
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

    public List<Map<String, Object>> retieveEntities(String packageName, String entityName,
            EntityQuerySpecDto querySpecDto, String procInstId, String nodeInstId) {

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
        
        if(targetEntityOid.startsWith(Constants.TEMPORARY_ENTITY_ID_PREFIX)) {
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

        DynamicEntityValueDto targetEntityValueDto = workflowInstCreationCtx.findByEntityDataId(targetEntityOid);
        if (targetEntityValueDto == null) {
            return entityDataList;
        }

        Map<String, Object> targetEntityValueAsMap = convertDynamicEntityValueDto(targetEntityValueDto);
        entityDataList.add(targetEntityValueAsMap);
        return entityDataList;
    }

    protected Map<String, Object> convertDynamicEntityValueDto(DynamicEntityValueDto entityValueDto) {
        Map<String, Object> targetEntityValueAsMap = new HashMap<>();
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
        List<Map<String, Object>> results = extractEntitiesDataFromPluginResponse(respDto.getData());

        // TODO process ref attribute

        return results;
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
