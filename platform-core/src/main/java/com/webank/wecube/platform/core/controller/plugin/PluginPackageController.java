package com.webank.wecube.platform.core.controller.plugin;

import static com.webank.wecube.platform.core.dto.plugin.CommonResponseDto.okay;
import static com.webank.wecube.platform.core.dto.plugin.CommonResponseDto.okayWithData;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.webank.wecube.platform.core.dto.plugin.CommonResponseDto;
import com.webank.wecube.platform.core.dto.plugin.MenuItemDto;
import com.webank.wecube.platform.core.dto.plugin.PluginDeclarationDto;
import com.webank.wecube.platform.core.dto.plugin.PluginPackageAuthoritiesDto;
import com.webank.wecube.platform.core.dto.plugin.PluginPackageDependencyDto;
import com.webank.wecube.platform.core.dto.plugin.PluginPackageInfoDto;
import com.webank.wecube.platform.core.dto.plugin.PluginPackageRuntimeResouceDto;
import com.webank.wecube.platform.core.dto.plugin.S3PluginActifactDto;
import com.webank.wecube.platform.core.dto.plugin.SystemVariableDto;
import com.webank.wecube.platform.core.dto.plugin.UploadPackageResultDto;
import com.webank.wecube.platform.core.service.plugin.PluginArtifactsMgmtService;
import com.webank.wecube.platform.core.service.plugin.PluginPackageMgmtService;

@RestController
@RequestMapping("/v1")
public class PluginPackageController {

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
    public CommonResponseDto fetchAllPluginPackages(
            @RequestParam(value = "distinct", required = false, defaultValue = "false") boolean ifDistinct) {
        if (ifDistinct) {
            return okayWithData(pluginPackageMgmtService.getDistinctPluginPackages());
        } else {
            return okayWithData(pluginPackageMgmtService.fetchAllPluginPackages());
        }

    }

    /**
     * 
     * @param packageId
     * @return
     */
    @PostMapping("/packages/register/{package-id:.+}")
    public CommonResponseDto registerPluginPackage(@PathVariable(value = "package-id") String packageId) {
        PluginPackageInfoDto pluginPackage = pluginPackageMgmtService.registerPluginPackage(packageId);
        return okayWithData(pluginPackage);
    }

    /**
     * 
     * @param packageId
     * @return
     */
    @PostMapping("/packages/decommission/{package-id:.+}")
    public CommonResponseDto decommissionPluginPackage(@PathVariable(value = "package-id") String packageId) {
        pluginPackageMgmtService.decommissionPluginPackage(packageId);
        return okay();
    }

    /**
     * 
     * @param packageId
     * @return
     */
    @GetMapping("/packages/{package-id}/dependencies")
    public CommonResponseDto fetchPluginPackageDependencies(@PathVariable(value = "package-id") String packageId) {
        PluginPackageDependencyDto dependencyDto = pluginPackageMgmtService.fetchPluginPackageDependencies(packageId);
        return okayWithData(dependencyDto);
    }

    /**
     * 
     * @param packageId
     * @return
     */
    @GetMapping("/packages/{package-id}/menus")
    public CommonResponseDto getMenusByPackageId(@PathVariable(value = "package-id") String packageId) {
        List<MenuItemDto> menuList = pluginPackageMgmtService.getMenusByPackageId(packageId);
        return okayWithData(menuList);
    }

    /**
     * 
     * @param packageId
     * @return
     */
    @GetMapping("/packages/{package-id}/system-parameters")
    public CommonResponseDto getSystemParamsByPackageId(@PathVariable(value = "package-id") String packageId) {
        List<SystemVariableDto> systemVariableSet = pluginPackageMgmtService.getSystemVarsByPackageId(packageId);
        return okayWithData(systemVariableSet);
    }

    /**
     * 
     * @param packageId
     * @return
     */
    @GetMapping("/packages/{package-id}/authorities")
    public CommonResponseDto getAuthorityByPackageId(@PathVariable(value = "package-id") String packageId) {
        List<PluginPackageAuthoritiesDto> authoritySet = pluginPackageMgmtService.getAuthoritiesByPackageId(packageId);
        return okayWithData(authoritySet);
    }

    /**
     * 
     * @param packageId
     * @return
     */
    @GetMapping("/packages/{package-id}/runtime-resources")
    public CommonResponseDto getResourceByPackageId(@PathVariable(value = "package-id") String packageId) {
        PluginPackageRuntimeResouceDto resouceFoundById = pluginPackageMgmtService.getResourcesByPackageId(packageId);
        return okayWithData(resouceFoundById);
    }

    /**
     * 
     * @param packageId
     * @return
     */
    @GetMapping("/packages/{package-id}/plugins")
    public CommonResponseDto getRichPluginConfigsByPackageId(@PathVariable(value = "package-id") String packageId) {
        return okayWithData(pluginPackageMgmtService.getRichPluginConfigsByPackageId(packageId));
    }

    /**
     * 
     * @param packageId
     * @return
     */
    @GetMapping("/packages/{package-id}/plugin-configs")
    public CommonResponseDto getPluginConfigsByPackageId(@PathVariable(value = "package-id") String packageId) {
        return okayWithData(pluginPackageMgmtService.getPluginConfigsByPackageId(packageId));
    }

    /**
     * 
     * @param packageId
     * @return
     */
    @GetMapping("/packages/{package-id}/plugin-config-outlines")
    public CommonResponseDto getPluginConfigOutlinesByPackageId(@PathVariable(value = "package-id") String packageId) {
        return okayWithData(pluginPackageMgmtService.getPluginConfigOutlinesByPackageId(packageId));
    }

    /**
     * 
     * @param packageId
     * @param pluginDeclarationDtos
     * @return
     */
    @PostMapping("/packages/{package-id}/plugin-configs/enable-in-batch")
    public CommonResponseDto enablePluginConfigInBatch(@PathVariable(value = "package-id") String packageId,
            @RequestBody List<PluginDeclarationDto> pluginDeclarationDtos) {
        pluginPackageMgmtService.enablePluginConfigsInBatchByPackageId(packageId, pluginDeclarationDtos);
        return okay();
    }

}
