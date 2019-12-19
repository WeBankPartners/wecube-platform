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
import com.webank.wecube.platform.auth.server.service.UserRoleRelationshipService;

import static com.webank.wecube.platform.auth.server.dto.CommonResponseDto.okayWithData;

import java.util.List;

import static com.webank.wecube.platform.auth.server.dto.CommonResponseDto.okay;

@RestController
@RequestMapping(ApplicationConstants.ApiInfo.PREFIX_DEFAULT)
public class UserRoleRelationshipController {

	private static final Logger log = LoggerFactory.getLogger(UserRoleRelationshipController.class);

	@Autowired
	UserRoleRelationshipService userRoleRelationshipService;

	@GetMapping("/users/{user-name}/roles")
	@ResponseBody
	public CommonResponseDto getRolesByUsername(@PathVariable(value = "user-name") String userName) {
		return okayWithData(userRoleRelationshipService.getRolesByUserName(userName));
	}

	@GetMapping("/roles/{role-id}/users")
	@ResponseBody
	public CommonResponseDto getUsersByRoleId(@PathVariable(value = "role-id") Long roleId) {
		return okayWithData(userRoleRelationshipService.getUsersByRoleId(roleId));
	}

	@PostMapping("/roles/{role-id}/users")
	@ResponseBody
	public CommonResponseDto grantRoleForUsers(@PathVariable(value = "role-id") Long roleId,
			@RequestBody List<Long> userIds) throws Exception {
		userRoleRelationshipService.grantRoleForUsers(roleId, userIds);
		return okay();
	}

	@DeleteMapping("/roles/{role-id}/users")
	@ResponseBody
	public CommonResponseDto revokeRoleForUsers(@PathVariable(value = "role-id") Long roleId,
			@RequestBody List<Long> userIds) throws Exception {
		userRoleRelationshipService.revokeRoleForUsers(roleId, userIds);
		return okay();
	}
}
