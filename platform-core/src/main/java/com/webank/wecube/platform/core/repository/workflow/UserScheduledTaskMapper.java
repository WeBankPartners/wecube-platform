package com.webank.wecube.platform.core.repository.workflow;

import java.util.Date;
import java.util.List;

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
    int updateByPrimaryKeySelectiveCas(@Param("record") UserScheduledTaskEntity record,
            @Param("expectedRev") int expectedRev);

    /**
     * 
     * @return
     */
    List<UserScheduledTaskEntity> selectAllOutstandingTasks();

    /**
     * 
     * @param procDefName
     * @param entityDataId
     * @param owner
     * @param startTime
     * @param endTime
     * @return
     */
    List<UserScheduledTaskEntity> selectAllAvailableTasksWithFilters(@Param("procDefName") String procDefName,
            @Param("entityDataId") String entityDataId, @Param("owner") String owner,@Param("scheduleMode") String scheduleMode,
            @Param("startDate") Date startDate, @Param("endDate") Date endDate);
}