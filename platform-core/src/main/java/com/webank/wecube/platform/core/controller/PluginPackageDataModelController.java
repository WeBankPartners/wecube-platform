package com.webank.wecube.platform.core.controller;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.JsonResponse;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageDataModel;
import com.webank.wecube.platform.core.dto.PluginPackageDataModelDto;
import com.webank.wecube.platform.core.dto.PluginPackageEntityDto;
import com.webank.wecube.platform.core.service.PluginPackageDataModelServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/v1")
//@RolesAllowed({MENU_COLLABORATION_PLUGIN_MANAGEMENT})
public class PluginPackageDataModelController {

    @Autowired
    private PluginPackageDataModelServiceImpl pluginPackageDataModelService;

    @GetMapping("/models2")
    @ResponseBody
    public JsonResponse allllDataModels() {
        Set<PluginPackageDataModelDto> pluginPackageDataModelDtos;
        try {
            pluginPackageDataModelDtos = pluginPackageDataModelService.allDataModels();
        } catch (WecubeCoreException ex) {
            return JsonResponse.error(ex.getMessage());
        }
        return JsonResponse.okayWithData(pluginPackageDataModelDtos);
    }

    @GetMapping("/models")
    @ResponseBody
    public JsonResponse getAllDataModels() {
        List<PluginPackageEntityDto> allPluginPackageEntityList;
        try {
            allPluginPackageEntityList = pluginPackageDataModelService.overview();
        } catch (WecubeCoreException ex) {
            return JsonResponse.error(ex.getMessage());
        }
        return JsonResponse.okayWithData(allPluginPackageEntityList);
    }

    @GetMapping("/models/package/{plugin-package-name}")
    @ResponseBody
    public JsonResponse pullDynamicDataModel(@PathVariable(value = "plugin-package-name") String packageName) {
        PluginPackageDataModelDto pluginPackageDataModelDto = pluginPackageDataModelService.pullDynamicDataModel(packageName);
        return JsonResponse.okayWithData(null);
    }

    @GetMapping("/packages/{package-name}/models")
    @ResponseBody
    public JsonResponse getDataModelByPackageId(@PathVariable(value = "package-name") String packageName) {
        List<PluginPackageEntityDto> allPluginPackageEntityList;
        try {
            allPluginPackageEntityList = pluginPackageDataModelService.packageView(packageName);
        } catch (WecubeCoreException ex) {
            return JsonResponse.error(ex.getMessage());
        }
        return JsonResponse.okayWithData(allPluginPackageEntityList);
    }
}
