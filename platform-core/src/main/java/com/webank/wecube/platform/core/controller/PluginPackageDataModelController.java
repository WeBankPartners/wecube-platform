package com.webank.wecube.platform.core.controller;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.JsonResponse;
import com.webank.wecube.platform.core.dto.PluginPackageEntityDto;
import com.webank.wecube.platform.core.service.PluginPackageDataModelService;
import com.webank.wecube.platform.core.service.PluginPackageDataModelServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;

import java.util.List;

import static com.webank.wecube.platform.core.domain.MenuItem.MENU_COLLABORATION_PLUGIN_MANAGEMENT;

@RestController
@RequestMapping("/v1")
//@RolesAllowed({MENU_COLLABORATION_PLUGIN_MANAGEMENT})
public class PluginPackageDataModelController {

    @Autowired
    private PluginPackageDataModelServiceImpl pluginPackageDataModelService;

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

    @GetMapping("/packages/{id}/models")
    @ResponseBody
    public JsonResponse getDataModelByPackageId(@PathVariable(value = "id") int packageId) {
        List<PluginPackageEntityDto> allPluginPackageEntityList;
        try {
            allPluginPackageEntityList = pluginPackageDataModelService.packageView(packageId);
        } catch (WecubeCoreException ex) {
            return JsonResponse.error(ex.getMessage());
        }
        return JsonResponse.okayWithData(allPluginPackageEntityList);
    }
}
