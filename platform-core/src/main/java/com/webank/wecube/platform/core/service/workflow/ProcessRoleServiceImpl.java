package com.webank.wecube.platform.core.service.workflow;

import com.webank.wecube.platform.core.dto.workflow.ProcRoleDto;
import com.webank.wecube.platform.core.jpa.workflow.ProcRoleBindingRepository;
import com.webank.wecube.platform.core.service.user.UserManagementServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ProcessRoleServiceImpl implements ProcessRoleService {
    private ProcRoleBindingRepository procRoleBindingRepository;

    @Autowired
    public ProcessRoleServiceImpl(ProcRoleBindingRepository procRoleBindingRepository) {
        this.procRoleBindingRepository = procRoleBindingRepository;
    }

    @Override
    public List<ProcRoleDto> updateProcRoleBinding(String procId, Map<String, List<String>> permissionRoleMap) {
        return null;
    }
}
