package com.webank.wecube.platform.core.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.webank.wecube.platform.core.service.workflow.ExtraTaskExecutor;

@Component
public class ExtraTaskJobsHandler {
    private static final Logger log = LoggerFactory.getLogger(ExtraTaskJobsHandler.class);
    
    @Autowired
    private ExtraTaskExecutor extraTaskExecutor;
    
    @Scheduled(cron="0 */5 * * * ?")
    public void extractOutstandingExtraTasks(){
        if(log.isInfoEnabled()){
            log.info("scheduled extra task execution start...");
        }
        
        try{
            extraTaskExecutor.execute();
        }catch(Exception e){
            log.error("extra task processing errors", e);
        }
        
        if(log.isInfoEnabled()){
            log.info("scheduled extra task execution end...");
        }
    }

}
