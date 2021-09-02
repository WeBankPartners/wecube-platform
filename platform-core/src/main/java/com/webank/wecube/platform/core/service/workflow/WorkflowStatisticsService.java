package com.webank.wecube.platform.core.service.workflow;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.dto.workflow.ProcDefInfoDto;
import com.webank.wecube.platform.core.dto.workflow.TaskNodeDefBriefDto;
import com.webank.wecube.platform.core.dto.workflow.TaskNodeDefObjectBindInfoDto;
import com.webank.wecube.platform.core.dto.workflow.WorkflowExecutionReportItemDto;

@Service
public class WorkflowStatisticsService {
    private static final Logger log = LoggerFactory.getLogger(WorkflowStatisticsService.class);
    
    public List<ProcDefInfoDto> fetchReleasedWorkflowDefs(){
        //TODO
        return null;
    }
    
    public List<TaskNodeDefBriefDto> fetchWorkflowTasknodeInfos(List<String> procDefIds){
        return null;
    }
    
    public List<TaskNodeDefObjectBindInfoDto> fetchWorkflowTasknodeBindings(List<String> taskNodeIds){
        return null;
    }
    
    public List<WorkflowExecutionReportItemDto> fetchWorkflowExecutionTasknodeReports(){
        return null;
    }
    
    public List<WorkflowExecutionReportItemDto> fetchWorkflowExecutionPluginReports(){
        return null;
    }
}
