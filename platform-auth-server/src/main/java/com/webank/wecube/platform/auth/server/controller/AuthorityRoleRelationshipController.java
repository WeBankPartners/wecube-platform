package com.webank.wecube.platform.auth.server.controller;

import static com.webank.wecube.platform.auth.server.dto.CommonResponseDto.okay;
import static com.webank.wecube.platform.auth.server.dto.CommonResponseDto.okayWithData;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.webank.wecube.platform.auth.server.common.ApplicationConstants;
import com.webank.wecube.platform.auth.server.dto.CommonResponseDto;
import com.webank.wecube.platform.auth.server.service.AuthorityRoleRelationshipService;

@RestController
@RequestMapping(ApplicationConstants.ApiInfo.PREFIX_DEFAULT)
public class AuthorityRoleRelationshipController {

    private static final Logger log = LoggerFactory.getLogger(AuthorityRoleRelationshipController.class);

    @Autowired
    AuthorityRoleRelationshipService authorityRoleRelationshipService;

    @GetMapping("/authoritys/{authority-id}/roles")
    @ResponseBody
    public CommonResponseDto getRolesByAuthorityId(@PathVariable(value = "authority-id") Long authorityId) {
        return okayWithData(authorityRoleRelationshipService.getRolesByAuthorityId(authorityId));
    }

    @GetMapping("/roles/{role-id}/authorities")
    @ResponseBody
    public CommonResponseDto getAuthoritysByRoleId(@PathVariable(value = "role-id") String roleId) {
        return okayWithData(authorityRoleRelationshipService.getAuthoritysByRoleId(roleId));
    }

    @PostMapping("/roles/{role-id}/authorities/grant")
    @ResponseBody
    public CommonResponseDto grantRoleForAuthoritiesByCode(@PathVariable(value = "role-id") String roleId,
            @RequestBody List<String> authorityCodes) throws Exception {
        log.info("grant authorities to role:roleId={},authorityCodes={}", roleId, authorityCodes);
        authorityRoleRelationshipService.grantRoleForAuthoritiesByCode(roleId, authorityCodes);
        return okay();
    }

    @PostMapping("/roles/{role-id}/authorities/revoke")
    @ResponseBody
    public CommonResponseDto revokeRoleForAuthoritiesByCode(@PathVariable(value = "role-id") String roleId,
            @RequestBody List<String> authorityCodes) throws Exception {
        log.info("revoke authorities from role:roleId={},authorityCodes={}", roleId, authorityCodes);
        authorityRoleRelationshipService.revokeRoleForAuthoritiesByCode(roleId, authorityCodes);
        return okay();
    }
}
