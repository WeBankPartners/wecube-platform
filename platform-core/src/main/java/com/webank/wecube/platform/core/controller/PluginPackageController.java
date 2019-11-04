package com.webank.wecube.platform.core.controller;

import com.webank.wecube.platform.core.commons.ApplicationProperties.PluginProperties;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.JsonResponse;
import com.webank.wecube.platform.core.domain.plugin.PluginPackage;
import com.webank.wecube.platform.core.dto.PluginPackageDependencyDto;
import com.webank.wecube.platform.core.dto.MenuItemDto;
import com.webank.wecube.platform.core.service.plugin.PluginConfigService;
import com.webank.wecube.platform.core.domain.SystemVariable;
import com.webank.wecube.platform.core.domain.plugin.*;
import com.webank.wecube.platform.core.dto.PluginPackageRuntimeResouceDto;
import com.webank.wecube.platform.core.service.plugin.PluginPackageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.util.List;
import java.util.Set;

import static com.webank.wecube.platform.core.domain.JsonResponse.okay;
import static com.webank.wecube.platform.core.domain.JsonResponse.error;
import static com.webank.wecube.platform.core.domain.JsonResponse.okayWithData;

@RestController
@RequestMapping("/v1/api")
public class PluginPackageController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private PluginProperties pluginProperties;
    @Autowired
    private PluginConfigService pluginConfigService;

    @Autowired
    private PluginPackageService pluginPackageService;

    @PostMapping("/packages")
    @ResponseBody
    public JsonResponse uploadPluginPackage(@RequestParam(value = "zip-file") MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("zip-file required.");

        PluginPackage pluginPackage = pluginPackageService.uploadPackage(file);
        return okayWithData(pluginPackage);
    }

    @GetMapping("/packages")
    @ResponseBody
    public JsonResponse getAllPluginPackages() {
        Iterable<PluginPackage> pluginPackages = pluginPackageService.getPluginPackages();
        return okayWithData(pluginPackages);
    }

    @PostMapping("/packages/register/{package-id}")
    @ResponseBody
    public JsonResponse registerPluginPackage(@PathVariable(value = "package-id") int packageId) {
        PluginPackage pluginPackage = null;
        try {
            pluginPackage = pluginPackageService.registerPluginPackage(packageId);
        } catch (Exception e) {
            return error(String.format("Failed to register plugin package with error message [%s]", e.getMessage()));
        }
        return okayWithData(pluginPackage);
    }

    @PostMapping("/packages/decommission/{package-id}")
    @ResponseBody
    public JsonResponse decommissionPluginPackage(@PathVariable(value = "package-id") int packageId) {
        try {
            pluginPackageService.decommissionPluginPackage(packageId);
        } catch (Exception e) {
            return error(String.format("Failed to decommission plugin package with error message [%s]", e.getMessage()));
        }
        return okay();
    }

    @GetMapping("/packages/{id}/dependencies")
    @ResponseBody
    public JsonResponse getDependenciesById(@PathVariable(value = "id") Integer packageId) {
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
    public JsonResponse getMenusById(@PathVariable(value = "id") Integer packageId) {
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
    public JsonResponse getSystemParamsById(@PathVariable(value = "id") Integer packageId) {
        Set<SystemVariable> systemVariableSet;
        try {
            systemVariableSet = pluginPackageService.getSystemVarsById(packageId);
        } catch (WecubeCoreException ex) {
            return error(ex.getMessage());
        }
        return okayWithData(systemVariableSet);
    }

    @GetMapping("/packages/{id}/authorities")
    @ResponseBody
    public JsonResponse getAuthorityById(@PathVariable(value = "id") Integer packageId) {
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
    public JsonResponse getResourceById(@PathVariable(value = "id") Integer packageId) {
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
    public JsonResponse getPluginsById(@PathVariable(value = "id") Integer packageId) {
        Set<PluginConfig> pluginConfigSet;
        try {
            pluginConfigSet = pluginPackageService.getPluginsById(packageId);
        } catch (WecubeCoreException ex) {
            return error(ex.getMessage());
        }
        return okayWithData(pluginConfigSet);
    }

}



