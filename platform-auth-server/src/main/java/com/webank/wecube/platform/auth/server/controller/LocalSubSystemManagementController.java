package com.webank.wecube.platform.auth.server.controller;

import static com.webank.wecube.platform.auth.server.dto.CommonResponseDto.okayWithData;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webank.wecube.platform.auth.server.common.ApplicationConstants;
import com.webank.wecube.platform.auth.server.dto.CommonResponseDto;
import com.webank.wecube.platform.auth.server.dto.SimpleSubSystemDto;
import com.webank.wecube.platform.auth.server.service.SubSystemManagementService;

@RestController
@RequestMapping(ApplicationConstants.ApiInfo.PREFIX_DEFAULT)
public class LocalSubSystemManagementController {

	@Autowired
	private SubSystemManagementService subSystemManagementService;

	@PostMapping("/sub-systems")
	public CommonResponseDto registerSubSystem(@RequestBody SimpleSubSystemDto subSystemDto)
			{
		return okayWithData(subSystemManagementService.registerSubSystem(subSystemDto));
	}

	@GetMapping("/sub-systems")
	public CommonResponseDto retrieveAllSubSystems(){
		return okayWithData(subSystemManagementService.retrieveAllSubSystems());
	}
}
