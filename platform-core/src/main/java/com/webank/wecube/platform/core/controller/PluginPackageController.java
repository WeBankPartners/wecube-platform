package com.webank.wecube.platform.core.controller;

import com.webank.wecube.platform.core.commons.ApplicationProperties.PluginProperties;
import com.webank.wecube.platform.core.domain.JsonResponse;
import com.webank.wecube.platform.core.domain.plugin.PluginPackage;
import com.webank.wecube.platform.core.dto.PluginPackageDto;
import com.webank.wecube.platform.core.service.plugin.PluginConfigService;
import com.webank.wecube.platform.core.service.plugin.PluginPackageService;
import com.webank.wecube.platform.core.support.PluginPackageValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import static com.webank.wecube.platform.core.domain.JsonResponse.okay;
import static com.webank.wecube.platform.core.domain.JsonResponse.okayWithData;

@RestController
@RequestMapping("/v1/api")
public class PluginPackageController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private PluginProperties pluginProperties;
    @Autowired
    private PluginConfigService pluginConfigService;

    @Autowired
    private PluginPackageService pluginPackageService;

    @Autowired
    private PluginPackageValidator validator;

    @PostMapping("/packages")
    @ResponseBody
    public JsonResponse uploadPluginPackage(@RequestParam(value = "zip-file") MultipartFile file) throws Exception {
        validator.validate(file.getInputStream());
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("zip-file required.");

        PluginPackage pluginPackage = pluginPackageService.uploadPackage(file);
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

}



