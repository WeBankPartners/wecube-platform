package com.webank.wecube.core.service.workflow.parse;

import java.util.Date;

import javax.annotation.PostConstruct;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.webank.wecube.core.domain.workflow.ServiceInvocationEvent;
import com.webank.wecube.core.domain.workflow.ServiceInvocationEventImpl;
import com.webank.wecube.core.domain.workflow.entity.ProcessInstanceStatusEntity;
import com.webank.wecube.core.domain.workflow.entity.TraceStatus;
import com.webank.wecube.core.jpa.workflow.ProcessInstanceStatusRepository;
import com.webank.wecube.core.service.workflow.WorkflowConstants;
import com.webank.wecube.core.service.workflow.delegate.QueueHolder;

@Component
public class ProcessInstanceEndListener implements ExecutionListener {
    private static final Logger log = LoggerFactory.getLogger(ProcessInstanceEndListener.class);

    @PostConstruct
    public void afterPropertiesSet() {
        log.info("{} added ", ProcessInstanceEndListener.class.getName());
    }

    @Override
    public void notify(DelegateExecution execution) throws Exception {

        ProcessInstanceStatusRepository processInstanceStatusRepository = SpringApplicationContextUtil
                .getBean(ProcessInstanceStatusRepository.class);
        ProcessInstanceStatusEntity procInstEntity = processInstanceStatusRepository
                .findOneByprocInstanceId(execution.getId());

        if (procInstEntity == null) {
            log.error("process instance status doesnt exist,procInstanceId={},procIntanceBizKey={}", execution.getId(),
                    execution.getProcessBusinessKey());
            throw new IllegalStateException("process instance status doesnt exist");
        }

        if (hasErrorEndEvent(execution)) {
            logProcessInstanceError(procInstEntity, processInstanceStatusRepository);
            return;
        }

        if (hasErrorVariable(execution)) {
            logProcessInstanceError(procInstEntity, processInstanceStatusRepository);
            return;
        }

        sendProcessInstanceEndNotification(execution);

        logSuccessEnd(execution);
        logProcessInstanceSuccess(procInstEntity, processInstanceStatusRepository);

    }

    protected void sendProcessInstanceEndNotification(DelegateExecution execution) throws Exception {
        ServiceInvocationEventImpl event = new ServiceInvocationEventImpl();
        event.setBusinessKey(execution.getProcessBusinessKey());
        event.setDefinitionId(execution.getProcessDefinitionId());
        event.setInstanceId(execution.getProcessInstanceId());

        event.setEventType(ServiceInvocationEvent.EventType.PROCESS_END_NOTIFICATION);

        try {
            QueueHolder.putServiceInvocationEvent(event);
        } catch (Throwable e) {
            log.error("plugin invocation errors", e);
            throw e;
        }
    }

    private boolean hasErrorVariable(DelegateExecution execution) {
        Object processWithError = execution.getVariable(WorkflowConstants.VAR_KEY_PROCESS_WITH_ERROR);
        log.info("# varibale {} {}", WorkflowConstants.VAR_KEY_PROCESS_WITH_ERROR, processWithError);

        if (processWithError != null && (Boolean.class.isAssignableFrom(processWithError.getClass()))) {
            if ((boolean) processWithError) {
                logErrorEnd(execution);
                return true;
            }
        }

        return false;
    }

    private boolean hasErrorEndEvent(DelegateExecution execution) {
        if (execution instanceof ExecutionEntity) {
            ExecutionEntity entity = (ExecutionEntity) execution;
            Object typeProperty = entity.getActivity().getProperty("type");
            if ((typeProperty instanceof String)) {
                if ("errorEndEvent".equalsIgnoreCase((String) typeProperty)) {
                    logErrorEnd(execution);
                    return true;
                }
            }
        }

        return false;
    }

    protected void logErrorEnd(DelegateExecution execution) {
        log.error("process {}, businessKey {} ended with error", execution.getProcessInstanceId(),
                execution.getBusinessKey());
    }

    protected void logSuccessEnd(DelegateExecution execution) {
        log.info("# process {}, businessKey {} ended successfully", execution.getProcessInstanceId(),
                execution.getBusinessKey());
    }

    protected void logProcessInstanceError(ProcessInstanceStatusEntity procInstEntity,
            ProcessInstanceStatusRepository processInstanceStatusRepository) {
        Date currTime = new Date();
        procInstEntity.setUpdatedBy(WorkflowConstants.DEFAULT_USER);
        procInstEntity.setUpdatedTime(currTime);
        procInstEntity.setEndTime(currTime);
        procInstEntity.setStatus(TraceStatus.Faulted);

        processInstanceStatusRepository.save(procInstEntity);
    }

    protected void logProcessInstanceSuccess(ProcessInstanceStatusEntity procInstEntity,
            ProcessInstanceStatusRepository processInstanceStatusRepository) {
        Date currTime = new Date();
        procInstEntity.setUpdatedBy(WorkflowConstants.DEFAULT_USER);
        procInstEntity.setUpdatedTime(currTime);
        procInstEntity.setEndTime(currTime);
        procInstEntity.setStatus(TraceStatus.Completed);

        processInstanceStatusRepository.save(procInstEntity);
    }
}
