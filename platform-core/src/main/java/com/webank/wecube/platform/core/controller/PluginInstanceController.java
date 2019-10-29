package com.webank.wecube.platform.core.controller;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.JsonResponse;
import com.webank.wecube.platform.core.dto.CreateInstanceDto;
import com.webank.wecube.platform.core.service.PluginInstanceService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.webank.wecube.platform.core.domain.plugin.PluginInstance;

import java.util.List;

import static com.webank.wecube.platform.core.domain.JsonResponse.okay;
import static com.webank.wecube.platform.core.domain.JsonResponse.okayWithData;

@RestController
@Slf4j
@RequestMapping("/v1/api")
public class PluginInstanceController {

    @Autowired
    private PluginInstanceService pluginInstanceService;

    @GetMapping("/available-container-hosts")
    @ResponseBody
    public JsonResponse getAvailableContainerHosts() {
        return okayWithData(pluginInstanceService.getAvailableContainerHosts());
    }

    @GetMapping("/hosts/{host-ip}/next-available-port")
    @ResponseBody
    public JsonResponse getAvailablePortByHostIp(@PathVariable(value = "host-ip") String hostIp) {
        return okayWithData(pluginInstanceService.getAvailablePortByHostIp(hostIp));
    }

    @PostMapping("/packages/{package-id}/hosts/{host-ip}/ports/{port}/instance/launch")
    @ResponseBody
    public JsonResponse createPluginInstanceByPackageIdAndHostIp(@PathVariable(value = "package-id") int packageId,
            @PathVariable(value = "host-ip") String hostIp, @PathVariable(value = "port") int port) {
        try {
            pluginInstanceService.launchPluginInstance(packageId, hostIp, port);
        } catch (Exception e) {
            throw new WecubeCoreException("Create plugin instance failed. Error is " + e.getMessage());
        }
        return okay();
    }

    @DeleteMapping("/packages/instances/{instance-id}")
    @ResponseBody
    public JsonResponse removePluginInstance(@PathVariable(value = "instance-id") int instanceId) {
        try {
            pluginInstanceService.removePluginInstanceById(instanceId);
        } catch (Exception e) {
            log.info("Remove plugin package instance failed. Meet error: {}", e);
            throw new WecubeCoreException("Remove plugin package instance failed.");
        }
        return okay();
    }

//    @GetMapping("/instances/packages/{package-id}")
//    @ResponseBody
//    public JsonResponse getInstancesByPackageId() {
//        return okayWithData(pluginInstanceService.getAllInstances());
//    }

    @GetMapping("/packages/{package-id}/instances")
    @ResponseBody
    public JsonResponse getAvailableInstancesByPackageId(@PathVariable(value = "package-id") int packageId) {
        return okayWithData(pluginInstanceService.getAvailableInstancesByPackageId(packageId));
    }

}
