package com.webank.wecube.platform.core.repository.workflow;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.webank.wecube.platform.core.entity.workflow.GraphNodeEntity;

public interface GraphNodeRepository extends JpaRepository<GraphNodeEntity, Integer> {

	@Query("select t from GraphNodeEntity t where t.processSessionId = :processSessionId")
	List<GraphNodeEntity> findAllByProcessSessionId(@Param("processSessionId")String processSessionId);
	
	@Query("select t from GraphNodeEntity t where t.procInstId = :procInstId")
	List<GraphNodeEntity> findAllByProcInstId(@Param("procInstId")Integer procInstId);
}
