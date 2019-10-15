package com.webank.wecube.core.service;

import static com.webank.wecube.core.support.cmdb.dto.v2.PaginationQuery.defaultQueryObject;
import static com.webank.wecube.core.utils.CollectionUtils.groupUp;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.webank.wecube.core.commons.ApplicationProperties.CmdbDataProperties;
import com.webank.wecube.core.commons.WecubeCoreException;
import com.webank.wecube.core.domain.workflow.CiRoutineItem;
import com.webank.wecube.core.dto.ci.ResourceTreeDto;
import com.webank.wecube.core.support.cmdb.CmdbDataNotFoundException;
import com.webank.wecube.core.support.cmdb.CmdbServiceV2Stub;
import com.webank.wecube.core.support.cmdb.dto.v2.AdhocIntegrationQueryDto;
import com.webank.wecube.core.support.cmdb.dto.v2.CatCodeDto;
import com.webank.wecube.core.support.cmdb.dto.v2.CatTypeDto;
import com.webank.wecube.core.support.cmdb.dto.v2.CategoryDto;
import com.webank.wecube.core.support.cmdb.dto.v2.CiTypeAttrDto;
import com.webank.wecube.core.support.cmdb.dto.v2.CiTypeDto;
import com.webank.wecube.core.support.cmdb.dto.v2.IntegrationQueryDto;
import com.webank.wecube.core.support.cmdb.dto.v2.OperateCiDto;
import com.webank.wecube.core.support.cmdb.dto.v2.PaginationQuery;
import com.webank.wecube.core.support.cmdb.dto.v2.PaginationQuery.Sorting;
import com.webank.wecube.core.support.cmdb.dto.v2.PaginationQueryResult;
import com.webank.wecube.core.support.cmdb.dto.v2.Relationship;
import com.webank.wecube.core.support.cmdb.dto.v2.ZoneLinkDto;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
public class CmdbResourceService {

    private static final String CONSTANT_CAT_TYPE_ID = "catTypeId";
    private static final String CONSTANT_CI_TYPE = "ciType";
    private static final String CONSTANT_CAT_CAT_TYPE = "cat.catType";
    private static final String CONSTANT_SELECT = "select";
    private static final String CONSTANT_GUID_PATH = "root$guid";
    private static final String CONSTANT_INPUT_TYPE = "inputType";
    private static final String CONSTANT_CI_TYPE_ID = "ciTypeId";

    @Autowired
    CmdbDataProperties cmdbDataProperties;

    @Autowired
    CmdbServiceV2Stub cmdbServiceV2Stub;

    public void swapCiTypeLayerPosition(int layerId, int targetLayerId) {
        CatCodeDto enumCode = cmdbServiceV2Stub.getEnumCodeById(layerId);
        CatCodeDto targetEnumCode = cmdbServiceV2Stub.getEnumCodeById(targetLayerId);

        Integer seqNo = enumCode.getSeqNo();
        enumCode.setSeqNo(targetEnumCode.getSeqNo());
        targetEnumCode.setSeqNo(seqNo);

        cmdbServiceV2Stub.updateEnumCodes(enumCode, targetEnumCode);
    }

    public void swapCiTypeAttributePosition(int attributeId, int targetAttributeId) {
        CiTypeAttrDto attribute = cmdbServiceV2Stub.getCiTypeAttribute(attributeId);
        CiTypeAttrDto targetAttribute = cmdbServiceV2Stub.getCiTypeAttribute(targetAttributeId);

        CiTypeAttrDto updateSourceAttribute = new CiTypeAttrDto();
        updateSourceAttribute.setCiTypeAttrId(attribute.getCiTypeAttrId());
        updateSourceAttribute.setDisplaySeqNo(targetAttribute.getDisplaySeqNo());

        CiTypeAttrDto updateTargetAttribute = new CiTypeAttrDto();
        updateTargetAttribute.setCiTypeAttrId(targetAttribute.getCiTypeAttrId());
        updateTargetAttribute.setDisplaySeqNo(attribute.getDisplaySeqNo());

        cmdbServiceV2Stub.updateCiTypeAttributes(updateSourceAttribute, updateTargetAttribute);
    }

    public LinkedHashMap<String, List> getEnumCategoryByMultipleTypes(String types, Integer ciTypeId) {
        LinkedHashMap<String, List> enumData = new LinkedHashMap<>();
        if (types.contains("system")) {
            enumData.put("system", getAllSystemEnumCategories().getContents());
        }
        if (types.contains("common")) {
            enumData.put("common", getAllCommonEnumCategories().getContents());
        }
        if (types.contains("private")) {
            try {
                enumData.put("private", getPrivateEnumByCiTypeId(ciTypeId).getContents());
            } catch (CmdbDataNotFoundException e) {
                log.warn(e.getErrorMessage());
            }
        }
        return enumData;
    }

    public PaginationQueryResult<CategoryDto> getAllSystemEnumCategories() {
        PaginationQuery queryObject = new PaginationQuery();
        queryObject.addEqualsFilter(CONSTANT_CAT_TYPE_ID, cmdbDataProperties.getEnumCategoryTypeSystem());
        return cmdbServiceV2Stub.queryEnumCategories(queryObject);
    }

    public PaginationQueryResult<CategoryDto> getAllCommonEnumCategories() {
        PaginationQuery queryObject = new PaginationQuery();
        queryObject.addEqualsFilter(CONSTANT_CAT_TYPE_ID, cmdbDataProperties.getEnumCategoryTypeCommon());
        return cmdbServiceV2Stub.queryEnumCategories(queryObject);
    }

    public PaginationQueryResult<CategoryDto> getPrivateEnumByCiTypeId(Integer ciTypeId) {
        if (ciTypeId == null) {
            throw new WecubeCoreException("'ciTypeId' is required");
        }
        CatTypeDto catType = cmdbServiceV2Stub.getEnumCategoryTypeByCiTypeId(ciTypeId);
        if (catType == null) {
            throw new WecubeCoreException(String.format("Can not found CategoryType by CiTypeId(%d)", ciTypeId));
        }
        if (catType.getCatTypeId() == null) {
            throw new WecubeCoreException(String.format("Can not found CategoryTypeId by CiTypeId(%d)", ciTypeId));
        }
        return cmdbServiceV2Stub.getEnumCategoriesByTypeId(catType.getCatTypeId());
    }

    public List<CatCodeDto> getCiTypesGroupByLayers(boolean withAttributes, String status) {
        return getCiTypesInGroups(cmdbDataProperties.getEnumCategoryCiTypeLayer(), withAttributes, status, CiTypeDto::getLayerId);
    }

    public List<CatCodeDto> getCiTypesGroupByCatalogs(boolean withAttributes, String status) {
        return getCiTypesInGroups(cmdbDataProperties.getEnumCategoryCiTypeCatalog(), withAttributes, status, CiTypeDto::getCatalogId);
    }

    public List<CatCodeDto> getAllLayers() {
        return getEnumCodesByCategoryName(cmdbDataProperties.getEnumCategoryCiTypeLayer());
    }

