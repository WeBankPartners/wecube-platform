package com.webank.wecube.platform.core.service.workflow;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.workflow.ProcessInstanceInfoDto;
import com.webank.wecube.platform.core.dto.workflow.StartProcInstanceRequestDto;
import com.webank.wecube.platform.core.dto.workflow.TaskNodeDefObjectBindInfoDto;
import com.webank.wecube.platform.core.entity.workflow.ProcDefInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.ProcExecBindingEntity;
import com.webank.wecube.platform.core.entity.workflow.ProcInstInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeDefInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeInstInfoEntity;
import com.webank.wecube.platform.core.jpa.workflow.ProcExecBindingRepository;
import com.webank.wecube.platform.core.jpa.workflow.ProcInstInfoRepository;
import com.webank.wecube.platform.core.jpa.workflow.ProcessDefInfoRepository;
import com.webank.wecube.platform.core.jpa.workflow.TaskNodeDefInfoRepository;
import com.webank.wecube.platform.core.jpa.workflow.TaskNodeInstInfoRepository;
import com.webank.wecube.platform.workflow.commons.LocalIdGenerator;

@Service
public class WorkflowProcessInstanceService {
    private static final Logger log = LoggerFactory.getLogger(WorkflowProcessInstanceService.class);

    @Autowired
    private ProcessDefInfoRepository processDefInfoRepository;

    @Autowired
    private ProcInstInfoRepository procInstInfoRepository;

    @Autowired
    private TaskNodeDefInfoRepository taskNodeDefInfoRepository;

    @Autowired
    private TaskNodeInstInfoRepository taskNodeInstInfoRepository;
    
    @Autowired
    private ProcExecBindingRepository procExecBindingRepository;
    
    @Autowired
    private WorkflowEngineService workflowEngineService;
    
    public List<ProcessInstanceInfoDto> getProcessInstances(){
        List<ProcessInstanceInfoDto> result = new ArrayList<>();
        List<ProcInstInfoEntity> procInstInfoEntities = procInstInfoRepository.findAll();
        
        for(ProcInstInfoEntity e : procInstInfoEntities){
            ProcessInstanceInfoDto d = new ProcessInstanceInfoDto();
            d.setCreatedTime("");
            d.setId(e.getId());
            d.setOperator(e.getOperator());
            d.setProcDefId(e.getProcDefId());
            d.setProcInstKey(e.getProcInstKey());
            d.setStatus(e.getStatus());
            
            result.add(d);
        }
        
        return result;
    }
    
    public ProcessInstanceInfoDto getProcessInstanceById(Integer id){
        
        
        ProcessInstanceInfoDto d = new ProcessInstanceInfoDto();
        
        return d;
    }

