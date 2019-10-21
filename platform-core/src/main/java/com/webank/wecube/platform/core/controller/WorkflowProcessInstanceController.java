package com.webank.wecube.platform.core.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webank.wecube.platform.core.dto.CommonResponseDto;
import com.webank.wecube.platform.core.service.workflow.WorkflowProcessInstanceService;

@RestController
@RequestMapping("/v1/api")
public class WorkflowProcessInstanceController {
    private static final Logger log = LoggerFactory.getLogger(WorkflowProcessInstanceController.class);
    
    @Autowired
    private WorkflowProcessInstanceService procInstService;
    
    @PostMapping("/process/instances")
    public CommonResponseDto createProcessInstance(){
        return null;
    }

    @GetMapping("/process/instances")
    public CommonResponseDto getProcessInstances(){
        return null;
    }
    
    @GetMapping("/process/instances/{id}")
    public CommonResponseDto getProcessInstance(){
        return null;
    }
    
    @PutMapping("/process/instances/{id}")
    public CommonResponseDto modifyProcessInstance(){
        return null;
    }
}
