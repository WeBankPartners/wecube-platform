package com.webank.wecube.core.service.workflow.parse;

import java.util.Collection;
import java.util.Date;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.NativeHistoricActivityInstanceQuery;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.webank.wecube.core.domain.workflow.entity.ProcessInstanceStatusEntity;
import com.webank.wecube.core.domain.workflow.entity.TraceStatus;
import com.webank.wecube.core.jpa.workflow.ProcessInstanceStatusRepository;
import com.webank.wecube.core.service.workflow.WorkflowConstants;

@Component
public class ProcessInstanceEndListener implements ExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(ProcessInstanceEndListener.class);

    @Override
    public void notify(DelegateExecution execution) throws Exception {

        String procInstanceId = execution.getId();
        String procInstanceBizKey = execution.getProcessBusinessKey();


        ProcessInstanceStatusRepository processInstanceStatusRepository = SpringApplicationContextUtil
                .getBean(ProcessInstanceStatusRepository.class);
        ProcessInstanceStatusEntity procInstEntity = processInstanceStatusRepository.findOneByprocInstanceId(procInstanceId);

        if(procInstEntity == null){
            log.error("process instance status doesnt exist,procInstanceId={},procIntanceBizKey={}", procInstanceId, procInstanceBizKey);
        }
        
        Date currTime = new Date();
        procInstEntity.setUpdatedBy("system");
        procInstEntity.setUpdatedTime(currTime);
        procInstEntity.setEndTime(currTime);
        
        if (execution instanceof ExecutionEntity) {
            ExecutionEntity entity = (ExecutionEntity) execution;
            ActivityImpl activity = entity.getActivity();
            if (activity != null) {
                Object typeProperty = activity.getProperty("type");
                if (typeProperty != null && (typeProperty instanceof String)) {
                    if ("errorEndEvent".equalsIgnoreCase((String) typeProperty)) {
                        log.error("#############################################################");
                        log.error("###### process {} ,businessKey {} ended with error",
                                execution.getProcessInstanceId(), execution.getBusinessKey());
                        log.error("#############################################################");
                        
                        procInstEntity.setStatus(TraceStatus.Faulted);
                        processInstanceStatusRepository.save(procInstEntity);
                        //
                        return;
                    }
                }
            }
        }

        Object processWithError = execution.getVariable(WorkflowConstants.VAR_KEY_PROCESS_WITH_ERROR);
        log.info("###### varibale {} {}", WorkflowConstants.VAR_KEY_PROCESS_WITH_ERROR, processWithError);

        if (processWithError != null && (Boolean.class.isAssignableFrom(processWithError.getClass()))) {
            if ((boolean) processWithError) {
                log.error("#############################################################");
                log.error("###### process {}, businessKey {} ended with error", execution.getProcessInstanceId(),
                        execution.getBusinessKey());
                log.error("#############################################################");
                
                procInstEntity.setStatus(TraceStatus.Faulted);
                processInstanceStatusRepository.save(procInstEntity);

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
            log.error("#############################################################");
            log.error("###### process {}, businessKey {} ended with error", execution.getProcessInstanceId(),
                    execution.getBusinessKey());
            log.error("#############################################################");
            
            procInstEntity.setStatus(TraceStatus.Faulted);
            processInstanceStatusRepository.save(procInstEntity);

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
            log.error("#############################################################");
            log.error("###### process {}, businessKey {} ended with error", execution.getProcessInstanceId(),
                    execution.getBusinessKey());
            log.error("#############################################################");
            
            procInstEntity.setStatus(TraceStatus.Faulted);
            processInstanceStatusRepository.save(procInstEntity);

            return;
        } else {
            log.info("###### none error event found, process {} businessKey {}", execution.getProcessInstanceId(),
                    execution.getBusinessKey());
        }

        log.info("###################################################################");
        log.info("###### process {}, businessKey {} ended normally", execution.getProcessInstanceId(),
                execution.getBusinessKey());
        log.info("###################################################################");
        
        procInstEntity.setStatus(TraceStatus.Completed);
        processInstanceStatusRepository.save(procInstEntity);
    }

}
