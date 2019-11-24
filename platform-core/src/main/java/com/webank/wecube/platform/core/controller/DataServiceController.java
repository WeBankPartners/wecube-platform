package com.webank.wecube.platform.core.controller;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.JsonResponse;
import com.webank.wecube.platform.core.service.DataModelExpressionServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1")
public class DataServiceController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private DataModelExpressionServiceImpl dataModelExpressionService;

    @GetMapping("/packages/{package-name}/entities/{entity-name}/retrieve")
    @ResponseBody
    public JsonResponse getRefByIdInfoByPackageNameAndEntityName(
            @PathVariable(value = "package-name") String packageName,
            @PathVariable(value = "entity-name") String entityName
    ) {
        try {
            return JsonResponse.okayWithData(dataModelExpressionService.retrieveEntity(packageName, entityName));
        } catch (WecubeCoreException ex) {
            return JsonResponse.error(ex.getMessage());
        }

    }

    @PostMapping("/packages/{package-name}/entities/{entity-name}/create")
    public JsonResponse createCiData(@PathVariable("package-name") String packageName,
                                     @PathVariable("entity-name") String entityName,
                                     @RequestBody List<Map<String, Object>> request) {
        try {
            return JsonResponse.okayWithData(dataModelExpressionService.createEntity(packageName, entityName, request));
        } catch (WecubeCoreException ex) {
            return JsonResponse.error(ex.getMessage());
        }

    }

    @PostMapping("/packages/{package-name}/entities/{entity-name}/update")
    public JsonResponse updateCiData(@PathVariable("package-name") String packageName,
                                     @PathVariable("entity-name") String entityName,
                                     @RequestBody List<Map<String, Object>> request) {
        try {
            return JsonResponse.okayWithData(dataModelExpressionService.updateEntity(packageName, entityName, request));
        } catch (WecubeCoreException ex) {
            return JsonResponse.error(ex.getMessage());
        }
    }

    @PostMapping("/packages/{package-name}/entities/{entity-name}/delete")
    public JsonResponse deleteCiData(@PathVariable("package-name") String packageName,
                                     @PathVariable("entity-name") String entityName,
                                     @RequestBody List<Map<String, Object>> request) {
        dataModelExpressionService.deleteEntity(packageName, entityName, request);
        return JsonResponse.okay();
    }

}
