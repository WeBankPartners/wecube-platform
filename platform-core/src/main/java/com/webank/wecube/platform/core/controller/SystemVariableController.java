package com.webank.wecube.platform.core.controller;

import static com.webank.wecube.platform.core.dto.CommonResponseDto.okay;
import static com.webank.wecube.platform.core.dto.CommonResponseDto.okayWithData;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webank.wecube.platform.core.dto.CommonResponseDto;
import com.webank.wecube.platform.core.dto.QueryRequestDto;
import com.webank.wecube.platform.core.dto.SystemVariableDto;
import com.webank.wecube.platform.core.service.SystemVariableService;

@RestController
@RequestMapping("/v1")
public class SystemVariableController {

    @Autowired
    private SystemVariableService systemVariableService;

    @PostMapping("/system-variables/retrieve")
    public CommonResponseDto retrieveSystemVariables(@RequestBody QueryRequestDto queryRequest) {
        systemVariableService.validatePermission();
        return okayWithData(systemVariableService.retrieveSystemVariables(queryRequest));
    }

    @PostMapping("/system-variables/create")
    public CommonResponseDto createSystemVariables(@RequestBody List<SystemVariableDto> resourceSystemVariables) {
        systemVariableService.validatePermission();
        return okayWithData(systemVariableService.createSystemVariables(resourceSystemVariables));
    }

    @PostMapping("/system-variables/update")
    public CommonResponseDto updateSystemVariables(@RequestBody List<SystemVariableDto> resourceSystemVariables) {
        systemVariableService.validatePermission();
        return okayWithData(systemVariableService.updateSystemVariables(resourceSystemVariables));
    }

    @PostMapping("/system-variables/delete")
    public CommonResponseDto deleteSystemVariables(@RequestBody List<SystemVariableDto> resourceSystemVariables) {
        systemVariableService.validatePermission();
        systemVariableService.deleteSystemVariables(resourceSystemVariables);
        return okay();
    }

    @GetMapping("/system-variables/constant/system-variable-scope")
    public CommonResponseDto retrieveSystemVariableScope() {
        systemVariableService.validatePermission();
        return okayWithData(systemVariableService.retrieveSystemVariableScope());
    }
}
