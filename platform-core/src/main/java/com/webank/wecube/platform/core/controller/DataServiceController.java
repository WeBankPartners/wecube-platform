package com.webank.wecube.platform.core.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.webank.wecube.platform.core.dto.CommonResponseDto;
import com.webank.wecube.platform.core.service.datamodel.NonExpressionServiceImpl;

@RestController
@RequestMapping("/v1")
public class DataServiceController {

    @Autowired
    private NonExpressionServiceImpl nonExpressionService;

    @PostMapping("/packages/{package-name}/entities/{entity-name}/create")
    public CommonResponseDto createEntity(@PathVariable("package-name") String packageName,
            @PathVariable("entity-name") String entityName, @RequestBody List<Map<String, Object>> request) {
        return CommonResponseDto.okayWithData(nonExpressionService.createEntity(packageName, entityName, request));
    }

    @GetMapping("/packages/{package-name}/entities/{entity-name}/retrieve")
    public CommonResponseDto retrieveEntity(@PathVariable(value = "package-name") String packageName,
            @PathVariable(value = "entity-name") String entityName,
            @RequestParam(required = false) Map<String, String> allFilters) {
        return CommonResponseDto.okayWithData(nonExpressionService.retrieveEntity(packageName, entityName, allFilters));
    }

    @PostMapping("/packages/{package-name}/entities/{entity-name}/update")
    public CommonResponseDto updateEntity(@PathVariable("package-name") String packageName,
            @PathVariable("entity-name") String entityName, @RequestBody List<Map<String, Object>> request) {
        return CommonResponseDto.okayWithData(nonExpressionService.updateEntity(packageName, entityName, request));
    }

    @PostMapping("/packages/{package-name}/entities/{entity-name}/delete")
    public CommonResponseDto deleteEntity(@PathVariable("package-name") String packageName,
            @PathVariable("entity-name") String entityName, @RequestBody List<Map<String, Object>> request) {
        nonExpressionService.deleteEntity(packageName, entityName, request);
        return CommonResponseDto.okay();

    }

}
