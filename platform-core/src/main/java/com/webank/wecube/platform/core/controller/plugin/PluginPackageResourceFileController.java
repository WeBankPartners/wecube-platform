package com.webank.wecube.platform.core.controller.plugin;

import static com.webank.wecube.platform.core.dto.plugin.CommonResponseDto.okayWithData;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webank.wecube.platform.core.dto.plugin.CommonResponseDto;
import com.webank.wecube.platform.core.service.plugin.PluginPackageResourceFileService;

@RestController
@RequestMapping("/v1")
public class PluginPackageResourceFileController {

    @Autowired
    private PluginPackageResourceFileService resourceFileService;

    @GetMapping("/resource-files")
    public CommonResponseDto getAllPluginPackageResourceFiles() {
        return okayWithData(resourceFileService.getAllPluginPackageResourceFiles());
    }

}
