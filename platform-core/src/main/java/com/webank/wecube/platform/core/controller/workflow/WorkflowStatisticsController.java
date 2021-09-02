package com.webank.wecube.platform.core.controller.workflow;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webank.wecube.platform.core.dto.plugin.CommonResponseDto;
import com.webank.wecube.platform.core.service.workflow.WorkflowStatisticsService;
import com.webank.wecube.platform.core.support.plugin.dto.WorkflowDefInfoDto;
import com.webank.wecube.platform.core.support.plugin.dto.WorkflowNodeDefInfoDto;

@RestController
@RequestMapping("/v1")
public class WorkflowStatisticsController {
    
    @Autowired
    private WorkflowStatisticsService workflowStatisticsService;

    @GetMapping("/statistics/process/definitions")
    public CommonResponseDto fetchReleasedWorkflowDefs() {
        List<WorkflowDefInfoDto> procDefInfos = workflowStatisticsService.fetchReleasedWorkflowDefs();
        return CommonResponseDto.okayWithData(procDefInfos);
    }
    
    @PostMapping("/statistics/process/definitions/tasknodes/query")
    public CommonResponseDto fetchWorkflowTasknodeInfos() {
        //TODO
        List<WorkflowNodeDefInfoDto> nodeDefInfos = null;
        return CommonResponseDto.okayWithData(nodeDefInfos);
    }
    
    @PostMapping("/statistics/process/definitions/tasknodes/tasknode-bindings/query")
    public CommonResponseDto fetchWorkflowTasknodeBindings() {
        //TODO
        return null;
    }
    
    @PostMapping("/statistics/process/definitions/executions/tasknodes/reports/query")
    public CommonResponseDto fetchWorkflowExecutionTasknodeReports() {
        //TODO
        return null;
    }
    
    @PostMapping("/statistics/process/definitions/executions/plugin/reports/query")
    public CommonResponseDto fetchWorkflowExecutionPluginReports() {
        //TODO
        return null;
    }
    
    @PostMapping("/statistics/process/definitions/executions/report-details/query")
    public CommonResponseDto fetchWorkflowExecutionReportDetails() {
        //TODO
        return null;
    }
}