    public List<CiTypeAttrDto> getCiTypeReferenceBy(Integer ciTypeId) {
        PaginationQuery queryObject = new PaginationQuery().addEqualsFilter("referenceId", ciTypeId).addInFilter(CONSTANT_INPUT_TYPE, Arrays.asList("ref", "multiRef")).addReferenceResource(CONSTANT_CI_TYPE);
        queryObject.addReferenceResource(CONSTANT_CI_TYPE);
        return cmdbServiceV2Stub.queryCiTypeAttributes(queryObject);
    }

    public List<CiTypeAttrDto> getCiTypeReferenceTo(Integer ciTypeId) {
        PaginationQuery queryObject = new PaginationQuery().addEqualsFilter(CONSTANT_CI_TYPE_ID, ciTypeId).addInFilter(CONSTANT_INPUT_TYPE, Arrays.asList("ref", "multiRef"));
        queryObject.addReferenceResource(CONSTANT_CI_TYPE);
        return cmdbServiceV2Stub.queryCiTypeAttributes(queryObject);
    }

    public List<String> getAvailableCiTypeZoomLevels() {
        CategoryDto cat = cmdbServiceV2Stub.getEnumCategoryByName(cmdbDataProperties.getEnumCategoryCiTypeZoomLevels());
        List<CatCodeDto> catCodes = cmdbServiceV2Stub.getEnumCodesByCategoryId(cat.getCatId());
        List<String> zoomLevels = new ArrayList<>();
        for (CatCodeDto catCode : catCodes) {
            zoomLevels.add(catCode.getCode());
        }
        return zoomLevels;
    }

    public List<CatCodeDto> createLayer(CatCodeDto catCode) {
        catCode.setCatId(getLayerCategoryId());
        catCode.setSeqNo(getMaxLayerSeqNumber() + 1);
        return cmdbServiceV2Stub.createEnumCodes(catCode);
    }

    private int getMaxLayerSeqNumber() {
        List<CatCodeDto> catCodesResult = getEnumCodesByCategoryName(cmdbDataProperties.getEnumCategoryCiTypeLayer());
        Integer maxSeq = 0;
        for (CatCodeDto code : catCodesResult) {
            if (code.getSeqNo() > maxSeq) {
                maxSeq = code.getSeqNo();
            }
        }
        return maxSeq;
    }

    public List<CatCodeDto> createEnumCodes(CatCodeDto catCode) {

        if (catCode == null || catCode.getCatId().equals(0)) {
            throw new WecubeCoreException("Category Id is required");
        }
        if (catCode.getCatId().equals(getLayerCategoryId())) {
            catCode.setSeqNo(getMaxLayerSeqNumber() + 1);
        }
        return cmdbServiceV2Stub.createEnumCodes(catCode);
    }


    private List<CatCodeDto> getCiTypesInGroups(String categoryName, boolean withAttributes, String status, Function<CiTypeDto, Object> parentMapperOfElement) {
        List<CatCodeDto> ciTypeGroups = getEnumCodesByCategoryName(categoryName);
        List<CiTypeDto> ciTypes = cmdbServiceV2Stub.getAllCiTypes(withAttributes, status);

        return groupUp(ciTypeGroups, ciTypes, CatCodeDto::getCodeId, CatCodeDto::getCiTypes, parentMapperOfElement);
    }

    private List<CatCodeDto> getEnumCodesByCategoryName(String categoryName) {
        CategoryDto categories = cmdbServiceV2Stub.getEnumCategoryByName(categoryName);
        if (categories == null) throw new WecubeCoreException("Category not found.");
        return cmdbServiceV2Stub.getEnumCodesByCategoryId(categories.getCatId());
    }

    private Integer getLayerCategoryId() {
        return cmdbServiceV2Stub.getEnumCategoryByName(cmdbDataProperties.getEnumCategoryCiTypeLayer()).getCatId();
    }

    public PaginationQueryResult<CatCodeDto> querySystemEnumCodesWithRefResources(PaginationQuery queryObject) {
        queryObject.addEqualsFilter(CONSTANT_CAT_CAT_TYPE, cmdbDataProperties.getEnumCategoryTypeSystem());
        queryObject.addReferenceResource("cat");
        queryObject.addReferenceResource(CONSTANT_CAT_CAT_TYPE);
        return cmdbServiceV2Stub.queryEnumCodes(queryObject);
    }

    public PaginationQueryResult<CatCodeDto> queryNonSystemEnumCodesWithRefResources(PaginationQuery queryObject) {
        queryObject.addNotEqualsFilter("cat.catTypeId", cmdbDataProperties.getEnumCategoryTypeSystem());
        queryObject.addReferenceResource("cat");
        queryObject.addReferenceResource(CONSTANT_CAT_CAT_TYPE);
        return cmdbServiceV2Stub.queryEnumCodes(queryObject);
    }

    public PaginationQueryResult<CategoryDto> getAllNonSystemEnumCategories() {
        PaginationQuery queryObject = defaultQueryObject();
        queryObject.addNotEqualsFilter(CONSTANT_CAT_TYPE_ID, cmdbDataProperties.getEnumCategoryTypeSystem());
        return cmdbServiceV2Stub.queryEnumCategories(queryObject);
    }

    public List<CatCodeDto> getGroupListByCatId(int categoryId) {
        return cmdbServiceV2Stub.getEnumCodesByCategoryId(cmdbServiceV2Stub.getEnumCategoryByCatId(categoryId).getGroupTypeId());
    }

    public List<CatCodeDto> getCiTypeStatusOptions(int ciTypeId) {
        List<CiTypeAttrDto> statusCiTypeAttributes = cmdbServiceV2Stub.queryCiTypeAttributes(
                defaultQueryObject(CONSTANT_CI_TYPE_ID, ciTypeId)
                        .addEqualsFilter("propertyName", cmdbDataProperties.getStatusAttributeName()));
        if (isNotEmpty(statusCiTypeAttributes)) {
            return cmdbServiceV2Stub.getEnumCodesByCategoryId(statusCiTypeAttributes.get(0).getReferenceId());
        }
        return Lists.newArrayList();
    }

    public PaginationQueryResult<Object> getCiDataByCiTypeId(Integer ciTypeId) {
        return cmdbServiceV2Stub.queryCiData(ciTypeId, defaultQueryObject());
    }

