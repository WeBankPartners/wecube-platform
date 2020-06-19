package com.webank.wecube.platform.core.service.workflow;

import java.util.List;

import com.webank.wecube.platform.core.dto.workflow.ProcRoleDto;
import com.webank.wecube.platform.core.dto.workflow.ProcRoleOverviewDto;
import com.webank.wecube.platform.core.dto.workflow.ProcRoleRequestDto;

public interface ProcessRoleService {
    ProcRoleOverviewDto retrieveRoleIdByProcId(String procId);

    void createProcRoleBinding(String procId, ProcRoleRequestDto procRoleRequestDto);

    void updateProcRoleBinding(String procId, ProcRoleRequestDto procRoleRequestDto);

    List<ProcRoleDto> retrieveAllProcessByRoleIdList(List<String> roleIdList);

    List<ProcRoleDto> retrieveProcessByRoleIdListAndPermission(List<String> roleIdList, String permissionStr);

    void deleteProcRoleBinding(String procId, ProcRoleRequestDto procRoleRequestDto);
}
