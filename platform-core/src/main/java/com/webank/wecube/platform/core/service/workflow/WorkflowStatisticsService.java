package com.webank.wecube.platform.core.service.workflow;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.support.plugin.dto.WorkflowDefInfoDto;

@Service
public class WorkflowStatisticsService {
    private static final Logger log = LoggerFactory.getLogger(WorkflowStatisticsService.class);
    
    public List<WorkflowDefInfoDto> fetchReleasedWorkflowDefs(){
        //TODO
        return null;
    }
    
    
}
