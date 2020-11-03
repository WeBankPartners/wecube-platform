package com.webank.wecube.platform.core.service.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.workflow.ProcInstInfoDto;
import com.webank.wecube.platform.core.dto.workflow.ProcessDataPreviewDto;
import com.webank.wecube.platform.core.dto.workflow.StartProcInstRequestDto;
import com.webank.wecube.platform.core.dto.workflow.TaskNodeDefObjectBindInfoDto;
import com.webank.wecube.platform.core.entity.event.OperationEventEntity;
import com.webank.wecube.platform.core.entity.workflow.ProcDefInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeDefInfoEntity;
import com.webank.wecube.platform.core.jpa.event.OperationEventRepository;
import com.webank.wecube.platform.core.jpa.workflow.ProcDefInfoMapper;
import com.webank.wecube.platform.core.jpa.workflow.TaskNodeDefInfoRepository;
import com.webank.wecube.platform.core.service.workflow.WorkflowDataService;
import com.webank.wecube.platform.core.service.workflow.WorkflowProcInstService;

@Service
public class OperationEventProcStarter {

    private static final Logger log = LoggerFactory.getLogger(OperationEventProcStarter.class);

    @Autowired
    private ProcDefInfoMapper processDefInfoRepository;

    @Autowired
    private TaskNodeDefInfoRepository taskNodeDefInfoRepository;
    
    @Autowired
    private WorkflowProcInstService workflowProcInstService;
    
    @Autowired
    private OperationEventRepository operationEventRepository;
    
    @Autowired
    private WorkflowDataService workflowDataService;
    
    public OperationEventEntity startInstantOperationEventProcess(OperationEventEntity operEventEntity){
        String procDefKey = operEventEntity.getOperationKey();

        ProcDefInfoEntity procDefEntity = findSuitableProcDefInfoEntityWithProcDefKey(procDefKey);

        if (procDefEntity == null) {
            log.error("such process is not available with procDefKey={}", procDefKey);
            throw new WecubeCoreException("3225",String.format("Process definition key {%s} is NOT available.", procDefKey), procDefKey);
        }
        
        String entityDataId = operEventEntity.getOperationData();
        String entityTypeId = procDefEntity.getRootEntity();
        
        StartProcInstRequestDto initDto = new StartProcInstRequestDto();
        initDto.setEntityDataId(entityDataId);
        initDto.setEntityTypeId(entityTypeId);
        initDto.setProcDefId(procDefEntity.getId());
        
        log.info("try to generate process data preview for :{} {} {}", procDefEntity.getId(), entityTypeId, entityDataId);
        ProcessDataPreviewDto processDataPreview = workflowDataService.generateProcessDataPreview(procDefEntity.getId(), entityDataId);
        String processSessionId = processDataPreview.getProcessSessionId();
        initDto.setProcessSessionId(processSessionId);
        
        ProcInstInfoDto procInst = workflowProcInstService.createProcessInstance(initDto);
        
        operEventEntity.setUpdatedTime(new Date());
        operEventEntity.setProcDefId(procDefEntity.getId());
        operEventEntity.setProcInstId(String.valueOf(procInst.getId()));
        operEventEntity.setProcInstKey(procInst.getProcInstKey());
        
        OperationEventEntity flushedOperEventEntity = operationEventRepository.saveAndFlush(operEventEntity);
        
        return flushedOperEventEntity;
    }

    public void startOperationEventProcess(OperationEventEntity operEventEntity) {
        String procDefKey = operEventEntity.getOperationKey();

        ProcDefInfoEntity procDefEntity = findSuitableProcDefInfoEntityWithProcDefKey(procDefKey);

        if (procDefEntity == null) {
            log.error("such process is not available with procDefKey={}", procDefKey);
            throw new WecubeCoreException("3225",String.format("Process definition key {%s} is NOT available.", procDefKey), procDefKey);
        }

        String entityDataId = operEventEntity.getOperationData();
        String entityTypeId = procDefEntity.getRootEntity();

        StartProcInstRequestDto initDto = new StartProcInstRequestDto();
        initDto.setEntityDataId(entityDataId);
        initDto.setEntityTypeId(entityTypeId);
        initDto.setProcDefId(procDefEntity.getId());

        List<TaskNodeDefInfoEntity> taskNodeDefs = taskNodeDefInfoRepository.findAllByProcDefId(procDefEntity.getId());
        List<TaskNodeDefObjectBindInfoDto> taskNodeBinds = new ArrayList<TaskNodeDefObjectBindInfoDto>();
        
        for(TaskNodeDefInfoEntity tnDef : taskNodeDefs){
            if(TaskNodeDefInfoEntity.NODE_TYPE_SUBPROCESS.equalsIgnoreCase(tnDef.getNodeType())){
                TaskNodeDefObjectBindInfoDto bindDto = new TaskNodeDefObjectBindInfoDto();
                bindDto.setEntityTypeId(entityTypeId);
                bindDto.setEntityDataId(entityDataId);
                bindDto.setNodeDefId(tnDef.getId());
                bindDto.setOrderedNo(tnDef.getOrderedNo());
                
                taskNodeBinds.add(bindDto);
            }
        }
        
        initDto.addAllTaskNodeDefObjectBindInfos(taskNodeBinds);
        
        ProcInstInfoDto procInst = workflowProcInstService.createProcessInstance(initDto);
        
        operEventEntity.setUpdatedTime(new Date());
        operEventEntity.setProcDefId(procDefEntity.getId());
        operEventEntity.setProcInstId(String.valueOf(procInst.getId()));
        operEventEntity.setProcInstKey(procInst.getProcInstKey());
        
        operationEventRepository.saveAndFlush(operEventEntity);
        
    }

    private ProcDefInfoEntity findSuitableProcDefInfoEntityWithProcDefKey(String procDefKey) {
        List<ProcDefInfoEntity> procDefEntities = processDefInfoRepository
                .findAllDeployedProcDefsByProcDefKey(procDefKey, ProcDefInfoEntity.DEPLOYED_STATUS);

        if (procDefEntities == null || procDefEntities.isEmpty()) {
            return null;
        }

        Collections.sort(procDefEntities, new Comparator<ProcDefInfoEntity>() {

            @Override
            public int compare(ProcDefInfoEntity o1, ProcDefInfoEntity o2) {
                if (o1.getProcDefVer() == null && o2.getProcDefVer() == null) {
                    return 0;
                }

                if (o1.getProcDefVer() == null && o2.getProcDefVer() != null) {
                    return -1;
                }

                if (o1.getProcDefVer() != null && o2.getProcDefVer() == null) {
                    return 1;
                }

                if (o1.getProcDefVer() == o2.getProcDefVer()) {
                    return 0;
                }

                return o1.getProcDefVer() > o2.getProcDefVer() ? -1 : 1;
            }

        });

        return procDefEntities.get(0);
    }

}
