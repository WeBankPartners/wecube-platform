package com.webank.wecube.core.controller;

import com.webank.wecube.core.domain.JsonResponse;
import com.webank.wecube.core.dto.PluginModelEntityDto;
import com.webank.wecube.core.service.plugin.PluginModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;

import java.util.List;
import java.util.Objects;

import static com.webank.wecube.core.domain.MenuItem.MENU_COLLABORATION_PLUGIN_MANAGEMENT;

@RestController
@RolesAllowed({MENU_COLLABORATION_PLUGIN_MANAGEMENT})
public class PluginModelController {

    @Autowired
    private PluginModelService pluginModelService;

    @GetMapping("/models")
    @ResponseBody
    public JsonResponse getAllDataModels() {
        List<PluginModelEntityDto> allPluginModelEntityList = pluginModelService.overview();
        return JsonResponse.okayWithData(allPluginModelEntityList);
    }

    @GetMapping("/packages/{id}/models")
    @ResponseBody
    public JsonResponse getDataModelByPackageId(@PathVariable(value = "id") int packageId) {
        List<PluginModelEntityDto> allPluginModelEntityList = pluginModelService.packageView(packageId);
        return JsonResponse.okayWithData(allPluginModelEntityList);
    }
}
