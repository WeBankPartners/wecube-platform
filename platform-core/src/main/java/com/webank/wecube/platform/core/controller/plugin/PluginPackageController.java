package com.webank.wecube.platform.core.controller.plugin;

import static com.webank.wecube.platform.core.dto.CommonResponseDto.error;
import static com.webank.wecube.platform.core.dto.CommonResponseDto.okay;
import static com.webank.wecube.platform.core.dto.CommonResponseDto.okayWithData;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.SystemVariable;
import com.webank.wecube.platform.core.domain.plugin.PluginPackage;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageAuthority;
import com.webank.wecube.platform.core.dto.CommonResponseDto;
import com.webank.wecube.platform.core.dto.MenuItemDto;
import com.webank.wecube.platform.core.dto.PluginPackageDependencyDto;
import com.webank.wecube.platform.core.dto.PluginPackageRuntimeResouceDto;
import com.webank.wecube.platform.core.dto.S3PluginActifactDto;
import com.webank.wecube.platform.core.service.plugin.PluginPackageService;

@RestController
@RequestMapping("/v1")
public class PluginPackageController {

    @Autowired
    private PluginPackageService pluginPackageService;

    @GetMapping("/plugin-artifacts")
    public CommonResponseDto listS3PluginActifacts() {
        return okayWithData(pluginPackageService.listS3PluginActifacts());
    }

    @PostMapping("/plugin-artifacts/pull-requests")
    public CommonResponseDto createS3PluginActifactPullRequest(@RequestBody S3PluginActifactDto pullRequestDto) {
        return okayWithData(pluginPackageService.createS3PluginActifactPullRequest(pullRequestDto));
    }

    @GetMapping("/plugin-artifacts/pull-requests/{request-id}")
    public CommonResponseDto queryS3PluginActifactPullRequest(@PathVariable(value = "request-id") String requestId) {
        return okayWithData(pluginPackageService.queryS3PluginActifactPullRequest(requestId));
    }

    @PostMapping("/packages")
    public CommonResponseDto uploadPluginPackage(@RequestParam(value = "zip-file") MultipartFile file) throws Exception {
        if (file == null || file.isEmpty())
            throw new IllegalArgumentException("zip-file required.");

        PluginPackage pluginPackage = pluginPackageService.uploadPackage(file);
        return okayWithData(pluginPackage);
    }

    @GetMapping("/packages")
    public CommonResponseDto getAllPluginPackages(
            @RequestParam(value = "distinct", required = false, defaultValue = "false") boolean ifDistinct) {
        if (ifDistinct) {
            return okayWithData(pluginPackageService.getAllDistinctPluginPackageNameList());
        } else {
            return okayWithData(pluginPackageService.getPluginPackages());
        }

    }

    @PostMapping("/packages/register/{package-id:.+}")
    public CommonResponseDto registerPluginPackage(@PathVariable(value = "package-id") String packageId) {
        PluginPackage pluginPackage = null;
        try {
            pluginPackage = pluginPackageService.registerPluginPackage(packageId);
        } catch (Exception e) {
            return error(String.format("Failed to register plugin package with error message [%s]", e.getMessage()));
        }
        return okayWithData(pluginPackage);
    }

    @PostMapping("/packages/decommission/{package-id:.+}")
    public CommonResponseDto decommissionPluginPackage(@PathVariable(value = "package-id") String packageId) {
        try {
            pluginPackageService.decommissionPluginPackage(packageId);
        } catch (Exception e) {
            return error(
                    String.format("Failed to decommission plugin package with error message [%s]", e.getMessage()));
        }
        return okay();
    }

    @GetMapping("/packages/{id}/dependencies")
    public CommonResponseDto getDependenciesById(@PathVariable(value = "id") String packageId) {
        PluginPackageDependencyDto dependencySetFoundById;
        try {
            dependencySetFoundById = pluginPackageService.getDependenciesById(packageId);
        } catch (WecubeCoreException ex) {
            return error(ex.getMessage());
        }
        return okayWithData(dependencySetFoundById);
    }

    @GetMapping("/packages/{id}/menus")
    public CommonResponseDto getMenusById(@PathVariable(value = "id") String packageId) {
        List<MenuItemDto> menuList;
        try {
            menuList = pluginPackageService.getMenusById(packageId);
        } catch (WecubeCoreException ex) {
            return error(ex.getMessage());
        }
        return okayWithData(menuList);
    }

    @GetMapping("/packages/{id}/system-parameters")
    public CommonResponseDto getSystemParamsById(@PathVariable(value = "id") String packageId) {
        List<SystemVariable> systemVariableSet;
        try {
            systemVariableSet = pluginPackageService.getSystemVarsById(packageId);
        } catch (WecubeCoreException ex) {
            return error(ex.getMessage());
        }
        return okayWithData(systemVariableSet);
    }

    @GetMapping("/packages/{id}/authorities")
    public CommonResponseDto getAuthorityById(@PathVariable(value = "id") String packageId) {
        Set<PluginPackageAuthority> authoritySet;
        try {
            authoritySet = pluginPackageService.getAuthoritiesById(packageId);
        } catch (WecubeCoreException ex) {
            return error(ex.getMessage());
        }
        return okayWithData(authoritySet);
    }

    @GetMapping("/packages/{id}/runtime-resources")
    public CommonResponseDto getResourceById(@PathVariable(value = "id") String packageId) {
        PluginPackageRuntimeResouceDto resouceFoundById;
        try {
            resouceFoundById = pluginPackageService.getResourcesById(packageId);
        } catch (WecubeCoreException ex) {
            return error(ex.getMessage());
        }
        return okayWithData(resouceFoundById);
    }

    @GetMapping("/packages/{id}/plugins")
    public CommonResponseDto getPluginsById(@PathVariable(value = "id") String packageId) {
        return okayWithData(pluginPackageService.getPluginConfigsByPackageId(packageId, true));
    }

    @GetMapping("/packages/{id}/plugin-configs")
    public CommonResponseDto getPluginConfigsByPackageId(@PathVariable(value = "id") String packageId) {
        return okayWithData(pluginPackageService.getPluginConfigsByPackageId(packageId, false));
    }

}
