package com.webank.wecube.platform.core.controller;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.JsonResponse;
import com.webank.wecube.platform.core.domain.SystemVariable;
import com.webank.wecube.platform.core.domain.plugin.PluginPackage;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageAuthority;
import com.webank.wecube.platform.core.dto.MenuItemDto;
import com.webank.wecube.platform.core.dto.PluginPackageDependencyDto;
import com.webank.wecube.platform.core.dto.PluginPackageRuntimeResouceDto;
import com.webank.wecube.platform.core.service.plugin.PluginPackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

import static com.webank.wecube.platform.core.domain.JsonResponse.*;

@RestController
@RequestMapping("/v1")
public class PluginPackageController {

    @Autowired
    private PluginPackageService pluginPackageService;

    @PostMapping("/packages")
    @ResponseBody
    public JsonResponse uploadPluginPackage(@RequestParam(value = "zip-file") MultipartFile file) throws Exception {
        if (file == null || file.isEmpty())
            throw new IllegalArgumentException("zip-file required.");

        PluginPackage pluginPackage = pluginPackageService.uploadPackage(file);
        return okayWithData(pluginPackage);
    }

    @GetMapping("/packages")
    @ResponseBody
    public JsonResponse getAllPluginPackages(
            @RequestParam(value = "distinct", required = false, defaultValue = "false") boolean ifDistinct) {
        if (ifDistinct) {
            return okayWithData(pluginPackageService.getAllDistinctPluginPackageNameList());
        } else {
            return okayWithData(pluginPackageService.getPluginPackages());
        }

    }

    @PostMapping("/packages/register/{package-id:.+}")
    @ResponseBody
    public JsonResponse registerPluginPackage(@PathVariable(value = "package-id") String packageId) {
        PluginPackage pluginPackage = null;
        try {
            pluginPackage = pluginPackageService.registerPluginPackage(packageId);
        } catch (Exception e) {
            return error(String.format("Failed to register plugin package with error message [%s]", e.getMessage()));
        }
        return okayWithData(pluginPackage);
    }

    @PostMapping("/packages/decommission/{package-id:.+}")
    @ResponseBody
    public JsonResponse decommissionPluginPackage(@PathVariable(value = "package-id") String packageId) {
        try {
            pluginPackageService.decommissionPluginPackage(packageId);
        } catch (Exception e) {
            return error(
                    String.format("Failed to decommission plugin package with error message [%s]", e.getMessage()));
        }
        return okay();
    }

    @GetMapping("/packages/{id}/dependencies")
    @ResponseBody
    public JsonResponse getDependenciesById(@PathVariable(value = "id") String packageId) {
        PluginPackageDependencyDto dependencySetFoundById;
        try {
            dependencySetFoundById = pluginPackageService.getDependenciesById(packageId);
        } catch (WecubeCoreException ex) {
            return error(ex.getMessage());
        }
        return okayWithData(dependencySetFoundById);
    }

    @GetMapping("/packages/{id}/menus")
    @ResponseBody
    public JsonResponse getMenusById(@PathVariable(value = "id") String packageId) {
        List<MenuItemDto> menuList;
        try {
            menuList = pluginPackageService.getMenusById(packageId);
        } catch (WecubeCoreException ex) {
            return error(ex.getMessage());
        }
        return okayWithData(menuList);
    }

    @GetMapping("/packages/{id}/system-parameters")
    @ResponseBody
    public JsonResponse getSystemParamsById(@PathVariable(value = "id") String packageId) {
        List<SystemVariable> systemVariableSet;
        try {
            systemVariableSet = pluginPackageService.getSystemVarsById(packageId);
        } catch (WecubeCoreException ex) {
            return error(ex.getMessage());
        }
        return okayWithData(systemVariableSet);
    }

    @GetMapping("/packages/{id}/authorities")
    @ResponseBody
    public JsonResponse getAuthorityById(@PathVariable(value = "id") String packageId) {
        Set<PluginPackageAuthority> authoritySet;
        try {
            authoritySet = pluginPackageService.getAuthoritiesById(packageId);
        } catch (WecubeCoreException ex) {
            return error(ex.getMessage());
        }
        return okayWithData(authoritySet);
    }

    @GetMapping("/packages/{id}/runtime-resources")
    @ResponseBody
    public JsonResponse getResourceById(@PathVariable(value = "id") String packageId) {
        PluginPackageRuntimeResouceDto resouceFoundById;
        try {
            resouceFoundById = pluginPackageService.getResourcesById(packageId);
        } catch (WecubeCoreException ex) {
            return error(ex.getMessage());
        }
        return okayWithData(resouceFoundById);
    }

    @GetMapping("/packages/{id}/plugins")
    @ResponseBody
    public JsonResponse getPluginsById(@PathVariable(value = "id") String packageId) {
        return okayWithData(pluginPackageService.getPluginConfigsByPackageId(packageId, true));
    }

    @GetMapping("/packages/{id}/plugin-configs")
    @ResponseBody
    public JsonResponse getPluginConfigsByPackageId(@PathVariable(value = "id") String packageId) {
        return okayWithData(pluginPackageService.getPluginConfigsByPackageId(packageId, false));
    }

}
