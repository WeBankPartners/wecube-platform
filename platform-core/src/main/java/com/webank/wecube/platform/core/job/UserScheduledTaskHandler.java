package com.webank.wecube.platform.core.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.webank.wecube.platform.core.service.workflow.UserScheduledTaskService;

@Component
public class UserScheduledTaskHandler {
    private static final Logger log = LoggerFactory.getLogger(UserScheduledTaskHandler.class);
    
    @Autowired
    private UserScheduledTaskService userScheduledTaskService;

    @Scheduled(cron="*/10 * * * * ?")
    public void executeUserScheduledTasks(){
        if(log.isTraceEnabled()){
            log.trace("scheduled user task execution start...");
        }
        
        try{
            userScheduledTaskService.execute();
        }catch(Exception e){
            log.info("scheduled user task processing errors", e);
        }
        
        if(log.isTraceEnabled()){
            log.trace("scheduled user task execution end...");
        }
    }
}
