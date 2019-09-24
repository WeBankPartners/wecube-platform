package com.webank.wecube.platform.auth.server.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webank.wecube.platform.auth.server.common.ApplicationConstants;
import com.webank.wecube.platform.auth.server.dto.CredentialDto;
import com.webank.wecube.platform.auth.server.dto.JwtTokenDto;

@RestController
@RequestMapping(ApplicationConstants.ApiInfo.PREFIX_DEFAULT)
public class AuthenticationController {
    private static final Logger log = LoggerFactory.getLogger(AuthenticationController.class);

    @PostMapping("/logout")
    public JwtTokenDto userCentralLogin(HttpServletRequest request, HttpServletResponse response,
            @RequestBody CredentialDto credentialDto) {
        
        log.info("=== login ===");
        JwtTokenDto dto = new JwtTokenDto();
        dto.setName("logined");
        dto.setPassword("123456");
        
        return dto;

    }

}
