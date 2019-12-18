package com.webank.wecube.platform.core.service.workflow;

import com.webank.wecube.platform.core.DatabaseBasedTest;
import com.webank.wecube.platform.core.dto.workflow.ProcRoleRequestDto;
import com.webank.wecube.platform.core.jpa.workflow.ProcRoleBindingRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class ProcessRoleServiceTest extends DatabaseBasedTest {

    final String PROC_ID = "PROCID";
    final String MGMT_PERMISSION_STR = "MGMT";
    final String USE_PERMISSION_STR = "USE";
    final Integer DATA_SIZE = 10;
    @Autowired
    ProcessRoleServiceImpl processRoleService;
    @Autowired
    ProcRoleBindingRepository procRoleBindingRepository;

    @Test
    public void createProcRoleBindingShouldSucceed() {

        List<ProcRoleRequestDto> mgmtProcRoleRequestDtoList = mockProcRoleRequestDtoList(MGMT_PERMISSION_STR, DATA_SIZE);
        List<ProcRoleRequestDto> useProcRoleRequestDtoList = mockProcRoleRequestDtoList(USE_PERMISSION_STR, DATA_SIZE);
        // bind process and role with MGMT permission
        mgmtProcRoleRequestDtoList.forEach(procRoleRequestDto -> this.processRoleService.updateProcRoleBinding(PROC_ID, procRoleRequestDto));

        // bind process and role with USE permission
        assertThat(this.procRoleBindingRepository.findAllByProcId(PROC_ID).size()).isEqualTo(DATA_SIZE);
        useProcRoleRequestDtoList.forEach(procRoleRequestDto -> this.processRoleService.updateProcRoleBinding(PROC_ID, procRoleRequestDto));
        assertThat(this.procRoleBindingRepository.findAllByProcId(PROC_ID).size()).isEqualTo(DATA_SIZE * 2);
    }

    private List<ProcRoleRequestDto> mockProcRoleRequestDtoList(String permissionStr, Integer dataSize) {
        List<ProcRoleRequestDto> result = new ArrayList<>();
        for (int i = 0; i < dataSize; i++) {
            result.add(new ProcRoleRequestDto(permissionStr, (long) i));
        }
        return result;
    }

}
