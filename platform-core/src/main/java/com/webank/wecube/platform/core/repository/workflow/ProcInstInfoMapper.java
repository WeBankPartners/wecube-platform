package com.webank.wecube.platform.core.repository.workflow;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.webank.wecube.platform.core.entity.workflow.ProcDefInfoOverviewEntity;
import com.webank.wecube.platform.core.entity.workflow.ProcInstInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.ProcInstInfoQueryEntity;

@Repository
public interface ProcInstInfoMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(ProcInstInfoEntity record);

    int insertSelective(ProcInstInfoEntity record);

    ProcInstInfoEntity selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ProcInstInfoEntity record);

    int updateByPrimaryKey(ProcInstInfoEntity record);

    /**
     * 
     * @param procInstKernelId
     * @return
     */
    ProcInstInfoEntity selectOneByProcInstKernelId(@Param("procInstKernelId") String procInstKernelId);

    /**
     * 
     * @param procDefIds
     * @return
     */
    List<ProcInstInfoEntity> selectByProcDefIdIn(@Param("procDefIds") List<String> procDefIds);

    /**
     * 
     * @param procDefId
     * @return
     */
    List<ProcInstInfoEntity> selectAllByProcDefId(@Param("procDefId") String procDefId);

    /**
     * 
     * @param roleNames
     * @return
     */
    List<ProcInstInfoQueryEntity> selectAllByProcInstInfoByRoleNames(@Param("roleNames") List<String> roleNames);

    /**
     * 
     * @param roleNames
     * @return
     */
    List<ProcInstInfoQueryEntity> selectAllProcInstInfoByCriteria(@Param("roleNames") List<String> roleNames,
            @Param("startTime") Date startTime,
            @Param("endTime") Date endTime,
            @Param("entityDataName") String entityDataName,
            @Param("procInstName") String procInstName,
            @Param("operator") String operator,
            @Param("status") String status,
            @Param("id") Integer id
            );
    
    
    /**
     * 
     * @param id
     * @return
     */
    ProcInstInfoQueryEntity selectQueryEntityByPrimaryKey(@Param("id") Integer id);

    /**
     * 
     * @return
     */
    List<ProcDefInfoOverviewEntity> selectAllProcDefInfoOverviewEntities();

    /**
     * 
     * @param procDefId
     * @param status
     * @return
     */
    int countProcDefInfoOverviewEntities(@Param("procDefId") String procDefId, @Param("status") String status,
            @Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * 
     * @param procDefId
     * @param status
     * @param startDate
     * @param endDate
     * @return
     */
    List<ProcInstInfoEntity> selectProcDefInfoOverviewEntities(@Param("procDefId") String procDefId,
            @Param("status") String status, @Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * 
     * @param procDefNames
     * @param startDate
     * @param endDate
     * @return
     */
    List<ProcDefInfoOverviewEntity> selectAllProcDefInfoOverviewEntitiesByCriteria(
            @Param("procDefIds") List<String> procDefIds, @Param("startDate") Date startDate,
            @Param("endDate") Date endDate, @Param("sortField") String sortField, @Param("sortType") String sortType);

    /**
     * 
     * @param procDefId
     * @param status
     * @param startDate
     * @param endDate
     * @param procBatchKey
     * @return
     */
    int countByProcBatchKey(@Param("procDefName") String procDefName, @Param("status") String status,
            @Param("startDate") Date startDate, @Param("endDate") Date endDate,
            @Param("procBatchKey") String procBatchKey);

    /**
     * 
     * @param procDefId
     * @param status
     * @param startDate
     * @param endDate
     * @param procBatchKey
     * @return
     */
    List<ProcInstInfoEntity> selectAllByProcBatchKey(@Param("procDefName") String procDefName,
            @Param("status") String status, @Param("startDate") Date startDate, @Param("endDate") Date endDate,
            @Param("procBatchKey") String procBatchKey);
}
