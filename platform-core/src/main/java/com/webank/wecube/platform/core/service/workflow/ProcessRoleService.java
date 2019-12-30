package com.webank.wecube.platform.core.service.workflow;

import com.webank.wecube.platform.core.dto.workflow.ProcRoleDto;
import com.webank.wecube.platform.core.dto.workflow.ProcRoleOverviewDto;
import com.webank.wecube.platform.core.dto.workflow.ProcRoleRequestDto;
import com.webank.wecube.platform.core.entity.workflow.ProcRoleBindingEntity;

import java.util.List;
import java.util.Map;

public interface ProcessRoleService {
    ProcRoleOverviewDto retrieveRoleIdByProcId(String token, String procId);

    void createProcRoleBinding(String procId, ProcRoleRequestDto procRoleRequestDto);

    void updateProcRoleBinding(String token, String procId, ProcRoleRequestDto procRoleRequestDto);

    List<ProcRoleDto> retrieveAllProcessByRoleIdList(List<String> roleIdList);

    List<ProcRoleDto> retrieveProcessByRoleIdListAndPermission(List<String> roleIdList, String permissionStr);

    void deleteProcRoleBinding(String token, String procId, ProcRoleRequestDto procRoleRequestDto);
}
