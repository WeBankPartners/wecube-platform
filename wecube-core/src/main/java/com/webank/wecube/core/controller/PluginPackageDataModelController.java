package com.webank.wecube.core.controller;

import com.webank.wecube.core.domain.JsonResponse;
import com.webank.wecube.core.dto.PluginPackageEntityDto;
import com.webank.wecube.core.service.PluginPackageDataModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;

import java.util.List;

import static com.webank.wecube.core.domain.MenuItem.MENU_COLLABORATION_PLUGIN_MANAGEMENT;

@RestController
@RequestMapping("/")
@RolesAllowed({MENU_COLLABORATION_PLUGIN_MANAGEMENT})
public class PluginPackageDataModelController {

    @Autowired
    private PluginPackageDataModelService pluginPackageDataModelService;

    @GetMapping("/models")
    @ResponseBody
    public JsonResponse getAllDataModels() {
        List<PluginPackageEntityDto> allPluginPackageEntityList = pluginPackageDataModelService.overview();
        return JsonResponse.okayWithData(allPluginPackageEntityList);
    }

    @GetMapping("/packages/{id}/models")
    @ResponseBody
    public JsonResponse getDataModelByPackageId(@PathVariable(value = "id") int packageId) {
        List<PluginPackageEntityDto> allPluginPackageEntityList = pluginPackageDataModelService.packageView(packageId);
        return JsonResponse.okayWithData(allPluginPackageEntityList);
    }
}
