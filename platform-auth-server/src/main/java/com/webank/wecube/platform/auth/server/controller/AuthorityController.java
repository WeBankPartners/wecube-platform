package com.webank.wecube.platform.auth.server.controller;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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
import com.webank.wecube.platform.auth.server.dto.CreateApiDto;
import com.webank.wecube.platform.auth.server.dto.CreateAuthorityDto;
import com.webank.wecube.platform.auth.server.dto.CreateRoleDto;
import com.webank.wecube.platform.auth.server.dto.CreateUserDto;
import com.webank.wecube.platform.auth.server.service.ApiService;
import com.webank.wecube.platform.auth.server.service.AuthorityService;
import com.webank.wecube.platform.auth.server.service.RoleService;
import com.webank.wecube.platform.auth.server.service.UserService;

import static com.webank.wecube.platform.auth.server.dto.CommonResponseDto.okayWithData;
import static com.webank.wecube.platform.auth.server.dto.CommonResponseDto.okay;

@RestController
@RequestMapping(ApplicationConstants.ApiInfo.PREFIX_DEFAULT)
public class AuthorityController {

	private static final Logger log = LoggerFactory.getLogger(AuthorityController.class);

	@Autowired
	AuthorityService authorityService;

	@PostMapping("/authorities")
	@ResponseBody
	// TODO
	// @PreAuthorize(value = "hasRole('AUTH_ADMIN') or hasAuthority('SUB_SYSTEM')")
	public CommonResponseDto createRole(@RequestBody CreateAuthorityDto createAuthorityDto, HttpServletRequest request)
			throws Exception {
		return okayWithData(authorityService.create(createAuthorityDto));
	}

	@GetMapping("/authorities")
	@ResponseBody
	public CommonResponseDto retrieveRole(HttpServletRequest request) throws Exception {
		return okayWithData(authorityService.retrieve());
	}

	@DeleteMapping("/authorities/{authority-id}")
	@ResponseBody
	public CommonResponseDto deleteRole(@PathVariable(value = "authority-id") Long authorityId,
			HttpServletRequest request) throws Exception {
		authorityService.delete(authorityId);
		return okay();
	}
}
