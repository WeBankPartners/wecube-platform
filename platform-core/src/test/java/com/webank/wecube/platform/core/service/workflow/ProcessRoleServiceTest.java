package com.webank.wecube.platform.core.service.workflow;

import com.google.common.collect.Lists;
import com.webank.wecube.platform.core.DatabaseBasedTest;
import com.webank.wecube.platform.core.entity.workflow.ProcRoleBindingEntity;
import com.webank.wecube.platform.core.jpa.workflow.ProcRoleBindingRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ProcessRoleServiceTest extends DatabaseBasedTest {
    @Autowired
    private ProcessRoleServiceImpl processRoleService;
    @Autowired
    private ProcRoleBindingRepository procRoleBindingRepository;

    @Test
    public void createProcRoleBindingShouldSucceed() {
        ProcRoleBindingEntity procRoleBindingEntityOne = new ProcRoleBindingEntity("1", ProcRoleBindingEntity.permissionEnum.MGMT, 1L);
        ProcRoleBindingEntity procRoleBindingEntityTwo = new ProcRoleBindingEntity("2", ProcRoleBindingEntity.permissionEnum.MGMT, 1L);
        this.procRoleBindingRepository.saveAll(Lists.newArrayList(procRoleBindingEntityOne, procRoleBindingEntityTwo));
        Iterable<ProcRoleBindingEntity> all = this.procRoleBindingRepository.findAll();
        System.out.println(all);
    }


    @Test
    public void retrieveProcRoleBindingShouldSucceed() {

    }

    @Test
    public void updateProcRoleBindingShouldSucceed() {

    }

    @Test
    public void deleteProcRoleBindingShouldSucceed() {

    }
}
