package com.webank.wecube.platform.auth.server.controller;

import java.security.Principal;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webank.wecube.platform.auth.server.common.ApplicationConstants;
import com.webank.wecube.platform.auth.server.dto.CommonResponseDto;

@RestController
@RequestMapping(ApplicationConstants.ApiInfo.PREFIX_DEFAULT)
public class TokenController {
    
    private static final Logger log = LoggerFactory.getLogger(TokenController.class);
    
    @Autowired
    private UserDetailsService userDetailsService;
    
    @PostConstruct
    public void afterPropertiesSet(){
        Assert.notNull(userDetailsService, "user details service should provide");
    }

    @GetMapping("/token")
    public CommonResponseDto refreshToken(HttpServletRequest request, HttpServletResponse response, Principal principal){
        log.info("refresh token ===");
        
        
        UserDetails userDetails = userDetailsService.loadUserByUsername(principal.getName());
        
        log.info("UserDetails:{}", userDetails);
        
        return CommonResponseDto.okayWithData(userDetails);
    }
}
