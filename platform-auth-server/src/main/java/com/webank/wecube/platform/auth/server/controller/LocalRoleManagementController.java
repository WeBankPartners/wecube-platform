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
import org.springframework.web.bind.annotation.RestController;

import com.webank.wecube.platform.auth.server.common.ApplicationConstants;
import com.webank.wecube.platform.auth.server.dto.CommonResponseDto;
import com.webank.wecube.platform.auth.server.dto.RoleAuthoritiesGrantDto;
import com.webank.wecube.platform.auth.server.dto.SimpleAuthorityDto;
import com.webank.wecube.platform.auth.server.dto.SimpleLocalRoleDto;
import com.webank.wecube.platform.auth.server.service.RoleManagementService;

@RestController
@RequestMapping(ApplicationConstants.ApiInfo.PREFIX_DEFAULT)
public class LocalRoleManagementController {

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
	public CommonResponseDto unregisterLocalRoleById(@PathVariable(value = "role-id") String roleId) {
		roleManagementService.unregisterLocalRoleById(roleId);
		return okay();
	}

	@GetMapping("/roles/{role-id}/authorities")
	public CommonResponseDto retrieveAllAuthoritiesByRoleId(@PathVariable(value = "role-id") String roleId) {
		return okayWithData(roleManagementService.retrieveAllAuthoritiesByRoleId(roleId));
	}

	@PostMapping("/roles/{role-id}/authorities")
	public CommonResponseDto configureRoleWithAuthoritiesById(@PathVariable(value = "role-id") String roleId,
			@RequestBody List<SimpleAuthorityDto> authorityDtos) {
		roleManagementService.configureRoleWithAuthoritiesById(roleId, authorityDtos);
		return okay();
	}

	@PostMapping("/roles/authorities-grants")
	public CommonResponseDto configureRoleWithAuthoritiesByName(
			@RequestBody RoleAuthoritiesGrantDto roleAuthoritiesGrantDto) {
		//TODO
		return null;
	}

	@PostMapping("/roles/{role-id}/authorities/revoke")
	public CommonResponseDto revokeRoleAuthoritiesById(@PathVariable(value = "role-id") String roleId,
			@RequestBody List<SimpleAuthorityDto> authorityDtos) {
		roleManagementService.revokeRoleAuthoritiesById(roleId, authorityDtos);
		return okay();
	}
}
