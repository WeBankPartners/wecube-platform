package com.webank.wecube.platform.core.service.user;

import com.webank.wecube.platform.core.dto.CommonResponseDto;

import java.util.List;

public interface UserManagementService {
    List<CommonResponseDto> createUser(List<Object> requestBody);

    List<CommonResponseDto> retrieveUser();

    List<CommonResponseDto> updateUser(List<Object> requestBody);

    List<CommonResponseDto> deleteUser(List<Object> requestBody);

    List<CommonResponseDto> createRole(List<Object> requestBody);

    List<CommonResponseDto> retrieveRole();

    List<CommonResponseDto> updateRole(List<Object> requestBody);

    List<CommonResponseDto> deleteRole(List<Object> requestBody);

    List<CommonResponseDto> grantRoleToUser(List<Object> requestBody);

    List<CommonResponseDto> revokeRoleFromUser(List<Object> requestBody);

}
