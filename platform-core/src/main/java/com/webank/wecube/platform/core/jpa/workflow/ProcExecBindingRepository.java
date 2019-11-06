package com.webank.wecube.platform.core.jpa.workflow;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.webank.wecube.platform.core.entity.workflow.ProcExecBindingEntity;

public interface ProcExecBindingRepository extends JpaRepository<ProcExecBindingEntity, Integer> {

    @Query("select t from ProcExecBindingEntity t "
            + " where t.procInstId = :procInstId and t.taskNodeInstId = :nodeInstId and t.bindType = 'taskNode' ")
    List<ProcExecBindingEntity> findAllTaskNodeBindings(@Param("nodeInstId") Integer procInstId,
            @Param("nodeInstId") Integer nodeInstId);
}
