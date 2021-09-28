package com.webank.wecube.platform.core.repository.workflow;

import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.webank.wecube.platform.core.entity.workflow.ProcDefAuthInfoQueryEntity;
import com.webank.wecube.platform.core.entity.workflow.ProcDefInfoEntity;

@Repository
public interface ProcDefInfoMapper {
    int deleteByPrimaryKey(String id);

    int insert(ProcDefInfoEntity record);

    int insertSelective(ProcDefInfoEntity record);

    ProcDefInfoEntity selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(ProcDefInfoEntity record);

    int updateByPrimaryKey(ProcDefInfoEntity record);

    /**
     * 
     * @return
     */
    List<ProcDefInfoEntity> selectAllDeployedOrDraftProcDefs();

    /**
     * 
     * @param procDefName
     * @return
     */
    List<ProcDefInfoEntity> selectAllDeployedProcDefsByProcDefName(@Param("procDefName") String procDefName);

    /**
     * 
     * @param procDefKey
     * @param status
     * @return
     */
    List<ProcDefInfoEntity> selectAllDeployedProcDefsByProcDefKey(@Param("procDefKey") String procDefKey,
            @Param("status") String status);
    
    /**
     * 
     * @param roleNames
     * @return
     */
    List<ProcDefAuthInfoQueryEntity> selectAllAuthorizedProcDefs(@Param("roleNames") Set<String> roleNames);
    
    /**
     * 
     * @param status
     * @return
     */
    List<ProcDefInfoEntity> selectAllProcDefsByStatus(@Param("status") String status);

}
