package com.webank.wecube.platform.workflow.parse;

import java.util.Collection;
import java.util.Date;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.ServiceTask;
import org.camunda.bpm.model.bpmn.instance.SubProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.webank.wecube.platform.workflow.WorkflowConstants;
import com.webank.wecube.platform.workflow.commons.LocalIdGenerator;
import com.webank.wecube.platform.workflow.entity.ProcessInstanceStatusEntity;
import com.webank.wecube.platform.workflow.entity.ServiceNodeStatusEntity;
import com.webank.wecube.platform.workflow.model.NodeType;
import com.webank.wecube.platform.workflow.model.TraceStatus;
import com.webank.wecube.platform.workflow.repository.ProcessInstanceStatusMapper;
import com.webank.wecube.platform.workflow.repository.ServiceNodeStatusMapper;

/**
 * 
 * @author gavin
 *
 */
@Component("ProcessInstanceStartListener")
public class ProcessInstanceStartListener implements ExecutionListener {
    private static final Logger log = LoggerFactory.getLogger(ProcessInstanceStartListener.class);

    @Override
    public void notify(DelegateExecution execution) throws Exception {
        String procInstanceId = execution.getId();
        String procInstanceBizKey = execution.getProcessBusinessKey();

        ProcessDefinition procDef = execution.getProcessEngine().getRepositoryService().createProcessDefinitionQuery()
                .processDefinitionId(execution.getProcessDefinitionId()).singleResult();

        BpmnModelInstance bpmnModelInstance = execution.getProcessEngine().getRepositoryService()
                .getBpmnModelInstance(procDef.getId());

        log.debug("process starting,procDefId={},procDefName={},procDefKey={},procInstId={},procInstKey={}",
                procDef.getId(), procDef.getName(), procDef.getKey(), procInstanceId, procInstanceBizKey);

        org.camunda.bpm.model.bpmn.instance.Process process = bpmnModelInstance.getModelElementById(procDef.getKey());
        Collection<ServiceTask> serviceTasks = process.getChildElementsByType(ServiceTask.class);
        Collection<SubProcess> subProcesses = process.getChildElementsByType(SubProcess.class);

        Date currTime = new Date();
        ProcessInstanceStatusMapper processInstanceStatusRepository = SpringApplicationContextUtil
                .getBean(ProcessInstanceStatusMapper.class);
        ServiceNodeStatusMapper serviceNodeStatusRepository = SpringApplicationContextUtil
                .getBean(ServiceNodeStatusMapper.class);
        
        ProcessInstanceStatusEntity instanceEntity = new ProcessInstanceStatusEntity();
        instanceEntity.setId(LocalIdGenerator.generateId());
        instanceEntity.setCreatedBy(WorkflowConstants.DEFAULT_USER);
        instanceEntity.setCreatedTime(currTime);
        instanceEntity.setProcDefId(procDef.getId());
        instanceEntity.setProcDefKey(procDef.getKey());
        instanceEntity.setProcDefName(procDef.getName());
        instanceEntity.setProcInstKey(procInstanceBizKey);
        instanceEntity.setProcInstId(procInstanceId);
        instanceEntity.setStartTime(currTime);
        instanceEntity.setStatus(TraceStatus.InProgress);
        
        processInstanceStatusRepository.insert(instanceEntity);

        for (ServiceTask node : serviceTasks) {
            ServiceNodeStatusEntity entity = new ServiceNodeStatusEntity();
            entity.setId(LocalIdGenerator.generateId());
            entity.setCreatedBy(WorkflowConstants.DEFAULT_USER);
            entity.setCreatedTime(currTime);
            entity.setNodeId(node.getId());
            entity.setNodeName(node.getName());
            entity.setNodeType(NodeType.SERVICE_TASK);
            entity.setProcInstKey(procInstanceBizKey);
            entity.setProcInstId(procInstanceId);
            entity.setStartTime(currTime);
            entity.setStatus(TraceStatus.NotStarted);
            entity.setTryTimes(0);

            serviceNodeStatusRepository.insert(entity);
        }

        for (SubProcess node : subProcesses) {
            ServiceNodeStatusEntity entity = new ServiceNodeStatusEntity();
            entity.setId(LocalIdGenerator.generateId());
            entity.setCreatedBy(WorkflowConstants.DEFAULT_USER);
            entity.setCreatedTime(currTime);
            entity.setNodeId(node.getId());
            entity.setNodeName(node.getName());
            entity.setNodeType(NodeType.SUB_PROCESS);
            entity.setProcInstKey(procInstanceBizKey);
            entity.setProcInstId(procInstanceId);
            entity.setStartTime(currTime);
            entity.setStatus(TraceStatus.NotStarted);
            entity.setTryTimes(0);

            serviceNodeStatusRepository.insert(entity);
        }
    }

}
