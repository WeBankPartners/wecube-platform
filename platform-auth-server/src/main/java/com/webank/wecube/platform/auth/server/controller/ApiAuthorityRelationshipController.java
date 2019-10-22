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
import com.webank.wecube.platform.auth.server.service.ApiAuthorityRelationshipService;

import static com.webank.wecube.platform.auth.server.dto.CommonResponseDto.okayWithData;

import java.util.List;

import static com.webank.wecube.platform.auth.server.dto.CommonResponseDto.okay;

@RestController
@RequestMapping(ApplicationConstants.ApiInfo.PREFIX_DEFAULT)
public class ApiAuthorityRelationshipController {

	private static final Logger log = LoggerFactory.getLogger(ApiAuthorityRelationshipController.class);

	@Autowired
	ApiAuthorityRelationshipService apiAuthorityRelationshipService;

	@GetMapping("/apis/{api-id}/authoritys")
	@ResponseBody
	public CommonResponseDto getAuthoritysByApiId(@PathVariable(value = "api-id") Long apiId) {
		return okayWithData(apiAuthorityRelationshipService.getAuthoritysByApiId(apiId));
	}

	@GetMapping("/authoritys/{authority-id}/apis")
	@ResponseBody
	public CommonResponseDto getApisByAuthorityId(@PathVariable(value = "authority-id") Long authorityId) {
		return okayWithData(apiAuthorityRelationshipService.getApisByAuthorityId(authorityId));
	}

	@PostMapping("/authoritys/{authority-id}/apis")
	@ResponseBody
	public CommonResponseDto grantAuthorityForApis(@PathVariable(value = "authority-id") Long authorityId,
			@RequestBody List<Long> apiIds) throws Exception {
		apiAuthorityRelationshipService.grantAuthorityForApis(authorityId, apiIds);
		return okay();
	}

	@DeleteMapping("/authoritys/{authority-id}/apis")
	@ResponseBody
	public CommonResponseDto revokeAuthorityForApis(@PathVariable(value = "authority-id") Long authorityId,
			@RequestBody List<Long> apiIds) throws Exception {
		apiAuthorityRelationshipService.revokeAuthorityForApis(authorityId, apiIds);
		return okay();
	}
}
