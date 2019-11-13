package com.webank.wecube.platform.core.controller;

import com.webank.wecube.platform.core.domain.JsonResponse;
import com.webank.wecube.platform.core.dto.PluginPackageDataModelDto;
import com.webank.wecube.platform.core.service.PluginPackageDataModelServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1")
//@RolesAllowed({MENU_COLLABORATION_PLUGIN_MANAGEMENT})
public class PluginPackageDataModelController {

    @Autowired
    private PluginPackageDataModelServiceImpl pluginPackageDataModelService;

    @GetMapping("/models")
    @ResponseBody
    public JsonResponse allDataModels() {
        return JsonResponse.okayWithData(pluginPackageDataModelService.overview());
    }

    @GetMapping("/packages/{package-name}/models")
    @ResponseBody
    public JsonResponse getDataModelByPackageName(@PathVariable(value = "package-name") String packageName) {
        return JsonResponse.okayWithData(pluginPackageDataModelService.packageView(packageName));
    }

    @GetMapping("/models/package/{plugin-package-name}")
    @ResponseBody
    public JsonResponse pullDynamicDataModel(@PathVariable(value = "plugin-package-name") String packageName) {
        return JsonResponse.okayWithData(pluginPackageDataModelService.pullDynamicDataModel(packageName));
    }

    @PostMapping("/models")
    public JsonResponse applyNewDataModel(@RequestBody PluginPackageDataModelDto dataModelDto) {
        return JsonResponse.okayWithData(pluginPackageDataModelService.register(dataModelDto, true));
    }
}
