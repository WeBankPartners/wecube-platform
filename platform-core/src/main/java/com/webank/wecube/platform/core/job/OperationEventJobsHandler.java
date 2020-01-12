package com.webank.wecube.platform.core.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.webank.wecube.platform.core.service.event.OperationEventsExecutor;

@Component
public class OperationEventJobsHandler {
    
    private static final Logger log = LoggerFactory.getLogger(OperationEventJobsHandler.class);
    
    @Autowired
    private OperationEventsExecutor operationEventsProcessor;
    
    @Scheduled(cron="")
    public void extractOutstandingOperationEvents(){
        if(log.isDebugEnabled()){
            log.debug("");
        }
        
        try{
            operationEventsProcessor.execute();
        }catch(Exception e){
            log.error("");
        }
    }
    
    

}
