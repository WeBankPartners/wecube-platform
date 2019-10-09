package com.webank.wecube.core.controller;

import com.webank.wecube.core.domain.JsonResponse;
import com.webank.wecube.core.dto.PluginModelEntityDto;
import com.webank.wecube.core.service.plugin.PluginModelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;

import java.util.List;

import static com.webank.wecube.core.domain.MenuItem.MENU_COLLABORATION_PLUGIN_MANAGEMENT;

@RestController
@Slf4j
@RequestMapping("/plugin")
@RolesAllowed({MENU_COLLABORATION_PLUGIN_MANAGEMENT})
public class PluginModelController {

    @Autowired
    private PluginModelService pluginModelService;

    @GetMapping("/packages/datamodels")
    @ResponseBody
    public JsonResponse getAllDataModels() {
        List<PluginModelEntityDto> allPluginModelEntityList = pluginModelService.overview();
        return JsonResponse.okayWithData(allPluginModelEntityList);
    }

    @GetMapping("/packages/datamodels/{package-id}")
    @ResponseBody
    public JsonResponse getDataModelByPackageId(@PathVariable(value = "package-id") int packageId) {
        List<PluginModelEntityDto> allPluginModelEntityList = pluginModelService.packageView(packageId);
        return JsonResponse.okayWithData(allPluginModelEntityList);
    }
}
