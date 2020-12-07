package com.webank.wecube.platform.core.controller.workflow;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webank.wecube.platform.core.dto.plugin.CommonResponseDto;
import com.webank.wecube.platform.core.dto.workflow.DynamicWorkflowInstCreationInfoDto;
import com.webank.wecube.platform.core.dto.workflow.DynamicWorkflowInstInfoDto;
import com.webank.wecube.platform.core.dto.workflow.ProcInstTerminationRequestDto;
import com.webank.wecube.platform.core.dto.workflow.WorkflowDefInfoDto;
import com.webank.wecube.platform.core.dto.workflow.WorkflowNodeDefInfoDto;
import com.webank.wecube.platform.core.service.workflow.WorkflowPublicAccessService;

@RestController
@RequestMapping("/v1")
public class WorkflowPublicAccessController {
    
    @Autowired
    private WorkflowPublicAccessService workflowPublicAccessService;
    
    @GetMapping("/release/process/definitions")
    public CommonResponseDto fetchLatestReleasedWorkflowDefs() {
        List<WorkflowDefInfoDto> procDefInfos = workflowPublicAccessService.fetchLatestReleasedWorkflowDefs();
        return CommonResponseDto.okayWithData(procDefInfos);
    }
    
    @GetMapping("/release/process/definitions/{proc-def-id}/tasknodes")
    public CommonResponseDto fetchWorkflowTasknodeInfos(@PathVariable("proc-def-id")String procDefId) {
        List<WorkflowNodeDefInfoDto> nodeDefInfos = workflowPublicAccessService.fetchWorkflowTasknodeInfos(procDefId);
        return CommonResponseDto.okayWithData(nodeDefInfos);
    }

    
    @PostMapping("/release/process/instances")
    public CommonResponseDto createNewWorkflowInstance(@RequestBody DynamicWorkflowInstCreationInfoDto creationInfoDto) {
        DynamicWorkflowInstInfoDto procInstInfo = workflowPublicAccessService.createNewWorkflowInstance(creationInfoDto);
        return CommonResponseDto.okayWithData(procInstInfo);
    }
    
    @PostMapping("/release/process/instances/{proc-inst-id}/terminations")
    public CommonResponseDto createWorkflowInstanceTerminationRequest(@RequestBody ProcInstTerminationRequestDto requestDto){
        workflowPublicAccessService.createWorkflowInstanceTerminationRequest(requestDto);
        return CommonResponseDto.okay();
    }
}
