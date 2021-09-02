package com.webank.wecube.platform.core.controller.workflow;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webank.wecube.platform.core.dto.plugin.CommonResponseDto;
import com.webank.wecube.platform.core.dto.workflow.ProcDefInfoDto;
import com.webank.wecube.platform.core.dto.workflow.TaskNodeDefBriefDto;
import com.webank.wecube.platform.core.dto.workflow.TaskNodeDefObjectBindInfoDto;
import com.webank.wecube.platform.core.dto.workflow.WorkflowExecutionReportItemDto;
import com.webank.wecube.platform.core.dto.workflow.WorkflowExecutionReportQueryDto;
import com.webank.wecube.platform.core.service.workflow.WorkflowStatisticsService;

@RestController
@RequestMapping("/v1")
public class WorkflowStatisticsController {
    
    @Autowired
    private WorkflowStatisticsService workflowStatisticsService;

    /**
     * 
     * @return
     */
    @GetMapping("/statistics/process/definitions")
    public CommonResponseDto fetchReleasedWorkflowDefs() {
        List<ProcDefInfoDto> procDefInfos = workflowStatisticsService.fetchReleasedWorkflowDefs();
        return CommonResponseDto.okayWithData(procDefInfos);
    }
    
    /**
     * 
     * @return
     */
    @PostMapping("/statistics/process/definitions/tasknodes/query")
    public CommonResponseDto fetchWorkflowTasknodeInfos(@RequestBody List<String> procDefIds) {
        List<TaskNodeDefBriefDto> nodeDefInfos = workflowStatisticsService.fetchWorkflowTasknodeInfos(procDefIds);
        return CommonResponseDto.okayWithData(nodeDefInfos);
    }
    
    @PostMapping("/statistics/process/definitions/tasknodes/tasknode-bindings/query")
    public CommonResponseDto fetchWorkflowTasknodeBindings(@RequestBody List<String> taskNodeIds) {
        
        List<TaskNodeDefObjectBindInfoDto> bindings = workflowStatisticsService.fetchWorkflowTasknodeBindings(taskNodeIds);
        return CommonResponseDto.okayWithData(bindings);
    }
    
    @PostMapping("/statistics/process/definitions/executions/tasknodes/reports/query")
    public CommonResponseDto fetchWorkflowExecutionTasknodeReports(@RequestBody WorkflowExecutionReportQueryDto queryDto) {
        //TODO
        List<WorkflowExecutionReportItemDto> items = workflowStatisticsService.fetchWorkflowExecutionTasknodeReports();
        return CommonResponseDto.okayWithData(items);
    }
    
    @PostMapping("/statistics/process/definitions/executions/plugin/reports/query")
    public CommonResponseDto fetchWorkflowExecutionPluginReports(@RequestBody WorkflowExecutionReportQueryDto queryDto) {
        //TODO
        List<WorkflowExecutionReportItemDto> items = workflowStatisticsService.fetchWorkflowExecutionPluginReports();
        return CommonResponseDto.okayWithData(items);
    }
    
    @PostMapping("/statistics/process/definitions/executions/report-details/query")
    public CommonResponseDto fetchWorkflowExecutionReportDetails() {
        //TODO
        return null;
    }
}
