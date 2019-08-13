package com.webank.wecube.core.controller;

import static com.google.common.collect.Lists.newArrayList;
import static com.webank.wecube.core.domain.JsonResponse.okay;
import static com.webank.wecube.core.domain.JsonResponse.okayWithData;
import static com.webank.wecube.core.domain.JsonResponse.error;
import static com.webank.wecube.core.domain.MenuItem.*;
import static com.webank.wecube.core.support.cmdb.dto.v2.PaginationQuery.defaultQueryObject;
import static com.webank.wecube.core.utils.BooleanUtils.isTrue;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.webank.wecube.core.commons.ApplicationProperties;
import com.webank.wecube.core.commons.WecubeCoreException;
import com.webank.wecube.core.domain.JsonResponse;
import com.webank.wecube.core.support.cmdb.dto.v2.OperateCiDto;
import com.webank.wecube.core.service.CmdbResourceService;
import com.webank.wecube.core.support.cmdb.CmdbServiceV2Stub;
import com.webank.wecube.core.support.cmdb.dto.v2.CiTypeAttrDto;
import com.webank.wecube.core.support.cmdb.dto.v2.CiTypeDto;
import com.webank.wecube.core.support.cmdb.dto.v2.PaginationQuery;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/cmdb")
public class CmdbCiManagementController {

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private CmdbServiceV2Stub cmdbServiceV2Stub;

    @Autowired
    private CmdbResourceService cmdbResourceService;

    @RolesAllowed({ MENU_COLLABORATION_WORKFLOW_ORCHESTRATION, MENU_COLLABORATION_PLUGIN_MANAGEMENT,
            MENU_IMPLEMENTATION_ARTIFACT_MANAGEMENT, MENU_ADMIN_CMDB_MODEL_MANAGEMENT, MENU_DESIGNING_CI_DATA_MANAGEMENT, MENU_DESIGNING_CI_DATA_ENQUIRY,
            MENU_DESIGNING_RESOURCE_PLANNING, MENU_DESIGNING_PLANNING, MENU_DESIGNING_APPLICATION_ARCHITECTURE })
    @GetMapping("/ci-types")
    @ResponseBody
    public JsonResponse getCiTypes(@RequestParam(name = "group-by", required = false) String groupBy, @RequestParam(name = "with-attributes", required = false) String withAttributes, @RequestParam(name = "status", required = false) String status) {
        if ("catalog".equalsIgnoreCase(groupBy)) {
            return okayWithData(cmdbResourceService.getCiTypesGroupByCatalogs(isTrue(withAttributes), status));
        } else if ("layer".equalsIgnoreCase(groupBy)) {
            return okayWithData(cmdbResourceService.getCiTypesGroupByLayers(isTrue(withAttributes), status));
        } else {
            return okayWithData(cmdbServiceV2Stub.getAllCiTypes(isTrue(withAttributes), status));
        }
    }

    @RolesAllowed({MENU_ADMIN_CMDB_MODEL_MANAGEMENT})
    @PostMapping("/ci-types/create")
    @ResponseBody
    public JsonResponse createCiType(@RequestBody CiTypeDto ciTypeDto) {
        return okayWithData(cmdbServiceV2Stub.createCiTypes(ciTypeDto));
    }

    //TO DO: Unused by UI, to be discarded?
    @GetMapping("/ci-types/{ci-type-id}")
    @ResponseBody
    public JsonResponse getCiType(@PathVariable(value = "ci-type-id") int ciTypeId, @RequestParam(name = "with-attributes", required = false) String withAttributes) {
        //return okayWithData(cmdbServiceV2Stub.getCiType(ciTypeId, isTrue(withAttributes)));
        return error("This API will be discarded as never used.");
    }


//    @GetMapping("/ci-types/{ci-type-id}/header")
//    @ResponseBody
//    public JsonResponse getCiDataHeader(@PathVariable(value = "ci-type-id") int ciTypeId) {
//        return okayWithData(cmdbServiceV2Stub.getCiTypeAttributesByCiTypeId(ciTypeId));
//    }

    @RolesAllowed({MENU_ADMIN_CMDB_MODEL_MANAGEMENT})
    @PutMapping("/ci-types/{ci-type-id}")
    @ResponseBody
    public JsonResponse updateCiType(@PathVariable(value = "ci-type-id") int ciTypeId, @RequestBody CiTypeDto ciTypeDto) {
        cmdbServiceV2Stub.updateCiTypes(ciTypeDto);
        return okay();
    }

