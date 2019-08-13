package com.webank.wecube.core.service.workflow;

import java.io.IOException;
import java.util.*;

import com.webank.wecube.core.service.PluginConfigService;
import com.webank.wecube.core.support.cmdb.dto.v2.*;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.model.bpmn.instance.EndEvent;
import org.camunda.bpm.model.bpmn.instance.EventDefinition;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecube.core.commons.WecubeCoreException;
import com.webank.wecube.core.domain.plugin.PluginConfigInterface;
import com.webank.wecube.core.domain.workflow.CiRoutineItem;
import com.webank.wecube.core.domain.workflow.ProcessDefinitionTaskServiceEntity;
import com.webank.wecube.core.domain.workflow.FlowNodeVO;
import com.webank.wecube.core.jpa.ProcessDefinitionEntityRepository;
import com.webank.wecube.core.jpa.ProcessDefinitionTaskServiceEntityRepository;
import com.webank.wecube.core.jpa.PluginConfigRepository;
import com.webank.wecube.core.jpa.ProcessTaskEntityRepository;
import com.webank.wecube.core.jpa.ProcessTransactionEntityRepository;
import com.webank.wecube.core.support.cmdb.CmdbServiceV2Stub;

public abstract class AbstractProcessService {
    private static final Logger log = LoggerFactory.getLogger(AbstractProcessService.class);

    static final String NODE_TYPE_NAME_END_EVENT = "endEvent";

    static final String NODE_TYPE_NAME_ERROR_END_EVENT = "errEndEvent";

    static final String NODE_TYPE_NAME_ERROR_EVENT_DEFINITION = "errorEventDefinition";

    @Autowired
    protected RuntimeService runtimeService;

    @Autowired
    protected ProcessEngine processEngine;

    @Autowired
    protected RepositoryService repositoryService;

    @Autowired
    protected HistoryService historyService;

    @Autowired
    protected CmdbServiceV2Stub cmdbServiceV2Stub;

    @Autowired
    protected ProcessDefinitionEntityRepository coreProcessDefinitionEntityRepository;

    @Autowired
    protected ProcessDefinitionTaskServiceEntityRepository coreProcessDefinitionTaskServiceEntityRepository;

    @Autowired
    protected ProcessTransactionEntityRepository processTransactionEntityRepository;

    @Autowired
    protected PluginConfigRepository pluginConfigRepository;

    @Autowired
    protected ProcessTaskEntityRepository processTaskEntityRepository;

    @Autowired
    PluginConfigService pluginConfigService;

    protected String procDefKeyCmdbAttrName = "orchestration";

    @SuppressWarnings("unchecked")
    protected int calCiTypeIdForTaskServiceNode(Integer rootCiTypeId, String rootCiDataGuid,
                                                ProcessDefinitionTaskServiceEntity entity) throws IOException {
        String ciRoutineExpStr = entity.getBindCiRoutineExp();
        if (StringUtils.isBlank(ciRoutineExpStr)) {
            log.error("CI routine expression is empty, taskNode={}", entity.getTaskNodeId());
            throw new WecubeCoreException("cannot be able to know CI routine expression");
        }

        ObjectMapper mapper = new ObjectMapper();
        JavaType javaType = mapper.getTypeFactory().constructCollectionType(ArrayList.class, CiRoutineItem.class);
        List<CiRoutineItem> routines = (List<CiRoutineItem>) mapper.readValue(ciRoutineExpStr.getBytes(), javaType);

        if (routines.isEmpty()) {
            log.error("routine expression is empty, taskNode={}", entity.getTaskNodeId());
            throw new WecubeCoreException("routine expression is empty");
        }

        CiRoutineItem item = routines.get(routines.size() - 1);

        return item.getCiTypeId();
    }

    @SuppressWarnings("unchecked")
    protected List<SimpleCiDataInfo> calCiDataInfosForTaskServiceNode(Integer rootCiTypeId, String rootCiDataGuid,
                                                                      ProcessDefinitionTaskServiceEntity entity)
            throws IOException {

        List<SimpleCiDataInfo> ciDataInfos = new ArrayList<SimpleCiDataInfo>();

        String ciRoutineExpStr = entity.getBindCiRoutineExp();
        if (StringUtils.isBlank(ciRoutineExpStr)) {
            log.warn("CI routine express is empty, taskNode={}", entity.getTaskNodeId());
            return ciDataInfos;
        }

        String serviceName = entity.getBindServiceName();
        PluginConfigInterface inf = pluginConfigService.getPluginConfigInterfaceByServiceName(serviceName);

        List<CiRoutineItem> routines = buildRoutines(ciRoutineExpStr);
        List<Map<String, Object>> data = buildIntegrationQueryAndGetQueryResult(inf, rootCiDataGuid, routines);

        for (Map<String, Object> ciMap : data) {
            String guid = (String) ciMap.get("tail$guid");

            if (StringUtils.isBlank(guid)) {
                log.warn("guid is blank, ciMap={}", ciMap);
                continue;
            }

            int ciTypeId = routines.get(routines.size() - 1).getCiTypeId();

            SimpleCiDataInfo ciDataInfo = new SimpleCiDataInfo(ciTypeId, guid);

            ciDataInfos.add(ciDataInfo);
        }

        return ciDataInfos;

    }

