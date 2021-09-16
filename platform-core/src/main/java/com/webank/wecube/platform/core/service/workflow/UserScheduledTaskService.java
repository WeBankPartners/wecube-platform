package com.webank.wecube.platform.core.service.workflow;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webank.wecube.platform.core.commons.AuthenticationContextHolder;
import com.webank.wecube.platform.core.dto.workflow.UserScheduledTaskDto;
import com.webank.wecube.platform.core.dto.workflow.UserScheduledTaskQueryDto;
import com.webank.wecube.platform.core.entity.workflow.UserScheduledTaskEntity;
import com.webank.wecube.platform.core.repository.workflow.UserScheduledTaskMapper;
import com.webank.wecube.platform.core.utils.Constants;
import com.webank.wecube.platform.workflow.commons.LocalIdGenerator;

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
        if (taskDto == null) {
            return null;
        }

        UserScheduledTaskEntity taskEntity = new UserScheduledTaskEntity();
        taskEntity.setId(LocalIdGenerator.generateId());
        taskEntity.setCreatedTime(new Date());
        taskEntity.setCreatedBy(AuthenticationContextHolder.getCurrentUsername());

        String owner = taskDto.getOwner();
        if (StringUtils.isBlank(owner)) {
            owner = AuthenticationContextHolder.getCurrentUsername();
        }

        taskEntity.setOwner(owner);

        taskEntity.setProcDefId(taskDto.getProcDefId());
        taskEntity.setProcDefName(taskDto.getProcDefName());

        taskEntity.setEntityDataId(taskDto.getEntityDataId());
        taskEntity.setEntityDataName(taskDto.getEntityDataName());

        taskEntity.setScheduleMode(taskDto.getScheduleMode());

        String scheduleExpr = taskDto.getScheduleExpr();
        taskEntity.setScheduleExpr(scheduleExpr);
        taskEntity.setStatus(Constants.SCHEDULE_TASK_READY);

        userScheduledTaskMapper.insert(taskEntity);

        taskDto.setId(taskEntity.getId());
        taskDto.setOwner(owner);

        return taskDto;
    }

    /**
     * 
     * @param taskDtos
     */
    @Transactional
    public void stopUserScheduledTasks(List<UserScheduledTaskDto> taskDtos) {
        if (taskDtos == null || taskDtos.isEmpty()) {
            return;
        }

        for (UserScheduledTaskDto taskDto : taskDtos) {
            UserScheduledTaskEntity taskEntity = userScheduledTaskMapper.selectByPrimaryKey(taskDto.getId());
            if (taskEntity == null) {
                continue;
            }

            taskEntity.setUpdatedBy(AuthenticationContextHolder.getCurrentUsername());
            taskEntity.setUpdatedTime(new Date());

            taskEntity.setStatus(Constants.SCHEDULE_TASK_STOPPED);

            userScheduledTaskMapper.updateByPrimaryKeySelective(taskEntity);
        }
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
    @Transactional
    public List<UserScheduledTaskDto> updateUserSchecduledTasks(List<UserScheduledTaskDto> taskDtos) {
        if (taskDtos == null || taskDtos.isEmpty()) {
            return null;
        }

        for (UserScheduledTaskDto taskDto : taskDtos) {
            UserScheduledTaskEntity taskEntity = userScheduledTaskMapper.selectByPrimaryKey(taskDto.getId());
            if (taskEntity == null) {
                continue;
            }

            taskEntity.setUpdatedBy(AuthenticationContextHolder.getCurrentUsername());
            taskEntity.setUpdatedTime(new Date());
            taskEntity.setScheduleMode(taskDto.getScheduleMode());
            taskEntity.setScheduleExpr(taskDto.getScheduleExpr());

            userScheduledTaskMapper.updateByPrimaryKeySelective(taskEntity);
        }
        return taskDtos;
    }

    /**
     * 
     * @param taskDtos
     */
    @Transactional
    public void deleteUserSchecduledTasks(List<UserScheduledTaskDto> taskDtos) {
        if (taskDtos == null || taskDtos.isEmpty()) {
            return;
        }

        for (UserScheduledTaskDto taskDto : taskDtos) {
            UserScheduledTaskEntity taskEntity = userScheduledTaskMapper.selectByPrimaryKey(taskDto.getId());
            if (taskEntity == null) {
                continue;
            }

            taskEntity.setUpdatedBy(AuthenticationContextHolder.getCurrentUsername());
            taskEntity.setUpdatedTime(new Date());

            taskEntity.setStatus(Constants.SCHEDULE_TASK_DELETED);

            userScheduledTaskMapper.updateByPrimaryKeySelective(taskEntity);
        }
    }

    /**
     * 
     * @param taskDtos
     */
    @Transactional
    public void resumeUserScheduledTasks(List<UserScheduledTaskDto> taskDtos) {
        if (taskDtos == null || taskDtos.isEmpty()) {
            return;
        }

        for (UserScheduledTaskDto taskDto : taskDtos) {
            UserScheduledTaskEntity taskEntity = userScheduledTaskMapper.selectByPrimaryKey(taskDto.getId());
            if (taskEntity == null) {
                continue;
            }

            taskEntity.setUpdatedBy(AuthenticationContextHolder.getCurrentUsername());
            taskEntity.setUpdatedTime(new Date());

            taskEntity.setStatus(Constants.SCHEDULE_TASK_READY);

            userScheduledTaskMapper.updateByPrimaryKeySelective(taskEntity);
        }
    }

    /**
     * 
     */
    public void execute() {
        if (log.isDebugEnabled()) {
            log.debug("About to execute user scheduled tasks.");
        }

        try {
            doExecute();
        } catch (Exception e) {

        }

        if (log.isDebugEnabled()) {
            log.debug("Finished executing user scheduled tasks.");
        }
    }

    protected void doExecute() {

        List<UserScheduledTaskEntity> outstandingTasks = scanReadyUserTasks();

        if (outstandingTasks == null || outstandingTasks.isEmpty()) {
            return;
        }

        for (UserScheduledTaskEntity outstandingTask : outstandingTasks) {
            tryHandleSingleUserScheduledTask(outstandingTask);
        }

    }

    protected void tryHandleSingleUserScheduledTask(UserScheduledTaskEntity outstandingTask){
        boolean meetExecution = determineExecution(outstandingTask);
        if(!meetExecution){
            return;
        }
        
        performExecution(outstandingTask);
        
        postPerformExecution(outstandingTask);
    }

    protected List<UserScheduledTaskEntity> scanReadyUserTasks() {
        // TODO
        return null;
    }

    protected boolean determineExecution(UserScheduledTaskEntity userTask) {
        // step 1 check if meets any execution
        boolean meetExecution = false;
        String scheduleMode = userTask.getScheduleMode();
        if(StringUtils.isBlank(scheduleMode)){
            return false;
        }
        
        String status = userTask.getStatus();
        if(!Constants.SCHEDULE_TASK_READY.equalsIgnoreCase(status)){
            return false;
        }
        
        if(Constants.SCHEDULE_MODE_MONTHLY.equalsIgnoreCase(scheduleMode)){
            meetExecution = meetMonthlyExecution(userTask);
        }else if(Constants.SCHEDULE_MODE_WEEKLY.equalsIgnoreCase(scheduleMode)){
            meetExecution = meetWeeklyExecution(userTask);
        }else if(Constants.SCHEDULE_MODE_DAILY.equalsIgnoreCase(scheduleMode)){
            meetExecution = meetDailyExecution(userTask);
        }else if(Constants.SCHEDULE_MODE_HOURLY.equalsIgnoreCase(scheduleMode)){
            meetExecution = meetHourlyExecution(userTask);
        }else{
            //
        }
        
        if(!meetExecution){
            return false;
        }

        // step 2 try to update status and lock
        int expectedRev = userTask.getRev();
        int newRev = expectedRev + 1;
        userTask.setStatus(Constants.SCHEDULE_TASK_RUNNING);
        userTask.setRev(newRev);
        int updateResult = userScheduledTaskMapper.updateByPrimaryKeySelectiveCas(userTask, expectedRev);
        if(updateResult > 0){
            return true; 
        }else{
            log.info("Failed to get lock for user scheduled task:{}", userTask.getId());
            return false;
        }

    }

    protected boolean meetMonthlyExecution(UserScheduledTaskEntity userTask) {
        // TODO
        return false;
    }

    protected boolean meetWeeklyExecution(UserScheduledTaskEntity userTask) {
        // TODO
        return false;
    }

    protected boolean meetDailyExecution(UserScheduledTaskEntity userTask) {
        // TODO
        return false;
    }

    protected boolean meetHourlyExecution(UserScheduledTaskEntity userTask) {
        // TODO
        return false;
    }

    protected void performExecution(UserScheduledTaskEntity userTask) {
        // TODO
    }
    
    protected void postPerformExecution(UserScheduledTaskEntity userTask) {
        // TODO
    }
}
