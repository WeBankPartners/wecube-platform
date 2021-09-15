package com.webank.wecube.platform.core.service.workflow;

import java.util.List;

import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.dto.workflow.UserScheduledTaskDto;
import com.webank.wecube.platform.core.dto.workflow.UserScheduledTaskQueryDto;
import com.webank.wecube.platform.core.entity.workflow.UserScheduledTaskEntity;

@Service
public class UserScheduledTaskService {

    public void execute(){
        
    }
    
    public List<UserScheduledTaskDto> fetchUserScheduledTasks(UserScheduledTaskQueryDto queryDto){
        //TODO
        return null;
    }
    
    public UserScheduledTaskDto createUserScheduledTask(UserScheduledTaskDto taskDto){
        //TODO
        return null;
    }
    
    public List<UserScheduledTaskDto> updateUserSchecduledTasks(List<UserScheduledTaskDto> taskDtos){
        //TODO
        return null;
    }
    
    public void deleteUserSchecduledTasks(List<UserScheduledTaskDto> taskDtos){
        //TODO
    }
    
    public void turnOnUserScheduledTasks(List<UserScheduledTaskDto> taskDtos){
        
    }
    
    public void stopUserScheduledTasks(List<UserScheduledTaskDto> taskDtos){
        
    }
    
    
    
    protected List<UserScheduledTaskEntity> scanReadyUserTasks(){
        //TODO
        return null;
    }
    
    protected boolean determineExecution(UserScheduledTaskEntity userTask){
        //TODO
        return false;
    }
    
    protected void performExecution(UserScheduledTaskEntity userTask){
        //TODO
    }
}
