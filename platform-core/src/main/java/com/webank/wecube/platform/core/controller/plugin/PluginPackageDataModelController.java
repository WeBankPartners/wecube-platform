package com.webank.wecube.platform.core.controller.plugin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webank.wecube.platform.core.dto.CommonResponseDto;
import com.webank.wecube.platform.core.dto.DataModelEntityDto;
import com.webank.wecube.platform.core.dto.PluginPackageAttributeDto;
import com.webank.wecube.platform.core.dto.PluginPackageDataModelDto;
import com.webank.wecube.platform.core.service.PluginPackageDataModelServiceImpl;

@RestController
@RequestMapping("/v1")
// @RolesAllowed({MENU_COLLABORATION_PLUGIN_MANAGEMENT})
public class PluginPackageDataModelController {

    @Autowired
    private PluginPackageDataModelServiceImpl pluginPackageDataModelService;

    @GetMapping("/models")
    public CommonResponseDto allDataModels() {
        return CommonResponseDto.okayWithData(pluginPackageDataModelService.overview());
    }

    @GetMapping("/packages/{package-name}/models")
    public CommonResponseDto getDataModelByPackageName(@PathVariable(value = "package-name") String packageName) {
        return CommonResponseDto.okayWithData(pluginPackageDataModelService.packageView(packageName));
    }

    @GetMapping("/models/package/{plugin-package-name:.+}")
    public CommonResponseDto pullDynamicDataModel(@PathVariable(value = "plugin-package-name") String packageName) {
        return CommonResponseDto.okayWithData(pluginPackageDataModelService.pullDynamicDataModel(packageName));
    }

    @GetMapping("/models/package/{plugin-package-name}/entity/{entity-name}/refById")
    public CommonResponseDto getRefByIdInfoByPackageNameAndEntityName(
            @PathVariable(value = "plugin-package-name") String packageName,
            @PathVariable(value = "entity-name") String entityName) {
        return CommonResponseDto.okayWithData(pluginPackageDataModelService.getRefByInfo(packageName, entityName));
    }

    @PostMapping("/models")
    public CommonResponseDto applyNewDataModel(@RequestBody PluginPackageDataModelDto dataModelDto) {
        return CommonResponseDto.okayWithData(pluginPackageDataModelService.register(dataModelDto, true));
    }

    @GetMapping("/models/package/{plugin-package-name}/entity/{entity-name}/attributes")
    public CommonResponseDto getAttributeInfoByPackageNameAndEntityName(
            @PathVariable(value = "plugin-package-name") String packageName,
            @PathVariable(value = "entity-name") String entityName) {
        List<PluginPackageAttributeDto> result;
        result = pluginPackageDataModelService.entityView(packageName, entityName);
        return CommonResponseDto.okayWithData(result);
    }

    @GetMapping("/models/package/{plugin-package-name}/entity/{entity-name}")
    public CommonResponseDto getEntityInfoByPackageNameAndEntityName(
            @PathVariable(value = "plugin-package-name") String packageName,
            @PathVariable(value = "entity-name") String entityName) {
        DataModelEntityDto result = pluginPackageDataModelService.getEntityByPackageNameAndName(packageName,
                entityName);
        return CommonResponseDto.okayWithData(result);
    }
}
