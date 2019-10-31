package com.webank.wecube.platform.core.controller;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.JsonResponse;
import com.webank.wecube.platform.core.service.plugin.PluginPackageResourceFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static com.webank.wecube.platform.core.domain.JsonResponse.error;
import static com.webank.wecube.platform.core.domain.JsonResponse.okayWithData;

@RestController
@RequestMapping("/v1/api")
public class PluginPackageResourceFileController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private PluginPackageResourceFileService resourceFileService;

    @GetMapping("/resource-files")
    @ResponseBody
    public JsonResponse getAllPluginPackageResourceFiles() {
        try {
            return okayWithData(resourceFileService.getAllPluginPackageResourceFiles());
        } catch (WecubeCoreException e) {
            log.error("Failed to get all PluginPackage resource files.");
            return error(e.getMessage());
        }
    }

}
