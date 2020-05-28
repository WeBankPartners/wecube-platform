package com.webank.wecube.platform.auth.server.controller;

import static com.webank.wecube.platform.auth.server.dto.CommonResponseDto.okayWithData;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webank.wecube.platform.auth.server.common.ApplicationConstants;
import com.webank.wecube.platform.auth.server.dto.CommonResponseDto;
import com.webank.wecube.platform.auth.server.dto.SimpleSubSystemDto;
import com.webank.wecube.platform.auth.server.dto.SubSystemTokenDto;
import com.webank.wecube.platform.auth.server.service.SubSystemManagementService;

@RestController
@RequestMapping(ApplicationConstants.ApiInfo.PREFIX_DEFAULT)
public class LocalSubSystemManagementController {

    @Autowired
    private SubSystemManagementService subSystemManagementService;

    @PostMapping("/sub-systems")
    public CommonResponseDto registerSubSystem(@RequestBody SimpleSubSystemDto subSystemDto) {
        return okayWithData(subSystemManagementService.registerSubSystem(subSystemDto));
    }

    @PostMapping("/sub-systems/tokens")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN')")
    public CommonResponseDto registerSubSystemAccessToken(@RequestBody SubSystemTokenDto subSystemTokenDto) {
        return okayWithData(subSystemManagementService.registerSubSystemAccessToken(subSystemTokenDto));
    }

    @GetMapping("/sub-systems")
    public CommonResponseDto retrieveAllSubSystems() {
        return okayWithData(subSystemManagementService.retrieveAllSubSystems());
    }

    @GetMapping("/sub-systems/{system-code}/apikey")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN')")
    public CommonResponseDto retrieveAllSubSystems(@PathVariable("system-code") String systemCode) {

        return okayWithData(subSystemManagementService.retrieveSubSystemApikey(systemCode));
    }
}
