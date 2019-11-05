package com.webank.wecube.platform.core.controller;

import static com.webank.wecube.platform.core.domain.JsonResponse.*;
import static com.webank.wecube.platform.core.domain.MenuItem.MENU_ADMIN_BASE_DATA_MANAGEMENT;

import java.util.List;

import javax.annotation.security.RolesAllowed;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.webank.wecube.platform.core.domain.JsonResponse;
import com.webank.wecube.platform.core.domain.SystemVariable;
import com.webank.wecube.platform.core.service.SystemVariableService;

@RestController
//@RolesAllowed({MENU_ADMIN_BASE_DATA_MANAGEMENT})
public class SystemVariableController {

    @Autowired
    private SystemVariableService systemVariableService;

    @GetMapping("/system-variables/supported-scope-types")
    @ResponseBody
    public JsonResponse getSupportedScopeTypes() {
        List<String> types = systemVariableService.getSupportedScopeTypes();
        return okayWithData(types);
    }

    @GetMapping("/system-variables/global")
    @ResponseBody
    public JsonResponse getGlobalSystemVariables(@RequestParam(value = "status", required = false) String status) {
        List<SystemVariable> variables = systemVariableService.getGlobalSystemVariables(status);
        return okayWithData(variables);
    }

    @GetMapping("/system-variables")
    @ResponseBody
    public JsonResponse getSystemVariables(@RequestParam(value = "scope-type") String scopeType
            , @RequestParam(value = "scope-value") String scopeValue
            , @RequestParam(value = "status", required = false) String status) {
        List<SystemVariable> variables = systemVariableService.getSystemVariables(scopeType, scopeValue, status);
        return okayWithData(variables);
    }

    @GetMapping("/system-variables/all")
    @ResponseBody
    public JsonResponse getAllSystemVariables(@RequestParam(value = "status", required = false) String status) {
        List<SystemVariable> variables = systemVariableService.getAllSystemVariables(status);
        return okayWithData(variables);
    }

    @GetMapping("/system-variables/{var-id}")
    @ResponseBody
    public JsonResponse getSystemVariableById(@PathVariable(value = "var-id") int varId) {
        SystemVariable systemVariable;
        try {
            systemVariable = systemVariableService.getSystemVariableById(varId);
        } catch (WecubeCoreException ex){
            return error(ex.getMessage());
        }
        return okayWithData(systemVariable);
    }

    @PostMapping("/system-variables/save")
    @ResponseBody
    public JsonResponse saveSystemVariables(@RequestBody List<SystemVariable> variables) {
        return okayWithData(systemVariableService.saveSystemVariables(variables));
    }

    @PostMapping("/system-variables/enable")
    @ResponseBody
    public JsonResponse enableSystemVariables(@RequestBody List<Integer> variableIds) {
        systemVariableService.enableSystemVariables(variableIds);
        return okay();
    }

    @PostMapping("/system-variables/disable")
    @ResponseBody
    public JsonResponse disableSystemVariables(@RequestBody List<Integer> variableIds) {
        systemVariableService.disableSystemVariables(variableIds);
        return okay();
    }

    @PostMapping("/system-variables/delete")
    @ResponseBody
    public JsonResponse deleteSystemVariables(@RequestBody List<Integer> variableIds) {
        systemVariableService.deleteSystemVariables(variableIds);
        return okay();
    }
}



