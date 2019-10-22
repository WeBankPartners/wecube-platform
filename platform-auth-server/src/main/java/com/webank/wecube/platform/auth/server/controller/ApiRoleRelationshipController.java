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
import com.webank.wecube.platform.auth.server.service.ApiRoleRelationshipService;

import static com.webank.wecube.platform.auth.server.dto.CommonResponseDto.okayWithData;

import java.util.List;

import static com.webank.wecube.platform.auth.server.dto.CommonResponseDto.okay;

@RestController
@RequestMapping(ApplicationConstants.ApiInfo.PREFIX_DEFAULT)
public class ApiRoleRelationshipController {

	private static final Logger log = LoggerFactory.getLogger(ApiRoleRelationshipController.class);

	@Autowired
	ApiRoleRelationshipService apiRoleRelationshipService;

	@GetMapping("/apis/{api-id}/roles")
	@ResponseBody
	public CommonResponseDto getRolesByApiId(@PathVariable(value = "api-id") Long apiId) {
		return okayWithData(apiRoleRelationshipService.getRolesByApiId(apiId));
	}

	@GetMapping("/roles/{role-id}/apis")
	@ResponseBody
	public CommonResponseDto getApisByRoleId(@PathVariable(value = "role-id") Long roleId) {
		return okayWithData(apiRoleRelationshipService.getApisByRoleId(roleId));
	}

	@PostMapping("/roles/{role-id}/apis")
	@ResponseBody
	public CommonResponseDto grantRoleForApis(@PathVariable(value = "role-id") Long roleId,
			@RequestBody List<Long> apiIds) throws Exception {
		apiRoleRelationshipService.grantRoleForApis(roleId, apiIds);
		return okay();
	}

	@DeleteMapping("/roles/{role-id}/apis")
	@ResponseBody
	public CommonResponseDto revokeRoleForApis(@PathVariable(value = "role-id") Long roleId,
			@RequestBody List<Long> apiIds) throws Exception {
		apiRoleRelationshipService.revokeRoleForApis(roleId, apiIds);
		return okay();
	}
}