    @RolesAllowed({ MENU_ADMIN_CMDB_MODEL_MANAGEMENT })
    @DeleteMapping("/ci-types/{ci-type-id}")
    @ResponseBody
    public JsonResponse deleteCiType(@PathVariable(value = "ci-type-id") int ciTypeId) {
        cmdbServiceV2Stub.deleteCiTypes(ciTypeId);
        return okay();
    }

    @RolesAllowed({ MENU_ADMIN_CMDB_MODEL_MANAGEMENT, MENU_DESIGNING_CI_DATA_MANAGEMENT })
    @PostMapping("/ci-types/{ci-type-id}/icon")
    @ResponseBody
    public JsonResponse uploadCiTypeIcon(@PathVariable(value = "ci-type-id") int ciTypeId, @RequestParam(value = "file", required = false) MultipartFile file) throws WecubeCoreException {
        if (file.getSize() > applicationProperties.getMaxFileSize().toBytes()) {
            String errorMessage = String.format("Upload image icon for CiType (%s) failed due to file size (%s bytes) exceeded limitation (%s KB).", ciTypeId, file.getSize(), applicationProperties.getMaxFileSize().toKilobytes());
            log.warn(errorMessage);
            throw new WecubeCoreException(errorMessage);
        }
        return okayWithData(cmdbServiceV2Stub.uploadCiTypeIcon(ciTypeId, file.getResource()));
    }

    @RolesAllowed({ MENU_ADMIN_CMDB_MODEL_MANAGEMENT })
    @PostMapping("/ci-types/{ci-type-id}/apply")
    @ResponseBody
    public JsonResponse applyCiType(@PathVariable(value = "ci-type-id") Integer ciTypeId) {
        Integer[] ciTypeIdArray = {ciTypeId};
        return okayWithData(cmdbServiceV2Stub.applyCiType(ciTypeIdArray));
    }

    @PostMapping("/ci-types/apply")
    @ResponseBody
    public JsonResponse applyCiTypes() {
        return okayWithData(cmdbServiceV2Stub.applyCiTypes());
    }

    @RolesAllowed({MENU_ADMIN_CMDB_MODEL_MANAGEMENT,MENU_COLLABORATION_PLUGIN_MANAGEMENT,
        MENU_COLLABORATION_WORKFLOW_ORCHESTRATION,MENU_IMPLEMENTATION_ARTIFACT_MANAGEMENT,MENU_DESIGNING_CI_INTEGRATED_QUERY_MANAGEMENT})
    @GetMapping("/ci-types/{ci-type-id}/references/by")
    @ResponseBody
    public JsonResponse getCiTypeReferenceBy(@PathVariable(value = "ci-type-id") int ciTypeId) {
        return okayWithData(cmdbResourceService.getCiTypeReferenceBy(ciTypeId));
    }

    @RolesAllowed({MENU_ADMIN_CMDB_MODEL_MANAGEMENT,MENU_COLLABORATION_PLUGIN_MANAGEMENT,
        MENU_COLLABORATION_WORKFLOW_ORCHESTRATION,MENU_IMPLEMENTATION_ARTIFACT_MANAGEMENT,MENU_DESIGNING_CI_INTEGRATED_QUERY_MANAGEMENT})
    @GetMapping("/ci-types/{ci-type-id}/references/to")
    @ResponseBody
    public JsonResponse getCiTypeReferenceTo(@PathVariable(value = "ci-type-id") int ciTypeId) {
        return okayWithData(cmdbResourceService.getCiTypeReferenceTo(ciTypeId));
    }

    @RolesAllowed({ MENU_ADMIN_CMDB_MODEL_MANAGEMENT })
    @PostMapping("/ci-types/{ci-type-id}/attributes/create")
    @ResponseBody
    public JsonResponse createCiTypeAttribute(@PathVariable(value = "ci-type-id") int ciTypeId, @RequestBody CiTypeAttrDto ciTypeAttrDto) {
        ciTypeAttrDto.setCiTypeId(ciTypeId);
        return okayWithData(cmdbServiceV2Stub.createCiTypeAttribute(ciTypeAttrDto));
    }

