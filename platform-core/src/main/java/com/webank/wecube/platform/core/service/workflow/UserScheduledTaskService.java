package com.webank.wecube.platform.core.service.workflow;

import java.util.List;

import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.entity.workflow.UserScheduledTaskEntity;

@Service
public class UserScheduledTaskService {

    public void execute(){
        
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
