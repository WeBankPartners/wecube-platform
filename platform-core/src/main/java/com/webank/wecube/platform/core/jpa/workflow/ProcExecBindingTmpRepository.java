package com.webank.wecube.platform.core.jpa.workflow;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.webank.wecube.platform.core.entity.workflow.ProcExecBindingTmpEntity;

public interface ProcExecBindingTmpRepository extends JpaRepository<ProcExecBindingTmpEntity, Integer> {

	@Query("select t from ProcExecBindingTmpEntity t "
			+ " where t.nodeDefId = :nodeDefId and t.procSessionId = :processSessionId and t.bindType = 'taskNode' ")
	List<ProcExecBindingTmpEntity> findAllNodeBindingsByNodeAndSession(@Param("nodeDefId") String nodeDefId,
			@Param("processSessionId") String processSessionId);
	
	@Query("select t from ProcExecBindingTmpEntity t "
			+ " where t.procSessionId = :processSessionId and t.bindType = 'taskNode' ")
	List<ProcExecBindingTmpEntity> findAllNodeBindingsBySession(@Param("processSessionId") String processSessionId);
	
	@Query("select t from ProcExecBindingTmpEntity t "
            + " where t.procSessionId = :processSessionId and t.bindType = 'process' ")
	List<ProcExecBindingTmpEntity> findAllRootBindingsBySession(@Param("processSessionId") String processSessionId);
}
