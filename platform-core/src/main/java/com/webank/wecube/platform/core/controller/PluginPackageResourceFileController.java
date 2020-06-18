package com.webank.wecube.platform.core.controller;

import static com.webank.wecube.platform.core.dto.CommonResponseDto.error;
import static com.webank.wecube.platform.core.dto.CommonResponseDto.okayWithData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.CommonResponseDto;
import com.webank.wecube.platform.core.service.plugin.PluginPackageResourceFileService;

@RestController
@RequestMapping("/v1")
public class PluginPackageResourceFileController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private PluginPackageResourceFileService resourceFileService;

    @GetMapping("/resource-files")
    public CommonResponseDto getAllPluginPackageResourceFiles() {
        try {
            return okayWithData(resourceFileService.getAllPluginPackageResourceFiles());
        } catch (WecubeCoreException e) {
            log.error("Failed to get all PluginPackage resource files.");
            return error(e.getMessage());
        }
    }

}
