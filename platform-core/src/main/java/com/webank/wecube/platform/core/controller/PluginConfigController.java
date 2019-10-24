package com.webank.wecube.platform.core.controller;

import com.webank.wecube.platform.core.domain.JsonResponse;
import com.webank.wecube.platform.core.domain.plugin.PluginConfig;
import com.webank.wecube.platform.core.service.plugin.PluginConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static com.webank.wecube.platform.core.domain.JsonResponse.okay;
import static com.webank.wecube.platform.core.domain.JsonResponse.okayWithData;

@RestController
@RequestMapping("/v1/api")
public class PluginConfigController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private PluginConfigService pluginConfigService;

    @PostMapping("/plugins")
    @ResponseBody
    public JsonResponse savePluginConfig(@RequestBody PluginConfig pluginConfig) {
        return okayWithData(pluginConfigService.savePluginConfig(pluginConfig));
    }

    @PostMapping("/plugins/register")
    @ResponseBody
    public JsonResponse registerPlugin(@RequestBody PluginConfig pluginConfig) {
        return okayWithData(pluginConfigService.registerPlugin(pluginConfig));
    }

    @DeleteMapping("/plugins/{plugin-config-id}")
    @ResponseBody
    public JsonResponse deletePlugin(@PathVariable(value = "plugin-config-id") int pluginConfigId) {
        pluginConfigService.deletePlugin(pluginConfigId);
        return okay();
    }


}
