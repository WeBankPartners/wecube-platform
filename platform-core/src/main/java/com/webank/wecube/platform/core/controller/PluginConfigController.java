package com.webank.wecube.platform.core.controller;

import static com.webank.wecube.platform.core.dto.CommonResponseDto.okay;
import static com.webank.wecube.platform.core.dto.CommonResponseDto.okayWithData;

import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.webank.wecube.platform.core.dto.CommonResponseDto;
import com.webank.wecube.platform.core.dto.PluginConfigDto;
import com.webank.wecube.platform.core.dto.TargetEntityFilterRuleDto;
import com.webank.wecube.platform.core.service.plugin.PluginConfigService;

@RestController
@RequestMapping("/v1")
public class PluginConfigController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private PluginConfigService pluginConfigService;

    @GetMapping(value = "/plugins/packages/export/{plugin-package-id:.+}", produces = { MediaType.ALL_VALUE })
    public ResponseEntity<byte[]> exportPluginPackageRegistries(
            @PathVariable(value = "plugin-package-id") String pluginPackageId) {
        log.info("request received to export plugin package registries for {}", pluginPackageId);
        String xmlData = pluginConfigService.exportPluginRegistersForOnePackage(pluginPackageId);
        byte[] filedataBytes = xmlData.getBytes(Charset.forName("UTF-8"));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.add("Content-Disposition", String.format("attachment;filename=%s", "register-config.xml"));
        return ResponseEntity.ok().headers(headers).contentLength(filedataBytes.length)
                .contentType(MediaType.APPLICATION_OCTET_STREAM).body(filedataBytes);
    }

    @PostMapping("/plugins/packages/import/{plugin-package-id:.+}")
    public CommonResponseDto importPluginPackageRegistries(
            @PathVariable(value = "plugin-package-id") String pluginPackageId,
            @RequestParam(value = "xml-file") MultipartFile xmlFile) {
        return CommonResponseDto.okay();
    }

    @PostMapping("/plugins")
    public CommonResponseDto savePluginConfig(@RequestBody PluginConfigDto pluginConfigDto) {
        return okayWithData(pluginConfigService.savePluginConfig(pluginConfigDto));
    }

    @GetMapping("/plugins/interfaces/enabled")
    public CommonResponseDto queryAllEnabledPluginConfigInterface() {
        return okayWithData(pluginConfigService.queryAllLatestEnabledPluginConfigInterface());
    }

    @GetMapping("/plugins/interfaces/package/{package-name}/entity/{entity-name}/enabled")
    public CommonResponseDto queryAllEnabledPluginConfigInterfaceForEntityName(
            @PathVariable(value = "package-name") String packageName,
            @PathVariable(value = "entity-name") String entityName) {
        return okayWithData(
                pluginConfigService.queryAllEnabledPluginConfigInterfaceForEntity(packageName, entityName, null));
    }

    @PostMapping("/plugins/interfaces/package/{package-name}/entity/{entity-name}/enabled/query-by-target-entity-filter-rule")
    public CommonResponseDto queryAllEnabledPluginConfigInterfaceByEntityNameAndFilterRule(
            @PathVariable(value = "package-name") String packageName,
            @PathVariable(value = "entity-name") String entityName, @RequestBody TargetEntityFilterRuleDto filterRule) {
        return okayWithData(
                pluginConfigService.queryAllEnabledPluginConfigInterfaceForEntity(packageName, entityName, filterRule));
    }

    @PostMapping("/plugins/enable/{plugin-config-id:.+}")
    public CommonResponseDto enablePlugin(@PathVariable(value = "plugin-config-id") String pluginConfigId) {
        return okayWithData(pluginConfigService.enablePlugin(pluginConfigId));
    }

    @PostMapping("/plugins/disable/{plugin-config-id:.+}")
    public CommonResponseDto disablePlugin(@PathVariable(value = "plugin-config-id") String pluginConfigId) {
        return okayWithData(pluginConfigService.disablePlugin(pluginConfigId));
    }

    @GetMapping("/plugins/interfaces/{plugin-config-id:.+}")
    public CommonResponseDto queryPluginConfigInterfaceByConfigId(
            @PathVariable(value = "plugin-config-id") String pluginConfigId) {
        return okayWithData(pluginConfigService.queryPluginConfigInterfaceByConfigId(pluginConfigId));
    }

    @DeleteMapping("/plugins/configs/{plugin-config-id:.+}")
    public CommonResponseDto deletePluginConfigByConfigId(
            @PathVariable(value = "plugin-config-id") String pluginConfigId) {
        try {
            pluginConfigService.deletePluginConfigById(pluginConfigId);
        } catch (Exception e) {
            log.error(e.getMessage());
            return CommonResponseDto.error(e.getMessage());
        }
        return okay();
    }

}
