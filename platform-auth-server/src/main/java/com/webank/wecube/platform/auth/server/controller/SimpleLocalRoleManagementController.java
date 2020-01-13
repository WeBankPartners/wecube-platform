package com.webank.wecube.platform.auth.server.controller;

import static com.webank.wecube.platform.auth.server.dto.CommonResponseDto.okay;
import static com.webank.wecube.platform.auth.server.dto.CommonResponseDto.okayWithData;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.webank.wecube.platform.auth.server.common.ApplicationConstants;
import com.webank.wecube.platform.auth.server.dto.CommonResponseDto;
import com.webank.wecube.platform.auth.server.dto.SimpleLocalRoleDto;
import com.webank.wecube.platform.auth.server.service.RoleManagementService;

@RestController
@RequestMapping(ApplicationConstants.ApiInfo.PREFIX_DEFAULT)
public class SimpleLocalRoleManagementController {

    @Autowired
    private RoleManagementService roleManagementService;

    @PostMapping("/roles")
    public CommonResponseDto registerLocalRole(@RequestBody SimpleLocalRoleDto roleDto) {
        SimpleLocalRoleDto result = roleManagementService.registerLocalRole(roleDto);
        return okayWithData(result);
    }

    @GetMapping("/roles")
    public CommonResponseDto retrieveAllLocalRoles() {
        List<SimpleLocalRoleDto> result = roleManagementService.retrieveAllLocalRoles();
        return okayWithData(result);
    }

    @GetMapping("/roles/{role-id}")
    public CommonResponseDto retrieveRoleInfo(@PathVariable(value = "role-id") String roleId) {
        SimpleLocalRoleDto result = roleManagementService.retriveLocalRoleByRoleId(roleId);
        return okayWithData(result);
    }

    @DeleteMapping("/roles/{role-id}")
    public CommonResponseDto deleteRole(@PathVariable(value = "role-id") String roleId) {
        roleManagementService.unregisterLocalRole(roleId);
        return okay();
    }
    
    @GetMapping("/roles/{role-id}/authorities")
    public CommonResponseDto retrieveAllRoleAuthoritiesByRoleId(@PathVariable(value = "role-id") String roleId) {
        return okayWithData(authorityRoleRelationshipService.getAuthoritysByRoleId(roleId));
    }

    @PostMapping("/roles/{role-id}/authoritys")
    @ResponseBody
    public CommonResponseDto grantRoleForAuthoritys(@PathVariable(value = "role-id") String roleId,
                                                    @RequestBody List<Long> authorityIds) throws Exception {
        authorityRoleRelationshipService.grantRoleForAuthoritys(roleId, authorityIds);
        return okay();
    }

    @DeleteMapping("/roles/{role-id}/authoritys")
    @ResponseBody
    public CommonResponseDto revokeRoleForAuthoritys(@PathVariable(value = "role-id") String roleId,
                                                     @RequestBody List<Long> authorityIds) throws Exception {
        authorityRoleRelationshipService.revokeRoleForAuthoritys(roleId, authorityIds);
        return okay();
    }
}
