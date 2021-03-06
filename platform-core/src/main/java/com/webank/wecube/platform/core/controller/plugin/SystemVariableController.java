package com.webank.wecube.platform.core.controller.plugin;

import static com.webank.wecube.platform.core.dto.plugin.CommonResponseDto.okay;
import static com.webank.wecube.platform.core.dto.plugin.CommonResponseDto.okayWithData;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webank.wecube.platform.core.dto.plugin.CommonResponseDto;
import com.webank.wecube.platform.core.dto.plugin.QueryRequestDto;
import com.webank.wecube.platform.core.dto.plugin.SystemVariableDto;
import com.webank.wecube.platform.core.service.plugin.SystemVariableService;

@RestController
@RequestMapping("/v1")
public class SystemVariableController {

    @Autowired
    private SystemVariableService systemVariableService;

    @PostMapping("/system-variables/retrieve")
    @PreAuthorize("hasAnyAuthority('ADMIN_SYSTEM_PARAMS','SUB_SYSTEM')")
    public CommonResponseDto retrieveSystemVariables(@RequestBody QueryRequestDto queryRequest) {
//        systemVariableService.validatePermission();
        return okayWithData(systemVariableService.retrieveSystemVariables(queryRequest));
    }

    /**
     * ADMIN_SYSTEM_PARAMS
     * @param resourceSystemVariables
     * @return
     */
    @PostMapping("/system-variables/create")
    @PreAuthorize("hasAnyAuthority('ADMIN_SYSTEM_PARAMS')")
    public CommonResponseDto createSystemVariables(@RequestBody List<SystemVariableDto> resourceSystemVariables) {
        systemVariableService.validatePermission();
        return okayWithData(systemVariableService.createSystemVariables(resourceSystemVariables));
    }

    @PostMapping("/system-variables/update")
    @PreAuthorize("hasAnyAuthority('ADMIN_SYSTEM_PARAMS')")
    public CommonResponseDto updateSystemVariables(@RequestBody List<SystemVariableDto> resourceSystemVariables) {
        systemVariableService.validatePermission();
        return okayWithData(systemVariableService.updateSystemVariables(resourceSystemVariables));
    }

    @PostMapping("/system-variables/delete")
    @PreAuthorize("hasAnyAuthority('ADMIN_SYSTEM_PARAMS')")
    public CommonResponseDto deleteSystemVariables(@RequestBody List<SystemVariableDto> resourceSystemVariables) {
        systemVariableService.validatePermission();
        systemVariableService.deleteSystemVariables(resourceSystemVariables);
        return okay();
    }

    @GetMapping("/system-variables/constant/system-variable-scope")
    @PreAuthorize("hasAnyAuthority('ADMIN_SYSTEM_PARAMS')")
    public CommonResponseDto retrieveSystemVariableScope() {
        systemVariableService.validatePermission();
        return okayWithData(systemVariableService.retrieveSystemVariableScope());
    }
}
