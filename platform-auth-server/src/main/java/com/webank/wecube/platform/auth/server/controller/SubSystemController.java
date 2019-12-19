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
import com.webank.wecube.platform.auth.server.dto.CreateSubsystemDto;
import com.webank.wecube.platform.auth.server.dto.CreateUserDto;
import com.webank.wecube.platform.auth.server.service.ApiService;
import com.webank.wecube.platform.auth.server.service.AuthorityService;
import com.webank.wecube.platform.auth.server.service.RoleService;
import com.webank.wecube.platform.auth.server.service.SubsystemService;
import com.webank.wecube.platform.auth.server.service.UserService;

import static com.webank.wecube.platform.auth.server.dto.CommonResponseDto.okayWithData;
import static com.webank.wecube.platform.auth.server.dto.CommonResponseDto.okay;

@RestController
@RequestMapping(ApplicationConstants.ApiInfo.PREFIX_DEFAULT)
public class SubSystemController {

	private static final Logger log = LoggerFactory.getLogger(SubSystemController.class);

	@Autowired
	SubsystemService subsystemService;

	@PostMapping("/sub-systems")
	@ResponseBody
	// TODO
	// @PreAuthorize(value = "hasRole('AUTH_ADMIN') or hasAuthority('SUB_SYSTEM')")
	public CommonResponseDto createRole(@RequestBody CreateSubsystemDto createSubsystemDto, HttpServletRequest request)
			throws Exception {
		return okayWithData(subsystemService.create(createSubsystemDto));
	}

	@GetMapping("/sub-systems")
	@ResponseBody
	public CommonResponseDto retrieveRole(HttpServletRequest request) throws Exception {
		return okayWithData(subsystemService.retrieve());
	}

	@DeleteMapping("/sub-systems/{sub-system-id}")
	@ResponseBody
	public CommonResponseDto deleteRole(@PathVariable(value = "sub-system-id") Long subSystemId,
			HttpServletRequest request) throws Exception {
		subsystemService.delete(subSystemId);
		return okay();
	}
}
