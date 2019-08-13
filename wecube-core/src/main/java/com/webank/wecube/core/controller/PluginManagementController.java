package com.webank.wecube.core.controller;

import com.webank.wecube.core.commons.ApplicationProperties.PluginProperties;
import com.webank.wecube.core.commons.WecubeCoreException;
import com.webank.wecube.core.domain.JsonResponse;
import com.webank.wecube.core.domain.plugin.*;
import com.webank.wecube.core.dto.CreateInstanceDto;
import com.webank.wecube.core.service.PluginPackageService;
import com.webank.wecube.core.support.plugin.dto.PluginRequest.PluginLoggingInfoSearchDetailRequest;
import com.webank.wecube.core.service.PluginConfigService;
import com.webank.wecube.core.service.PluginInstanceService;
import com.webank.wecube.core.support.plugin.dto.PortalRequestBody.SearchPluginLogRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.webank.wecube.core.domain.plugin.PluginInstance;


import javax.annotation.security.RolesAllowed;
import java.util.List;

import static com.webank.wecube.core.domain.JsonResponse.okay;
import static com.webank.wecube.core.domain.JsonResponse.okayWithData;
import static com.webank.wecube.core.domain.MenuItem.MENU_COLLABORATION_PLUGIN_MANAGEMENT;

@RestController
@Slf4j
@RequestMapping("/plugin")
@RolesAllowed({MENU_COLLABORATION_PLUGIN_MANAGEMENT})
public class PluginManagementController {

    @Autowired
    private PluginProperties pluginProperties;
    @Autowired
    private PluginConfigService pluginConfigService;
    @Autowired
    private PluginInstanceService pluginInstanceService;
    @Autowired
    private PluginPackageService pluginPackageService;

    @PostMapping("/upload")
    @ResponseBody
    public JsonResponse uploadPluginPackage(@RequestParam(value = "zip-file") MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("zip-file required.");
        PluginPackage pluginPackage = pluginPackageService.uploadPackage(file.getInputStream(), file.getOriginalFilename());
        return okay().withData(pluginPackage);
    }

    @GetMapping("/packages")
    @ResponseBody
    public JsonResponse getAllPluginPackages() {
        Iterable<PluginPackage> pluginPackages = pluginConfigService.getPluginPackages();
        return okayWithData(pluginPackages);
    }

    @DeleteMapping("/packages/{package-id}")
    @ResponseBody
    public JsonResponse deletePluginPackage(@PathVariable(value = "package-id") int packageId) {
        pluginPackageService.deletePluginPackage(packageId);
        return okay();
    }

    @PostMapping("/packages/{package-id}/preconfigure")
    @ResponseBody
    public JsonResponse preconfigurePluginPackage(@PathVariable(value = "package-id") int packageId) {
        pluginPackageService.preconfigurePluginPackage(packageId);
        return okay();
    }

    @GetMapping("/configs/{config-id}/interfaces")
    @ResponseBody
    public JsonResponse getPluginConfigInterfacesByConfigId(@PathVariable(value = "config-id") int configId) {
        List<PluginConfigInterface> pluginConfigInterfaces = pluginConfigService.getPluginConfigInterfaces(configId);
        return okayWithData(pluginConfigInterfaces);
    }

    @GetMapping("/latest-online-interfaces")
    @ResponseBody
    public JsonResponse getLatestOnlinePluginInterfaces(@RequestParam(value = "ci-type-id", required = false) Integer ciTypeId) {
        List<PluginConfigInterface> pluginConfigInterfaces = pluginConfigService.getLatestOnlinePluginInterfaces(ciTypeId);
        return okayWithData(pluginConfigInterfaces);
    }

    @GetMapping("/configs/{config-id}/filtering-rules")
    @ResponseBody
    public JsonResponse getPluginConfigFilteringRulesByConfigId(@PathVariable(value = "config-id") int configId) {
        Iterable<PluginConfigFilteringRule> pluginConfigFilteringRules = pluginConfigService.getPluginConfigFilteringRules(configId);
        return okayWithData(pluginConfigFilteringRules);
    }