    @RolesAllowed({ MENU_IMPLEMENTATION_BATCH_JOB, MENU_ADMIN_CMDB_MODEL_MANAGEMENT, MENU_COLLABORATION_PLUGIN_MANAGEMENT,
            MENU_COLLABORATION_WORKFLOW_ORCHESTRATION, MENU_IMPLEMENTATION_ARTIFACT_MANAGEMENT,
            MENU_DESIGNING_CI_INTEGRATED_QUERY_MANAGEMENT, MENU_DESIGNING_RESOURCE_PLANNING, MENU_DESIGNING_PLANNING,
            MENU_DESIGNING_APPLICATION_DEPLOYMENT, MENU_DESIGNING_CI_DATA_MANAGEMENT, MENU_DESIGNING_CI_DATA_ENQUIRY, MENU_DESIGNING_APPLICATION_ARCHITECTURE})
    @GetMapping("/ci-types/{ci-type-id}/attributes")
    @ResponseBody
    public JsonResponse getCiTypeAttributes(@PathVariable(value = "ci-type-id") int ciTypeId, @RequestParam(name = "accept-input-types", required = false) String acceptInputTypes) {
        if (isNotEmpty(acceptInputTypes)) {
            return okayWithData(cmdbServiceV2Stub.queryCiTypeAttributes(defaultQueryObject("ciTypeId", ciTypeId).addInFilter("inputType", newArrayList(acceptInputTypes.split(",")))));
        } else {
            return okayWithData(cmdbServiceV2Stub.getCiTypeAttributesByCiTypeId(ciTypeId));
        }
    }

    //TO DO: Unused by UI, to be discarded?
    @GetMapping("/ci-types/{ci-type-id}/attributes/{attribute-id}")
    @ResponseBody
    public JsonResponse getCiTypeAttribute(@PathVariable(value = "ci-type-id") int ciTypeId, @PathVariable(value = "attribute-id") int attributeId) {
        //return okayWithData(cmdbServiceV2Stub.getCiTypeAttribute(attributeId));
        return error("This API will be discarded as never used.");
    }

    @RolesAllowed({ MENU_ADMIN_CMDB_MODEL_MANAGEMENT })
    @PutMapping("/ci-types/{ci-type-id}/attributes/{attribute-id}")
    @ResponseBody
    public JsonResponse updateCiTypeAttribute(@PathVariable(value = "ci-type-id") int ciTypeId, @PathVariable(value = "attribute-id") int attributeId, @RequestBody CiTypeAttrDto ciTypeAttrDto) {
        return okayWithData(cmdbServiceV2Stub.updateCiTypeAttributes(ciTypeAttrDto));
    }

    @RolesAllowed({ MENU_ADMIN_CMDB_MODEL_MANAGEMENT })
    @DeleteMapping("/ci-types/{ci-type-id}/attributes/{attribute-id}")
    @ResponseBody
    public JsonResponse deleteCiTypeAttribute(@PathVariable(value = "ci-type-id") int ciTypeId, @PathVariable(value = "attribute-id") int attributeId) {
        cmdbServiceV2Stub.deleteCiTypeAttributes(attributeId);
        return okay();
    }

    @RolesAllowed({ MENU_ADMIN_CMDB_MODEL_MANAGEMENT })
    @PostMapping("/ci-types/{ci-type-id}/ci-type-attributes/apply")
    @ResponseBody
    public JsonResponse applyCiTypeAttributes(@PathVariable(value = "ci-type-id") Integer ciTypeId, @RequestBody Integer[] ciTypeAttrIds) {
        return okayWithData(cmdbServiceV2Stub.applyCiTypeAttributes(ciTypeAttrIds));
    }

    @RolesAllowed({ MENU_ADMIN_CMDB_MODEL_MANAGEMENT })
    @PostMapping("/ci-types/{ci-type-id}/attributes/{attribute-id}/swap-position")
    @ResponseBody
    public JsonResponse swapCiTypeAttributePosition(@PathVariable(value = "ci-type-id") int ciTypeId, @PathVariable(value = "attribute-id") int attributeId, @RequestParam(value = "target-attribute-id") int targetAttributeId) {
        cmdbResourceService.swapCiTypeAttributePosition(attributeId, targetAttributeId);
        return okay();
    }

    @PostMapping("/ci-types/{ci-type-id}/ci-data/create")
    @ResponseBody
    public JsonResponse createCiData(@PathVariable(value = "ci-type-id") int ciTypeId,
                                     @RequestBody Map<String, Object> ciData) {
        return okayWithData(cmdbServiceV2Stub.createCiData(ciTypeId, ciData));
    }

