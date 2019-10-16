package com.webank.wecube.core.controller;

import com.webank.wecube.core.domain.JsonResponse;
import com.webank.wecube.core.service.CmdbResourceService;
import com.webank.wecube.core.support.cmdb.CmdbServiceV2Stub;
import com.webank.wecube.core.support.cmdb.dto.v2.CatCodeDto;
import com.webank.wecube.core.support.cmdb.dto.v2.CatTypeDto;
import com.webank.wecube.core.support.cmdb.dto.v2.CategoryDto;
import com.webank.wecube.core.support.cmdb.dto.v2.PaginationQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;
import java.util.List;

import static com.webank.wecube.core.domain.JsonResponse.error;
import static com.webank.wecube.core.domain.JsonResponse.okay;
import static com.webank.wecube.core.domain.JsonResponse.okayWithData;
import static com.webank.wecube.core.domain.MenuItem.*;

@RestController
@Slf4j
@RequestMapping("/cmdb")
public class CmdbEnumManagementController {

    @Autowired
    private CmdbServiceV2Stub cmdbServiceV2Stub;

    @Autowired
    private CmdbResourceService cmdbResourceService;


    @RolesAllowed({MENU_ADMIN_CMDB_MODEL_MANAGEMENT})
    @GetMapping("/enum/category-types")
    @ResponseBody
    public JsonResponse getAllEnumCategoryTypes() {
        return okayWithData(cmdbServiceV2Stub.getAllEnumCategoryTypes());
    }

    @RolesAllowed({MENU_ADMIN_CMDB_MODEL_MANAGEMENT})
    @PostMapping("/enum/category-types/{category-type-id}/categories/create")
    @ResponseBody
    public JsonResponse createEnumCategory(@PathVariable(value = "category-type-id", required = false) Integer categoryTypeId, @RequestBody CategoryDto categoryDto) {
        return okayWithData(cmdbServiceV2Stub.createEnumCategories(categoryDto));
    }

    @RolesAllowed({MENU_ADMIN_CMDB_MODEL_MANAGEMENT})
    @GetMapping("/enum/category-types/categories")
    @ResponseBody
    public JsonResponse getAllEnumCategories() {
        return okayWithData(cmdbServiceV2Stub.getAllEnumCategories());
    }

    @RolesAllowed({MENU_ADMIN_CMDB_MODEL_MANAGEMENT})
    @GetMapping("/enum/category-types/{category-type-id}/categories")
    @ResponseBody
    public JsonResponse getEnumCategoriesByTypeId(@PathVariable(value = "category-type-id") Integer categoryTypeId) {
        return okayWithData(cmdbServiceV2Stub.getEnumCategoriesByTypeId(categoryTypeId).getContents());
    }

    @RolesAllowed({MENU_ADMIN_CMDB_MODEL_MANAGEMENT})
    @GetMapping("/enum/category-types/categories/query-by-multiple-types")
    @ResponseBody
    public JsonResponse getEnumCategoriesByTypes(@RequestParam(name = "types") String types, @RequestParam(name = "ci-type-id", required = false) Integer ciTypeId) {
        return okayWithData(cmdbResourceService.getEnumCategoryByMultipleTypes(types, ciTypeId));
    }

    @RolesAllowed({MENU_ADMIN_CMDB_MODEL_MANAGEMENT})
    @PutMapping("/enum/category-types/{category-type-id}/categories/{category-id}")
    @ResponseBody
    public JsonResponse updateEnumCategory(@PathVariable(value = "category-type-id", required = false) Integer categoryTypeId,
                                           @PathVariable(value = "category-id", required = false) Integer categoryId,
                                           @RequestBody CategoryDto categoryDto) {
        cmdbServiceV2Stub.updateEnumCategories(categoryDto);
        return okay();
    }

    //TO DO: Unused by UI, to be discarded?
    @DeleteMapping("/enum/category-types/{category-type-id}/categories/{category-id}")
    @ResponseBody
    public JsonResponse deleteEnumCategory(@PathVariable(value = "category-type-id", required = false) Integer categoryTypeId, @PathVariable(value = "category-id") int categoryId) {
        //cmdbServiceV2Stub.deleteEnumCategories(categoryId);
        //return okay();
        return error("This API will be discarded as never used.");
    }

