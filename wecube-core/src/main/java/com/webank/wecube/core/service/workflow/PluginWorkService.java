package com.webank.wecube.core.service.workflow;

import static org.apache.commons.lang3.StringUtils.trim;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.webank.wecube.core.commons.WecubeCoreException;
import com.webank.wecube.core.domain.plugin.PluginConfig;
import com.webank.wecube.core.domain.plugin.PluginConfigInterface;
import com.webank.wecube.core.domain.plugin.PluginInstance;
import com.webank.wecube.core.domain.plugin.PluginTriggerCommand;
import com.webank.wecube.core.domain.workflow.TaskNodeExecLogEntity;
import com.webank.wecube.core.domain.workflow.TaskNodeExecVariableEntity;
import com.webank.wecube.core.interceptor.UsernameStorage;
import com.webank.wecube.core.jpa.TaskNodeExecLogEntityRepository;
import com.webank.wecube.core.jpa.TaskNodeExecVariableEntityRepository;
import com.webank.wecube.core.support.cmdb.dto.v2.OperateCiDto;

@Service
public class PluginWorkService {
    private static final Logger log = LoggerFactory.getLogger(PluginWorkService.class);
    public static final int SERVICE_TASK_EXECUTE_SUCC = 0;
    public static final int SERVICE_TASK_EXECUTE_ERR = 1;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private ManagementService managementService;

    @Autowired
    private TaskNodeExecLogEntityRepository taskNodeExecLogEntityRepository;
    
    @Autowired
    TaskNodeExecVariableEntityRepository taskNodeExecVariableEntityRepository;

    public void logFailureExecution(String processInstanceBizKey, String taskNodeId, String errMsg) {
        TaskNodeExecLogEntity execLog = taskNodeExecLogEntityRepository
                .findEntityByInstanceBusinessKeyAndTaskNodeId(processInstanceBizKey, taskNodeId);
        if (execLog != null) {
            execLog.setErrCode(TaskNodeExecLogEntity.ERR_CODE_ERR);
            execLog.setErrMsg(errMsg);
            execLog.setUpdatedTime(new Date());
            execLog.setUpdatedBy(UsernameStorage.getIntance().get());
            execLog.setTaskNodeStatus(ProcessInstanceService.FAULTED);

            taskNodeExecLogEntityRepository.saveAndFlush(execLog);
        } else {
            log.warn("cannot find {} with processInstanceBizKey={},taskNodeId={}",
                    TaskNodeExecLogEntity.class.getSimpleName(), processInstanceBizKey, taskNodeId);
        }
    }
    
    public void logCompleteExecution(String processInstanceBizKey, String taskNodeId, String responseData, String errMsg){
        TaskNodeExecLogEntity execLog = taskNodeExecLogEntityRepository
                .findEntityByInstanceBusinessKeyAndTaskNodeId(processInstanceBizKey, taskNodeId);
        if (execLog != null) {
            execLog.setErrCode(TaskNodeExecLogEntity.ERR_CODE_OK);
            execLog.setResponseData(responseData);
            execLog.setUpdatedTime(new Date());
            execLog.setUpdatedBy(UsernameStorage.getIntance().get());
            execLog.setTaskNodeStatus(ProcessInstanceService.COMPLETED);

            taskNodeExecLogEntityRepository.saveAndFlush(execLog);
        }
    }

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

	public void saveTaskNodeInvocationParameter(PluginTriggerCommand cmd, String processInstanceBizKey,
			int rootCiTypeId, String operator, String serviceName, PluginConfigInterface inf, PluginConfig pluginConfig,
			List<Map<String, Object>> pluginParameters, PluginInstance chosenInstance, String taskNodeId) {
        TaskNodeExecLogEntity execLog = taskNodeExecLogEntityRepository
                .findEntityByInstanceBusinessKeyAndTaskNodeId(processInstanceBizKey, taskNodeId);

        Date curTime = new Date();
        if (execLog == null) {
            log.error("such execution log doesnt exist,bizKey={},nodeId={}", processInstanceBizKey, taskNodeId);
            throw new WecubeCoreException("Execution errors");
        }
        execLog.setPreStatus(inf.getFilterStatus());
        execLog.setPostStatus(inf.getResultStatus());
        execLog.setRootCiTypeId(rootCiTypeId);

        execLog.setUpdatedBy(operator);
        execLog.setUpdatedTime(curTime);

        execLog.setExecutionId(cmd.getProcessExecutionId());
        execLog.setRequestUrl(getInstanceAddress(chosenInstance));
        execLog.setRequestData(marshalRequestData(pluginParameters));

        TaskNodeExecLogEntity savedExecLog = taskNodeExecLogEntityRepository.save(execLog);

        List<TaskNodeExecVariableEntity> vars = taskNodeExecVariableEntityRepository.findEntitiesByExecLog(savedExecLog.getId());


        saveTaskNodeExecVariable(pluginParameters, vars, pluginConfig.getCmdbCiTypeId(), execLog);

	}
	
