package com.webank.wecube.platform.core.repository.workflow;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.webank.wecube.platform.core.entity.workflow.UserScheduledTaskEntity;

@Repository
public interface UserScheduledTaskMapper {
    int deleteByPrimaryKey(String id);

    int insert(UserScheduledTaskEntity record);

    int insertSelective(UserScheduledTaskEntity record);

    UserScheduledTaskEntity selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(UserScheduledTaskEntity record);

    int updateByPrimaryKey(UserScheduledTaskEntity record);
    
    /**
     * 
     * @param record
     * @param expectedRev
     * @return
     */
    int updateByPrimaryKeySelectiveCas(@Param("record") UserScheduledTaskEntity record, @Param("expectedRev") int expectedRev);
}