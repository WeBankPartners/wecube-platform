package com.webank.wecube.platform.auth.server.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webank.wecube.platform.auth.server.common.ApplicationConstants;
import com.webank.wecube.platform.auth.server.dto.JwtTokenDto;

@RestController
@RequestMapping(ApplicationConstants.ApiInfo.PREFIX_DEFAULT)
public class TokenController {

    @GetMapping("/token/refresh")
    public JwtTokenDto refreshToken(){
        JwtTokenDto token = new JwtTokenDto();
        token.setName("jwt token");
        token.setPassword("111111");
        
        return token;
    }
}
