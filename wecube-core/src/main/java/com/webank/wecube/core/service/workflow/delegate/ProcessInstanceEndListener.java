package com.webank.wecube.core.service.workflow.delegate;

import java.util.Collection;

import javax.annotation.PostConstruct;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.NativeHistoricActivityInstanceQuery;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.webank.wecube.core.domain.workflow.ServiceInvocationEvent;
import com.webank.wecube.core.domain.workflow.ServiceInvocationEventImpl;
import com.webank.wecube.core.service.workflow.WorkflowConstants;

@Component
public class ProcessInstanceEndListener implements ExecutionListener {
    private static final String CONSTANT_PARTITION_LINE = "#############################################################";
    private static final String ERROR_MESSAGE_PROCESS_BUSINESSKEY_ENDED_WITH_ERROR = "###### process {}, businessKey {} ended with error";
    private static final Logger log = LoggerFactory.getLogger(ProcessInstanceEndListener.class);

    @PostConstruct
    public void afterPropertiesSet() {
        log.info("{} added ", ProcessInstanceEndListener.class.getName());
    }

    @Override
    public void notify(DelegateExecution execution) throws Exception {
        if (checkTypeProperty(execution)) {
            return;
        }

        Object processWithError = execution.getVariable(WorkflowConstants.VARIABLE_KEY_PROCESS_WITH_ERROR);
        log.info("###### varibale {} {}", WorkflowConstants.VARIABLE_KEY_PROCESS_WITH_ERROR, processWithError);

        if (processWithError != null && (Boolean.class.isAssignableFrom(processWithError.getClass()))) {
            if ((boolean) processWithError) {
                log.error(ERROR_MESSAGE_PROCESS_BUSINESSKEY_ENDED_WITH_ERROR, execution.getProcessInstanceId(),
                        execution.getBusinessKey());
                return;
            }
        }

        // to check parent process instance
        String sql = String.format(
                "select * from %s t where t.ACT_TYPE_ in ('errorEndEvent', 'boundaryError') and t.PARENT_ACT_INST_ID_ = #{parentActInstId}",
                execution.getProcessEngine().getManagementService().getTableName(HistoricActivityInstance.class));

        NativeHistoricActivityInstanceQuery errorEndEventNativeQuery = execution.getProcessEngine().getHistoryService()
                .createNativeHistoricActivityInstanceQuery().sql(sql).parameter("parentActInstId", execution.getId());

        Collection<HistoricActivityInstance> nativeErrorEndEvents = errorEndEventNativeQuery.list();

        if (nativeErrorEndEvents != null && (!nativeErrorEndEvents.isEmpty())) {
            log.error(ERROR_MESSAGE_PROCESS_BUSINESSKEY_ENDED_WITH_ERROR, execution.getProcessInstanceId(),
                    execution.getBusinessKey());
            return;
        }

        // to check root process instance
        String rootSql = String.format(
                "select * from %s t where t.ACT_TYPE_ in ('errorEndEvent', 'boundaryError') and t.ROOT_PROC_INST_ID_ = #{rootInstanceId}",
                execution.getProcessEngine().getManagementService().getTableName(HistoricActivityInstance.class));

        NativeHistoricActivityInstanceQuery rootErrorEndEventNativeQuery = execution.getProcessEngine()
                .getHistoryService().createNativeHistoricActivityInstanceQuery().sql(rootSql)
                .parameter("rootInstanceId", execution.getProcessInstanceId());

        Collection<HistoricActivityInstance> rootNativeErrorEndEvents = rootErrorEndEventNativeQuery.list();

        if (rootNativeErrorEndEvents != null && (!rootNativeErrorEndEvents.isEmpty())) {
            log.error(ERROR_MESSAGE_PROCESS_BUSINESSKEY_ENDED_WITH_ERROR, execution.getProcessInstanceId(),
                    execution.getBusinessKey());
            return;
        }
        log.info("###### none error event found, process {} businessKey {}", execution.getProcessInstanceId(),
                execution.getBusinessKey());


        log.info(CONSTANT_PARTITION_LINE);
        log.info("###### process {}, businessKey {} ended normally", execution.getProcessInstanceId(),
                execution.getBusinessKey());
        log.info(CONSTANT_PARTITION_LINE);

        log.info("process instance with id [{}] and bizKey [{}] normally ended", execution.getProcessInstanceId(),
                execution.getProcessBusinessKey());

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

    private boolean checkTypeProperty(DelegateExecution execution) {
        if (execution instanceof ExecutionEntity) {
            ExecutionEntity entity = (ExecutionEntity) execution;
            Object typeProperty = entity.getActivity().getProperty("type");
            if ((typeProperty instanceof String)) {
                if ("errorEndEvent".equalsIgnoreCase((String) typeProperty)) {
                    log.error(ERROR_MESSAGE_PROCESS_BUSINESSKEY_ENDED_WITH_ERROR, execution.getProcessInstanceId(),
                            execution.getBusinessKey());
                    return true;
                }
            }
        }
        return false;
    }

}
