package com.webank.wecube.platform.core.service.workflow;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecube.platform.core.commons.ApplicationProperties;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.entity.plugin.PluginConfigInterfaceParameters;
import com.webank.wecube.platform.core.entity.plugin.PluginConfigInterfaces;
import com.webank.wecube.platform.core.entity.workflow.ProcExecBindingEntity;
import com.webank.wecube.platform.core.entity.workflow.ProcInstInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeDefInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeInstInfoEntity;
import com.webank.wecube.platform.core.model.workflow.PluginInvocationCommand;
import com.webank.wecube.platform.core.model.workflow.WorkflowNotifyEvent;
import com.webank.wecube.platform.core.repository.workflow.ExtraTaskMapper;
import com.webank.wecube.platform.core.repository.workflow.ProcDefInfoMapper;
import com.webank.wecube.platform.core.repository.workflow.ProcExecBindingMapper;
import com.webank.wecube.platform.core.repository.workflow.ProcExecContextMapper;
import com.webank.wecube.platform.core.repository.workflow.ProcInstInfoMapper;
import com.webank.wecube.platform.core.repository.workflow.TaskNodeDefInfoMapper;
import com.webank.wecube.platform.core.repository.workflow.TaskNodeExecParamMapper;
import com.webank.wecube.platform.core.repository.workflow.TaskNodeExecRequestMapper;
import com.webank.wecube.platform.core.repository.workflow.TaskNodeInstInfoMapper;
import com.webank.wecube.platform.core.repository.workflow.TaskNodeParamMapper;
import com.webank.wecube.platform.core.service.dme.EntityDataRouteFactory;
import com.webank.wecube.platform.core.service.dme.EntityQueryExpressionParser;
import com.webank.wecube.platform.core.service.dme.StandardEntityOperationService;
import com.webank.wecube.platform.core.service.plugin.PluginConfigMgmtService;
import com.webank.wecube.platform.core.service.plugin.PluginInstanceMgmtService;
import com.webank.wecube.platform.core.service.plugin.SystemVariableService;
import com.webank.wecube.platform.core.support.plugin.PluginInvocationRestClient;
import com.webank.wecube.platform.core.support.plugin.PluginTaskFormRestClient;
import com.webank.wecube.platform.workflow.WorkflowConstants;

public abstract class AbstractPluginInvocationService extends AbstractWorkflowService {

    protected static final String CALLBACK_PARAMETER_KEY = "callbackParameter";
    protected static final String INPUT_PARAMETER_KEY_OPERATOR = "operator";

    protected static final String RESULT_CODE_OK = "0";
    protected static final String RESULT_CODE_ERR = "1";
    
    protected static final String PLUGIN_RESULT_CODE_FAIL = "1";
    protected static final String PLUGIN_RESULT_CODE_PARTIALLY_FAIL = "1";
    protected static final String PLUGIN_RESULT_CODE_PARTIALLY_KEY = "errorCode";

    protected static final String DATA_TYPE_STRING = "string";
    protected static final String DATA_TYPE_NUMBER = "number";

    protected static final String DEFAULT_VALUE_DATA_TYPE_STRING = "";
    protected static final int DEFAULT_VALUE_DATA_TYPE_NUMBER = 0;

    protected static final int MAX_PARAM_VAL_SIZE = 3000;
    
    protected static final String PARAM_NAME_TASK_FORM_INPUT = "taskFormInput";
    protected static final String PARAM_NAME_TASK_FORM_OUTPUT = "taskFormOutput";
    
    protected static final String TEMPORARY_ENTITY_ID_PREFIX = "OID-";

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected PluginInvocationResultService pluginInvocationResultService;

    @Autowired
    protected TaskNodeExecRequestMapper taskNodeExecRequestRepository;

    @Autowired
    protected TaskNodeInstInfoMapper taskNodeInstInfoRepository;

    @Autowired
    protected TaskNodeDefInfoMapper taskNodeDefInfoRepository;

    @Autowired
    protected TaskNodeExecParamMapper taskNodeExecParamRepository;

    @Autowired
    protected PluginConfigMgmtService pluginConfigMgmtService;

