package com.webank.wecube.platform.core.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.webank.wecube.platform.core.dto.CommonResponseDto;
import com.webank.wecube.platform.core.dto.workflow.InterfaceParameterDto;
import com.webank.wecube.platform.core.dto.workflow.ProcDefInfoDto;
import com.webank.wecube.platform.core.dto.workflow.ProcDefOutlineDto;
import com.webank.wecube.platform.core.dto.workflow.TaskNodeDefBriefDto;
import com.webank.wecube.platform.core.service.workflow.WorkflowProcDefService;

@RestController
@RequestMapping("/v1")
public class WorkflowProcessDefinitionController {
    private static final Logger log = LoggerFactory.getLogger(WorkflowProcessDefinitionController.class);

    @Autowired
    private WorkflowProcDefService procDefService;

    @PostMapping("/process/definitions/deploy")
    public CommonResponseDto deployProcessDefinition(@RequestBody ProcDefInfoDto requestDto) {
        if (log.isDebugEnabled()) {
            log.debug("deploy process:procDefKey={},procDefName={},rootEntity={}", requestDto.getProcDefKey(),
                    requestDto.getProcDefName(), requestDto.getRootEntity());
        }

        ProcDefOutlineDto result = procDefService.deployProcessDefinition(requestDto);
        return CommonResponseDto.okayWithData(result);
    }

    @PostMapping("/process/definitions/draft")
    public CommonResponseDto draftProcessDefinition(@RequestBody ProcDefInfoDto requestDto) {
        if (log.isDebugEnabled()) {
            log.debug("draft process:procDefKey={},procDefName={},rootEntity={}", requestDto.getProcDefKey(),
                    requestDto.getProcDefName(), requestDto.getRootEntity());
        }

        ProcDefInfoDto result = procDefService.draftProcessDefinition(requestDto);
        return CommonResponseDto.okayWithData(result);
    }

    @GetMapping("/process/definitions")
    public CommonResponseDto getProcessDefinitions(
            @RequestParam(name = "includeDraft", required = false, defaultValue = "1") int includeDraft) {

        boolean includeDraftProcDef = (includeDraft == 1 ? true : false);
        List<ProcDefInfoDto> result = procDefService.getProcessDefinitions(includeDraftProcDef);
        return CommonResponseDto.okayWithData(result);
    }

    @GetMapping("/process/definitions/{id}/detail")
    public CommonResponseDto getProcessDefinition(@PathVariable(name = "id") String id) {
        ProcDefInfoDto result = procDefService.getProcessDefinition(id);
        return CommonResponseDto.okayWithData(result);
    }

    @GetMapping("/process/definitions/{id}/outline")
    public CommonResponseDto getProcessDefinitionOutline(@PathVariable(name = "id") String id) {
        ProcDefOutlineDto result = procDefService.getProcessDefinitionOutline(id);
        return CommonResponseDto.okayWithData(result);
    }

    @GetMapping("/process/definitions/{proc-def-id}/tasknodes/briefs")
    public CommonResponseDto getTaskNodeBriefs(@PathVariable("proc-def-id") String procDefId) {
        List<TaskNodeDefBriefDto> result = procDefService.getTaskNodeBriefs(procDefId);

        return CommonResponseDto.okayWithData(result);
    }

    @GetMapping("/process/definitions/{proc-def-id}/tasknodes/{node-def-id}")
    public CommonResponseDto getTaskNodeParameters(@PathVariable("proc-def-id") String procDefId,
            @PathVariable("node-def-id") String nodeDefId) {
        List<InterfaceParameterDto> result = procDefService.getTaskNodeParameters(procDefId,nodeDefId);
        return CommonResponseDto.okayWithData(result);
    }

}
