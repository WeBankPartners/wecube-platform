package com.webank.wecube.platform.core.jpa.workflow;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.webank.wecube.platform.core.entity.workflow.ProcExecBindingTmpEntity;

public interface ProcExecBindingTmpRepository extends JpaRepository<ProcExecBindingTmpEntity, Integer> {

	@Query("select t from ProcExecBindingTmpEntity t "
			+ " where t.nodeDefId = :nodeDefId and t.procSessionId = :processSessionId and t.bindType = 'taskNode' ")
	List<ProcExecBindingTmpEntity> getAllByNodeAndSession(@Param("nodeDefId") String nodeDefId,
			@Param("processSessionId") String processSessionId);
	
	@Query("select t from ProcExecBindingTmpEntity t "
			+ " where t.procSessionId = :processSessionId and t.bindType = 'taskNode' ")
	List<ProcExecBindingTmpEntity> getAllBySession(@Param("processSessionId") String processSessionId);
}
