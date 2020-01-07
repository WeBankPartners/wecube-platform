package com.webank.wecube.platform.auth.server.controller;

import javax.servlet.http.HttpServletRequest;

import com.webank.wecube.platform.auth.server.entity.SysRoleEntity;
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
import com.webank.wecube.platform.auth.server.dto.CreateRoleDto;
import com.webank.wecube.platform.auth.server.dto.CreateUserDto;
import com.webank.wecube.platform.auth.server.service.RoleService;
import com.webank.wecube.platform.auth.server.service.UserService;

import static com.webank.wecube.platform.auth.server.dto.CommonResponseDto.*;

@RestController
@RequestMapping(ApplicationConstants.ApiInfo.PREFIX_DEFAULT)
public class RoleController {

    private static final Logger log = LoggerFactory.getLogger(RoleController.class);

    @Autowired
    RoleService roleService;

    @PostMapping("/roles")
    @ResponseBody
    // TODO
    // @PreAuthorize(value = "hasRole('AUTH_ADMIN') or hasAuthority('SUB_SYSTEM')")
    public CommonResponseDto createRole(@RequestBody CreateRoleDto createRoleDto, HttpServletRequest request)
            throws Exception {
        SysRoleEntity sysRoleEntity;
        try {
            sysRoleEntity = roleService.create(createRoleDto);
        } catch (Exception ex) {
            return error(ex.getMessage());
        }
        return okayWithData(sysRoleEntity);
    }

    @GetMapping("/roles")
    @ResponseBody
    public CommonResponseDto retrieveRole(HttpServletRequest request) {
        return okayWithData(roleService.retrieve());
    }

    @GetMapping("/roles/{role-id}")
    @ResponseBody
    public CommonResponseDto retrieveRoleInfo(@PathVariable(value = "role-id") String roleId) {
        SysRoleEntity result;
        try {
            result = roleService.getRoleByIdIfExisted(roleId);
        } catch (Exception e) {
            return error(e.getMessage());
        }
        return okayWithData(result);
    }

    @DeleteMapping("/roles/{role-id}")
    @ResponseBody
    public CommonResponseDto deleteRole(@PathVariable(value = "role-id") String roleId, HttpServletRequest request) {
        roleService.delete(roleId);
        return okay();
    }
}
