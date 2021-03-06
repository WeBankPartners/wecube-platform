package com.webank.wecube.platform.core.controller.plugin;

import static com.webank.wecube.platform.core.dto.plugin.CommonResponseDto.okay;
import static com.webank.wecube.platform.core.dto.plugin.CommonResponseDto.okayWithData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.plugin.CommonResponseDto;
import com.webank.wecube.platform.core.service.plugin.PluginInstanceMgmtService;

@RestController
@RequestMapping("/v1")
public class PluginInstanceController {

    private static final Logger log = LoggerFactory.getLogger(PluginInstanceController.class);

    @Autowired
    private PluginInstanceMgmtService pluginInstanceMgmtService;

    /**
     * 
     * @return
     */
    @GetMapping("/available-container-hosts")
    public CommonResponseDto getAvailableContainerHosts() {
        return okayWithData(pluginInstanceMgmtService.getAvailableContainerHosts());
    }

    /**
     * 
     * @param hostIp
     * @return
     */
    @GetMapping("/hosts/{host-ip}/next-available-port")
    public CommonResponseDto getAvailablePortByHostIp(@PathVariable(value = "host-ip") String hostIp) {
        return okayWithData(pluginInstanceMgmtService.getAvailablePortByHostIp(hostIp));
    }

    /**
     * 
     * @param packageId
     * @param hostIp
     * @param port
     * @return
     */
    @PostMapping("/packages/{package-id}/hosts/{host-ip}/ports/{port}/instance/launch")
    public CommonResponseDto createPluginInstanceByPackageIdAndHostIp(
            @PathVariable(value = "package-id") String packageId, @PathVariable(value = "host-ip") String hostIp,
            @PathVariable(value = "port") int port) {
        try {
            pluginInstanceMgmtService.launchPluginInstance(packageId, hostIp, port);
        } catch (Exception e) {
            String msg = String.format("Launch plugin instance failed. Error is %s" , e.getMessage());
            throw new WecubeCoreException("3271",msg, e.getMessage());
        }
        return okay();
    }

    /**
     * 
     * @param instanceId
     * @return
     */
    @DeleteMapping("/packages/instances/{instance-id}/remove")
    public CommonResponseDto removePluginInstance(@PathVariable(value = "instance-id") String instanceId) {
        try {
            pluginInstanceMgmtService.removePluginInstanceById(instanceId);
        } catch (Exception e) {
            log.info("Remove plugin package instance failed. Meet error: {}", e);
            throw new WecubeCoreException("3272","Remove plugin package instance failed.");
        }
        return okay();
    }

    /**
     * 
     * @param packageId
     * @return
     */
    @GetMapping("/packages/{package-id}/instances")
    public CommonResponseDto getAvailableInstancesByPackageId(@PathVariable(value = "package-id") String packageId) {
        return okayWithData(pluginInstanceMgmtService.getAvailableInstancesByPackageId(packageId));
    }

}
