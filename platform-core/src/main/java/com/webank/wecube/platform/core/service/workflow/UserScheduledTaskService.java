package com.webank.wecube.platform.core.service.workflow;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.dto.workflow.UserScheduledTaskDto;
import com.webank.wecube.platform.core.dto.workflow.UserScheduledTaskQueryDto;
import com.webank.wecube.platform.core.entity.workflow.UserScheduledTaskEntity;
import com.webank.wecube.platform.core.repository.workflow.UserScheduledTaskMapper;

@Service
public class UserScheduledTaskService {

    private static final Logger log = LoggerFactory.getLogger(UserScheduledTaskService.class);

    @Autowired
    private UserScheduledTaskMapper userScheduledTaskMapper;

    /**
     * 
     * @param taskDto
     * @return
     */
    public UserScheduledTaskDto createUserScheduledTask(UserScheduledTaskDto taskDto) {
        // TODO
        return null;
    }

    /**
     * 
     * @param taskDtos
     */
    public void stopUserScheduledTasks(List<UserScheduledTaskDto> taskDtos) {

    }

    /**
     * 
     * @param queryDto
     * @return
     */
    public List<UserScheduledTaskDto> fetchUserScheduledTasks(UserScheduledTaskQueryDto queryDto) {
        // TODO
        return null;
    }

    /**
     * 
     * @param taskDtos
     * @return
     */
    public List<UserScheduledTaskDto> updateUserSchecduledTasks(List<UserScheduledTaskDto> taskDtos) {
        // TODO
        return null;
    }

    /**
     * 
     * @param taskDtos
     */
    public void deleteUserSchecduledTasks(List<UserScheduledTaskDto> taskDtos) {
        // TODO
    }

    /**
     * 
     * @param taskDtos
     */
    public void resumeUserScheduledTasks(List<UserScheduledTaskDto> taskDtos) {

    }

    /**
     * 
     */
    public void execute() {

    }

    protected List<UserScheduledTaskEntity> scanReadyUserTasks() {
        // TODO
        return null;
    }

    protected boolean determineExecution(UserScheduledTaskEntity userTask) {
        // TODO
        return false;
    }

    protected void performExecution(UserScheduledTaskEntity userTask) {
        // TODO
    }
}