    private void saveTaskNodeExecVariable(List<Map<String, Object>> pluginParameters, List<TaskNodeExecVariableEntity> vars, int ciTypeId, TaskNodeExecLogEntity execLog) {
        for (Map<String, Object> inputDataMap : pluginParameters) {
            String guid = (String) inputDataMap.get("guid");
            if (StringUtils.isBlank(guid)) {
                continue;
            }

            boolean contains = false;
            for (TaskNodeExecVariableEntity var : vars) {
                if (guid.equalsIgnoreCase(var.getCiGuid())) {
                    contains = true;
                    break;
                }
            }

            if (contains) {
                continue;
            }

            TaskNodeExecVariableEntity execVar = new TaskNodeExecVariableEntity();
            execVar.setCiGuid(guid);
            execVar.setConfirmed(false);
            execVar.setCiTypeId(ciTypeId);
            execVar.setTaskNodeExecLog(execLog);

            taskNodeExecVariableEntityRepository.save(execVar);
        }
    }
    
	
	public List<Map<String,Object>> getTaskNodeProcessResultValues(String processInstanceBizKey, String serviceTaskNodeId, List<String> names) {
		TaskNodeExecLogEntity execLog = taskNodeExecLogEntityRepository
                .findEntityByInstanceBusinessKeyAndTaskNodeId(processInstanceBizKey, serviceTaskNodeId);
		List<Map<String,Object>> resultValues = new LinkedList<>();
        if (execLog != null) {
        	String respData = execLog.getResponseData();
        	if(Strings.isNullOrEmpty(respData)) {
        		return resultValues;
        	}
        	
            ObjectMapper mapper = new ObjectMapper();
            try {
            	List<Map> values = (List<Map>)mapper.readValue(respData, List.class);
            	if(values !=null && values.size()>0) {
            		for(Map itemMap:values) {
            			Map<String,Object> resultItem = new HashMap<>();
            			for(String name:names) {
            				if(itemMap.containsKey(name)) {
            					resultItem.put(name, itemMap.get(names));
            				}
            			}
            			resultValues.add(resultItem);
            		}
            	}
	            return resultValues;
	        } catch (IOException e) {
	            return null;
	        }
        }else {
        	return resultValues;
        }
		
	}
	
	public List<OperateCiDto> getOperateCiObjects(String bizKey) {
        List<TaskNodeExecLogEntity> execLogs = taskNodeExecLogEntityRepository
                .findEntitiesByInstanceBusinessKey(bizKey);

        List<OperateCiDto> operateCiObjects = new ArrayList<OperateCiDto>();

        for (TaskNodeExecLogEntity execLog : execLogs) {
            List<TaskNodeExecVariableEntity> execVars = taskNodeExecVariableEntityRepository
                    .findEntitiesByExecLog(execLog.getId());
            for (TaskNodeExecVariableEntity execVar : execVars) {
                String guid = execVar.getCiGuid();
                int ciTypeId = execVar.getCiTypeId();

                OperateCiDto dto = new OperateCiDto(guid, ciTypeId);
                operateCiObjects.add(dto);
            }
        }

		return operateCiObjects;
	}
	
    private String marshalRequestData(Object data) {
        if (data == null) {
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            String json = mapper.writeValueAsString(data);
            return json;
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private String getInstanceAddress(PluginInstance instance) {
        return trim(instance.getHost()) + ":" + trim(instance.getPort().toString());
    }
       
}
