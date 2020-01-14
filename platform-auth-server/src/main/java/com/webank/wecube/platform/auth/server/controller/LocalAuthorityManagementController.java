package com.webank.wecube.platform.auth.server.controller;

import static com.webank.wecube.platform.auth.server.dto.CommonResponseDto.okayWithData;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webank.wecube.platform.auth.server.common.ApplicationConstants;
import com.webank.wecube.platform.auth.server.dto.CommonResponseDto;
import com.webank.wecube.platform.auth.server.dto.SimpleAuthorityDto;
import com.webank.wecube.platform.auth.server.service.AuthorityManagementService;

@RestController
@RequestMapping(ApplicationConstants.ApiInfo.PREFIX_DEFAULT)
public class LocalAuthorityManagementController {

    @Autowired
    private AuthorityManagementService authorityManagementService;

    @PostMapping("/authorities")
    public CommonResponseDto registerLocalAuthority(@RequestBody SimpleAuthorityDto authorityDto) {
        return okayWithData(authorityManagementService.registerLocalAuthority(authorityDto));
    }

    @GetMapping("/authorities")
    public CommonResponseDto retrieveAllLocalAuthorites() {
        return okayWithData(authorityManagementService.retrieveAllLocalAuthorites());
    }
}
