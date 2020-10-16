package com.webank.wecube.platform.auth.server.controller;

import static com.webank.wecube.platform.auth.server.dto.CommonResponseDto.okay;
import static com.webank.wecube.platform.auth.server.dto.CommonResponseDto.okayWithData;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webank.wecube.platform.auth.server.common.ApplicationConstants;
import com.webank.wecube.platform.auth.server.dto.CommonResponseDto;
import com.webank.wecube.platform.auth.server.dto.SimpleLocalUserDto;
import com.webank.wecube.platform.auth.server.dto.SimpleLocalUserPassDto;
import com.webank.wecube.platform.auth.server.service.UserManagementService;

@RestController
@RequestMapping(ApplicationConstants.ApiInfo.PREFIX_DEFAULT)
public class LocalUserManagementController {

    @Autowired
    private UserManagementService userManagementService;

    @PostMapping("/users")
    public CommonResponseDto registerLocalUser(@RequestBody SimpleLocalUserDto userDto) throws Exception {
        SimpleLocalUserDto result = userManagementService.registerLocalUser(userDto);
        return okayWithData(result);
    }
    
    @PostMapping("/users/change-password")
    public CommonResponseDto modifyLocalUserPassword(@RequestBody SimpleLocalUserPassDto userPassDto){
        SimpleLocalUserDto result = userManagementService.modifyLocalUserPassword(userPassDto);
        return okayWithData(result);
    }
    
    @PostMapping("/users/reset-password")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN')")
    public CommonResponseDto resetLocalUserPassword(@RequestBody SimpleLocalUserPassDto userPassDto){
        String result = userManagementService.resetLocalUserPassword(userPassDto);
        return okayWithData(result);
    }

    @PutMapping("/users/usernames/{username}")
    public CommonResponseDto modifyLocalUserInfomation(@PathVariable("username") String username,
            @RequestBody SimpleLocalUserDto userDto) {
        SimpleLocalUserDto result = userManagementService.modifyLocalUserInfomation(username, userDto);
        return okayWithData(result);
    }

    @GetMapping("/users")
    public CommonResponseDto retrieveAllUsers() {
        return okayWithData(userManagementService.retrieveAllActiveUsers());
    }
    
    @GetMapping("/users/{user-id}")
    public CommonResponseDto retrieveUserByUserId(@PathVariable(value = "user-id") String userId){
        return okayWithData(userManagementService.retireveLocalUserByUserid(userId));
    }

    @DeleteMapping("/users/{user-id}")
    public CommonResponseDto unregisterLocalUser(@PathVariable(value = "user-id") String userId) {
        userManagementService.unregisterLocalUser(userId);
        return okay();
    }
    
    @GetMapping("/roles/{role-id}/users")
    public CommonResponseDto getUsersByRoleId(@PathVariable(value = "role-id") String roleId) {
        return okayWithData(userManagementService.getLocalUsersByRoleId(roleId));
    }
    
    @GetMapping("/users/{username}/roles")
    public CommonResponseDto getRolesByUsername(@PathVariable(value = "username") String userName) {
        return okayWithData(userManagementService.getLocalRolesByUsername(userName));
    }

    @PostMapping("/roles/{role-id}/users")
    public CommonResponseDto configureUserRolesById(@PathVariable(value = "role-id") String roleId,
                                               @RequestBody List<SimpleLocalUserDto> userDtos){
        userManagementService.configureUserRolesById(roleId, userDtos);
        return okay();
    }

    @PostMapping("/roles/{role-id}/users/revoke")
    public CommonResponseDto revokeUserRolesById(@PathVariable(value = "role-id") String roleId,
                                                @RequestBody List<SimpleLocalUserDto> userDtos){
        userManagementService.revokeUserRolesById(roleId, userDtos);
        return okay();
    }
}
