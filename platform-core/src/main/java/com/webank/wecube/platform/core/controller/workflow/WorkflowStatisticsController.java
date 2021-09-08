package com.webank.wecube.platform.core.controller.workflow;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webank.wecube.platform.core.dto.plugin.CommonResponseDto;
import com.webank.wecube.platform.core.dto.plugin.QueryResponse;
import com.webank.wecube.platform.core.dto.workflow.ProcDefInfoDto;
import com.webank.wecube.platform.core.dto.workflow.TaskNodeDefBriefDto;
import com.webank.wecube.platform.core.dto.workflow.TaskNodeDefObjectBindInfoDto;
import com.webank.wecube.platform.core.dto.workflow.WorkflowExecutionOverviewDto;
import com.webank.wecube.platform.core.dto.workflow.WorkflowExecutionReportDetailDto;
import com.webank.wecube.platform.core.dto.workflow.WorkflowExecutionReportDetailQueryDto;
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
    @GetMapping("/statistics/process/definitions/tasknodes/service-ids")
    public CommonResponseDto fetchAllPluginConfigInterfaces() {
        List<String> serviceIds = workflowStatisticsService.fetchAllPluginConfigInterfaces();
        return CommonResponseDto.okayWithData(serviceIds);
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

    /**
     * 
     * @param taskNodeIds
     * @return
     */
    @PostMapping("/statistics/process/definitions/tasknodes/tasknode-bindings/query")
    public CommonResponseDto fetchWorkflowTasknodeBindings(@RequestBody List<String> taskNodeIds) {

        List<TaskNodeDefObjectBindInfoDto> bindings = workflowStatisticsService
                .fetchWorkflowTasknodeBindings(taskNodeIds);
        return CommonResponseDto.okayWithData(bindings);
    }
    
    /**
     * 
     * @param taskNodeIds
     * @return
     */
    @PostMapping("/statistics/process/definitions/service-ids/tasknode-bindings/query")
    public CommonResponseDto fetchWorkflowPluginBindings(@RequestBody List<String> serviceIds) {

        List<TaskNodeDefObjectBindInfoDto> bindings = workflowStatisticsService
                .fetchWorkflowPluginBindings(serviceIds);
        return CommonResponseDto.okayWithData(bindings);
    }

    /**
     * 
     * @param queryDto
     * @return
     */
    @PostMapping("/statistics/process/definitions/executions/tasknodes/reports/query")
    public CommonResponseDto fetchWorkflowExecutionTasknodeReports(
            @RequestBody WorkflowExecutionReportQueryDto queryDto) {
        QueryResponse<WorkflowExecutionReportItemDto> itemsResponse = workflowStatisticsService.fetchWorkflowExecutionTasknodeReports(queryDto);
        return CommonResponseDto.okayWithData(itemsResponse);
    }

    /**
     * 
     * @param queryDto
     * @return
     */
    @PostMapping("/statistics/process/definitions/executions/plugin/reports/query")
    public CommonResponseDto fetchWorkflowExecutionPluginReports(
            @RequestBody WorkflowExecutionReportQueryDto queryDto) {
        QueryResponse<WorkflowExecutionReportItemDto> itemsResponse = workflowStatisticsService.fetchWorkflowExecutionPluginReports(queryDto);
        return CommonResponseDto.okayWithData(itemsResponse);
    }

    /**
     * 
     * @param queryDto
     * @return
     */
    @PostMapping("/statistics/process/definitions/executions/tasknodes/report-details/query")
    public CommonResponseDto fetchWorkflowExecutionTasknodeReportDetails(@RequestBody WorkflowExecutionReportDetailQueryDto queryDto) {
        List<WorkflowExecutionReportDetailDto> details = workflowStatisticsService.fetchWorkflowExecutionTasknodeReportDetails(queryDto);
        return CommonResponseDto.okayWithData(details);
    }
    
    /**
     * 
     * @param queryDto
     * @return
     */
    @PostMapping("/statistics/process/definitions/executions/plugin/report-details/query")
    public CommonResponseDto fetchWorkflowExecutionPluginReportDetails(@RequestBody WorkflowExecutionReportDetailQueryDto queryDto) {
        List<WorkflowExecutionReportDetailDto> details  = workflowStatisticsService.fetchWorkflowExecutionPluginReportDetails(queryDto);
        return CommonResponseDto.okayWithData(details);
    }
    
    /**
     * 
     * @return
     */
    @GetMapping("/statistics/process/definitions/executions/overviews")
    public CommonResponseDto fetchWorkflowExecutionOverviews() {
        List<WorkflowExecutionOverviewDto>  overviewDtos= workflowStatisticsService.fetchWorkflowExecutionOverviews();
        return CommonResponseDto.okayWithData(overviewDtos);
    }
}
