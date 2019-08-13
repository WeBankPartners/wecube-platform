package com.webank.wecube.core.service.workflow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PluginWorkService {
    private static final Logger log = LoggerFactory.getLogger(PluginWorkService.class);
    public static final int SERVICE_TASK_EXECUTE_SUCC = 0;
    public static final int SERVICE_TASK_EXECUTE_ERR = 1;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private ManagementService managementService;

    public void responseServiceTaskResult(String processInstanceBizKey, String executionId, String serviceCode,
                                          int resultCode) {
        log.info("process response for service task, processInstanceBizKey={},serviceCode={},resultCode={}",
                processInstanceBizKey, serviceCode, resultCode);

        ProcessInstance instance = null;

        int repeatTimes = 6;

        while (repeatTimes > 0) {
            instance = runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(processInstanceBizKey)
                    .singleResult();
            if (instance != null) {
                break;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.warn("meet exception, InterruptedException: " + e.getMessage());
                Thread.currentThread().interrupt();
            }

            repeatTimes--;
        }

        if (instance == null) {
            log.error("cannot find process instance with such id, processInstanceBizKey={}", processInstanceBizKey);
            throw new RuntimeException("none process instance found");
        }

        String instanceId = instance.getId();
        String businessKey = instance.getBusinessKey();

        String parentExecutionId = executionId;

        String sql = String.format("select * from %s t where t.PARENT_ID_ = #{parentExecutionId}",
                managementService.getTableName(Execution.class));

        List<Execution> subExecutions = runtimeService.createNativeExecutionQuery().sql(sql)
                .parameter("parentExecutionId", parentExecutionId).list();

        if (subExecutions == null || subExecutions.isEmpty()) {
            log.warn("###### none subexecution found,instance {} businessKey {} execution {}", instanceId, businessKey,
                    parentExecutionId);

            return;
        }

        if (resultCode != SERVICE_TASK_EXECUTE_SUCC) {
            resultCode = SERVICE_TASK_EXECUTE_ERR;
        }

        boolean successful = (resultCode == SERVICE_TASK_EXECUTE_SUCC ? true : false);

        for (Execution ec : subExecutions) {
            String subExecutionId = ec.getId();
            List<EventSubscription> signalEventSubscriptions = runtimeService.createEventSubscriptionQuery()
                    .eventType("signal").executionId(subExecutionId).list();

            if (signalEventSubscriptions.isEmpty()) {
                log.debug(
                        "###### none signal EventSubscription found for instance {} businessKey {} parentExecution {} execution {}",
                        instanceId, businessKey, parentExecutionId, ec.getId());
                continue;
            }
            for (EventSubscription es : signalEventSubscriptions) {
                String eventName = es.getEventName();
                Map<String, Object> boundVariables = new HashMap<String, Object>();
                boundVariables.put("ackCode", resultCode);
                boundVariables.put("ok", successful);

                runtimeService.createSignalEvent(eventName).executionId(ec.getId()).setVariables(boundVariables).send();

                log.debug("###### delivered {} to execution {}, instanceId {}, businessKey {} activityId {}", eventName,
                        es.getId(), instanceId, businessKey, es.getActivityId());
            }
        }

    }
}
