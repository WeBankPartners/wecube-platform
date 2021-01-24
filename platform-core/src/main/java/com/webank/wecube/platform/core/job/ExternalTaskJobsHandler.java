package com.webank.wecube.platform.core.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.webank.wecube.platform.core.service.workflow.ExternalTaskExecutor;

@Component
public class ExternalTaskJobsHandler {
    private static final Logger log = LoggerFactory.getLogger(ExternalTaskJobsHandler.class);
    
    @Autowired
    private ExternalTaskExecutor externalTaskExecutor;
    
    @Scheduled(cron="0 */5 * * * ?")
    public void extractOutstandingOperationEvents(){
        if(log.isInfoEnabled()){
            log.info("scheduled execution start...");
        }
        
        try{
            externalTaskExecutor.execute();
        }catch(Exception e){
            log.error("external task processing errors", e);
        }
        
        if(log.isInfoEnabled()){
            log.info("scheduled execution end...");
        }
    }

}
