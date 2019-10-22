package com.webank.wecube.core.service.workflow;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.history.NativeHistoricActivityInstanceQuery;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.ServiceTask;
import org.camunda.bpm.model.bpmn.instance.StartEvent;
import org.camunda.bpm.model.bpmn.instance.SubProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecube.core.commons.WecubeCoreException;
import com.webank.wecube.core.domain.plugin.PluginConfigInterface;
import com.webank.wecube.core.domain.workflow.AttachVO;
import com.webank.wecube.core.domain.workflow.CiRoutineItem;
import com.webank.wecube.core.domain.workflow.FlowNodeVO;
import com.webank.wecube.core.domain.workflow.ProcessDefinitionTaskServiceEntity;
import com.webank.wecube.core.domain.workflow.ProcessInstanceOutline;
import com.webank.wecube.core.domain.workflow.ProcessInstanceStartRequest;
import com.webank.wecube.core.domain.workflow.ProcessInstanceStartResponse;
import com.webank.wecube.core.domain.workflow.ProcessInstanceVO;
import com.webank.wecube.core.domain.workflow.ProcessTaskEntity;
import com.webank.wecube.core.domain.workflow.ProcessTaskVO;
import com.webank.wecube.core.domain.workflow.ProcessTransactionEntity;
import com.webank.wecube.core.domain.workflow.ProcessTransactionVO;
import com.webank.wecube.core.domain.workflow.RestartProcessInstanceRequest;
import com.webank.wecube.core.domain.workflow.StartProcessInstaceWithCiDataReq;
import com.webank.wecube.core.domain.workflow.StartProcessInstaceWithCiDataReqChunk;
import com.webank.wecube.core.domain.workflow.TaskNodeExecLogEntity;
import com.webank.wecube.core.interceptor.UsernameStorage;
import com.webank.wecube.core.jpa.TaskNodeExecLogEntityRepository;
import com.webank.wecube.core.support.cmdb.dto.v2.AdhocIntegrationQueryDto;
import com.webank.wecube.core.support.cmdb.dto.v2.CiTypeAttrDto;
import com.webank.wecube.core.support.cmdb.dto.v2.IntegrationQueryDto;
import com.webank.wecube.core.support.cmdb.dto.v2.PaginationQuery;
import com.webank.wecube.core.support.cmdb.dto.v2.PaginationQueryResult;
import com.webank.wecube.platform.workflow.commons.LocalIdGenerator;
import com.webank.wecube.platform.workflow.entity.ServiceNodeStatusEntity;
import com.webank.wecube.platform.workflow.repository.ServiceNodeStatusRepository;

@Service
public class ProcessInstanceService extends AbstractProcessService {
    private static final String ERROR_MESSAGE_PROCESS_INSTANCE_STARTING_FAILED = "process instance starting failed";
    private static final Logger log = LoggerFactory.getLogger(ProcessInstanceService.class);

    public static final String NOT_STARTED = "NotStarted";
    public static final String INPROGRESS = "InProgress";
    public static final String COMPLETED = "Completed";
    public static final String FAULTED = "Faulted";
    public static final String TIMEOUTED = "Timeouted";

    private static final String DATE_FORMAT_PATTERN = "yyyyMMdd HH:mm:ss ";

    @Autowired
    private TaskNodeExecLogEntityRepository taskNodeExecLogEntityRepository;

    @Autowired
    private ServiceNodeStatusRepository serviceNodeStatusRepository;
    
    @Autowired
    private TaskService taskService;

    public ProcessInstanceOutline refreshProcessInstanceStatus(String businessKey) {
        List<ProcessInstance> instances = runtimeService.createProcessInstanceQuery().active()
                .processInstanceBusinessKey(businessKey).list();

        String instanceId = null;
        if (instances.size() >= 1) {
            instanceId = instances.get(0).getProcessInstanceId();
        }

        if (instanceId == null) {
            List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceBusinessKey(businessKey).rootProcessInstances().orderByProcessInstanceEndTime()
                    .desc().list();
            if (historicProcessInstances.size() >= 1) {
                HistoricProcessInstance historicProcessInstance = historicProcessInstances.get(0);
                instanceId = historicProcessInstance.getId();
            }
        }

        if (StringUtils.isBlank(instanceId)) {
            log.error("Illegal business key, businessKey={}", businessKey);
            throw new WecubeCoreException("Illegal business key");
        }

        ProcessInstanceOutline outline = getProcessInstanceOutline(instanceId);

        return outline;
    }

    public List<ProcessTransactionVO> listProcessTransactions() {
        List<ProcessTransactionVO> trans = new ArrayList<ProcessTransactionVO>();

        String currUser = UsernameStorage.getIntance().get();
        if (StringUtils.isBlank(currUser)) {
            return trans;
        }

        List<ProcessTransactionEntity> tranEntities = processTransactionEntityRepository.findAllByOperator(currUser);

        if (tranEntities.isEmpty()) {
            return trans;
        }

        for (ProcessTransactionEntity entity : tranEntities) {
            trans.add(processTransactionVO(entity));
        }

        return trans;
    }

