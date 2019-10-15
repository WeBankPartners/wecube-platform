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
import com.webank.wecube.platform.auth.server.dto.CreateUserDto;
import com.webank.wecube.platform.auth.server.service.UserService;

import static com.webank.wecube.platform.auth.server.dto.CommonResponseDto.okayWithData;
import static com.webank.wecube.platform.auth.server.dto.CommonResponseDto.okay;

@RestController
@RequestMapping(ApplicationConstants.ApiInfo.PREFIX_DEFAULT)
public class UserController {

	private static final Logger log = LoggerFactory.getLogger(UserController.class);

	@Autowired
	UserService userService;

	@PostMapping("/users")
	@ResponseBody
	// TODO
	// @PreAuthorize(value = "hasRole('AUTH_ADMIN') or hasAuthority('SUB_SYSTEM')")
	public CommonResponseDto createUser(@RequestBody CreateUserDto createUserDto, HttpServletRequest request)
			throws Exception {
		return okayWithData(userService.create(createUserDto));
	}

	@GetMapping("/users")
	@ResponseBody
	public CommonResponseDto retrieveUser(HttpServletRequest request) throws Exception {
		return okayWithData(userService.retrieve());
	}

	@DeleteMapping("/users/{user-id}")
	@ResponseBody
	public CommonResponseDto deleteUser(@PathVariable(value = "user-id") Long id, HttpServletRequest request)
			throws Exception {
		userService.delete(id);
		return okay();
	}
}
