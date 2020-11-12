package com.webank.wecube.platform.core.controller.plugin;

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
import com.webank.wecube.platform.core.domain.plugin.PluginPackageAuthority;
import com.webank.wecube.platform.core.dto.CommonResponseDto;
import com.webank.wecube.platform.core.dto.MenuItemDto;
import com.webank.wecube.platform.core.dto.PluginDeclarationDto;
import com.webank.wecube.platform.core.dto.PluginPackageDependencyDto;
import com.webank.wecube.platform.core.dto.PluginPackageInfoDto;
import com.webank.wecube.platform.core.dto.PluginPackageRuntimeResouceDto;
import com.webank.wecube.platform.core.dto.S3PluginActifactDto;
import com.webank.wecube.platform.core.dto.plugin.UploadPackageResultDto;
import com.webank.wecube.platform.core.service.plugin.PluginArtifactsMgmtService;
import com.webank.wecube.platform.core.service.plugin.PluginPackageMgmtService;
import com.webank.wecube.platform.core.service.plugin.PluginPackageService;

@RestController
@RequestMapping("/v1")
public class PluginPackageController {

    @Autowired
    private PluginPackageService pluginPackageService;
    
    @Autowired
    private PluginPackageMgmtService pluginPackageMgmtService;
    
    @Autowired
    private PluginArtifactsMgmtService pluginArtifactsMgmtService;

    /**
     * 
     * @return
     */
    @GetMapping("/plugin-artifacts")
    public CommonResponseDto listS3PluginActifacts() {
        return okayWithData(pluginArtifactsMgmtService.listS3PluginActifacts());
    }

    /**
     * 
     * @param pullRequestDto
     * @return
     */
    @PostMapping("/plugin-artifacts/pull-requests")
    public CommonResponseDto createS3PluginActifactPullRequest(@RequestBody S3PluginActifactDto pullRequestDto) {
        return okayWithData(pluginArtifactsMgmtService.createS3PluginActifactPullRequest(pullRequestDto));
    }

    /**
     * 
     * @param requestId
     * @return
     */
    @GetMapping("/plugin-artifacts/pull-requests/{request-id}")
    public CommonResponseDto queryS3PluginActifactPullRequest(@PathVariable(value = "request-id") String requestId) {
        return okayWithData(pluginArtifactsMgmtService.queryS3PluginActifactPullRequest(requestId));
    }

    /**
     * 
     * @param file
     * @return
     * @throws Exception
     */
    @PostMapping("/packages")
    public CommonResponseDto uploadPluginPackage(@RequestParam(value = "zip-file") MultipartFile file)
            throws Exception {
        if (file == null || file.isEmpty())
            throw new IllegalArgumentException("zip-file required.");

        UploadPackageResultDto result = pluginArtifactsMgmtService.uploadPackage(file);
        return okayWithData(result);
    }

    /**
     * 
     * @param ifDistinct
     * @return
     */
    @GetMapping("/packages")
    public CommonResponseDto getAllPluginPackages(
            @RequestParam(value = "distinct", required = false, defaultValue = "false") boolean ifDistinct) {
        if (ifDistinct) {
            return okayWithData(pluginPackageMgmtService.getDistinctPluginPackages());
        } else {
            return okayWithData(pluginPackageMgmtService.getPluginPackages());
        }

    }

    @PostMapping("/packages/register/{package-id:.+}")
    public CommonResponseDto registerPluginPackage(@PathVariable(value = "package-id") String packageId) {
        try {
            PluginPackageInfoDto pluginPackage = pluginPackageMgmtService.registerPluginPackage(packageId);
            return okayWithData(pluginPackage);
        } catch (Exception e) {
            String msg = String.format("Failed to register plugin package with error message [%s]", e.getMessage());
            throw new WecubeCoreException("3307", msg, e.getMessage());
        }
    }

    @PostMapping("/packages/decommission/{package-id:.+}")
    public CommonResponseDto decommissionPluginPackage(@PathVariable(value = "package-id") String packageId) {
        try {
            pluginPackageService.decommissionPluginPackage(packageId);
        } catch (Exception e) {
            String msg = String.format("Failed to decommission plugin package with error message [%s]", e.getMessage());
            throw new WecubeCoreException("3308", msg, e.getMessage());
        }
        return okay();
    }

    @GetMapping("/packages/{id}/dependencies")
    public CommonResponseDto getDependenciesByPackageId(@PathVariable(value = "id") String packageId) {
        PluginPackageDependencyDto dependencySetFoundById;
        dependencySetFoundById = pluginPackageService.getDependenciesById(packageId);
        return okayWithData(dependencySetFoundById);
    }

    @GetMapping("/packages/{id}/menus")
    public CommonResponseDto getMenusByPackageId(@PathVariable(value = "id") String packageId) {
        List<MenuItemDto> menuList;
        menuList = pluginPackageService.getMenusById(packageId);
        return okayWithData(menuList);
    }

    @GetMapping("/packages/{id}/system-parameters")
    public CommonResponseDto getSystemParamsByPackageId(@PathVariable(value = "id") String packageId) {
        List<SystemVariable> systemVariableSet;
        systemVariableSet = pluginPackageService.getSystemVarsById(packageId);
        return okayWithData(systemVariableSet);
    }

    @GetMapping("/packages/{id}/authorities")
    public CommonResponseDto getAuthorityByPackageId(@PathVariable(value = "id") String packageId) {
        Set<PluginPackageAuthority> authoritySet;
        authoritySet = pluginPackageService.getAuthoritiesById(packageId);
        return okayWithData(authoritySet);
    }

    @GetMapping("/packages/{id}/runtime-resources")
    public CommonResponseDto getResourceByPackageId(@PathVariable(value = "id") String packageId) {
        PluginPackageRuntimeResouceDto resouceFoundById;
        resouceFoundById = pluginPackageService.getResourcesById(packageId);
        return okayWithData(resouceFoundById);
    }

    @GetMapping("/packages/{id}/plugins")
    public CommonResponseDto getPluginsByPackageId(@PathVariable(value = "id") String packageId) {
        return okayWithData(pluginPackageService.getPluginConfigsByPackageId(packageId, true));
    }

    @GetMapping("/packages/{id}/plugin-configs")
    public CommonResponseDto getPluginConfigsByPackageId(@PathVariable(value = "id") String packageId) {
        return okayWithData(pluginPackageService.getPluginConfigsByPackageId(packageId, false));
    }

    @GetMapping("/packages/{package-id}/plugin-config-outlines")
    public CommonResponseDto getPluginConfigOutlinesByPackageId(@PathVariable(value = "package-id") String packageId) {
        return okayWithData(pluginPackageService.getPluginConfigOutlinesByPackageId(packageId));
    }

    @PostMapping("/packages/{package-id}/plugin-configs/enable-in-batch")
    public CommonResponseDto enablePluginConfigInBatch(@PathVariable(value = "package-id") String packageId,
            @RequestBody List<PluginDeclarationDto> pluginDeclarationDtos) {
        pluginPackageService.enablePluginConfigInBatchByPackageId(packageId, pluginDeclarationDtos);
        return okay();
    }

}