    public List<ProcessInstanceOutline> refreshStatusesProcessTransactions(int processTransactionId) {
        List<ProcessInstanceOutline> outlines = new ArrayList<ProcessInstanceOutline>();

        Optional<ProcessTransactionEntity> transOptional = processTransactionEntityRepository
                .findById(processTransactionId);
        if (!transOptional.isPresent()) {
            log.warn("such process transaction does not exist, processTransactionId={}", processTransactionId);
            return outlines;
        }

        ProcessTransactionEntity trans = transOptional.get();

        List<ProcessTaskEntity> tasks = processTaskEntityRepository.findAllByTransaction(trans.getId());

        for (ProcessTaskEntity task : tasks) {

            if (StringUtils.isNotBlank(task.getProcessInstanceId())) {
                ProcessInstanceOutline outline = getProcessInstanceOutline(task.getProcessInstanceId());
                outlines.add(outline);
            } else {
                log.error("cannot find such process instance id with task, taskId={}", task.getId());
            }
        }

        return outlines;
    }

    public List<ProcessInstanceStartResponse> startProcessInstancesWithCiDataInbatch(
            StartProcessInstaceWithCiDataReqChunk requestChunk) {
        List<ProcessInstanceStartResponse> resps = new ArrayList<ProcessInstanceStartResponse>();

        if (requestChunk == null) {
            return resps;
        }

        if (requestChunk.getRequests() == null || requestChunk.getRequests().isEmpty()) {
            log.warn("empty process definitions provided to start process instance");
            return resps;
        }

        List<StartProcessInstaceWithCiDataReq> requests = requestChunk.getRequests();

        for (StartProcessInstaceWithCiDataReq request : requests) {
            doVerifyStartProcessInstaceWithCiDataReq(request);
        }

        Date startDate = new Date();
        String currUser = UsernameStorage.getIntance().get();
        ProcessTransactionEntity trans = new ProcessTransactionEntity();
        trans.setStartTime(startDate);
        trans.setOperator(currUser);
        trans.setCreateBy(currUser);
        trans.setName(LocalIdGenerator.INSTANCE.generateTimestampedId());

        trans.setCreateTime(startDate);
        trans.setStatus(INPROGRESS);

        ObjectMapper mapper = new ObjectMapper();

        String jsonAttach = null;
        try {
            jsonAttach = mapper.writeValueAsString(requestChunk.getAttach());
        } catch (Exception e) {
            log.error("object marshal errors", e);
        }

        trans.setAttach(jsonAttach);

        trans.setName(String.format("%s [%s]", currUser, formatDate(startDate)));
        ProcessTransactionEntity savedTrans = processTransactionEntityRepository.save(trans);

        for (StartProcessInstaceWithCiDataReq request : requests) {
            ProcessInstanceStartResponse resp = doStartProcessInstanceWithCiData(request, savedTrans);
            resps.add(resp);

            List<ProcessTaskEntity> tasks = processTaskEntityRepository
                    .findTaskByProcessInstanceKey(resp.getBusinessKey());

            if (!tasks.isEmpty()) {
                ProcessTaskEntity task = tasks.get(0);
                task.setProcessInstanceId(resp.getProcessInstanceId());
                task.setUpdateTime(new Date());
                task.setUpdateBy(currUser);
                processTaskEntityRepository.saveAndFlush(task);
            }
        }

        return resps;
    }

    public ProcessInstanceStartResponse startProcessInstanceWithCiData(StartProcessInstaceWithCiDataReq request) {

        doVerifyStartProcessInstaceWithCiDataReq(request);
        ProcessInstanceStartResponse resp = doStartProcessInstanceWithCiData(request, null);

        List<ProcessTaskEntity> tasks = processTaskEntityRepository.findTaskByProcessInstanceKey(resp.getBusinessKey());

        if (!tasks.isEmpty()) {
            ProcessTaskEntity task = tasks.get(0);
            task.setProcessInstanceId(resp.getProcessInstanceId());
            task.setUpdateTime(new Date());
            task.setUpdateBy(UsernameStorage.getIntance().get());
            processTaskEntityRepository.saveAndFlush(task);
        }

        List<TaskNodeExecLogEntity> logEntities = this.taskNodeExecLogEntityRepository
                .findEntitiesByInstanceBusinessKey(resp.getBusinessKey());

        for (TaskNodeExecLogEntity logEntity : logEntities) {
            logEntity.setInstanceId(resp.getProcessInstanceId());
            this.taskNodeExecLogEntityRepository.save(logEntity);
        }

        return resp;

    }

