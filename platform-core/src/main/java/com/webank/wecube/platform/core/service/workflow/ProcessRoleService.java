package com.webank.wecube.platform.core.service.workflow;

import java.util.List;

import com.webank.wecube.platform.core.dto.workflow.ProcRoleDto;
import com.webank.wecube.platform.core.dto.workflow.ProcRoleOverviewDto;
import com.webank.wecube.platform.core.dto.workflow.ProcRoleRequestDto;

public interface ProcessRoleService {
    /**
     * 
     * @param procId
     * @return
     */
    ProcRoleOverviewDto retrieveRoleNamesByProcess(String procId);

    /**
     * 
     * @param procId
     * @param procRoleRequestDto
     */
    void createProcRoleBinding(String procId, ProcRoleRequestDto procRoleRequestDto);

    /**
     * 
     * @param procId
     * @param procRoleRequestDto
     */
    void updateProcRoleBinding(String procId, ProcRoleRequestDto procRoleRequestDto);

    /**
     * 
     * @param roleIdList
     * @return
     */
    List<ProcRoleDto> retrieveAllProcessByRoles(List<String> roleIdList);

    /**
     * 
     * @param roleNames
     * @param permission
     * @return
     */
    List<ProcRoleDto> retrieveProcessByRolesAndPermission(List<String> roleNames, String permission);

    /**
     * 
     * @param procId
     * @param procRoleRequestDto
     */
    void deleteProcRoleBinding(String procId, ProcRoleRequestDto procRoleRequestDto);
}