    @RolesAllowed({MENU_ADMIN_CMDB_MODEL_MANAGEMENT, MENU_ADMIN_BASE_DATA_MANAGEMENT, MENU_IMPLEMENTATION_ARTIFACT_MANAGEMENT})
    @PostMapping("/enum/category-types/{category-type-id}/categories/{category-id}/codes/create")
    @ResponseBody
    public JsonResponse createEnumCode(@PathVariable(value = "category-type-id", required = false) Integer categoryTypeId,
                                       @PathVariable(value = "category-id", required = false) Integer categoryId,
                                       @RequestBody CatCodeDto catCodeDto) {
        return okayWithData(cmdbResourceService.createEnumCodes(catCodeDto));
    }

    @RolesAllowed({MENU_ADMIN_PERMISSION_MANAGEMENT, MENU_COLLABORATION_PLUGIN_MANAGEMENT, MENU_DESIGNING_APPLICATION_ARCHITECTURE, MENU_DESIGNING_CI_DATA_MANAGEMENT, MENU_DESIGNING_CI_DATA_ENQUIRY, MENU_DESIGNING_APPLICATION_DEPLOYMENT,
            MENU_DESIGNING_PLANNING, MENU_DESIGNING_RESOURCE_PLANNING, MENU_DESIGNING_CI_INTEGRATED_QUERY_EXECUTION, MENU_IMPLEMENTATION_BATCH_JOB})
    @GetMapping("/enum/category-types/{category-type-id}/categories/{category-id}/codes")
    @ResponseBody
    public JsonResponse getEnumCodesByCategoryId(@PathVariable(value = "category-type-id") Integer categoryTypeId,
                                                 @PathVariable(value = "category-id") Integer categoryId) {
        return okayWithData(cmdbServiceV2Stub.getEnumCodesByCategoryId(categoryId));
    }

    @PutMapping("/enum/category-types/{category-type-id}/categories/{category-id}/codes/{code-id}")
    @ResponseBody
    public JsonResponse updateEnumCode(@PathVariable(value = "category-type-id", required = false) Integer categoryTypeId,
                                       @PathVariable(value = "category-id", required = false) Integer categoryId,
                                       @PathVariable(value = "code-id", required = false) Integer codeId,
                                       @RequestBody CatCodeDto catCodeDto) {
        cmdbServiceV2Stub.updateEnumCodes(catCodeDto);
        return okay();
    }

    @DeleteMapping("/enum/category-types/{category-type-id}/categories/{category-id}/codes/{code-id}")
    @ResponseBody
    public JsonResponse deleteEnumCode(@PathVariable(value = "category-type-id", required = false) Integer categoryTypeId,
                                       @PathVariable(value = "category-id", required = false) Integer categoryId,
                                       @PathVariable(value = "code-id") Integer codeId) {
        cmdbServiceV2Stub.deleteEnumCodes(codeId);
        return okay();
    }

    @RolesAllowed({MENU_ADMIN_CMDB_MODEL_MANAGEMENT, MENU_DESIGNING_CI_DATA_MANAGEMENT, MENU_DESIGNING_CI_DATA_ENQUIRY})
    @GetMapping("/ci-type-layers")
    public JsonResponse getCiTypeLayers() {
        return okayWithData(cmdbResourceService.getAllLayers());
    }

    @RolesAllowed({MENU_ADMIN_CMDB_MODEL_MANAGEMENT})
    @PostMapping("/ci-type-layers/create")
    @ResponseBody
    public JsonResponse createCiTypeLayer(@RequestBody CatCodeDto catCodeDto) {
        return okayWithData(cmdbResourceService.createLayer(catCodeDto));
    }

    @RolesAllowed({MENU_ADMIN_CMDB_MODEL_MANAGEMENT})
    @PostMapping("/ci-type-layers/{layer-id}/swap-position")
    @ResponseBody
    public JsonResponse swapCiTypeLayerPosition(@PathVariable(value = "layer-id") int layerId, @RequestParam(value = "target-layer-id") int targetLayerId) {
        cmdbResourceService.swapCiTypeLayerPosition(layerId, targetLayerId);
        return okay();
    }

    @RolesAllowed({MENU_ADMIN_CMDB_MODEL_MANAGEMENT})
    @DeleteMapping("/ci-type-layers/{layer-id}")
    @ResponseBody
    public JsonResponse deleteCiTypeLayer(@PathVariable(value = "layer-id") int layerId) {
        cmdbServiceV2Stub.deleteEnumCodes(layerId);
        return okay();
    }

