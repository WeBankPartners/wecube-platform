package com.webank.wecube.platform.core.service.workflow;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.entity.workflow.ExtraTaskEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeInstInfoEntity;
import com.webank.wecube.platform.core.model.workflow.PluginInvocationCommand;
import com.webank.wecube.platform.core.repository.workflow.ExtraTaskMapper;

@Service
public class TaskNodeDynamicBindRetryProcessor {

    private static final Logger log = LoggerFactory.getLogger(TaskNodeDynamicBindRetryProcessor.class);

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ExtraTaskMapper extraTaskMapper;
    
    @Autowired
    private PluginInvocationService pluginInvocationService;
    
    @Autowired
    private WorkflowEngineService workflowEngineService;

    public void process(ExtraTaskEntity taskEntity) {
        log.info("About to process extra task:{}:{}", taskEntity.getId(), taskEntity.getTaskType());
        String taskDef = taskEntity.getTaskDef();
        if(StringUtils.isBlank(taskDef)){
            log.info("task def is blank.");
            return;
        }
        
        PluginInvocationCommand cmd = null;
        try {
             cmd = objectMapper.readValue(taskDef, PluginInvocationCommand.class);
        } catch (Exception e) {
           log.error("errors while read json to object.", e);
           throw new WecubeCoreException("Failed to read task definition.", e);
        }

        if(cmd == null){
            return;
        }
        
        String taskNodeStatus = workflowEngineService.getTaskNodeStatus(cmd.getProcInstId(), cmd.getNodeId());
        if(!TaskNodeInstInfoEntity.IN_PROGRESS_STATUS.equalsIgnoreCase(taskNodeStatus)){
            log.info("current task node status is : {}, and ignore to execute.", taskNodeStatus);
        }else{
            pluginInvocationService.invokePluginInterface(cmd);
        }
        
        int expectRev = taskEntity.getRev();
        taskEntity.setUpdatedTime(new Date());
        taskEntity.setRev(expectRev + 1); 
        taskEntity.setStatus(ExtraTaskEntity.STATUS_COMPLETED);
        taskEntity.setEndTime(new Date());
        
        extraTaskMapper.updateByPrimaryKeySelectiveCas(taskEntity, expectRev);
    }

}
