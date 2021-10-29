package com.webank.wecube.platform.core.repository.workflow;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.webank.wecube.platform.core.entity.workflow.ProcExecBindingEntity;
import com.webank.wecube.platform.core.entity.workflow.ProcExecBindingPluginStatistics;
import com.webank.wecube.platform.core.entity.workflow.ProcExecBindingTasknodeStatistics;

@Repository
public interface ProcExecBindingMapper {

    int deleteByPrimaryKey(Integer id);

    int insert(ProcExecBindingEntity record);

    int insertSelective(ProcExecBindingEntity record);

    ProcExecBindingEntity selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ProcExecBindingEntity record);

    int updateByPrimaryKey(ProcExecBindingEntity record);

    /**
     * 
     * @param procInstId
     * @param nodeInstId
     * @return
     */
    List<ProcExecBindingEntity> selectAllTaskNodeBindings(@Param("procInstId") Integer procInstId,
            @Param("nodeInstId") Integer nodeInstId);

    /**
     * 
     * @param procInstId
     * @return
     */
    ProcExecBindingEntity selectProcInstBindings(@Param("procInstId") Integer procInstId);

    /**
     * 
     * @param procInstId
     * @return
     */
    List<ProcExecBindingEntity> selectAllTaskNodeBindingsByProcInstId(@Param("procInstId") Integer procInstId);

    /**
     * 
     * @param procInstId
     * @param nodeInstId
     * @return
     */
    List<ProcExecBindingEntity> selectAllBoundTaskNodeBindings(@Param("procInstId") Integer procInstId,
            @Param("nodeInstId") Integer nodeInstId);

    /**
     * 
     * @param procInstId
     * @param nodeInstId
     * @return
     */
    int deleteAllTaskNodeBindings(@Param("procInstId") Integer procInstId, @Param("nodeInstId") Integer nodeInstId);

    /**
     * 
     * @param entityDataId
     * @return
     */
    List<Integer> selectAllBoundRunningProcInstances(@Param("entityDataId") String entityDataId);

    /**
     * 
     * @param entityDataId
     * @return
     */
    int countAllBoundRunningProcInstances(@Param("entityDataId") String entityDataId);

    /**
     * 
     * @param entityDataId
     * @return
     */
    List<Integer> selectAllExclusiveBoundRunningProcInstances(@Param("entityDataId") String entityDataId);

    /**
     * 
     * @param entityDataId
     * @return
     */
    int countAllExclusiveBoundRunningProcInstances(@Param("entityDataId") String entityDataId);

    /**
     * 
     * @param entityDataId
     * @return
     */
    List<Integer> selectAllBoundRunningProcInstancesWithoutProcInst(@Param("entityDataId") String entityDataId,
            @Param("procInstId") int procInstId);

    /**
     * 
     * @param entityDataId
     * @return
     */
    int countAllBoundRunningProcInstancesWithoutProcInst(@Param("entityDataId") String entityDataId,
            @Param("procInstId") int procInstId);

    /**
     * 
     * @param entityDataId
     * @return
     */
    List<Integer> selectAllExclusiveBoundRunningProcInstancesWithoutProcInst(@Param("entityDataId") String entityDataId,
            @Param("procInstId") int procInstId);

    /**
     * 
     * @param entityDataId
     * @return
     */
    int countAllExclusiveBoundRunningProcInstancesWithoutProcInst(@Param("entityDataId") String entityDataId,
            @Param("procInstId") int procInstId);

    /**
     * 
     * @param nodeDefId
     * @return
     */
    List<ProcExecBindingEntity> selectAllTaskNodeBindingsByNodeDef(@Param("nodeDefId") String nodeDefId);

    /**
     * 
     * @param taskNodeIds
     * @param entityDataIds
     * @return
     */
    List<ProcExecBindingTasknodeStatistics> selectAllProcExecBindingTasknodeStatistics(
            @Param("nodeDefIds") List<String> nodeDefIds, @Param("entityDataIds") List<String> entityDataIds,
            @Param("startDate") Date startDate, @Param("endDate") Date endDate, @Param("sortField") String sortField,
            @Param("sortType") String sortType);

    /**
     * 
     * @param serviceIds
     * @param entityDataIds
     * @return
     */
    List<ProcExecBindingPluginStatistics> selectAllProcExecBindingPluginStatistics(
            @Param("serviceIds") List<String> serviceIds, @Param("entityDataIds") List<String> entityDataIds,
            @Param("startDate") Date startDate, @Param("endDate") Date endDate, @Param("sortField") String sortField,
            @Param("sortType") String sortType);

    /**
     * 
     * @param procInstId
     * @param entityDataId
     * @return
     */
    List<ProcExecBindingEntity> selectAllTaskNodeBindingsByProcInstIdAndDataId(@Param("procInstId") Integer procInstId,
            @Param("entityDataId") String entityDataId);

}
