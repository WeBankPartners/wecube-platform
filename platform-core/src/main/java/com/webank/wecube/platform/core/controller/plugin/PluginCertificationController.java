package com.webank.wecube.platform.core.controller.plugin;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.webank.wecube.platform.core.dto.plugin.CommonResponseDto;
import com.webank.wecube.platform.core.service.plugin.PluginCertificationService;

@RestController
@RequestMapping("/v1")
public class PluginCertificationController {
    private static final Logger log = LoggerFactory.getLogger(PluginCertificationController.class);

    @Autowired
    private PluginCertificationService pluginCertificationService;
    
    @GetMapping("/plugin-certifications")
    public CommonResponseDto getAllPluginCertifications() {
        //TODO
        
        return null;
    }
    
    @DeleteMapping("/plugin-certifications/{id}")
    public CommonResponseDto removePluginCertification(@PathVariable("id") String id) {
        //TODO
        return CommonResponseDto.okay();
    }
    
    @GetMapping(value = "/plugin-certifications/{id}/export", produces = { MediaType.ALL_VALUE })
    public ResponseEntity<byte[]> exportPluginCertification(@PathVariable("id") String id){
        //TODO
        return null;
    }
    
    @PostMapping(value = "/plugin-certifications/import", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public CommonResponseDto importPluginCertification(@RequestParam("uploadFile") MultipartFile file,
            HttpServletRequest request) {
        //TODO
        return null;
    }
}
