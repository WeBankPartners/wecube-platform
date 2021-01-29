package com.webank.wecube.platform.core.repository.workflow;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.webank.wecube.platform.core.entity.workflow.ExtraTaskEntity;

@Repository
public interface ExtraTaskMapper {

    int deleteByPrimaryKey(Long id);

    int insert(ExtraTaskEntity record);

    int insertSelective(ExtraTaskEntity record);

    ExtraTaskEntity selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ExtraTaskEntity record);

    int updateByPrimaryKey(ExtraTaskEntity record);

    /**
     * 
     * @param status
     * @return
     */
    List<ExtraTaskEntity> selectAllByStatus(@Param("status") String status);

    /**
     * 
     * @param record
     * @param expectRev
     * @return
     */
    int updateByPrimaryKeySelectiveCas(@Param("record") ExtraTaskEntity record, @Param("expectRev") int expectRev);

}