    public List<ZoneLinkDto> getAllZoneLinkDesignGroupByIdcDesign() {
        List<ZoneLinkDto> results = new ArrayList<>();

        List<Object> idcDesignData = cmdbServiceV2Stub.queryCiData(getDefaultCiTypeIdOfIdcDesign(), defaultQueryObject()).getContents();
        for (Object idcDesign : idcDesignData) {
            String idcDesignGuid = ((Map) ((Map) idcDesign).get("data")).get("guid").toString();

            ZoneLinkDto result = new ZoneLinkDto();
            result.setIdcGuid(idcDesignGuid);

            List<Object> zoneDesignData = cmdbServiceV2Stub.queryCiData(cmdbDataProperties.getCiTypeIdOfZoneDesign(), defaultQueryObject().addEqualsFilter("idc_design", idcDesignGuid)).getContents();
            List<String> zoneDesignList = new ArrayList<>();
            for (Object zoneDesign : zoneDesignData) {
                zoneDesignList.add(((Map) ((Map) zoneDesign).get("data")).get("guid").toString());
            }

            if (zoneDesignList.size() != 0) {
                List<Object> zoneDesignLinkData = cmdbServiceV2Stub.queryCiData(cmdbDataProperties.getCiTypeIdOfZoneLinkDesign(),
                        defaultQueryObject()
                                .addInFilter("zone_design1", zoneDesignList)
                                .addInFilter("zone_design2", zoneDesignList)
                                .setFiltersRelationship("or")
                ).getContents();
                result.setLinkList(zoneDesignLinkData);
            }
            results.add(result);
        }
        return results;
    }

    public List<ZoneLinkDto> getAllZoneLinkGroupByIdc() {
        List<ZoneLinkDto> results = new ArrayList<>();

        List<Object> idcData = cmdbServiceV2Stub.queryCiData(getDefaultCiTypeIdOfIdc(), defaultQueryObject()).getContents();
        for (Object idc : idcData) {
            String idcGuid = ((Map) ((Map) idc).get("data")).get("guid").toString();

            ZoneLinkDto result = new ZoneLinkDto();
            result.setIdcGuid(idcGuid);

            List<Object> zoneData = cmdbServiceV2Stub.queryCiData(cmdbDataProperties.getCiTypeIdOfZone(), defaultQueryObject().addEqualsFilter("idc", idcGuid)).getContents();
            List<String> zoneList = new ArrayList<>();
            for (Object zone : zoneData) {
                zoneList.add(((Map) ((Map) zone).get("data")).get("guid").toString());
            }
            if (zoneList.size() != 0) {
                List<Object> zoneLinkData = cmdbServiceV2Stub.queryCiData(cmdbDataProperties.getCiTypeIdOfZoneLink(),
                        defaultQueryObject()
                                .addInFilter("zone1", zoneList)
                                .addInFilter("zone2", zoneList)
                                .setFiltersRelationship("or")
                ).getContents();
                result.setLinkList(zoneLinkData);
            }
            results.add(result);
        }
        return results;
    }

    public List<ResourceTreeDto> getAllIdcImplementTrees() {
        Integer rootCiTypeId = getDefaultCiTypeIdOfIdc();
        List<ResourceTreeDto> resourceTrees = new ArrayList<>();

        recursiveGetChildrenData(rootCiTypeId, getSameCiTypesByCiTypeId(rootCiTypeId), resourceTrees, null);

        return resourceTrees;
    }


    public List<ResourceTreeDto> getAllIdcDesignTrees() {
        Integer rootCiTypeId = getDefaultCiTypeIdOfIdcDesign();
        List<ResourceTreeDto> resourceTrees = new ArrayList<>();

        recursiveGetChildrenData(rootCiTypeId, getSameCiTypesByCiTypeId(rootCiTypeId), resourceTrees, null);

        return resourceTrees;
    }

    private PaginationQuery buildQueryObjectWithEqualsFilter(Map<String, Object> filters) {
        PaginationQuery queryObject = defaultQueryObject();
        if (filters != null) {
            for (Map.Entry<String, Object> entry : filters.entrySet()) {
                queryObject.addEqualsFilter(entry.getKey(), entry.getValue());
            }
        }
        return queryObject;
    }

    public void recursiveGetChildrenData(Integer ciTypeId, List<Integer> limitedCiTypes, List<ResourceTreeDto> resourceTrees, Map<String, Object> inputFilters) {
        List<Object> ciDatas = cmdbServiceV2Stub.queryCiData(ciTypeId, buildQueryObjectWithEqualsFilter(inputFilters)).getContents();
        for (int i = 0; i < ciDatas.size(); i++) {
            Object ciData = ciDatas.get(i);
            Map ciDataMap = (Map) ((Map) ciData).get("data");
            resourceTrees.add(buildNewResourceTreeDto(ciData, ciTypeId));

            List<CiTypeAttrDto> childrenCiTypeRelativeAttributes = findChildrenCiTypeRelativeAttributes(ciTypeId, cmdbDataProperties.getReferenceNameOfBelong());

            if (childrenCiTypeRelativeAttributes.size() != 0) {
                recursiveGetChildrenDataByRelativeAttributes(childrenCiTypeRelativeAttributes, limitedCiTypes, ciDataMap.get("guid").toString(), resourceTrees.get(i).getChildren());
                continue;
            }

            List<CiTypeAttrDto> runningCiTypeRelativeAttributes = findChildrenCiTypeRelativeAttributes(ciTypeId, cmdbDataProperties.getReferenceNameOfRunning());
            if (runningCiTypeRelativeAttributes.size() != 0) {
                recursiveGetChildrenDataByRelativeAttributes(runningCiTypeRelativeAttributes, limitedCiTypes, ciDataMap.get("guid").toString(), resourceTrees.get(i).getChildren());
            }
        }
    }

    private void recursiveGetChildrenDataByRelativeAttributes(List<CiTypeAttrDto> childrenCiTypeRelativeAttributes, List<Integer> limitedCiTypes, String guid, List<ResourceTreeDto> children) {
        for (int j = 0; j < childrenCiTypeRelativeAttributes.size(); j++) {
            Map<String, Object> filter = new HashMap<>();
            if (!limitedCiTypes.contains(childrenCiTypeRelativeAttributes.get(j).getCiTypeId())) {
                continue;
            }
            filter.put(childrenCiTypeRelativeAttributes.get(j).getPropertyName(), guid);
            recursiveGetChildrenData(childrenCiTypeRelativeAttributes.get(j).getCiTypeId(), limitedCiTypes, children, filter);
        }
    }

    private List<CiTypeAttrDto> findRunningCiAttributesByCiTypeId(Integer ciTypeId) {
        List<CiTypeAttrDto> ChildrenCiTypeRelativeAttributes = new ArrayList<>();
        List<CiTypeAttrDto> referenceByList = getCiTypeReferenceBy(ciTypeId);
        for (CiTypeAttrDto attrDto : referenceByList) {
            if (attrDto.getReferenceName().equals(cmdbDataProperties.getReferenceNameOfRunning())) {
                ChildrenCiTypeRelativeAttributes.add(attrDto);
            }
        }
        return ChildrenCiTypeRelativeAttributes;
    }

    private List<CiTypeAttrDto> findRealizeCiAttributesByCiTypeId(Integer ciTypeId) {
        List<CiTypeAttrDto> ChildrenCiTypeRelativeAttributes = new ArrayList<>();
        List<CiTypeAttrDto> referenceByList = getCiTypeReferenceBy(ciTypeId);
        for (CiTypeAttrDto attrDto : referenceByList) {
            if (attrDto.getReferenceName().equals(cmdbDataProperties.getReferenceNameOfRealize())) {
                ChildrenCiTypeRelativeAttributes.add(attrDto);
            }
        }
        return ChildrenCiTypeRelativeAttributes;
    }