    @RolesAllowed({MENU_ADMIN_CMDB_MODEL_MANAGEMENT, MENU_ADMIN_BASE_DATA_MANAGEMENT, MENU_COLLABORATION_PLUGIN_MANAGEMENT,MENU_DESIGNING_CI_DATA_MANAGEMENT, MENU_DESIGNING_CI_DATA_ENQUIRY})
    @PostMapping("/enum/system/codes")
    @ResponseBody
    public JsonResponse querySystemEnumCodesWithRefResources(@RequestBody PaginationQuery queryObject) {
        return okayWithData(cmdbResourceService.querySystemEnumCodesWithRefResources(queryObject));
    }

    @RolesAllowed({MENU_ADMIN_CMDB_MODEL_MANAGEMENT, MENU_ADMIN_BASE_DATA_MANAGEMENT, MENU_DESIGNING_APPLICATION_DEPLOYMENT, MENU_IMPLEMENTATION_ARTIFACT_MANAGEMENT})
    @PostMapping("/enum/non-system/codes")
    @ResponseBody
    public JsonResponse queryNonSystemEnumCodesWithRefResources(@RequestBody PaginationQuery queryObject) {
        return okayWithData(cmdbResourceService.queryNonSystemEnumCodesWithRefResources(queryObject));
    }

    @RolesAllowed({MENU_ADMIN_CMDB_MODEL_MANAGEMENT, MENU_ADMIN_BASE_DATA_MANAGEMENT})
    @GetMapping("/enum/system-categories")
    @ResponseBody
    public JsonResponse getSystemCategories() {
        return okayWithData(cmdbResourceService.getAllSystemEnumCategories().getContents());
    }

    @RolesAllowed({MENU_ADMIN_CMDB_MODEL_MANAGEMENT, MENU_ADMIN_BASE_DATA_MANAGEMENT})
    @GetMapping("/enum/non-system-categories")
    @ResponseBody
    public JsonResponse getNonSystemCategories() {
        return okayWithData(cmdbResourceService.getAllNonSystemEnumCategories().getContents());
    }

    @RolesAllowed({MENU_ADMIN_CMDB_MODEL_MANAGEMENT, MENU_ADMIN_BASE_DATA_MANAGEMENT})
    @GetMapping("/enum/categories/{category-id}/group-list")
    @ResponseBody
    public JsonResponse getGroupListByCategoryId(@PathVariable(value = "category-id") int categoryId) {
        return okayWithData(cmdbResourceService.getGroupListByCatId(categoryId));
    }

    @RolesAllowed({MENU_ADMIN_CMDB_MODEL_MANAGEMENT, MENU_ADMIN_BASE_DATA_MANAGEMENT})
    @PostMapping("/enum/codes/update")
    @ResponseBody
    public JsonResponse updateEnumCodes(@RequestBody List<CatCodeDto> catCodeDtos) {
        return okayWithData(cmdbServiceV2Stub.updateEnumCodes(catCodeDtos.toArray(new CatCodeDto[catCodeDtos.size()])));
    }

    @RolesAllowed({MENU_ADMIN_CMDB_MODEL_MANAGEMENT, MENU_ADMIN_BASE_DATA_MANAGEMENT})
    @PostMapping("/enum/codes/delete")
    @ResponseBody
    public JsonResponse deleteEnumCodes(@RequestBody List<Integer> codeIds) {
        cmdbServiceV2Stub.deleteEnumCodes(codeIds.toArray(new Integer[codeIds.size()]));
        return okay();
    }

    @PostMapping("/enum/categories/query")
    @ResponseBody
    public JsonResponse queryEnumCategories(@RequestBody PaginationQuery queryObject) {
        return okayWithData(cmdbServiceV2Stub.queryEnumCategories(queryObject));
    }

    @PostMapping("/enum/category-types/{category-type-id}/categories/{category-id}/codes/query")
    @ResponseBody
    public JsonResponse queryEnumCodes(@PathVariable(value = "category-type-id", required = false) Integer categoryTypeId,
                                       @PathVariable(value = "category-id") Integer categoryId,
                                       @RequestBody PaginationQuery queryObject) {
        return okayWithData(cmdbServiceV2Stub.queryEnumCodes(queryObject.addEqualsFilter("catId", categoryId)));
    }


}



