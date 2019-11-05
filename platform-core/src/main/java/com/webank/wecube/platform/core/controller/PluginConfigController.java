package com.webank.wecube.platform.core.controller;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.JsonResponse;
import com.webank.wecube.platform.core.domain.plugin.PluginConfig;
import com.webank.wecube.platform.core.service.plugin.PluginConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static com.webank.wecube.platform.core.domain.JsonResponse.*;

@RestController
public class PluginConfigController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private PluginConfigService pluginConfigService;

    @PostMapping("/plugins")
    @ResponseBody
    public JsonResponse savePluginConfig(@RequestBody PluginConfig pluginConfig) {
        PluginConfig savedPluginConfig;
        try {
            savedPluginConfig = pluginConfigService.savePluginConfig(pluginConfig);
        } catch (WecubeCoreException e) {
            return error(e.getMessage());
        }
        return okayWithData(savedPluginConfig);
    }

    @PostMapping("/plugins/enable/{plugin-config-id}")
    @ResponseBody
    public JsonResponse enablePlugin(@PathVariable(value = "plugin-config-id") int pluginConfigId) {
        PluginConfig savedPluginConfig;
        try {
            savedPluginConfig = pluginConfigService.enablePlugin(pluginConfigId);
        } catch (WecubeCoreException e) {
            return error(e.getMessage());
        }
        return okayWithData(savedPluginConfig);
    }

    @PostMapping("/plugins/disable/{plugin-config-id}")
    @ResponseBody
    public JsonResponse disablePlugin(@PathVariable(value = "plugin-config-id") int pluginConfigId) {
        PluginConfig savedPluginConfig;
        try {
            savedPluginConfig = pluginConfigService.disablePlugin(pluginConfigId);
        } catch (WecubeCoreException e) {
            return error(e.getMessage());
        }
        return okayWithData(savedPluginConfig);
    }

}