    private ResourceTreeDto buildNewResourceTreeDto(Object ciData, Integer ciTypeId) {
        ResourceTreeDto resourceTree = new ResourceTreeDto();
        Map map = (Map) ((Map) ciData).get("data");
        resourceTree.setGuid(map.get("guid").toString());
        resourceTree.setCiTypeId(ciTypeId);
        List<CiTypeAttrDto> ciTypeAttributes = cmdbServiceV2Stub.getCiTypeAttributesByCiTypeId(ciTypeId);
        resourceTree.setAttrs(ciTypeAttributes);
        resourceTree.setData(((Map) ciData).get("data"));

        return resourceTree;
    }

    private ResourceTreeDto buildNewResourceTreeDto(Object ciData, Integer ciTypeId, String flag) {
        ResourceTreeDto resourceTree = buildNewResourceTreeDto(ciData, ciTypeId);
        resourceTree.setFlag(flag);

        return resourceTree;
    }

    private int getDefaultCiTypeIdOfIdcDesign() {
        return cmdbDataProperties.getCiTypeIdOfIdcDesign();
    }

    private int getDefaultCiTypeIdOfIdc() {
        return cmdbDataProperties.getCiTypeIdOfIdc();
    }

    private List<CiTypeAttrDto> findChildrenCiTypeRelativeAttributes(Integer ciTypeId, String referenceName) {
        List<CiTypeAttrDto> ChildrenCiTypeRelativeAttributes = new ArrayList<>();
        List<CiTypeAttrDto> referenceByList = getCiTypeReferenceBy(ciTypeId);
        for (CiTypeAttrDto attrDto : referenceByList) {
            if (attrDto.getReferenceName() != null && attrDto.getReferenceName().equals(referenceName)) {
                ChildrenCiTypeRelativeAttributes.add(attrDto);
            }
        }
        return ChildrenCiTypeRelativeAttributes;
    }


    private Integer getAttrIdByCiTypeId(int ciTypeId, String PropertyName) {
        List<CiTypeAttrDto> ciTypeAttributes = cmdbServiceV2Stub.getCiTypeAttributesByCiTypeId(ciTypeId);
        for (int j = 0; j < ciTypeAttributes.size(); j++) {
            if (PropertyName.equalsIgnoreCase(ciTypeAttributes.get(j).getPropertyName())) {
                return ciTypeAttributes.get(j).getCiTypeAttrId();
            }
        }

        return null;
    }

    private int getEnumCodeIdByCode(int enumCat, String enumCode) {
        List<CatCodeDto> catCodeList = cmdbServiceV2Stub.getEnumCodesByCategoryId(enumCat);
        for (CatCodeDto catCodeDto : catCodeList) {
            if (enumCode.equalsIgnoreCase(catCodeDto.getCode())) {
                return catCodeDto.getCodeId();
            }
        }
        return -1;
    }

    private String getEnumPropertyNameByCiTypeId(int ciTypeId, int enumCat) {
        List<CiTypeAttrDto> ciTypeAttributes = cmdbServiceV2Stub.getCiTypeAttributesByCiTypeId(ciTypeId);
        for (int j = 0; j < ciTypeAttributes.size(); j++) {
            if (ciTypeAttributes.get(j).getInputType().equals(CONSTANT_SELECT) && ciTypeAttributes.get(j).getReferenceId() == enumCat) {
                return ciTypeAttributes.get(j).getPropertyName();
            }
        }

        return null;
    }

    private IntegrationQueryDto travelRoutine(List<CiRoutineItem> routines, int filterCiTypeId, AdhocIntegrationQueryDto rootDto, int position) {
        if (position >= routines.size()) {
            return null;
        }

        CiRoutineItem item = routines.get(position);
        IntegrationQueryDto dto = new IntegrationQueryDto();
        dto.setName("index-" + position);
        dto.setCiTypeId(item.getCiTypeId());

        Relationship parentRs = new Relationship();
        parentRs.setAttrId(item.getParentRs().getAttrId());
        parentRs.setIsReferedFromParent(item.getParentRs().getIsReferedFromParent() == 1);
        dto.setParentRs(parentRs);

        IntegrationQueryDto childDto = travelRoutine(routines, filterCiTypeId, rootDto, ++position);
        if (childDto == null) {
            if (filterCiTypeId != item.getCiTypeId()) {
                log.error("routine tail ciType not right!!!");
                return null;
            }

            dto.setAttrs(Arrays.asList(getAttrIdByCiTypeId(item.getCiTypeId(), "guid"), getAttrIdByCiTypeId(item.getCiTypeId(), "r_guid")));
            dto.setAttrKeyNames(Arrays.asList("tail$guid", "tail$r_guid"));
        } else {
            dto.setChildren(Arrays.asList(childDto));
        }

        return dto;
    }

    private List<Map<String, Object>> getAllCiDataOfRootCi(int rootCiTypeId, int envEnumCat, String envEnumCode, int filterCiTypeId, String filterCiGuid, String routine) {
        List<CiRoutineItem> routineItems = new ArrayList<>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            JavaType javaType = mapper.getTypeFactory().constructCollectionType(ArrayList.class, CiRoutineItem.class);
            routineItems = (List<CiRoutineItem>) mapper.readValue(routine.getBytes(), javaType);
        } catch (JsonParseException e) {
            log.error("getAllCiDataOfRootCi erorrs", e);
        } catch (JsonMappingException e) {
            log.error("getAllCiDataOfRootCierorrs", e);
        } catch (IOException e) {
            log.error("getAllCiDataOfRootCi erorrs", e);
        }

        AdhocIntegrationQueryDto rootDto = new AdhocIntegrationQueryDto();

        PaginationQuery queryRequest = new PaginationQuery();
        List<PaginationQuery.Filter> filters = new ArrayList<PaginationQuery.Filter>();
        String enumPorpertyNameOfEnv = getEnumPropertyNameByCiTypeId(rootCiTypeId, envEnumCat);
        if (envEnumCode != null && enumPorpertyNameOfEnv != null) {
            PaginationQuery.Filter rootCifilter = new PaginationQuery.Filter("root$" + enumPorpertyNameOfEnv, "eq", getEnumCodeIdByCode(envEnumCat, envEnumCode));
            filters.add(rootCifilter);
        }

        PaginationQuery.Filter targetRifilter = new PaginationQuery.Filter("tail$r_guid", "eq", filterCiGuid);
        filters.add(targetRifilter);
        queryRequest.setFilters(filters);

        IntegrationQueryDto rootNode = new IntegrationQueryDto();
        rootNode.setName("root");
        rootNode.setCiTypeId(rootCiTypeId);

        List<Integer> attrs = new ArrayList<Integer>();
        List<String> attrKeyNames = new ArrayList<String>();

        attrs.add(getAttrIdByCiTypeId(rootCiTypeId, "guid"));
        attrKeyNames.add(CONSTANT_GUID_PATH);

