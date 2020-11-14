package com.webank.wecube.platform.core.controller.plugin;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.CommonResponseDto;
import com.webank.wecube.platform.core.service.plugin.PluginConfigMigrationService;
import com.webank.wecube.platform.core.service.plugin.PluginRegistryInfo;

@RestController
@RequestMapping("/v1")
public class PluginConfigMigrationController {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private PluginConfigMigrationService pluginConfigMigrationService;

    /**
     * 
     * @param pluginPackageId
     * @return
     */
    @GetMapping(value = "/plugins/packages/export/{plugin-package-id:.+}", produces = { MediaType.ALL_VALUE })
    public ResponseEntity<byte[]> exportPluginPackageRegistries(
            @PathVariable(value = "plugin-package-id") String pluginPackageId) {
        log.info("request received to export plugin package registries for {}", pluginPackageId);
        PluginRegistryInfo pluginRegistryInfo = pluginConfigMigrationService
                .exportPluginRegistersForOnePackage(pluginPackageId);
        byte[] filedataBytes = pluginRegistryInfo.getPluginPackageData().getBytes(Charset.forName("UTF-8"));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        DateFormat df = new SimpleDateFormat("yyyyMMdd");
        String sDate = df.format(new Date());
        String fileName = String.format("register-config-%s-%s.xml", pluginRegistryInfo.getPluginPackageName(), sDate);
        headers.add("Content-Disposition", String.format("attachment;filename=%s", fileName));
        return ResponseEntity.ok().headers(headers).contentLength(filedataBytes.length)
                .contentType(MediaType.APPLICATION_OCTET_STREAM).body(filedataBytes);
    }

    /**
     * 
     * @param pluginPackageId
     * @param xmlFile
     * @return
     */
    @PostMapping(value = "/plugins/packages/import/{plugin-package-id:.+}", consumes = {
            MediaType.MULTIPART_FORM_DATA_VALUE })
    public CommonResponseDto importPluginPackageRegistries(
            @PathVariable(value = "plugin-package-id") String pluginPackageId,
            @RequestParam(value = "xml-file") MultipartFile xmlFile) {

        if (xmlFile == null || xmlFile.getSize() <= 0) {
            log.error("invalid file content uploaded");
            throw new WecubeCoreException("3034", "Invalid file content uploaded.");
        }

        if (log.isInfoEnabled()) {
            log.info("About to import plugin package registries,filename={},size={}", xmlFile.getOriginalFilename(),
                    xmlFile.getSize());
        }

        try {
            String xmlData = IOUtils.toString(xmlFile.getInputStream(), Charset.forName("UTF-8"));

            pluginConfigMigrationService.importPluginRegistersForOnePackage(pluginPackageId, xmlData);
            return CommonResponseDto.okay();
        } catch (IOException e) {
            log.error("errors while reading upload file", e);
            throw new WecubeCoreException("3035", "Failed to import plugin package registries.");
        }

    }

}