    protected List<CiRoutineItem> buildRoutines(String ciRoutineExpStr) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(ciRoutineExpStr.getBytes(), mapper.getTypeFactory().constructCollectionType(ArrayList.class, CiRoutineItem.class));
    }

    protected List<Map<String, Object>> buildIntegrationQueryAndGetQueryResult(PluginConfigInterface inf, String rootCiDataGuid, List<CiRoutineItem> routines) throws IOException {
        List<Integer> filterStates = new ArrayList<Integer>();

        if (StringUtils.isNotBlank(inf.getFilterStatus())) {
            String[] strFilterStatuses = inf.getFilterStatus().trim().split(",");
            for (String strFilterStatus : strFilterStatuses) {
                filterStates.add(Integer.parseInt(strFilterStatus));
            }
        }

        AdhocIntegrationQueryDto rootDto = buildRootDto(routines.get(0), rootCiDataGuid);

        IntegrationQueryDto childQueryDto = travelRoutine(routines, rootDto, filterStates, 1);
        if (childQueryDto != null) {
            rootDto.getCriteria().setChildren(Collections.singletonList(childQueryDto));
        }
        return cmdbServiceV2Stub.adhocIntegrationQuery(rootDto).getContents();
    }

    protected AdhocIntegrationQueryDto buildRootDto(CiRoutineItem rootRoutineItem, String rootCiGuid) {
        AdhocIntegrationQueryDto dto = new AdhocIntegrationQueryDto();
        PaginationQuery queryRequest = new PaginationQuery();

//        PaginationQuery.Filter filter = new PaginationQuery.Filter("root$guid", "eq", rootCiGuid);
//        queryRequest.setFilters(Arrays.asList(filter));

        queryRequest.addEqualsFilter("root$guid", rootCiGuid);

        IntegrationQueryDto root = new IntegrationQueryDto();
        dto.setCriteria(root);
        dto.setQueryRequest(queryRequest);

        root.setName("root");
        root.setCiTypeId(rootRoutineItem.getCiTypeId());
        root.setAttrs(Arrays.asList(getGuidAttrIdByCiTypeId(rootRoutineItem.getCiTypeId())));
        root.setAttrKeyNames(Arrays.asList("root$guid"));

        return dto;
    }

    protected IntegrationQueryDto travelRoutine(List<CiRoutineItem> routines, AdhocIntegrationQueryDto rootDto,
                                                List<Integer> filterStates, int position) {
        if (position >= routines.size()) {
            return null;
        }

        CiRoutineItem item = routines.get(position);
        IntegrationQueryDto dto = new IntegrationQueryDto();
        dto.setName("a" + position);
        dto.setCiTypeId(item.getCiTypeId());

        Relationship parentRs = new Relationship();
        parentRs.setAttrId(item.getParentRs().getAttrId());
        parentRs.setIsReferedFromParent(item.getParentRs().getIsReferedFromParent() == 1);
        dto.setParentRs(parentRs);

        IntegrationQueryDto childDto = travelRoutine(routines, rootDto, filterStates, ++position);
        if (childDto == null) {
            List<CiTypeAttrDto> attrs = cmdbServiceV2Stub.getCiTypeAttributesByCiTypeId(item.getCiTypeId());
            CiTypeAttrDto guidAttr = findCiAttrFromCiAttrListByName("guid", attrs);
            CiTypeAttrDto stateAttr = findCiAttrFromCiAttrListByName("state", attrs);
            CiTypeAttrDto bizKeyAttr = findCiAttrFromCiAttrListByName("biz_key", attrs);

            dto.setAttrs(Arrays.asList(guidAttr.getCiTypeAttrId(), stateAttr.getCiTypeAttrId(),
                    bizKeyAttr.getCiTypeAttrId()));

            List<String> attrKeyNames = new ArrayList<String>();
            attrKeyNames.add("tail$guid");
            attrKeyNames.add("tail$state");
            attrKeyNames.add("tail$biz_key");

            dto.setAttrKeyNames(attrKeyNames);

            if (filterStates != null && (!filterStates.isEmpty())) {
                rootDto.getQueryRequest().addInFilter("tail$state", filterStates);
            }
        } else {
            dto.setChildren(Arrays.asList(childDto));
        }

        return dto;
    }

    protected Integer getGuidAttrIdByCiTypeId(int ciTypeId) {
        List<CiTypeAttrDto> attrDtos = cmdbServiceV2Stub.getCiTypeAttributesByCiTypeId(ciTypeId);
        for (CiTypeAttrDto dto : attrDtos) {
            if ("guid".equalsIgnoreCase(dto.getPropertyName())) {
                return dto.getCiTypeAttrId();
            }
        }

        return null;
    }

    protected CiTypeAttrDto findCiAttrFromCiAttrListByName(String attrName, List<CiTypeAttrDto> attrs) {
        CiTypeAttrDto attr = null;
        for (CiTypeAttrDto dto : attrs) {
            if (attrName.equals(dto.getPropertyName())) {
                attr = dto;
                break;
            }
        }

        return attr;
    }

    protected String getCodeByEnumCodeId(int enumCodeId) {
        CatCodeDto enumCode = cmdbServiceV2Stub.getEnumCodeById(enumCodeId);

        if (enumCode == null) {
            log.error("cannot find such enum,enumCodeId={}", enumCodeId);
            throw new WecubeCoreException("cannot find such enum");
        }

        String code = enumCode.getCode();

        return code;
    }

    protected CategoryDto queryCategory(String catName, int ciTypeId) {

        CatTypeDto catTypeDto = cmdbServiceV2Stub.getEnumCategoryTypeByCiTypeId(ciTypeId);

        PaginationQuery queryObject = new PaginationQuery();
        queryObject.addEqualsFilter("catName", catName);
        queryObject.setFilterRs("and");
        queryObject.addEqualsFilter("catTypeId", catTypeDto.getCatTypeId());

        PaginationQueryResult<CategoryDto> categoryDtos = cmdbServiceV2Stub.queryEnumCategories(queryObject);

        List<CategoryDto> catDtos = categoryDtos.getContents();

        if (catDtos == null || catDtos.isEmpty()) {
            return null;
        }

        return catDtos.get(0);
    }

    protected CatCodeDto queryEnumCodes(String enumCode, int catId) {
        PaginationQuery queryObject = new PaginationQuery();
        queryObject.addEqualsFilter("code", enumCode);
        queryObject.setFilterRs("and");
        queryObject.addEqualsFilter("catId", catId);

        PaginationQueryResult<CatCodeDto> result = cmdbServiceV2Stub.queryEnumCodes(queryObject);

        List<CatCodeDto> codeDtos = result.getContents();

        if (codeDtos == null || codeDtos.isEmpty()) {
            return null;
        }

        return codeDtos.get(0);
    }

    protected CatCodeDto createEnumCodes(int catId, String enumCode, String value) {

        CatCodeDto dto = new CatCodeDto();
        dto.setCatId(catId);
        dto.setCode(enumCode);
        dto.setValue(value);

        List<CatCodeDto> dtos = cmdbServiceV2Stub.createEnumCodes(dto);

        if (dtos == null || dtos.isEmpty()) {
            throw new WecubeCoreException("failed to create enum code");
        }

        return dtos.get(0);
    }

    protected FlowNodeVO buildFlowNodeVO(FlowNode fn) {
        FlowNodeVO vo = new FlowNodeVO();
        vo.setId(fn.getId());
//        vo.setName(fn.getName() != null ? fn.getName() : fn.getId());
        vo.setName(fn.getName());
        vo.setNodeTypeName(fn.getElementType().getTypeName());

        if (NODE_TYPE_NAME_END_EVENT.equalsIgnoreCase(fn.getElementType().getTypeName()) && (fn instanceof EndEvent)) {
            EndEvent endEvent = (EndEvent) fn;
            Collection<EventDefinition> eventDefs = endEvent.getEventDefinitions();
            if (eventDefs != null) {
                for (EventDefinition ed : eventDefs) {
                    if (NODE_TYPE_NAME_ERROR_EVENT_DEFINITION.equalsIgnoreCase(ed.getElementType().getTypeName())) {
                        vo.setNodeTypeName(NODE_TYPE_NAME_ERROR_END_EVENT);
                        break;
                    }
                }
            }
        }

        return vo;
    }

    public static class SimpleCiDataInfo {
        private int ciDataTypeId;
        private String guid;
        private String name;

        public SimpleCiDataInfo(int ciDataTypeId, String guid) {
            super();
            this.ciDataTypeId = ciDataTypeId;
            this.guid = guid;
        }

        public int getCiDataTypeId() {
            return ciDataTypeId;
        }

        public void setCiDataTypeId(int ciDataTypeId) {
            this.ciDataTypeId = ciDataTypeId;
        }

        public String getGuid() {
            return guid;
        }

        public void setGuid(String guid) {
            this.guid = guid;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }
}