    @RolesAllowed({MENU_DESIGNING_APPLICATION_ARCHITECTURE, MENU_DESIGNING_CI_DATA_MANAGEMENT, MENU_DESIGNING_APPLICATION_DEPLOYMENT,
        MENU_DESIGNING_PLANNING, MENU_DESIGNING_RESOURCE_PLANNING})
    @PostMapping("/ci-types/{ci-type-id}/ci-data/batch-create")
    @ResponseBody
    public JsonResponse createCiData(@PathVariable(value = "ci-type-id") int ciTypeId,
                                      @RequestBody List<Map<String, Object>> ciDataDtos) {
        return okayWithData(cmdbServiceV2Stub.createCiData(ciTypeId, ciDataDtos.toArray()));
    }

    @RolesAllowed({MENU_DESIGNING_APPLICATION_ARCHITECTURE, MENU_DESIGNING_CI_DATA_MANAGEMENT, MENU_DESIGNING_CI_DATA_ENQUIRY, MENU_DESIGNING_APPLICATION_DEPLOYMENT,
        MENU_DESIGNING_PLANNING, MENU_DESIGNING_RESOURCE_PLANNING})
    @PostMapping("/ci-types/{ci-type-id}/ci-data/query")
    @ResponseBody
    public JsonResponse queryCiData(@PathVariable(value = "ci-type-id") int ciTypeId,
                                    @RequestBody PaginationQuery queryObject) {
        return okayWithData(cmdbServiceV2Stub.queryCiData(ciTypeId, queryObject));
    }

    @PostMapping("/referenceCiData/{reference-attr-id}/query")
    @ResponseBody
    public JsonResponse queryReferenceCiData(@PathVariable(value = "reference-attr-id") int referenceAttrId,
                                             @RequestBody PaginationQuery queryObject) {
        return okayWithData(cmdbServiceV2Stub.queryReferenceCiData(referenceAttrId, queryObject));
    }

    @PostMapping("/referenceEnumCodes/{reference-attr-id}/query")
    @ResponseBody
    public JsonResponse queryReferenceEnumCodes(@PathVariable(value = "reference-attr-id") int referenceAttrId,
                                                @RequestBody PaginationQuery queryObject) {
        return okayWithData(cmdbServiceV2Stub.queryReferenceEnumCodes(referenceAttrId, queryObject));
    }

    @RolesAllowed({ MENU_DESIGNING_APPLICATION_ARCHITECTURE })
    @PutMapping("/ci-types/{ci-type-id}/ci-data/{ci-data-id}")
    @ResponseBody
    public JsonResponse updateCiData(@PathVariable(value = "ci-type-id") int ciTypeId,
                                     @PathVariable(value = "ci-data-id") String ciDataId, @RequestBody Map<String, Object> ciData) {
        return okayWithData(cmdbServiceV2Stub.updateCiData(ciTypeId, ciData));
    }

    @RolesAllowed({MENU_DESIGNING_APPLICATION_ARCHITECTURE, MENU_DESIGNING_CI_DATA_MANAGEMENT, MENU_DESIGNING_APPLICATION_DEPLOYMENT,
        MENU_DESIGNING_PLANNING, MENU_DESIGNING_RESOURCE_PLANNING})
    @PostMapping("/ci-types/{ci-type-id}/ci-data/batch-update")
    @ResponseBody
    public JsonResponse updateCiData(@PathVariable(value = "ci-type-id") int ciTypeId, @RequestBody List<Map<String, Object>> ciDatas) {
        return okayWithData(cmdbServiceV2Stub.updateCiData(ciTypeId, ciDatas.toArray()));
    }

    //TO DO: Unused by UI, to be discarded?
    @DeleteMapping("/ci-types/{ci-type-id}/ci-data/{ci-data-id}")
    @ResponseBody
    public JsonResponse deleteCiData(@PathVariable(value = "ci-type-id") int ciTypeId,
                                     @PathVariable(value = "ci-data-id") String ciDataId) {
        cmdbServiceV2Stub.deleteCiData(ciTypeId, ciDataId);
        return okay();
    }

    @RolesAllowed({MENU_DESIGNING_APPLICATION_ARCHITECTURE, MENU_DESIGNING_CI_DATA_MANAGEMENT, MENU_DESIGNING_APPLICATION_DEPLOYMENT,
        MENU_DESIGNING_PLANNING, MENU_DESIGNING_RESOURCE_PLANNING})
    @PostMapping("/ci-types/{ci-type-id}/ci-data/batch-delete")
    @ResponseBody
    public JsonResponse deleteCiData(@PathVariable(value = "ci-type-id") int ciTypeId, @RequestBody List<String> ciDataIds) {
        cmdbServiceV2Stub.deleteCiData(ciTypeId, ciDataIds.toArray());
        return okay();
    }