    @Autowired
    protected StandardEntityOperationService entityOperationService;
    
    @Autowired
    protected ApplicationProperties applicationProperties;
    
    @Autowired
    protected SimpleEncryptionService simpleEncryptionService;
    
    @Autowired
    protected PluginInvocationRestClient pluginInvocationRestClient;

    @Autowired
    protected PluginInvocationProcessor pluginInvocationProcessor;

    @Autowired
    protected ProcInstInfoMapper procInstInfoRepository;

    @Autowired
    protected ProcDefInfoMapper procDefInfoMapper;

    @Autowired
    protected PluginInstanceMgmtService pluginInstanceMgmtService;

    @Autowired
    protected ProcExecBindingMapper procExecBindingMapper;

    @Autowired
    protected SystemVariableService systemVariableService;

    @Autowired
    protected TaskNodeParamMapper taskNodeParamRepository;

    @Autowired
    protected WorkflowProcInstEndEventNotifier workflowProcInstEndEventNotifier;

    @Autowired
    protected RiskyCommandVerifier riskyCommandVerifier;

    @Autowired
    protected ExtraTaskMapper extraTaskMapper;
    
    @Autowired
    protected EntityQueryExpressionParser entityQueryExpressionParser;
    
    @Autowired
    protected PluginTaskFormRestClient pluginTaskFormRestClient;
    
    @Autowired
    protected ProcExecContextMapper procExecContextMapper;
    
    @Autowired
    protected EntityDataRouteFactory entityDataRouteFactory;
    
    
    @Autowired
    @Qualifier(value = "jwtSsoRestTemplate")
    protected RestTemplate jwtSsoRestTemplate;
    
    protected ObjectMapper objectMapper = new ObjectMapper();
    
    
    /**
     * 
     * @param cmd
     */
    public void handleProcessInstanceEndEvent(PluginInvocationCommand cmd) {
        if (log.isInfoEnabled()) {
            log.info("handle end event:{}", cmd);
        }

        Date currTime = new Date();

        ProcInstInfoEntity procInstEntity = null;
        int times = 0;

        while (times < 20) {
            procInstEntity = procInstInfoRepository.selectOneByProcInstKernelId(cmd.getProcInstId());
            if (procInstEntity != null) {
                break;
            }

            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                log.info("exceptions while handling end event.", e.getMessage());
            }

            times++;
        }

        if (procInstEntity == null) {
            log.warn("Cannot find process instance entity currently for {}", cmd.getProcInstId());
            return;
        }

        String oldProcInstStatus = procInstEntity.getStatus();
        if (ProcInstInfoEntity.INTERNALLY_TERMINATED_STATUS.equalsIgnoreCase(procInstEntity.getStatus())) {
            return;
        }
        procInstEntity.setUpdatedTime(currTime);
        procInstEntity.setUpdatedBy(WorkflowConstants.DEFAULT_USER);
        procInstEntity.setStatus(ProcInstInfoEntity.COMPLETED_STATUS);
        procInstInfoRepository.updateByPrimaryKeySelective(procInstEntity);
        log.info("updated process instance {} from {} to {}", procInstEntity.getId(), oldProcInstStatus,
                ProcInstInfoEntity.COMPLETED_STATUS);

        List<TaskNodeInstInfoEntity> nodeInstEntities = taskNodeInstInfoRepository
                .selectAllByProcInstId(procInstEntity.getId());
        List<TaskNodeDefInfoEntity> nodeDefEntities = taskNodeDefInfoRepository
                .selectAllByProcDefId(procInstEntity.getProcDefId());

        for (TaskNodeInstInfoEntity n : nodeInstEntities) {
            if ("endEvent".equals(n.getNodeType()) && n.getNodeId().equals(cmd.getNodeId())) {
                TaskNodeDefInfoEntity currNodeDefInfo = findExactTaskNodeDefInfoEntityWithNodeId(nodeDefEntities,
                        n.getNodeId());
                refreshStatusOfPreviousNodes(nodeInstEntities, currNodeDefInfo);
                n.setUpdatedTime(currTime);
                n.setUpdatedBy(WorkflowConstants.DEFAULT_USER);
                n.setStatus(TaskNodeInstInfoEntity.COMPLETED_STATUS);

                taskNodeInstInfoRepository.updateByPrimaryKeySelective(n);

                log.debug("updated node {} to {}", n.getId(), TaskNodeInstInfoEntity.COMPLETED_STATUS);
            }
        }

