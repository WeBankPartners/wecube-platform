package com.webank.wecube.platform.core.controller;

import com.webank.wecube.platform.core.commons.ApplicationProperties.PluginProperties;
import com.webank.wecube.platform.core.domain.JsonResponse;
import com.webank.wecube.platform.core.domain.plugin.PluginPackage;
import com.webank.wecube.platform.core.service.plugin.PluginPackageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import static com.webank.wecube.platform.core.domain.JsonResponse.okay;
import static com.webank.wecube.platform.core.domain.JsonResponse.okayWithData;

@RestController
@RequestMapping("/v1/api/packages")
public class PluginPackageController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private PluginProperties pluginProperties;

    @Autowired
    private PluginPackageService pluginPackageService;

    @PostMapping("/")
    @ResponseBody
    public JsonResponse uploadPluginPackage(@RequestParam(value = "zip-file") MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("zip-file required.");
        PluginPackage pluginPackage = pluginPackageService.uploadPackage(file).getPluginPackage();
        return okay().withData(pluginPackage);
    }

}



