package com.webank.wecube.platform.core.controller;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
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

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.CommonResponseDto;
import com.webank.wecube.platform.core.dto.workflow.ProcessDefinitionInfoDto;
import com.webank.wecube.platform.core.dto.workflow.TaskNodeInfoDto;
import com.webank.wecube.platform.core.service.workflow.WorkflowProcessDefinitionService;

@RestController
@RequestMapping("/v1/api")
public class WorkflowProcessDefinitionController {
    private static final Logger log = LoggerFactory.getLogger(WorkflowProcessDefinitionController.class);

    @Autowired
    private WorkflowProcessDefinitionService procDefService;

    @PostMapping("/process/definitions/deploy")
    public CommonResponseDto deployProcessDefinition(@RequestBody ProcessDefinitionInfoDto requestDto) {
        if (log.isDebugEnabled()) {
            log.debug("deploy process:procDefKey={},procDefName={},rootEntity={}", requestDto.getProcDefKey(),
                    requestDto.getProcDefName(),requestDto.getRootEntity());
        }
        
        ProcessDefinitionInfoDto result = procDefService.deployProcessDefinition(requestDto);
        return CommonResponseDto.okayWithData(result);
    }

    @PostMapping("/process/definitions/draft")
    public CommonResponseDto draftProcessDefinition(@RequestBody ProcessDefinitionInfoDto requestDto) {
        if(log.isDebugEnabled()){
            log.debug("draft process:procDefKey={},procDefName={},rootEntity={}", requestDto.getProcDefKey(),
                    requestDto.getProcDefName(),requestDto.getRootEntity());
        }
        
        ProcessDefinitionInfoDto result = procDefService.draftProcessDefinition(requestDto);
        return CommonResponseDto.okayWithData(result);
    }

    @GetMapping("/process/definitions")
    public CommonResponseDto getProcessDefinitions(
            @RequestParam(name = "includeDraft", required = false, defaultValue = "1") int includeDraft) {
        
        boolean includeDraftProcDef = (includeDraft == 1? true : false);
        List<ProcessDefinitionInfoDto> result = procDefService.getProcessDefinitions(includeDraftProcDef);
        return CommonResponseDto.okayWithData(result);
    }

    @GetMapping("/process/definitions/{id}")
    public CommonResponseDto getProcessDefinition(@PathVariable(name = "id") String id) {
        ProcessDefinitionInfoDto result = procDefService.getProcessDefinition(id);
        return CommonResponseDto.okayWithData(result);
    }

}