        if (envEnumCode != null && enumPorpertyNameOfEnv != null) {
            attrs.add(getAttrIdByCiTypeId(rootCiTypeId, enumPorpertyNameOfEnv));
            attrKeyNames.add("root$" + enumPorpertyNameOfEnv);
        }

        rootNode.setAttrs(attrs);
        rootNode.setAttrKeyNames(attrKeyNames);

        rootDto.setCriteria(rootNode);
        rootDto.setQueryRequest(queryRequest);

        IntegrationQueryDto childQueryDto = travelRoutine(routineItems, filterCiTypeId, rootDto, 1);
        if (childQueryDto != null) {
            rootDto.getCriteria().setChildren(Arrays.asList(childQueryDto));
        }

        PaginationQueryResult<Map<String, Object>> results = cmdbServiceV2Stub.adhocIntegrationQuery(rootDto);

        List<Map<String, Object>> ciDatas = results.getContents();

        return ciDatas;
    }

    public List<ResourceTreeDto> getAllDeployTreesFromSubSys(String envCode, String SystemDesignGuid) {
        List<ResourceTreeDto> deployTrees = new ArrayList<>();
        int systemDesignCiTypeId = cmdbDataProperties.getCiTypeIdOfSystemDesign();
        int subsysCiTypeId = cmdbDataProperties.getCiTypeIdOfSubsys();
        int envEnumCat = cmdbServiceV2Stub.getEnumCategoryByName(cmdbDataProperties.getEnumCategoryNameOfEnv()).getCatId();

        String stateEnumCode = cmdbDataProperties.getEnumCodeOfStateDelete();

        String routine = null;
        List<CatCodeDto> codeOfRoutines = cmdbServiceV2Stub.getEnumCodeByCodeAndCategoryName(
                cmdbDataProperties.getCodeOfDeployDetail(), cmdbDataProperties.getCatNameOfQueryDeployDesign());
        if (codeOfRoutines.size() > 0) {
            routine = codeOfRoutines.get(0).getValue();
        }

        if (routine == null) {
            return null;
        }

        List<Map<String, Object>> ciDatas = getAllCiDataOfRootCi(subsysCiTypeId, envEnumCat, envCode, systemDesignCiTypeId, SystemDesignGuid, routine);

        List<CiTypeAttrDto> attrOfSubsys = cmdbServiceV2Stub.getCiTypeAttributesByCiTypeIdAndPropertyName(subsysCiTypeId, cmdbDataProperties.getPropertyNameOfState());
        if (attrOfSubsys.size() == 0) {
            return null;
        }

        int stateEnumCatOfSubsys = attrOfSubsys.get(0).getReferenceId();

        for (int i = 0; i < ciDatas.size(); i++) {
            Object ciData = ciDatas.get(i);
            Map ciDataMap = (Map) ciData;

            Map<String, Object> filter = new HashMap<>();
            filter.put("guid", ciDataMap.get(CONSTANT_GUID_PATH).toString());

            List<ResourceTreeDto> resourceTrees = new ArrayList<>();
            recursiveGetChildrenDataFilterState(subsysCiTypeId, stateEnumCatOfSubsys, stateEnumCode, resourceTrees, filter);
            deployTrees.addAll(resourceTrees);
        }

        return deployTrees;
    }

    public List<ResourceTreeDto> getAllDesignTreesFromSystemDesign(String systemDesignGuid) {
        List<ResourceTreeDto> designTrees = new ArrayList<>();
        int systemDesignCiTypeId = cmdbDataProperties.getCiTypeIdOfSystemDesign();
        List<CiTypeAttrDto> attr = cmdbServiceV2Stub.getCiTypeAttributesByCiTypeIdAndPropertyName(systemDesignCiTypeId, cmdbDataProperties.getPropertyNameOfState());
        if (attr.size() == 0) {
            return null;
        }

        int stateEnumCat = attr.get(0).getReferenceId();
        String stateEnumCode = cmdbDataProperties.getEnumCodeOfStateDelete();

        Map<String, Object> filter = new HashMap<>();
        filter.put("guid", systemDesignGuid);
        recursiveGetChildrenDataFilterState(systemDesignCiTypeId, stateEnumCat, stateEnumCode, designTrees, filter);

        return designTrees;
    }

    public void recursiveGetChildrenDataFilterState(Integer ciTypeId, int stateEnumCat, String stateEnumCode, List<ResourceTreeDto> resourceTrees, Map<String, Object> inputFilters) {
        List<Object> ciDatas = cmdbServiceV2Stub.queryCiData(ciTypeId, buildQueryObjectWithEqualsFilter(inputFilters)).getContents();
        for (int i = 0; i < ciDatas.size(); i++) {
            Object ciData = ciDatas.get(i);
            Map ciDataMap = (Map) ((Map) ciData).get("data");
            ResourceTreeDto resourceTreeDto = buildNewResourceTreeDto(ciData, ciTypeId);

            List<CiTypeAttrDto> ciTypeAttributes = (List<CiTypeAttrDto>) resourceTreeDto.getAttrs();

            if (checkCiTypeAttributes(ciTypeAttributes, stateEnumCat, stateEnumCode, ciDataMap)) {
                continue;
            }

            resourceTrees.add(resourceTreeDto);
            List<CiTypeAttrDto> childrenCiTypeRelativeAttributes = findChildrenCiTypeRelativeAttributes(ciTypeId, cmdbDataProperties.getReferenceNameOfBelong());
            recursiveGetChildrenDataByRelativeAttributes(childrenCiTypeRelativeAttributes, stateEnumCode, ciDataMap.get("guid").toString(), resourceTrees.get(i).getChildren());
        }
    }

    private boolean checkCiTypeAttributes(List<CiTypeAttrDto> ciTypeAttributes, int stateEnumCat, String stateEnumCode, Map ciDataMap) {
        boolean delconFlag = true;
        for (CiTypeAttrDto ciTypeAttribute : ciTypeAttributes) {
            if (ciTypeAttribute.getInputType().equals(CONSTANT_SELECT) && ciTypeAttribute.getReferenceId() == stateEnumCat) {
                delconFlag = false;
                try {
                    Map catCodeData = (Map) ciDataMap.get(ciTypeAttribute.getPropertyName());
                    String fixedDate = ciDataMap.get("fixed_date").toString();
                    if (stateEnumCode.equalsIgnoreCase(catCodeData.get("code").toString()) && fixedDate != null && fixedDate.length() > 0) {
                        delconFlag = true;
                        break;
                    }
                } catch (Exception e) {
                    log.error("check state & fixed_date error:" + e.getMessage());
                }
            }
        }
        return delconFlag;
    }

    private void recursiveGetChildrenDataByRelativeAttributes(List<CiTypeAttrDto> childrenCiTypeRelativeAttributes, String stateEnumCode, String guid, List<ResourceTreeDto> children) {
        if (childrenCiTypeRelativeAttributes.size() != 0) {
            for (CiTypeAttrDto childrenCiTypeRelativeAttribute : childrenCiTypeRelativeAttributes) {
                Map<String, Object> filter = new HashMap<>();
                filter.put(childrenCiTypeRelativeAttribute.getPropertyName(), guid);

                List<CiTypeAttrDto> attr = cmdbServiceV2Stub.getCiTypeAttributesByCiTypeIdAndPropertyName(childrenCiTypeRelativeAttribute.getCiTypeId(), cmdbDataProperties.getPropertyNameOfState());
                if (attr.size() == 0) {
                    continue;
                }

                int stateEnumCatOfChildren = attr.get(0).getReferenceId();
                recursiveGetChildrenDataFilterState(childrenCiTypeRelativeAttribute.getCiTypeId(), stateEnumCatOfChildren, stateEnumCode, children, filter);
            }
        }
    }

    public void saveAllDesignTreesFromSystemDesign(String systemDesignGuid) {
        List<ResourceTreeDto> designTrees = getAllDesignTreesFromSystemDesign(systemDesignGuid);

        List<OperateCiDto> operateCiDtos = Lists.newArrayList();
        recursiveUpdateChildrenFixedDate(designTrees, operateCiDtos);
        cmdbServiceV2Stub.operateCiForState(operateCiDtos, "confirm");
    }

    public void recursiveUpdateChildrenFixedDate(List<ResourceTreeDto> designTrees, List<OperateCiDto> operateCiDtos) {

        for (ResourceTreeDto treeDto : designTrees) {
            Object fixedDate = ((Map) treeDto.getData()).get(cmdbDataProperties.getPropertyNameOfFixedDate());
            if (fixedDate == null || ((String) fixedDate).length() == 0) {
                operateCiDtos.add(new OperateCiDto(treeDto.getGuid(), treeDto.getCiTypeId()));
            }
            if (treeDto.getChildren() != null) {
                recursiveUpdateChildrenFixedDate(treeDto.getChildren(), operateCiDtos);
            }
        }
    }

    private List<Integer> getSameCiTypesByCiTypeId(Integer ciTypeId) {
        CiTypeDto ciTypeData = cmdbServiceV2Stub.getCiType(ciTypeId);
        PaginationQuery queryObject = defaultQueryObject().addEqualsFilter("layerId", ciTypeData.getLayerId());
        List<CiTypeDto> ciTypesData = cmdbServiceV2Stub.queryCiTypes(queryObject).getContents();
        List<Integer> ciTypesList = new ArrayList<>();
        for (CiTypeDto ciType : ciTypesData) {
            ciTypesList.add(ciType.getCiTypeId());
        }
        return ciTypesList;
    }

    public Object getSystemDesigns() {
        return getCiDataByCiTypeId(cmdbDataProperties.getCiTypeIdOfSystemDesign());
    }

    public Object getArchitectureDesignTabs() {
        return getEnumCodesByCategoryName(cmdbDataProperties.getCatNameOfArchitectureDesign());
    }

    public Object getDeploySystemDesigns() {
        return getCiDataByCiTypeId(cmdbDataProperties.getCiTypeIdOfSystemDesign());
    }

    public Object getDeployDesignTabs() {
        return getEnumCodesByCategoryName(cmdbDataProperties.getCatNameOfDeployDesign());
    }

    public Object getArchitectureCiData(Integer codeId, String systemDesignGuid, PaginationQuery queryObject) {
        return getCiData(codeId, null, systemDesignGuid, queryObject);
    }

    public Object getDeployCiData(Integer codeId, String envCode, String systemDesignGuid, PaginationQuery queryObject) {
        return getCiData(codeId, envCode, systemDesignGuid, queryObject);
    }

    private Object getCiData(Integer codeId, String envCode, String systemDesignGuid, PaginationQuery queryObject) {
        CatCodeDto code = cmdbServiceV2Stub.getEnumCodeById(codeId);
        Integer ciTypeId = Integer.parseInt(code.getCode());
        Integer envEnumCat = cmdbServiceV2Stub.getEnumCategoryByName(cmdbDataProperties.getEnumCategoryNameOfEnv()).getCatId();
        int systemDesignCiTypeId = cmdbDataProperties.getCiTypeIdOfSystemDesign();
        List<CatCodeDto> codeOfRoutines = cmdbServiceV2Stub.getEnumCodesByGroupId(code.getCodeId());
        String routineForGetingSystemDesignGuid = null;
        if (codeOfRoutines.size() > 0) {
            routineForGetingSystemDesignGuid = codeOfRoutines.get(0).getValue();
        }

        if (routineForGetingSystemDesignGuid == null) {
            return null;
        }

        List<Map<String, Object>> ciDatas = getAllCiDataOfRootCi(ciTypeId, envEnumCat, envCode, systemDesignCiTypeId,
                systemDesignGuid, routineForGetingSystemDesignGuid);
        if (queryObject == null) {
            queryObject = PaginationQuery.defaultQueryObject();
        }

        List<Object> guids = ciDatas.stream().map(item -> item.get(CONSTANT_GUID_PATH)).collect(Collectors.toList());
        if (guids.size() == 0) {
            return null;
        }

        queryObject.addInFilter("guid", guids);
        return cmdbServiceV2Stub.queryCiData(ciTypeId, queryObject);
    }

    public Object getPlanningDesignTabs() {
        return getEnumCodesByCategoryName(cmdbDataProperties.getCatNameOfPlanningDesign());
    }

    public Object getResourcePlanningTabs() {
        return getEnumCodesByCategoryName(cmdbDataProperties.getCatNameOfResourcePlanning());
    }

    public List<ResourceTreeDto> getApplicationFrameworkDesignDataTreeBySystemDesignGuid(String systemDesignGuid) {
        List<ResourceTreeDto> unitDesignDatas = new ArrayList<>();
        Map<String, Object> inputFilter = new HashMap<>();
        inputFilter.put("guid", systemDesignGuid);
        getBottomChildrenDataByBottomCiTypeId(cmdbDataProperties.getCiTypeIdOfSystemDesign(),
                cmdbDataProperties.getCiTypeIdOfUnitDesign(),
                unitDesignDatas,
                getSameCiTypesByCiTypeId(cmdbDataProperties.getCiTypeIdOfSystemDesign()),
                inputFilter,
                new HashMap<>());

        List<ResourceTreeDto> allIdcDesignDatas = getAllIdcDesignTrees();

        List<CiTypeAttrDto> relateCiAttrDtoList = getRelateCiTypeAttrByCiTypeId(cmdbDataProperties.getCiTypeIdOfUnitDesign());
        if (relateCiAttrDtoList.size() == 0) {
            return unitDesignDatas;
        }

        List<ResourceTreeDto> unitDesignDatasAfterGroup = groupByAttr(unitDesignDatas, relateCiAttrDtoList.get(0));
        if (unitDesignDatasAfterGroup.size() == 0) {
            return unitDesignDatas;
        }

        mergeDataToDataTree(allIdcDesignDatas, unitDesignDatasAfterGroup);
        return allIdcDesignDatas;
    }

    private List<CiTypeAttrDto> getRelateCiTypeAttrByCiTypeId(Integer ciTypeId) {
        List<CiTypeAttrDto> relateCiAttrs = new ArrayList<>();
        List<CiTypeAttrDto> referenceByList = getCiTypeReferenceTo(ciTypeId);
        for (CiTypeAttrDto attrDto : referenceByList) {
            if (attrDto.getReferenceName().equals(cmdbDataProperties.getReferenceNameOfRelate())) {
                relateCiAttrs.add(attrDto);
            }
        }
        return relateCiAttrs;
    }


    private List<CiTypeAttrDto> getRefToAttrByCiTypeIdAndRefName(Integer ciTypeId, String refName) {
        List<CiTypeAttrDto> relateCiAttrs = new ArrayList<>();
        List<CiTypeAttrDto> referenceByList = getCiTypeReferenceTo(ciTypeId);
        for (CiTypeAttrDto attrDto : referenceByList) {
            if (attrDto.getReferenceName().equals(refName)) {
                relateCiAttrs.add(attrDto);
            }
        }
        return relateCiAttrs;
    }

    private List<ResourceTreeDto> groupByAttr(List<ResourceTreeDto> toBeGroupData, CiTypeAttrDto relateCiAttrDto) {
        List<ResourceTreeDto> returnData = new ArrayList<>();
        for (ResourceTreeDto singleData : toBeGroupData) {
            boolean continueFlag = false;
            Map ciMap = (Map) singleData.getData();
            Object relateCiTypeDto = ciMap.get(relateCiAttrDto.getPropertyName());
            if (relateCiTypeDto == null) {
                continue;
            }

            String relateCiAttrValue = ((Map) relateCiTypeDto).get("guid").toString();

            if (returnData.size() != 0) {
                for (ResourceTreeDto returnDatum : returnData) {
                    if (returnDatum.getGuid().equals(relateCiAttrValue)) {
                        returnDatum.getChildren().add(singleData);
                        continueFlag = true;
                    }
                }
            }
            if (continueFlag) {
                continue;
            }
            ResourceTreeDto data = new ResourceTreeDto();
            List<ResourceTreeDto> newChildrenData = Lists.newArrayList(singleData);
            data.setCiTypeId(relateCiAttrDto.getReferenceId());
            data.setGuid(relateCiAttrValue);
            data.setChildren(newChildrenData);
            returnData.add(data);
        }
        return returnData;
    }

    public void getBottomChildrenDataByBottomCiTypeId(Integer ciTypeId, Integer bottomCiTypeId, List<ResourceTreeDto> bottomChildrenData, List<Integer> limitedCiTypeIds, Map<String, Object> inputFilters, Map<String, Object> subsystemFilters) {
        List<Object> ciDatas = cmdbServiceV2Stub.queryCiData(ciTypeId, buildQueryObjectWithEqualsFilter(inputFilters)).getContents();
        if (ciTypeId.equals(bottomCiTypeId)) {
            for (Object ciData : ciDatas) {
                ResourceTreeDto ci = buildNewResourceTreeDto(ciData, ciTypeId);
                bottomChildrenData.add(ci);
            }
            return;
        }

        boolean findBelongCi = false;
        for (Object ciData : ciDatas) {
            Map ciDataMap = (Map) ((Map) ciData).get("data");
            List<CiTypeAttrDto> childrenCiTypeRelativeAttributes = findChildrenCiTypeRelativeAttributes(ciTypeId, cmdbDataProperties.getReferenceNameOfBelong());
            if (childrenCiTypeRelativeAttributes.size() != 0) {
                findBelongCi = getBottomChildrenDataByRelativeAttributes(childrenCiTypeRelativeAttributes, limitedCiTypeIds, ciDataMap.get("guid").toString(), bottomChildrenData, bottomCiTypeId, subsystemFilters);
            }

            if (!findBelongCi) {
                List<CiTypeAttrDto> realizeCiTypeRelativeAttributes = findRealizeCiAttributesByCiTypeId(ciTypeId);
                if (realizeCiTypeRelativeAttributes.size() != 0) {
                    getBottomChildrenDataByRealizeAttributes(realizeCiTypeRelativeAttributes, limitedCiTypeIds, ciDataMap.get("guid").toString(), bottomChildrenData, bottomCiTypeId, subsystemFilters);
                }
            }
        }
    }

    private boolean getBottomChildrenDataByRelativeAttributes(List<CiTypeAttrDto> childrenCiTypeRelativeAttributes, List<Integer> limitedCiTypeIds, String guid, List<ResourceTreeDto> bottomChildrenData, Integer bottomCiTypeId, Map<String, Object> subsystemFilters) {
        for (CiTypeAttrDto childrenCiTypeRelativeAttribute : childrenCiTypeRelativeAttributes) {
            Map<String, Object> filter = new HashMap<>();
            if (!limitedCiTypeIds.contains(childrenCiTypeRelativeAttribute.getCiTypeId())) {
                continue;
            }
            if (childrenCiTypeRelativeAttribute.getCiTypeId().equals(cmdbDataProperties.getCiTypeIdOfSubsys())) {
                filter = subsystemFilters;
            }
            filter.put(childrenCiTypeRelativeAttribute.getPropertyName(), guid);

            getBottomChildrenDataByBottomCiTypeId(childrenCiTypeRelativeAttribute.getCiTypeId(), bottomCiTypeId, bottomChildrenData, limitedCiTypeIds, filter, subsystemFilters);
            return true;
        }
        return false;
    }

    private void getBottomChildrenDataByRealizeAttributes(List<CiTypeAttrDto> realizeCiTypeRelativeAttributes, List<Integer> limitedCiTypeIds, String guid, List<ResourceTreeDto> bottomChildrenData, Integer bottomCiTypeId, Map<String, Object> subsystemFilters) {
        for (CiTypeAttrDto realizeCiTypeRelativeAttribute : realizeCiTypeRelativeAttributes) {
            Map<String, Object> filter = new HashMap<>();
            if (!limitedCiTypeIds.contains(realizeCiTypeRelativeAttribute.getCiTypeId())) {
                continue;
            }
            filter.put(realizeCiTypeRelativeAttribute.getPropertyName(), guid);
            getBottomChildrenDataByBottomCiTypeId(realizeCiTypeRelativeAttribute.getCiTypeId(), bottomCiTypeId, bottomChildrenData, limitedCiTypeIds, filter, subsystemFilters);
        }
    }

    public List<ResourceTreeDto> getApplicationDeploymentDesignDataTreeBySystemDesignGuidAndEnvCode(String systemDesignGuid, Integer envCodeId) {
        List<ResourceTreeDto> instanceData = new ArrayList<>();
        Map<String, Object> systemDesignfilter = new HashMap<>();
        Map<String, Object> subsystemfilter = new HashMap<>();
        List<Integer> limitedCiTypeIdsOfGetInstanceData = Lists.newArrayList(cmdbDataProperties.getCiTypeIdOfSystemDesign(),
                cmdbDataProperties.getCiTypeIdOfSubsystemDesign(),
                cmdbDataProperties.getCiTypeIdOfSubsys(),
                cmdbDataProperties.getCiTypeIdOfUnit(),
                cmdbDataProperties.getCiTypeIdOfInstance());

        systemDesignfilter.put("guid", systemDesignGuid);
        subsystemfilter.put("env", envCodeId);
        getBottomChildrenDataByBottomCiTypeId(cmdbDataProperties.getCiTypeIdOfSystemDesign(),
                cmdbDataProperties.getCiTypeIdOfInstance(),
                instanceData,
                limitedCiTypeIdsOfGetInstanceData,
                systemDesignfilter,
                subsystemfilter);

        List<CiTypeAttrDto> relateCiAttrDtoList = getRefToAttrByCiTypeIdAndRefName(cmdbDataProperties.getCiTypeIdOfInstance(), cmdbDataProperties.getReferenceNameOfRunning());
        if (relateCiAttrDtoList.size() == 0 || relateCiAttrDtoList.get(0) == null) {
            return new ArrayList<>();
        }

        List<ResourceTreeDto> instanceDataAfterGroup = groupByAttr(instanceData, relateCiAttrDtoList.get(0));
        if (instanceDataAfterGroup.size() == 0) {
            return new ArrayList<>();
        }

        Integer rootCiTypeId = getDefaultCiTypeIdOfIdc();
        List<ResourceTreeDto> idcDataTree = new ArrayList<>();
        List<Integer> limitedCiTypeIdsOfGetIdcData = getSameCiTypesByCiTypeId(rootCiTypeId);
        limitedCiTypeIdsOfGetIdcData.add(cmdbDataProperties.getCiTypeIdOfHost());

        recursiveGetChildrenData(rootCiTypeId, limitedCiTypeIdsOfGetIdcData, idcDataTree, null);
        if (idcDataTree.size() == 0) {
            return new ArrayList<>();
        }

        mergeDataToDataTree(idcDataTree, instanceDataAfterGroup);
        return idcDataTree;
    }

    private void mergeDataToDataTree(List<ResourceTreeDto> dataTree, List<ResourceTreeDto> toBeMergeDatas) {
        for (int i = 0; i < dataTree.size(); i++) {
            MergeDataResult mergeDataResult = mergeData(dataTree, i, toBeMergeDatas);
            if (mergeDataResult.isEndOfData()) {
                break;
            }
            if (mergeDataResult.isContinueFlag()) {
                continue;
            }

            if (dataTree.get(i).getChildren().size() != 0) {
                mergeDataToDataTree(dataTree.get(i).getChildren(), toBeMergeDatas);
                if (dataTree.get(i).getChildren().size() != 0) {
                    continue;
                }
            }

            dataTree.remove(i);
            if (dataTree.size() == 0) {
                break;
            }
            i--;
        }
    }

    @Data
    private static class MergeDataResult {
        private boolean isEndOfData;
        private boolean continueFlag;
    }

    private MergeDataResult mergeData(List<ResourceTreeDto> dataTree, int index, List<ResourceTreeDto> toBeMergeDatas) {
        MergeDataResult returnFlags = new MergeDataResult();
        ResourceTreeDto childData = dataTree.get(index);
        if (childData.getCiTypeId().equals(toBeMergeDatas.get(0).getCiTypeId())) {
            boolean mergedFlag = false;
            for (ResourceTreeDto toBeMergeData : toBeMergeDatas) {
                if (childData.getGuid().equals(toBeMergeData.getGuid())) {
                    dataTree.get(index).setChildren(toBeMergeData.getChildren());
                    mergedFlag = true;
                }
            }
            if (!mergedFlag) {
                dataTree.remove(index);
                if (dataTree.size() == 0) {
                    returnFlags.setEndOfData(true);
                }
                index--;
            }
            returnFlags.setContinueFlag(true);
        }

        return returnFlags;
    }

    public List<Object> getAllIdcDesignData() {
        return cmdbServiceV2Stub.queryCiData(cmdbDataProperties.getCiTypeIdOfIdcDesign()).getContents();
    }

    public List<Object> getAllIdcData() {
        return cmdbServiceV2Stub.queryCiData(cmdbDataProperties.getCiTypeIdOfIdc()).getContents();
    }

    public List<ResourceTreeDto> getIdcDesignTreesByGuid(List<String> guids) {
        return getDataTreesByCiTypeIdAndGuid(cmdbDataProperties.getCiTypeIdOfIdcDesign(), guids);
    }

    public List<ResourceTreeDto> getIdcTreeByGuid(List<String> guids) {
        return getDataTreesByCiTypeIdAndGuid(cmdbDataProperties.getCiTypeIdOfIdc(), guids);
    }

    private List<ResourceTreeDto> getDataTreesByCiTypeIdAndGuid(int ciTypeId, List<String> guids) {
        List<Integer> sameLayerCiTypes = getSameCiTypesByCiTypeId(ciTypeId);
        List<ResourceTreeDto> resourceTrees = new ArrayList<>();
        List<Object> ciDatas = cmdbServiceV2Stub.queryCiData(ciTypeId, defaultQueryObject().addInFilter("guid", guids)).getContents();
        if (ciDatas.size() == 0) {
            return null;
        }

        for (int i = 0; i < ciDatas.size(); i++) {
            Object ciData = ciDatas.get(i);
            resourceTrees.add(buildNewResourceTreeDto(ciData, ciTypeId));
            Map ciDataMap = (Map) ((Map) ciData).get("data");
            List<CiTypeAttrDto> childrenCiTypeRelativeAttributes = findChildrenCiTypeRelativeAttributes(ciTypeId, cmdbDataProperties.getReferenceNameOfBelong());

            if (childrenCiTypeRelativeAttributes.size() == 0) {
                return resourceTrees;
            }

            for (CiTypeAttrDto childrenCiTypeRelativeAttribute : childrenCiTypeRelativeAttributes) {
                Map<String, Object> filter = new HashMap<>();
                if (!sameLayerCiTypes.contains(childrenCiTypeRelativeAttribute.getCiTypeId())) {
                    continue;
                }
                filter.put(childrenCiTypeRelativeAttribute.getPropertyName(), ciDataMap.get("guid").toString());
                recursiveGetChildrenData(childrenCiTypeRelativeAttribute.getCiTypeId(), sameLayerCiTypes, resourceTrees.get(i).getChildren(), filter);
            }
        }
        return resourceTrees;
    }


    void setCmdbServiceV2Stub(CmdbServiceV2Stub cmdbServiceV2Stub) {
        this.cmdbServiceV2Stub = cmdbServiceV2Stub;
    }

	public PaginationQueryResult<Object> queryCiData(Integer ciTypeId, PaginationQuery queryObject) {
		if (null == queryObject.getSorting())
			queryObject.setSorting(new Sorting(false, "created_date"));
		return cmdbServiceV2Stub.queryCiData(ciTypeId, queryObject);
	}

}


