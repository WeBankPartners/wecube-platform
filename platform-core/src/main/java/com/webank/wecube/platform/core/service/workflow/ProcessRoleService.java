package com.webank.wecube.platform.core.service.workflow;

import com.webank.wecube.platform.core.dto.workflow.ProcRoleDto;
import com.webank.wecube.platform.core.dto.workflow.ProcRoleResponseDto;
import com.webank.wecube.platform.core.entity.workflow.ProcDefInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.ProcRoleBindingEntity;

import java.util.List;
import java.util.Map;

public interface ProcessRoleService {

    ProcRoleResponseDto retrieveRoleIdByProcId(String procId);

    List<ProcRoleDto> updateProcRoleBinding(String procId, Map<String, List<Long>> permissionRoleMap);

    List<ProcRoleDto> retrieveProcessByRoleIdList(List<Long> roleIdList, ProcRoleBindingEntity.permissionEnum permissionEnum);

    void deleteByProcId(String procId);

}