    @RolesAllowed({ MENU_DESIGNING_APPLICATION_ARCHITECTURE })
    @PostMapping("/ci-types/{ci-type-id}/attributes/{attribute-id}/implement")
    @ResponseBody
    public JsonResponse implementCiTypeAttribute(@PathVariable(value = "ci-type-id") Integer ciTypeId, @PathVariable(value = "attribute-id") Integer attributeId, @RequestParam(value = "operation") String operation) {
        return okayWithData(cmdbServiceV2Stub.implementCiTypeAttribute(attributeId, operation));
    }

    @RolesAllowed({ MENU_DESIGNING_APPLICATION_ARCHITECTURE })
    @PostMapping("/ci-types/{ci-type-id}/implement")
    @ResponseBody
    public JsonResponse implementCiType(@PathVariable(value = "ci-type-id") Integer ciTypeId, @RequestParam(value = "operation") String operation) {
        return okayWithData(cmdbServiceV2Stub.implementCiType(ciTypeId, operation));
    }

    @RolesAllowed({ MENU_DESIGNING_PLANNING })
    @PostMapping("/data-tree/query-idc-design-tree")
    @ResponseBody
    public JsonResponse getIdcDesignTreeByGuid(@RequestBody List<String> idcDesignGuids) {
        return okayWithData(cmdbResourceService.getIdcDesignTreesByGuid(idcDesignGuids));
    }

    @RolesAllowed({ MENU_DESIGNING_RESOURCE_PLANNING })
    @PostMapping("/data-tree/query-idc-tree")
    @ResponseBody
    public JsonResponse getIdcImplementTreeByGuid(@RequestBody List<String> idcGuids) {
        return okayWithData(cmdbResourceService.getIdcTreeByGuid(idcGuids));
    }

    @RolesAllowed({ MENU_DESIGNING_PLANNING,  MENU_DESIGNING_APPLICATION_ARCHITECTURE})
    @GetMapping("/all-zone-link-design")
    @ResponseBody
    public JsonResponse getAllZoneLinkDesignGroupByIdcDesign() {
        return okayWithData(cmdbResourceService.getAllZoneLinkDesignGroupByIdcDesign());
    }

    @RolesAllowed({ MENU_DESIGNING_APPLICATION_DEPLOYMENT,  MENU_DESIGNING_RESOURCE_PLANNING})
    @GetMapping("/all-zone-link")
    @ResponseBody
    public JsonResponse getAllZoneLinkGroupByIdc() {
        return okayWithData(cmdbResourceService.getAllZoneLinkGroupByIdc());
    }

    @RolesAllowed({ MENU_DESIGNING_APPLICATION_DEPLOYMENT,  MENU_IMPLEMENTATION_APPLICATION_DEPLOYMENT})
    @GetMapping("/trees/all-deploy-trees/from-subsys")
    @ResponseBody
    public JsonResponse getAllDeployTreesFromSubSys(@RequestParam(value = "env-code") String envCode, @RequestParam(value = "system-design-guid") String systemDesignGuid) {
        return okayWithData(cmdbResourceService.getAllDeployTreesFromSubSys(envCode, systemDesignGuid));
    }

    @RolesAllowed({ MENU_DESIGNING_APPLICATION_ARCHITECTURE })
    @GetMapping("/trees/all-design-trees/from-system-design")
    @ResponseBody
    public JsonResponse getAllDesignTreesFromSystemDesign(@RequestParam(value = "system-design-guid") String systemDesignGuid) {
        return okayWithData(cmdbResourceService.getAllDesignTreesFromSystemDesign(systemDesignGuid));
    }

    @RolesAllowed({ MENU_DESIGNING_APPLICATION_ARCHITECTURE })
    @PostMapping("/trees/all-design-trees/from-system-design/save")
    @ResponseBody
    public JsonResponse saveAllDesignTreesFromSystemDesign(@RequestParam(value = "system-design-guid") String systemDesignGuid) {
        cmdbResourceService.saveAllDesignTreesFromSystemDesign(systemDesignGuid);
        return okay();
    }

    @RolesAllowed({ MENU_DESIGNING_APPLICATION_ARCHITECTURE, MENU_DESIGNING_APPLICATION_DEPLOYMENT})
    @GetMapping("/system-designs")
    @ResponseBody
    public JsonResponse getSystemDesigns() {
        return okayWithData(cmdbResourceService.getSystemDesigns());
    }