    public ProcessInstanceInfoDto createProcessInstance(StartProcInstanceRequestDto requestDto) {
        if (StringUtils.isBlank(requestDto.getProcDefId())) {
            if (log.isDebugEnabled()) {
                log.debug("Process definition ID is blank.");
            }
            throw new WecubeCoreException("Process definition ID is blank.");
        }

        // TODO verify root object id
        String rootObjectId = null;

        // TODO
        

        String procDefId = requestDto.getProcDefId();
        Optional<ProcDefInfoEntity> procDefInfoEntityOpt = processDefInfoRepository.findById(procDefId);

        if (!procDefInfoEntityOpt.isPresent()) {
            throw new WecubeCoreException(String.format("Invalid process definition ID:%s", procDefId));
        }

        ProcDefInfoEntity procDefInfoEntity = procDefInfoEntityOpt.get();
        if (!ProcDefInfoEntity.DEPLOYED_STATUS.equals(procDefInfoEntity.getStatus())) {
            log.error("expected status {} but {} for procDefId {}", ProcDefInfoEntity.DEPLOYED_STATUS,
                    procDefInfoEntity.getStatus(), procDefId);
            throw new WecubeCoreException(String.format("Invalid process definition ID:%s", procDefId));
        }
        
        if(StringUtils.isBlank(procDefInfoEntity.getProcDefKernelId())){
            log.error("cannot know process definition id for {}", procDefId);
            throw new WecubeCoreException(String.format("Invalid process definition ID:%s", procDefId));
        }

        String processInstanceKey = LocalIdGenerator.generateId();
        
        ProcInstInfoEntity procInstInfoEntity = new ProcInstInfoEntity();
        procInstInfoEntity.setStatus(ProcInstInfoEntity.NOT_STARTED_STATUS);
        procInstInfoEntity.setOperator("admin");
        procInstInfoEntity.setProcDefId(procDefId);
        procInstInfoEntity.setProcDefKey(procDefInfoEntity.getProcDefKey());
        procInstInfoEntity.setProcDefName(procDefInfoEntity.getProcDefName());
        procInstInfoEntity.setProcInstKey(processInstanceKey);

        procInstInfoRepository.save(procInstInfoEntity);

        List<TaskNodeDefInfoEntity> taskNodeDefInfoEntities = taskNodeDefInfoRepository.findAllByProcDefId(procDefId);

        for (TaskNodeDefInfoEntity taskNodeDefInfoEntity : taskNodeDefInfoEntities) {
            TaskNodeInstInfoEntity taskNodeInstInfoEntity = new TaskNodeInstInfoEntity();
            taskNodeInstInfoEntity.setStatus(TaskNodeInstInfoEntity.NOT_STARTED_STATUS);
            taskNodeInstInfoEntity.setNodeDefId(taskNodeDefInfoEntity.getId());
            taskNodeInstInfoEntity.setNodeId(taskNodeDefInfoEntity.getNodeId());
            taskNodeInstInfoEntity.setNodeName(taskNodeDefInfoEntity.getNodeName());
            taskNodeInstInfoEntity.setOperator("admin");
            taskNodeInstInfoEntity.setProcDefId(taskNodeDefInfoEntity.getProcDefId());
            taskNodeInstInfoEntity.setProcDefKey(taskNodeDefInfoEntity.getProcDefKey());
            taskNodeInstInfoEntity.setProcInstId(procInstInfoEntity.getId());
            taskNodeInstInfoEntity.setProcInstKey(procInstInfoEntity.getProcInstKey());

            taskNodeInstInfoRepository.save(taskNodeInstInfoEntity);

            List<TaskNodeDefObjectBindInfoDto> bindInfoDtos = pickUpTaskNodeDefObjectBindInfoDtos(requestDto,
                    taskNodeDefInfoEntity.getId());
            
            for(TaskNodeDefObjectBindInfoDto bindInfoDto : bindInfoDtos){
                ProcExecBindingEntity bindingEntity = new ProcExecBindingEntity();
                bindingEntity.setBindType(ProcExecBindingEntity.BIND_TYPE_TASK_NODE_INSTANCE);
                bindingEntity.setProcInstId(procInstInfoEntity.getId());
                bindingEntity.setProcDefId(procDefId);
                bindingEntity.setNodeDefId(bindInfoDto.getNodeDefId());
                bindingEntity.setTaskNodeInstId(taskNodeInstInfoEntity.getId());
                
                procExecBindingRepository.save(bindingEntity);
            }
        }

       
        //TODO
        ProcessInstance processInstance = workflowEngineService.startProcessInstance(procDefInfoEntity.getProcDefKernelId(), processInstanceKey);
        
        procInstInfoEntity.setUpdatedTime(new Date());
        procInstInfoEntity.setProcInstKernelId(processInstance.getId());
        procInstInfoEntity.setStatus(ProcInstInfoEntity.IN_PROGRESS_STATUS);
        
        procInstInfoRepository.save(procInstInfoEntity);
        
        ProcessInstanceInfoDto instDto = new ProcessInstanceInfoDto();
        instDto.setId(procInstInfoEntity.getId());
        instDto.setOperator(procInstInfoEntity.getOperator());
        instDto.setProcDefId(procDefId);
        instDto.setProcInstKey(processInstance.getBusinessKey());
        instDto.setStatus(procInstInfoEntity.getStatus());
//        instDto.setTaskNodeInstances(taskNodeInstances); //TODO

        return instDto;
    }

    private List<TaskNodeDefObjectBindInfoDto> pickUpTaskNodeDefObjectBindInfoDtos(
            StartProcInstanceRequestDto requestDto, String nodeDefId) {
        List<TaskNodeDefObjectBindInfoDto> result = new ArrayList<>();
        if (requestDto.getTaskNodeBinds() == null) {
            return result;
        }

        for (TaskNodeDefObjectBindInfoDto biDto : requestDto.getTaskNodeBinds()) {
            if (nodeDefId.equals(biDto.getNodeDefId())) {
                result.add(biDto);
            }
        }

        return result;
    }

}
