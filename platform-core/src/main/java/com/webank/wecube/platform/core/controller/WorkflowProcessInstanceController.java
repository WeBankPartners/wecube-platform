package com.webank.wecube.platform.core.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webank.wecube.platform.core.dto.CommonResponseDto;
import com.webank.wecube.platform.core.dto.workflow.ProcInstInfoDto;
import com.webank.wecube.platform.core.dto.workflow.ProcInstOutlineDto;
import com.webank.wecube.platform.core.dto.workflow.StartProcInstRequestDto;
import com.webank.wecube.platform.core.service.workflow.WorkflowProcInstService;

@RestController
@RequestMapping("/v1")
public class WorkflowProcessInstanceController {

    @Autowired
    private WorkflowProcInstService procInstService;

    @PostMapping("/process/instances")
    public CommonResponseDto createProcessInstance(StartProcInstRequestDto requestDto) {
        ProcInstInfoDto result = procInstService.createProcessInstance(requestDto);
        return CommonResponseDto.okayWithData(result);
    }

    @GetMapping("/process/instances")
    public CommonResponseDto getProcessInstances() {
        List<ProcInstInfoDto> result = procInstService.getProcessInstances();
        return CommonResponseDto.okayWithData(result);
    }

    @GetMapping("/process/instances/{id}")
    public CommonResponseDto getProcessInstance(@PathVariable(name="id") Integer procInstId) {
        ProcInstInfoDto result = procInstService.getProcessInstanceById(procInstId);
        if(result == null){
            return CommonResponseDto.error(String.format("Process instance [%s] does not exist.", procInstId));
        }
        
        return CommonResponseDto.okayWithData(result);
    }
    
//    @GetMapping("/process/instances/{id}/outline")
//    public CommonResponseDto getProcessInstanceOutline(@PathVariable(name="id") Integer procInstId){
//        ProcInstOutlineDto result = procInstService.getProcessInstanceOutline(procInstId);
//        
//        
//    }

    @PutMapping("/process/instances/{id}")
    public CommonResponseDto modifyProcessInstance() {
        return null;
    }
}
