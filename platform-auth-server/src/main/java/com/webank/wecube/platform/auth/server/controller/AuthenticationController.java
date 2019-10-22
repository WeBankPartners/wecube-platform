package com.webank.wecube.platform.auth.server.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webank.wecube.platform.auth.server.common.ApplicationConstants;
import com.webank.wecube.platform.auth.server.dto.CommonResponseDto;

@RestController
@RequestMapping(ApplicationConstants.ApiInfo.PREFIX_DEFAULT)
public class AuthenticationController {
    private static final Logger log = LoggerFactory.getLogger(AuthenticationController.class);

    @PutMapping("/logout")
    public CommonResponseDto userCentralLogout(HttpServletRequest request, HttpServletResponse response) {
        
        log.info("=== logout ===");
        
        return CommonResponseDto.okay();

    }
    
    @GetMapping("/ping")
    public CommonResponseDto ping(HttpServletRequest request, HttpServletResponse response){
        log.info("==== ping ======");
        return CommonResponseDto.okay();
    }

}