        workflowProcInstEndEventNotifier.notify(WorkflowNotifyEvent.PROCESS_INSTANCE_END, cmd, procInstEntity);

    }
    
    protected void refreshStatusOfPreviousNodes(List<TaskNodeInstInfoEntity> nodeInstEntities,
            TaskNodeDefInfoEntity currNodeDefInfo) {
        List<String> previousNodeIds = unmarshalNodeIds(currNodeDefInfo.getPrevNodeIds());
        log.debug("previousNodeIds:{}", previousNodeIds);
        for (String prevNodeId : previousNodeIds) {
            TaskNodeInstInfoEntity prevNodeInst = findExactTaskNodeInstInfoEntityWithNodeId(nodeInstEntities,
                    prevNodeId);
            log.debug("prevNodeInst:{} - {}", prevNodeInst, prevNodeId);
            if (prevNodeInst != null) {
                if (statelessNodeTypes.contains(prevNodeInst.getNodeType())
                        && !TaskNodeInstInfoEntity.COMPLETED_STATUS.equalsIgnoreCase(prevNodeInst.getStatus())) {
                    prevNodeInst.setUpdatedTime(new Date());
                    prevNodeInst.setUpdatedBy(WorkflowConstants.DEFAULT_USER);
                    prevNodeInst.setStatus(TaskNodeInstInfoEntity.COMPLETED_STATUS);

                    taskNodeInstInfoRepository.updateByPrimaryKeySelective(prevNodeInst);
                }
            }
        }
    }

    protected TaskNodeInstInfoEntity findExactTaskNodeInstInfoEntityWithNodeId(
            List<TaskNodeInstInfoEntity> nodeInstEntities, String nodeId) {
        for (TaskNodeInstInfoEntity nodeInst : nodeInstEntities) {
            if (nodeId.equalsIgnoreCase(nodeInst.getNodeId())) {
                return nodeInst;
            }
        }

        return null;
    }

    protected TaskNodeDefInfoEntity findExactTaskNodeDefInfoEntityWithNodeId(
            List<TaskNodeDefInfoEntity> nodeDefEntities, String nodeId) {
        for (TaskNodeDefInfoEntity nodeDef : nodeDefEntities) {
            if (nodeId.equalsIgnoreCase(nodeDef.getNodeId())) {
                return nodeDef;
            }
        }

        return null;
    }

    protected String trimWithMaxLength(String s) {
        if (s == null) {
            return "";
        }

        if (s.length() < 200) {
            return s;
        }

        return s.substring(0, 200);
    }

    protected String asString(Object val, String sType) {
        if (val == null) {
            return null;
        }
        if (val instanceof String && DATA_TYPE_STRING.equals(sType)) {
            return (String) val;
        }

        if (val instanceof Integer && DATA_TYPE_NUMBER.equals(sType)) {
            return String.valueOf(val);
        }

        return val.toString();

    }

    protected String trimExceedParamValue(String val, int size) {
        if (val.length() > size) {
            return val.substring(0, size);
        }

        return val;
    }

    protected Object fromString(String val, String sType) {
        if (DATA_TYPE_STRING.equals(sType)) {
            return val;
        }

        if (StringUtils.isBlank(val)) {
            return null;
        }

        if (DATA_TYPE_NUMBER.equals(sType)) {
            return Integer.parseInt(val);
        }

        return val;
    }

    protected String assembleValueList(List<Object> retDataValues) {
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        sb.append("[");

        for (Object dv : retDataValues) {
            if (!isFirst) {
                sb.append(",");
            } else {
                isFirst = false;
            }

            sb.append(dv == null ? "" : dv);
        }

        sb.append("]");

        return sb.toString();
    }

    protected List<Map<String, Object>> validateAndCastResultData(List<Object> resultData) {
        List<Map<String, Object>> outputParameterMaps = new ArrayList<Map<String, Object>>();

        for (Object obj : resultData) {
            if (obj == null) {
                continue;
            }

            if (!(obj instanceof Map)) {
                log.error("unexpected data type:returned object is not a instance of map, obj={}", obj);
                throw new WecubeCoreException("3163","Unexpected data type");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> retRecord = (Map<String, Object>) obj;

            outputParameterMaps.add(retRecord);
        }

        return outputParameterMaps;

    }

    protected PluginConfigInterfaceParameters findPreConfiguredPluginConfigInterfaceParameter(
            List<PluginConfigInterfaceParameters> outputParameters, String paramName) {
        for (PluginConfigInterfaceParameters p : outputParameters) {
            if (p.getName().equals(paramName)) {
                return p;
            }
        }

        return null;
    }
    
    protected String tryEncodeParamDataValue(String rawDataValue){
        if(StringUtils.isBlank(rawDataValue)){
            return rawDataValue;
        }
        
        String cipherDataValue = simpleEncryptionService.encodeToAesBase64(rawDataValue);
        return cipherDataValue;
    }
    
    protected String tryDecodeParamDataValue(String cipherDataValue){
        if(StringUtils.isBlank(cipherDataValue)){
            return cipherDataValue;
        }
        
        String rawDataValue = null;
        try{
            rawDataValue = simpleEncryptionService.decodeFromAesBase64(cipherDataValue);
        }catch(Exception e){
            log.info("errors while decode cipher data value:{},error:{}", cipherDataValue, e.getMessage());
            rawDataValue = cipherDataValue;
        }
        
        return rawDataValue;
    }
    
    protected boolean isSystemAutomationTaskNode(TaskNodeDefInfoEntity taskNodeDefEntity) {
        return TASK_CATEGORY_SSTN.equalsIgnoreCase(taskNodeDefEntity.getTaskCategory());
    }

    protected boolean isUserTaskNode(TaskNodeDefInfoEntity taskNodeDefEntity) {
        return TASK_CATEGORY_SUTN.equalsIgnoreCase(taskNodeDefEntity.getTaskCategory());
    }

    protected boolean isDataOperationTaskNode(TaskNodeDefInfoEntity taskNodeDefEntity) {
        return TASK_CATEGORY_SDTN.equalsIgnoreCase(taskNodeDefEntity.getTaskCategory());
    }
    
    protected boolean isDynamicBindTaskNode(TaskNodeDefInfoEntity taskNodeDef) {
        return TaskNodeDefInfoEntity.DYNAMIC_BIND_YES.equalsIgnoreCase(taskNodeDef.getDynamicBind());
    }

    protected boolean isBoundTaskNodeInst(TaskNodeInstInfoEntity taskNodeInst) {
        return TaskNodeInstInfoEntity.BIND_STATUS_BOUND.equalsIgnoreCase(taskNodeInst.getBindStatus());
    }
    
    protected void storeProcExecBindingEntities(List<ProcExecBindingEntity> nodeObjectBindings) {
        if (nodeObjectBindings == null || nodeObjectBindings.isEmpty()) {
            return;
        }

        for (ProcExecBindingEntity nob : nodeObjectBindings) {
            procExecBindingMapper.insert(nob);
        }
    }

    protected String marshalPluginInvocationCommand(PluginInvocationCommand cmd) {
        String json;
        try {
            json = objectMapper.writeValueAsString(cmd);
            return json;
        } catch (JsonProcessingException e) {
            throw new WecubeCoreException("Failed to marshal plugin invocation command.", e);
        }
    }
    
    protected String calculateDataModelExpression(TaskNodeDefInfoEntity f) {
        if (StringUtils.isBlank(f.getRoutineExp())) {
            return null;
        }

        String expr = f.getRoutineExp();

        if (StringUtils.isBlank(f.getServiceId())) {
            return expr;
        }

        PluginConfigInterfaces inter = pluginConfigMgmtService.getPluginConfigInterfaceByServiceName(f.getServiceId());
        if (inter == null) {
            return expr;
        }

        if (StringUtils.isBlank(inter.getFilterRule())) {
            return expr;
        }

        return expr + inter.getFilterRule();
    }

}