    @RolesAllowed({ MENU_DESIGNING_APPLICATION_ARCHITECTURE })
    @GetMapping("/architecture-designs/tabs")
    @ResponseBody
    public JsonResponse getArchitectureDesignTabs() {
        return okayWithData(cmdbResourceService.getArchitectureDesignTabs());
    }

    @RolesAllowed({ MENU_DESIGNING_APPLICATION_ARCHITECTURE })
    @PostMapping("/architecture-designs/tabs/ci-data")
    @ResponseBody
    public JsonResponse getArchitectureCiData(@RequestParam(value = "code-id") Integer codeId,
                                               @RequestParam(value = "system-design-guid") String systemDesignGuid,
                                               @RequestBody PaginationQuery queryObject) {
        return okayWithData(cmdbResourceService.getArchitectureCiData(codeId, systemDesignGuid, queryObject));
    }

    @RolesAllowed({ MENU_DESIGNING_APPLICATION_DEPLOYMENT})
    @GetMapping("/deploy-designs/tabs")
    @ResponseBody
    public JsonResponse getDeployDesignTabs() {
        return okayWithData(cmdbResourceService.getDeployDesignTabs());
    }

    @RolesAllowed({ MENU_DESIGNING_APPLICATION_DEPLOYMENT})
    @PostMapping("/deploy-designs/tabs/ci-data")
    @ResponseBody
    public JsonResponse getDeployCiData(@RequestParam(value = "code-id") Integer codeId,
                                        @RequestParam(value = "env-code") String envCode,
                                        @RequestParam(value = "system-design-guid") String systemDesignGuid,
                                        @RequestBody PaginationQuery queryObject) {
        return okayWithData(cmdbResourceService.getDeployCiData(codeId, envCode, systemDesignGuid, queryObject));
    }

    @RolesAllowed({MENU_DESIGNING_APPLICATION_ARCHITECTURE, MENU_DESIGNING_CI_DATA_MANAGEMENT, MENU_DESIGNING_APPLICATION_DEPLOYMENT,
        MENU_DESIGNING_PLANNING, MENU_DESIGNING_RESOURCE_PLANNING})
    @PostMapping("/ci/state/operate")
    @ResponseBody
    public JsonResponse operateCiForState(@RequestParam("operation") String operation,
                                          @RequestBody List<OperateCiDto> operateCiObject) {
        return okayWithData(cmdbServiceV2Stub.operateCiForState(operateCiObject, operation));
    }

    @GetMapping("/planning-designs/tabs")
    @ResponseBody
    public JsonResponse getPlanningDesignTabs() {
        return okayWithData(cmdbResourceService.getPlanningDesignTabs());
    }

    @GetMapping("/resource-planning/tabs")
    @ResponseBody
    public JsonResponse getResourcePlanningTabs() {
        return okayWithData(cmdbResourceService.getResourcePlanningTabs());
    }

    @RolesAllowed({ MENU_DESIGNING_APPLICATION_ARCHITECTURE })
    @GetMapping("/data-tree/application-framework-design")
    @ResponseBody
    public JsonResponse getApplicationFrameworkDesignDataTree(@RequestParam(value = "system-design-guid") String systemDesignGuid) {
        return okayWithData(cmdbResourceService.getApplicationFrameworkDesignDataTreeBySystemDesignGuid(systemDesignGuid));
    }

    @RolesAllowed({ MENU_DESIGNING_APPLICATION_DEPLOYMENT})
    @GetMapping("/data-tree/application-deployment-design")
    @ResponseBody
    public JsonResponse getApplicationDeploymentDesignDataTree(@RequestParam(value = "system-design-guid") String systemDesignGuid, @RequestParam(value = "env-code") Integer envCodeId) {
        return okayWithData(cmdbResourceService.getApplicationDeploymentDesignDataTreeBySystemDesignGuidAndEnvCode(systemDesignGuid, envCodeId));
    }

    @RolesAllowed({ MENU_DESIGNING_PLANNING})
    @GetMapping("/ci-data/all-idc-design")
    @ResponseBody
    public JsonResponse getAllIdcDesignData() {
        return okayWithData(cmdbResourceService.getAllIdcDesignData());
    }

    @RolesAllowed({ MENU_DESIGNING_PLANNING})
    @GetMapping("/ci-data/all-idc")
    @ResponseBody
    public JsonResponse getAllIdcData() {
        return okayWithData(cmdbResourceService.getAllIdcData());
    }
}
