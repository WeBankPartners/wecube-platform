package com.webank.wecube.core.service;

import static org.apache.commons.lang3.StringUtils.trim;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.webank.wecube.core.domain.plugin.PluginConfig;
import com.webank.wecube.core.domain.plugin.PluginConfigInterface;
import com.webank.wecube.core.domain.plugin.PluginInstance;
import com.webank.wecube.core.domain.plugin.PluginTriggerCommand;
import com.webank.wecube.core.domain.workflow.TaskNodeExecLogEntity;
import com.webank.wecube.core.domain.workflow.TaskNodeExecVariableEntity;
import com.webank.wecube.core.interceptor.UsernameStorage;
import com.webank.wecube.core.jpa.PluginInstanceRepository;
import com.webank.wecube.core.jpa.TaskNodeExecLogEntityRepository;
import com.webank.wecube.core.jpa.TaskNodeExecVariableEntityRepository;
import com.webank.wecube.core.support.cmdb.dto.v2.OperateCiDto;
import com.webank.wecube.core.support.plugin.PluginInterfaceInvoker.InvocationResult;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
public class PluginInvocationContextService {
    @Autowired
    TaskNodeExecVariableEntityRepository taskNodeExecVariableEntityRepository;
    
    @Autowired
    TaskNodeExecLogEntityRepository taskNodeExecLogEntityRepository;
    
    @Autowired
    PluginInstanceRepository pluginInstanceRepository;
   
	public void saveTaskNodeInvocationParameter(PluginTriggerCommand cmd, String processInstanceBizKey,
			int rootCiTypeId, String operator, String serviceName, PluginConfigInterface inf, PluginConfig pluginConfig,
			List<Map<String, Object>> pluginParameters, PluginInstance chosenInstance) {
		TaskNodeExecLogEntity execLog = taskNodeExecLogEntityRepository
                .findEntityByInstanceBusinessKeyAndTaskNodeId(processInstanceBizKey, cmd.getServiceTaskNodeId());

        Date curTime = new Date();
        if (execLog == null) {
            execLog = new TaskNodeExecLogEntity();
            execLog.setCreatedBy(operator);
            execLog.setCreatedTime(curTime);
            execLog.setInstanceBusinessKey(cmd.getProcessInstanceBizKey());
            execLog.setTaskNodeId(cmd.getServiceTaskNodeId());
            execLog.setPreStatus(inf.getFilterStatus());
            execLog.setPostStatus(inf.getResultStatus());
            execLog.setRootCiTypeId(rootCiTypeId);
            execLog.setServiceName(serviceName);

        } else {
            execLog.setUpdatedBy(operator);
            execLog.setUpdatedTime(curTime);
        }

        execLog.setExecutionId(cmd.getProcessExecutionId());
        execLog.setInstanceId(cmd.getProcessInstanceId());
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
    
	public void saveTaskNodeProcessResponse(InvocationResult pluginResponse, PluginTriggerCommand cmd,
			String processInstanceBizKey) {
		TaskNodeExecLogEntity execLog = taskNodeExecLogEntityRepository
                .findEntityByInstanceBusinessKeyAndTaskNodeId(processInstanceBizKey, cmd.getServiceTaskNodeId());
        if (execLog != null) {
            execLog.setErrCode("0");
            if(pluginResponse.getPluginResponse().isEmpty()) {
            	execLog.setErrMsg("response data is blank");
            }
            execLog.setResponseData(marshalRequestData(pluginResponse.getPluginResponse()));
            execLog.setUpdatedTime(new Date());
            execLog.setUpdatedBy(UsernameStorage.getIntance().get());
            execLog.setTaskNodeStatus("Completed");

            taskNodeExecLogEntityRepository.saveAndFlush(execLog);
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
