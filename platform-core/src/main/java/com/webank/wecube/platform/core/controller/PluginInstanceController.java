package com.webank.wecube.platform.core.controller;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.JsonResponse;
import com.webank.wecube.platform.core.service.plugin.PluginInstanceService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static com.webank.wecube.platform.core.domain.JsonResponse.okay;
import static com.webank.wecube.platform.core.domain.JsonResponse.okayWithData;

@RestController
@RequestMapping("/v1")
public class PluginInstanceController {

    private static final Logger log = LoggerFactory.getLogger(PluginInstanceController.class);

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
    public JsonResponse createPluginInstanceByPackageIdAndHostIp(@PathVariable(value = "package-id") String packageId,
            @PathVariable(value = "host-ip") String hostIp, @PathVariable(value = "port") int port) {
        try {
            pluginInstanceService.launchPluginInstance(packageId, hostIp, port);
        } catch (Exception e) {
            throw new WecubeCoreException("Launch plugin instance failed. Error is " + e.getMessage(),e);
        }
        return okay();
    }

    @DeleteMapping("/packages/instances/{instance-id}/remove")
    @ResponseBody
    public JsonResponse removePluginInstance(@PathVariable(value = "instance-id") String instanceId) {
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
    public JsonResponse getAvailableInstancesByPackageId(@PathVariable(value = "package-id") String packageId) {
        return okayWithData(pluginInstanceService.getAvailableInstancesByPackageId(packageId));
    }

}