    @PostMapping("/configs/{config-id}/release")
    @ResponseBody
    public JsonResponse releasePluginConfig(@PathVariable(value = "config-id") int configId) {
        pluginConfigService.releasePluginConfig(configId);
        return okay();
    }

    @PostMapping("/configs/{config-id}/decommission")
    @ResponseBody
    public JsonResponse decommissionPluginConfig(@PathVariable(value = "config-id") int configId) {
        pluginConfigService.decommissionPluginConfig(configId);
        return okay();
    }

    @PostMapping("/configs/{config-id}/save")
    @ResponseBody
    public JsonResponse savePluginConfig(@PathVariable(value = "config-id") int configId
            , @RequestParam int cmdbCiTypeId
            , @RequestParam String cmdbCiTypeName
            , @RequestBody PluginRegisteringModel pluginRegisteringModel) {
        pluginConfigService.savePluginConfig(configId, cmdbCiTypeId, cmdbCiTypeName, pluginRegisteringModel);
        return okay();
    }

    @GetMapping("/available-container-hosts")
    @ResponseBody
    public JsonResponse getAvailableContainerHosts() {
        return okay().withData(pluginProperties.getPluginHosts());
    }

    @GetMapping("/hosts/{host-ip}/next-available-port")
    @ResponseBody
    public JsonResponse getAvailablePortByHostIp(@PathVariable(value = "host-ip") String hostIp) {
        return okayWithData(pluginInstanceService.getAvailablePortByHostIp(hostIp));
    }

    @PostMapping("/packages/{package-id}/hosts/{host-ip}/ports/{port}/instance/launch")
    @ResponseBody
    public JsonResponse createPluginInstanceByPackageIdAndHostIp(@PathVariable(value = "package-id") int packageId,
                                                                 @PathVariable(value = "host-ip") String hostIp,
                                                                 @PathVariable(value = "port") int port,
                                                                 @RequestBody CreateInstanceDto createContainerParameters) {
        try {
            pluginInstanceService.createPluginInstance(packageId, hostIp, port, createContainerParameters.getAdditionalCreateContainerParameters());
        } catch (Exception e) {
            throw new WecubeCoreException("Create plugin package instance failed. Error is "+ e.getMessage());
        }
        return okay();
    }

    @DeleteMapping("/packages/instances/{instance-id}")
    @ResponseBody
    public JsonResponse removePluginInstance(@PathVariable(value = "instance-id") int instanceId) {

        log.info("instanceId={}", instanceId);
        try {
            pluginInstanceService.removePluginInstanceById(instanceId);
        } catch (Exception e) {
            log.info("Remove plugin package instance failed. Meet error: {}", e);
            throw new WecubeCoreException("Remove plugin package instance failed.");
        }
        return okay();
    }

    @GetMapping("/packages/instances")
    @ResponseBody
    public JsonResponse getAllInstances() {
        List<PluginInstance> allInstances = pluginInstanceService.getAllInstances();
        return okayWithData(allInstances);
    }


    @GetMapping("/packages/{package-id}/instances")
    @ResponseBody
    public JsonResponse getAvailableInstancesByPackageId(@PathVariable(value = "package-id") int packageId) {
        List<PluginInstance> allInstances = pluginInstanceService.getAvailableInstancesByPackageId(packageId);
        return okayWithData(allInstances);
    }

    @PostMapping("/packages/instances/log")
    @ResponseBody
    public JsonResponse getPluginInstanceLog(@RequestBody SearchPluginLogRequest request) {
        return okayWithData(pluginInstanceService.getPluginsLog(request));
    }

    @PostMapping("/packages/instances/{instance-id}/log-detail")
    @ResponseBody
    public JsonResponse getPluginInstanceLogDetail(@RequestBody PluginLoggingInfoSearchDetailRequest request, @PathVariable(value = "instance-id") int instanceId) {
        return okayWithData(pluginInstanceService.getPluginLogDetail(request, instanceId));
    }

}



