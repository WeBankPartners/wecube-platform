package com.webank.wecube.platform.core.controller;

import static com.webank.wecube.platform.core.domain.JsonResponse.okay;
import static com.webank.wecube.platform.core.domain.JsonResponse.okayWithData;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.webank.wecube.platform.core.domain.JsonResponse;
import com.webank.wecube.platform.core.dto.QueryRequest;
import com.webank.wecube.platform.core.dto.SystemVariableDto;
import com.webank.wecube.platform.core.service.SystemVariableService;

@RestController
@RequestMapping("/v1")
public class SystemVariableController {

    @Autowired
    private SystemVariableService systemVariableService;

    @PostMapping("/system-variables/retrieve")
    @ResponseBody
    public JsonResponse retrieveSystemVariables(@RequestBody QueryRequest queryRequest) {
        return okayWithData(systemVariableService.retrieveSystemVariables(queryRequest));
    }

    @PostMapping("/system-variables/create")
    @ResponseBody
    public JsonResponse createSystemVariables(@RequestBody List<SystemVariableDto> resourceSystemVariables) {
        return okayWithData(systemVariableService.createSystemVariables(resourceSystemVariables));
    }

    @PostMapping("/system-variables/update")
    @ResponseBody
    public JsonResponse updateSystemVariables(@RequestBody List<SystemVariableDto> resourceSystemVariables) {
        return okayWithData(systemVariableService.updateSystemVariables(resourceSystemVariables));
    }

    @PostMapping("/system-variables/delete")
    @ResponseBody
    public JsonResponse deleteSystemVariables(@RequestBody List<SystemVariableDto> resourceSystemVariables) {
        systemVariableService.deleteSystemVariables(resourceSystemVariables);
        return okay();
    }
}



