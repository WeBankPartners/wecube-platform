package com.webank.wecube.platform.core.repository.workflow;

import com.webank.wecube.platform.core.entity.workflow.UserScheduledTaskEntity;

public interface UserScheduledTaskMapper {
    int deleteByPrimaryKey(String id);

    int insert(UserScheduledTaskEntity record);

    int insertSelective(UserScheduledTaskEntity record);

    UserScheduledTaskEntity selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(UserScheduledTaskEntity record);

    int updateByPrimaryKey(UserScheduledTaskEntity record);
}