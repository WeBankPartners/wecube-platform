package com.webank.wecube.platform.auth.server.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.webank.wecube.platform.auth.server.service.AuthorityRoleRelationshipService;

import static com.webank.wecube.platform.auth.server.dto.CommonResponseDto.okayWithData;

import java.util.List;

import static com.webank.wecube.platform.auth.server.dto.CommonResponseDto.okay;

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

	@GetMapping("/roles/{role-id}/authoritys")
	@ResponseBody
	public CommonResponseDto getAuthoritysByRoleId(@PathVariable(value = "role-id") Long roleId) {
		return okayWithData(authorityRoleRelationshipService.getAuthoritysByRoleId(roleId));
	}

	@PostMapping("/roles/{role-id}/authoritys")
	@ResponseBody
	public CommonResponseDto grantRoleForAuthoritys(@PathVariable(value = "role-id") Long roleId,
			@RequestBody List<Long> authorityIds) throws Exception {
		authorityRoleRelationshipService.grantRoleForAuthoritys(roleId, authorityIds);
		return okay();
	}

	@DeleteMapping("/roles/{role-id}/authoritys")
	@ResponseBody
	public CommonResponseDto revokeRoleForAuthoritys(@PathVariable(value = "role-id") Long roleId,
			@RequestBody List<Long> authorityIds) throws Exception {
		authorityRoleRelationshipService.revokeRoleForAuthoritys(roleId, authorityIds);
		return okay();
	}
}
