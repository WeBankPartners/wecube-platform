package com.webank.wecube.platform.core.controller;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.JsonResponse;
import com.webank.wecube.platform.core.service.datamodel.NonExpressionServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1")
public class DataServiceController {

    private NonExpressionServiceImpl nonExpressionService;

    @Autowired
    public DataServiceController(NonExpressionServiceImpl nonExpressionService) {
        this.nonExpressionService = nonExpressionService;
    }

    @PostMapping("/packages/{package-name}/entities/{entity-name}/create")
    @ResponseBody
    public JsonResponse createEntity(@PathVariable("package-name") String packageName,
                                     @PathVariable("entity-name") String entityName,
                                     @RequestBody List<Map<String, Object>> request) {
        try {
            return JsonResponse.okayWithData(nonExpressionService.createEntity(packageName, entityName, request));
        } catch (WecubeCoreException ex) {
            return JsonResponse.error(ex.getMessage());
        }

    }

    @GetMapping("/packages/{package-name}/entities/{entity-name}/retrieve")
    @ResponseBody
    public JsonResponse retrieveEntity(
            @PathVariable(value = "package-name") String packageName,
            @PathVariable(value = "entity-name") String entityName
    ) {
        try {
            return JsonResponse.okayWithData(nonExpressionService.retrieveEntity(packageName, entityName));
        } catch (WecubeCoreException ex) {
            return JsonResponse.error(ex.getMessage());
        }

    }

    @PostMapping("/packages/{package-name}/entities/{entity-name}/update")
    @ResponseBody
    public JsonResponse updateEntity(@PathVariable("package-name") String packageName,
                                     @PathVariable("entity-name") String entityName,
                                     @RequestBody List<Map<String, Object>> request) {
        try {
            return JsonResponse.okayWithData(nonExpressionService.updateEntity(packageName, entityName, request));
        } catch (WecubeCoreException ex) {
            return JsonResponse.error(ex.getMessage());
        }
    }

    @PostMapping("/packages/{package-name}/entities/{entity-name}/delete")
    @ResponseBody
    public JsonResponse deleteEntity(@PathVariable("package-name") String packageName,
                                     @PathVariable("entity-name") String entityName,
                                     @RequestBody List<Map<String, Object>> request) {
        try {
            nonExpressionService.deleteEntity(packageName, entityName, request);
            return JsonResponse.okay();
        } catch (WecubeCoreException e) {
            return JsonResponse.error(e.getMessage());
        }

    }

}