    public ProcessInstanceStartResponse restartProcessInstance(RestartProcessInstanceRequest request) {
        log.info("to restart process instance,id={}, nodeId={}", request.getProcessInstanceId(),
                request.getActivityId());
        String processInstanceId = request.getProcessInstanceId();

        if (StringUtils.isBlank(request.getProcessInstanceId()) || StringUtils.isBlank(request.getActivityId())) {
            throw new WecubeCoreException("instance id and activity id should provide");
        }

        ProcessInstance runningInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId).singleResult();

        if (runningInstance != null) {
            return doRestartProcessInstanceFromRunningInstance(request, runningInstance);
        }

        HistoricProcessInstance hisInstance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId).singleResult();

        if (hisInstance == null) {
            log.error("such process instance doesn't exist in history, processInstanceId={}", processInstanceId);
            throw new WecubeCoreException("such process instance doesn't exist in history");
        }

        return doRestartProcessInstanceFromHistoricInstance(request, hisInstance);

    }
    
    private ProcessInstanceStartResponse doRestartProcessInstanceFromHistoricInstance(RestartProcessInstanceRequest request, HistoricProcessInstance hisInstance){
        HistoricProcessInstanceQuery hisQuery = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(request.getProcessInstanceId());

        runtimeService.restartProcessInstances(hisInstance.getProcessDefinitionId())
                .historicProcessInstanceQuery(hisQuery).startBeforeActivity(request.getActivityId())
                .initialSetOfVariables().execute();

        ProcessInstance restartedInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceBusinessKey(hisInstance.getBusinessKey()).singleResult();

        if (restartedInstance == null) {
            log.error("failed to restart process instance, business key = {}", hisInstance.getBusinessKey());
            throw new WecubeCoreException("failed to restart process instance");
        }

        ProcessInstanceStartResponse resp = new ProcessInstanceStartResponse();

        resp.setBusinessKey(restartedInstance.getBusinessKey());
        resp.setProcessDefinitionId(restartedInstance.getProcessDefinitionId());

        resp.setProcessInstanceId(restartedInstance.getProcessInstanceId());

        resp.setProcessExecutionId(restartedInstance.getProcessInstanceId());
        
        return resp;
    }

    private ProcessInstanceStartResponse doRestartProcessInstanceFromRunningInstance(
            RestartProcessInstanceRequest request, ProcessInstance runningInstance) {
        
        
        String instanceId = request.getProcessInstanceId();
        String taskDefKey = WorkflowConstants.PREFIX_EXCEPT_SUB_USER_TASK+request.getActivityId();
        
        String act = StringUtils.isBlank(request.getAct())? "retry" : request.getAct();

        Task task = taskService.createTaskQuery().processInstanceId(instanceId).active().taskDefinitionKey(taskDefKey).singleResult();

        if (task == null) {
            log.error("cannot find task with instanceId {} and taskId {}", instanceId, taskDefKey);
            throw new WecubeCoreException("process instance restarting failed");
        } else {

            String actVarName = String.format("%s_%s", WorkflowConstants.VAR_KEY_USER_ACT, request.getActivityId());

            log.info("to complete task {} put {} {}", taskDefKey, WorkflowConstants.VAR_KEY_USER_ACT, act);
            Map<String, Object> variables = new HashMap<String, Object>();
            variables.put(actVarName, act);

            taskService.complete(task.getId(), variables);
        }
        
        
        ProcessInstanceStartResponse resp = new ProcessInstanceStartResponse();

        resp.setBusinessKey(runningInstance.getBusinessKey());
        resp.setProcessDefinitionId(runningInstance.getProcessDefinitionId());

        resp.setProcessInstanceId(runningInstance.getProcessInstanceId());

        resp.setProcessExecutionId(runningInstance.getProcessInstanceId());

        return resp;
    }

    private String formatDate(Date date) {
        DateFormat df = new SimpleDateFormat(DATE_FORMAT_PATTERN);
        return df.format(date);
    }

    private void doVerifyStartProcessInstaceWithCiDataReq(StartProcessInstaceWithCiDataReq request) {
        if (request == null) {
            throw new WecubeCoreException("Illegal argument");
        }

        if (StringUtils.isBlank(request.getCiTypeId()) || StringUtils.isBlank(request.getCiDataId())) {
            throw new WecubeCoreException(
                    "Illegal argument,ciTypeId,ciDataId or process definition key may not provide");
        }

        int ciTypeId = Integer.parseInt(request.getCiTypeId());
        String guid = request.getCiDataId();

        String processDefinitionKey = getCodeByEnumCodeId(request.getProcessDefinitionKey());

        List<CiTypeAttrDto> attrs = cmdbServiceV2Stub.getCiTypeAttributesByCiTypeId(ciTypeId);

        CiTypeAttrDto orchestrationAttr = findCiAttrFromCiAttrListByName("orchestration", attrs);
        CiTypeAttrDto guidAttr = findCiAttrFromCiAttrListByName("guid", attrs);

        if (orchestrationAttr == null || guidAttr == null) {
            throw new WecubeCoreException("cannot find orchestration or guid attribute");
        }

        AdhocIntegrationQueryDto dto = new AdhocIntegrationQueryDto();

        IntegrationQueryDto criteria = new IntegrationQueryDto();
        PaginationQuery queryRequest = new PaginationQuery();

        PaginationQuery.Filter filter = new PaginationQuery.Filter("ci$guid", "eq", guid);
        queryRequest.setFilters(Arrays.asList(filter));

        dto.setCriteria(criteria);
        dto.setQueryRequest(queryRequest);

        criteria.setName("ci");
        criteria.setCiTypeId(ciTypeId);
        criteria.setAttrs(Arrays.asList(guidAttr.getCiTypeAttrId(), orchestrationAttr.getCiTypeAttrId()));
        criteria.setAttrKeyNames(Arrays.asList("ci$guid", "ci$orchestration"));

        PaginationQueryResult<Map<String, Object>> results = cmdbServiceV2Stub.adhocIntegrationQuery(dto);

        List<Map<String, Object>> ciDatas = results.getContents();

        if (ciDatas == null || ciDatas.size() != 1) {
            throw new WecubeCoreException("none cidata found or multiple records got");
        }

        Map<String, Object> ciData = ciDatas.get(0);

        @SuppressWarnings("unchecked")
        Map<String, Object> orchestrationMap = (Map<String, Object>) ciData.get("ci$orchestration");

        if (orchestrationMap == null || orchestrationMap.isEmpty()) {
            throw new WecubeCoreException("none orchestration found from cmdb");
        }

        String orchestration = (String) orchestrationMap.get("code");

        if (!processDefinitionKey.equals(orchestration)) {
            throw new WecubeCoreException("orchestration not match with the one configured in cmdb");
        }
    }

    private ProcessInstanceStartResponse doStartProcessInstanceWithCiData(StartProcessInstaceWithCiDataReq request,
            ProcessTransactionEntity trans) {

        String procDefKey = getCodeByEnumCodeId(request.getProcessDefinitionKey());

        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery().processDefinitionKey(procDefKey)
                .latestVersion().singleResult();

        if (definition == null) {
            log.error("none such process definition found, process definition key [{}]",
                    request.getProcessDefinitionKey());
            throw new WecubeCoreException("none process exists");
        }

        String processInstanceBizKey = generateProcessInstanceBusinessKey();
        log.debug("processInstanceBizKey:{}", processInstanceBizKey);

        try {
            prepareProcessInstanceBusinessKey(request, processInstanceBizKey, definition);
        } catch (Exception e) {
            log.error("process biz key erorrs", e);
            throw new WecubeCoreException(ERROR_MESSAGE_PROCESS_INSTANCE_STARTING_FAILED);
        }

        String processDefinitionId = definition.getId();

        String currUser = UsernameStorage.getIntance().get();

        ProcessTaskEntity task = processTaskEntity(currUser, request.getCiDataId(), request.getCiTypeId(),
                processInstanceBizKey, definition);

        if (trans != null) {
            task.setTransaction(trans);
            trans.addTask(task);
        }

        processTaskEntityRepository.save(task);

        Date currTime = new Date();

        List<ProcessDefinitionTaskServiceEntity> taskServiceEntities = processDefinitionTaskServiceEntityRepository
                .findTaskServicesByProcDefKeyAndVersion(procDefKey, definition.getVersion());
        for (ProcessDefinitionTaskServiceEntity entity : taskServiceEntities) {
            TaskNodeExecLogEntity logEntity = new TaskNodeExecLogEntity();
            logEntity.setCreatedBy(currUser);
            logEntity.setCreatedTime(currTime);
            logEntity.setTaskNodeId(entity.getTaskNodeId());
            logEntity.setInstanceBusinessKey(processInstanceBizKey);
            logEntity.setServiceName(entity.getBindServiceId());
            logEntity.setTaskNodeStatus(NOT_STARTED);

            taskNodeExecLogEntityRepository.save(logEntity);
        }

        ProcessInstance instance = runtimeService.startProcessInstanceById(processDefinitionId, processInstanceBizKey);

        if (instance == null) {
            log.error("process starting failed, process definition id [{}], business key [{}]", processDefinitionId,
                    processInstanceBizKey);
            throw new WecubeCoreException(ERROR_MESSAGE_PROCESS_INSTANCE_STARTING_FAILED);
        }

        ProcessInstanceStartResponse resp = new ProcessInstanceStartResponse();
        resp.setProcessInstanceId(instance.getProcessInstanceId());
        resp.setProcessDefinitionId(instance.getProcessDefinitionId());
        resp.setProcessExecutionId(instance.getId());
        resp.setBusinessKey(instance.getBusinessKey());

        resp.setProcessDefinitionKey(definition.getKey());
        resp.setProcessDefinitionVersion(definition.getVersion());

        return resp;
    }

    public ProcessInstanceStartResponse startProcessInstance(ProcessInstanceStartRequest request) {
        log.info("start process instance, request={}", request);
        String bizKey = String.valueOf(System.currentTimeMillis());
        ProcessInstance instance = runtimeService.startProcessInstanceById(request.getProcessDefinitionId(), bizKey);

        if (instance == null) {
            throw new RuntimeException("none instance created");
        }

        ProcessInstanceStartResponse resp = new ProcessInstanceStartResponse();
        resp.setProcessInstanceId(instance.getProcessInstanceId());
        resp.setProcessDefinitionId(instance.getProcessDefinitionId());
        resp.setProcessExecutionId(instance.getId());
        resp.setBusinessKey(instance.getBusinessKey());

        return resp;
    }

    public ProcessInstanceStartResponse startProcessInstanceWithBusinessKey(ProcessInstanceStartRequest request,
            String businessKey) {
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(request.getProcessDefinitionKey()).latestVersion().singleResult();

        if (definition == null) {
            log.error("none such process definition found, process definition key [{}]",
                    request.getProcessDefinitionKey());
            throw new WecubeCoreException("none process exists");
        }

        String processDefinitionId = definition.getId();

        ProcessInstance instance = runtimeService.startProcessInstanceById(processDefinitionId, businessKey);

        if (instance == null) {
            log.error("process starting failed, process definition id [{}], business key [{}]", processDefinitionId,
                    businessKey);
            throw new WecubeCoreException(ERROR_MESSAGE_PROCESS_INSTANCE_STARTING_FAILED);
        }

        ProcessInstanceStartResponse resp = new ProcessInstanceStartResponse();
        resp.setProcessInstanceId(instance.getProcessInstanceId());
        resp.setProcessDefinitionId(instance.getProcessDefinitionId());
        resp.setProcessExecutionId(instance.getId());
        resp.setBusinessKey(instance.getBusinessKey());

        return resp;
    }

    public List<ProcessInstanceVO> getProcessInstancesOfDefinition(String processDefinitionId) {
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId).singleResult();

        List<ProcessInstanceVO> instances = new ArrayList<ProcessInstanceVO>();
        List<ProcessInstance> runningInstances = runtimeService.createProcessInstanceQuery()
                .processDefinitionId(processDefinitionId).list();

        runningInstances.forEach(m -> {
            ProcessInstanceVO vo = new ProcessInstanceVO();
            vo.setProcessDefinitionId(m.getProcessDefinitionId());
            vo.setProcessDefinitionName(definition.getName());
            vo.setProcessInstanceId(m.getProcessInstanceId());
            vo.setEnded(false);
            vo.setStatus(INPROGRESS);

            instances.add(vo);
        });

        HistoricProcessInstanceQuery hisProcessInstanceQuery = historyService.createHistoricProcessInstanceQuery()
                .finished().processDefinitionId(processDefinitionId);

        List<HistoricProcessInstance> hisInstances = hisProcessInstanceQuery.list();

        hisInstances.forEach(m -> {
            ProcessInstanceVO vo = new ProcessInstanceVO();
            vo.setProcessDefinitionId(m.getProcessDefinitionId());
            vo.setProcessDefinitionName(definition.getName());
            vo.setProcessInstanceId(m.getId());
            vo.setEnded(true);
            vo.setStatus(COMPLETED);

            instances.add(vo);
        });

        return instances;
    }

    public ProcessInstanceOutline getProcessInstanceOutline(String processInstanceId) {
        log.debug("get process instance outline,processInstanceId={}", processInstanceId);

        ProcessInstanceOutline outline = buildProcessInstanceOutline(processInstanceId);

        String processDefinitionId = outline.getProcessDefinitionId();

        BpmnModelInstance bpmnModel = repositoryService.getBpmnModelInstance(processDefinitionId);

        Collection<org.camunda.bpm.model.bpmn.instance.Process> processes = bpmnModel
                .getModelElementsByType(org.camunda.bpm.model.bpmn.instance.Process.class);

        if (processes.size() != 1) {
            throw new RuntimeException("at least one process should be provided");
        }

        org.camunda.bpm.model.bpmn.instance.Process process = processes.iterator().next();

        Collection<StartEvent> startEvents = process.getChildElementsByType(StartEvent.class);

        if (startEvents.size() != 1) {
            throw new RuntimeException("only one start event supported");
        }

        StartEvent startEvent = startEvents.iterator().next();

        populateFlowNodes(outline, startEvent);

        refreshStatus(outline);

        return outline;
    }

    private void refreshStatus(ProcessInstanceOutline outline) {
        String processDefinitionId = outline.getProcessDefinitionId();
        String processInstanceId = outline.getProcessInstanceId();

        for (FlowNodeVO vo : outline.getFlowNodes()) {
            Collection<HistoricActivityInstance> activities = historyService.createHistoricActivityInstanceQuery()
                    .processDefinitionId(processDefinitionId).processInstanceId(processInstanceId)
                    .activityId(vo.getId()).finished().list();
            HistoricActivityInstance activity = null;

            if (activities.size() > 0) {
                activity = activities.iterator().next();
            }

            if (activity != null) {
                setFlowNodeVoStatus(activity, processInstanceId, vo, COMPLETED);
                continue;
            }

            HistoricActivityInstance inprogressActivity = processEngine.getHistoryService()
                    .createHistoricActivityInstanceQuery().processDefinitionId(processDefinitionId)
                    .processInstanceId(processInstanceId).activityType(vo.getNodeTypeName()).activityId(vo.getId())
                    .unfinished().singleResult();

            if (inprogressActivity != null) {
            	setFlowNodeVoStatus(inprogressActivity, processInstanceId, vo, INPROGRESS);
            }
        }
    }

    private void setFlowNodeVoStatus(HistoricActivityInstance activity, String processInstanceId, FlowNodeVO vo, String defaultStatus) {
        if (activity.getActivityType().equals("subProcess")) {

            ServiceNodeStatusEntity statusEntity = serviceNodeStatusRepository
                    .findOneByProcInstanceIdAndNodeId(processInstanceId, activity.getActivityId());
            if (statusEntity != null) {
                vo.setStartTime(formatDate(statusEntity.getStartTime()));
                vo.setStatus(statusEntity.getStatus().name());
                return;
            }

            String sql = String.format(
                    "select * from %s T where T.ACT_TYPE_ = 'errorEndEvent' and T.PROC_INST_ID_ = #{procInstId} and T.PARENT_ACT_INST_ID_ = #{parentActInstId}",
                    processEngine.getManagementService().getTableName(HistoricActivityInstance.class));
            NativeHistoricActivityInstanceQuery errorEndEventsNativeQuery = processEngine.getHistoryService()
                    .createNativeHistoricActivityInstanceQuery().sql(sql).parameter("procInstId", processInstanceId)
                    .parameter("parentActInstId", activity.getId());

            Collection<HistoricActivityInstance> nativeErrorEndEvents = errorEndEventsNativeQuery.list();

            if (nativeErrorEndEvents != null && (!nativeErrorEndEvents.isEmpty())) {
                vo.setStatus(FAULTED);
            } else {
                vo.setStatus(defaultStatus);
            }
        } else {
            vo.setStatus(defaultStatus);
        }
    }

    private String generateProcessInstanceBusinessKey() {
        return LocalIdGenerator.INSTANCE.generateTimestampedId();
    }

    private void prepareProcessInstanceBusinessKey(StartProcessInstaceWithCiDataReq request,
            String processInstanceBizKey, ProcessDefinition procDef)
            throws JsonParseException, JsonMappingException, IOException {
        Integer rootCiTypeId = Integer.parseInt(request.getCiTypeId());
        String rootCiDataGuid = request.getCiDataId();
        
        List<Object> rootCiResults = cmdbServiceV2Stub.getCiDataByGuid(rootCiTypeId,
                Arrays.asList(rootCiDataGuid));
        
        if(rootCiResults == null || rootCiResults.isEmpty()){
            log.error("root ci doesnt exist,rootCiTypeId={},rootCiDataGuid={}", rootCiTypeId, rootCiDataGuid);
            throw new WecubeCoreException(String.format("failed to mark process instance key for cidata [%s,%s]", rootCiTypeId, rootCiDataGuid));
        }
        
        Map<String, Object> recordMap = (Map<String, Object>) ((Map<String, Object>) rootCiResults.get(0)).get("data");
        String existedBizKey = (String) recordMap.get("biz_key");
        
        if(StringUtils.isNotBlank(existedBizKey) && (!existedBizKey.equals(processInstanceBizKey))){
            log.error("such ci data already has process intance bound,rootCiTypeId={},rootCiDataGuid={}", rootCiTypeId, rootCiDataGuid);
            throw new WecubeCoreException(String.format("cidata [%s,%s] isnt available", rootCiTypeId, rootCiDataGuid));
        }

        Map<String, Object> ciDataToUpdate = new HashMap<>();
        ciDataToUpdate.put("guid", rootCiDataGuid);
        ciDataToUpdate.put("biz_key", processInstanceBizKey);

        cmdbServiceV2Stub.updateCiData(rootCiTypeId, ciDataToUpdate);

        // process ci bound on service task
        List<ProcessDefinitionTaskServiceEntity> taskServices = processDefinitionTaskServiceEntityRepository
                .findTaskServicesByProcDefKeyAndVersion(procDef.getKey(), procDef.getVersion());

        BpmnModelInstance bpmnModel = repositoryService.getBpmnModelInstance(procDef.getId());

        Collection<org.camunda.bpm.model.bpmn.instance.Process> processes = bpmnModel
                .getModelElementsByType(org.camunda.bpm.model.bpmn.instance.Process.class);

        if (processes.size() != 1) {
            log.error("at least one process should be provided, processDefinitionId={}", procDef.getId());
            throw new WecubeCoreException("process definition is not correct");
        }

        org.camunda.bpm.model.bpmn.instance.Process process = processes.iterator().next();

        Collection<ServiceTask> serviceTasks = process.getChildElementsByType(ServiceTask.class);
        Collection<SubProcess> subProcesses = process.getChildElementsByType(SubProcess.class);

        for (ProcessDefinitionTaskServiceEntity entity : taskServices) {

            if (isServiceTask(entity, serviceTasks) || isSubProcess(entity, subProcesses)) {
                processTaskServiceNode(rootCiTypeId, rootCiDataGuid, entity, processInstanceBizKey);
            } else {
                log.warn("such task node ID does not exist in process definition, taskNodeId={}",
                        entity.getTaskNodeId());
            }
        }

    }

    private void processTaskServiceNode(Integer rootCiTypeId, String rootCiDataGuid,
            ProcessDefinitionTaskServiceEntity entity, String processInstanceBizKey) throws IOException {

        String ciRoutineExpStr = entity.getBindCiRoutineExp();
        if (StringUtils.isBlank(ciRoutineExpStr)) {
            log.warn("CI routine express is empty, taskNode={}", entity.getTaskNodeId());
            return;
        }

        String serviceName = entity.getBindServiceName();

        Optional<PluginConfigInterface> pluginConfigInterface = pluginConfigRepository
                .findLatestOnlinePluginConfigInterfaceByServiceNameAndFetchParameters(serviceName);
        if (!pluginConfigInterface.isPresent())
            throw new WecubeCoreException(
                    String.format("Plugin interface not found for serviceName [%s].", serviceName));
        PluginConfigInterface inf = pluginConfigInterface.get();

        List<CiRoutineItem> routines = buildRoutines(ciRoutineExpStr);

        List<Map<String, Object>> data = buildIntegrationQueryAndGetQueryResult(inf, rootCiDataGuid, routines);

        for (Map<String, Object> ciMap : data) {
            String guid = (String) ciMap.get("tail$guid");

            if (StringUtils.isBlank(guid)) {
                log.warn("guid is blank, ciMap={}", ciMap);
                continue;
            }

            String existedBizKey = (String) ciMap.get("tail$biz_key");

            if (StringUtils.isNotBlank(existedBizKey) && (!existedBizKey.equals(processInstanceBizKey))) {
                log.error("bizKey already existed, guid={}", guid);
                throw new WecubeCoreException("business key already existed but still going to write in");
            }

            Map<String, Object> reqCiData = new HashMap<String, Object>();
            reqCiData.put("guid", guid);
            reqCiData.put("biz_key", processInstanceBizKey);

            int ciTypeId = routines.get(routines.size() - 1).getCiTypeId();

            log.info("to update ci data,ciTypeId={}, ciData={}", ciTypeId, reqCiData);
            cmdbServiceV2Stub.updateCiData(ciTypeId, reqCiData);
        }

    }

    private void populateFlowNodes(ProcessInstanceOutline outline, FlowNode rootFlowNode) {
        FlowNodeVO rootFlowNodeVO = buildFlowNodeVO(rootFlowNode);
        rootFlowNodeVO.setProcessDefinitionId(outline.getProcessDefinitionId());
        rootFlowNodeVO.setProcessInstanceId(outline.getProcessInstanceId());
        rootFlowNodeVO.setExecutionId(outline.getExecutionId());

        rootFlowNodeVO.setStatus(NOT_STARTED);

        outline.addFlowNode(rootFlowNodeVO);
        populateSucceedings(outline, rootFlowNode);
    }

    private FlowNodeVO findSourceFlowNodeVO(ProcessInstanceOutline outline, FlowNode srcFlowNode) {
        FlowNodeVO srcFlowNodeVO = outline.findFlowNodeVOById(srcFlowNode.getId());
        if (srcFlowNodeVO != null) {
            return srcFlowNodeVO;
        }

        Collection<FlowNode> previousFlowNodes = srcFlowNode.getPreviousNodes().list();
        if (previousFlowNodes.isEmpty() || (previousFlowNodes.size() > 1)) {
            throw new RuntimeException("unknown previous flow node for node:" + srcFlowNode.getId());
        }

        FlowNode previousFlowNode = previousFlowNodes.iterator().next();

        return findSourceFlowNodeVO(outline, previousFlowNode);
    }

    private void populateSucceedings(ProcessInstanceOutline outline, FlowNode srcFlowNode) {
        Collection<FlowNode> succeedingFlowNodes = srcFlowNode.getSucceedingNodes().list();
        FlowNodeVO srcFlowNodeVO = findSourceFlowNodeVO(outline, srcFlowNode);

        for (FlowNode fn : succeedingFlowNodes) {
            FlowNodeVO vo = outline.findFlowNodeVOById(fn.getId());
            if (vo != null) {
                srcFlowNodeVO.addToNode(vo);
                vo.addFromNode(srcFlowNodeVO);
                populateSucceedings(outline, fn);
                continue;
            }

            // to exclude some nodes needing to hide
            if (fn.getElementType().getTypeName().equals("parallelGateway") && (fn.getPreviousNodes().count() == 1)) {
                vo = null;
                populateSucceedings(outline, fn);
                continue;
            }

            vo = buildFlowNodeVO(fn);
            vo.setExecutionId(outline.getExecutionId());
            vo.setProcessDefinitionId(outline.getProcessDefinitionId());
            vo.setProcessInstanceId(outline.getProcessInstanceId());
            vo.setStatus(NOT_STARTED);
            srcFlowNodeVO.addToNode(vo);
            vo.addFromNode(srcFlowNodeVO);

            outline.addFlowNode(vo);
            populateSucceedings(outline, fn);
        }
    }

    private ProcessInstanceOutline buildProcessInstanceOutline(String processInstanceId) {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId).singleResult();

        ProcessInstanceOutline outline = new ProcessInstanceOutline();
        if (processInstance != null) {
            outline.setEnded(processInstance.isEnded());
            outline.setStatus(INPROGRESS);
            outline.setExecutionId(processInstance.getProcessInstanceId());
            outline.setProcessDefinitionId(processInstance.getProcessDefinitionId());
            outline.setProcessInstanceId(processInstanceId);

            return outline;
        }

        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId).singleResult();

        if (historicProcessInstance == null) {
            throw new RuntimeException("unknown process instance with id " + processInstanceId);
        }

        outline.setEnded(true);
        outline.setEndTime(historicProcessInstance.getEndTime().toString());
        outline.setStartTime(historicProcessInstance.getStartTime().toString());
        outline.setExecutionId(historicProcessInstance.getId());
        outline.setProcessDefinitionId(historicProcessInstance.getProcessDefinitionId());
        outline.setProcessInstanceId(processInstanceId);
        outline.setStatus(COMPLETED);

        return outline;
    }

    private ProcessTransactionVO processTransactionVO(ProcessTransactionEntity entity) {
        ProcessTransactionVO vo = new ProcessTransactionVO();
        vo.setId(entity.getId());
        vo.setAliasName(entity.getAliasName());
        vo.setName(entity.getName());
        vo.setOperator(entity.getOperator());
        vo.setOperatorGroup(entity.getOperatorGroup());
        vo.setStatus(entity.getStatus());

        vo.setAttach(attachVO(entity));

        if (entity.getTasks() != null) {
            for (ProcessTaskEntity task : entity.getTasks()) {
                vo.addTask(processTaskVO(task));
            }
        }
        return vo;
    }

    private AttachVO attachVO(ProcessTransactionEntity entity) {
        if (StringUtils.isBlank(entity.getAttach())) {
            return null;
        }

        ObjectMapper mapper = new ObjectMapper();
        try {
            AttachVO attach = mapper.readValue(entity.getAttach(), AttachVO.class);
            return attach;
        } catch (Exception e) {
            log.error("errors while unmarshal object", e);
        }

        return null;
    }

    private ProcessTaskVO processTaskVO(ProcessTaskEntity entity) {
        ProcessTaskVO vo = new ProcessTaskVO();
        vo.setId(entity.getId());
        vo.setOperator(entity.getOperator());
        vo.setOperatorGroup(entity.getOperatorGroup());
        vo.setProcessDefinitionId(entity.getProcessDefinitionId());
        vo.setProcessDefinitionKey(entity.getProcessDefinitionKey());
        vo.setProcessDefinitionVersion(entity.getProcessDefinitionVersion());
        vo.setProcessInstanceId(entity.getProcessInstanceId());
        vo.setProcessInstanceKey(entity.getProcessInstanceKey());
        vo.setRootCiDataId(entity.getRootCiDataId());
        vo.setRootCiTypeId(entity.getRootCiTypeId());
        vo.setStatus(entity.getStatus());

        return vo;
    }

    private ProcessTaskEntity processTaskEntity(String currUser, String ciDataId, String ciTypeId,
            String instanceBizKey, ProcessDefinition procDef) {
        Date currTime = new Date();

        ProcessTaskEntity task = new ProcessTaskEntity();
        task.setCreateBy(currUser);
        task.setCreateTime(currTime);
        task.setStartTime(currTime);
        task.setOperator(currUser);
        task.setProcessDefinitionId(procDef.getId());
        task.setProcessDefinitionKey(procDef.getKey());
        task.setProcessDefinitionVersion(procDef.getVersion());
        // task.setProcessInstanceId(resp.getProcessInstanceId());
        task.setProcessInstanceKey(instanceBizKey);
        task.setRootCiDataId(ciDataId);
        task.setRootCiTypeId(Integer.parseInt(ciTypeId));

        task.setStatus(INPROGRESS);

        return task;
    }

}
