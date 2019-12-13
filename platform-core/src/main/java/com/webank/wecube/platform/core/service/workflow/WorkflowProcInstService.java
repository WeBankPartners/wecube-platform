package com.webank.wecube.platform.core.service.workflow;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.workflow.ProcInstInfoDto;
import com.webank.wecube.platform.core.dto.workflow.ProcInstOutlineDto;
import com.webank.wecube.platform.core.dto.workflow.ProceedProcInstRequestDto;
import com.webank.wecube.platform.core.dto.workflow.RequestObjectDto;
import com.webank.wecube.platform.core.dto.workflow.StartProcInstRequestDto;
import com.webank.wecube.platform.core.dto.workflow.TaskNodeDefObjectBindInfoDto;
import com.webank.wecube.platform.core.dto.workflow.TaskNodeExecContextDto;
import com.webank.wecube.platform.core.dto.workflow.TaskNodeInstDto;
import com.webank.wecube.platform.core.entity.workflow.ProcDefInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.ProcExecBindingEntity;
import com.webank.wecube.platform.core.entity.workflow.ProcInstInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeDefInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeExecParamEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeExecRequestEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeInstInfoEntity;
import com.webank.wecube.platform.core.jpa.workflow.ProcDefInfoRepository;
import com.webank.wecube.platform.core.jpa.workflow.ProcExecBindingRepository;
import com.webank.wecube.platform.core.jpa.workflow.ProcInstInfoRepository;
import com.webank.wecube.platform.core.jpa.workflow.TaskNodeDefInfoRepository;
import com.webank.wecube.platform.core.jpa.workflow.TaskNodeExecParamRepository;
import com.webank.wecube.platform.core.jpa.workflow.TaskNodeExecRequestRepository;
import com.webank.wecube.platform.core.jpa.workflow.TaskNodeInstInfoRepository;
import com.webank.wecube.platform.workflow.commons.LocalIdGenerator;
import com.webank.wecube.platform.workflow.model.ProcFlowNodeInst;
import com.webank.wecube.platform.workflow.model.ProcInstOutline;

@Service
public class WorkflowProcInstService extends AbstractWorkflowService {
    private static final Logger log = LoggerFactory.getLogger(WorkflowProcInstService.class);

    @Autowired
    private ProcDefInfoRepository processDefInfoRepository;

    @Autowired
    private ProcInstInfoRepository procInstInfoRepository;

    @Autowired
    private TaskNodeDefInfoRepository taskNodeDefInfoRepository;

    @Autowired
    private TaskNodeInstInfoRepository taskNodeInstInfoRepository;

    @Autowired
    private ProcExecBindingRepository procExecBindingRepository;

    @Autowired
    private WorkflowEngineService workflowEngineService;

    @Autowired
    protected TaskNodeExecParamRepository taskNodeExecParamRepository;

    @Autowired
    protected TaskNodeExecRequestRepository taskNodeExecRequestRepository;

    public TaskNodeExecContextDto getTaskNodeContextInfo(Integer procInstId, Integer nodeInstId) {
        Optional<TaskNodeInstInfoEntity> nodeEntityOpt = taskNodeInstInfoRepository.findById(nodeInstId);
        if (!nodeEntityOpt.isPresent()) {
            throw new WecubeCoreException("Invalid node instance id:" + nodeInstId);
        }

        TaskNodeInstInfoEntity nodeEntity = nodeEntityOpt.get();

        TaskNodeExecContextDto result = new TaskNodeExecContextDto();
        result.setNodeDefId(nodeEntity.getNodeDefId());
        result.setNodeId(nodeEntity.getNodeId());
        result.setNodeInstId(nodeEntity.getId());
        result.setNodeName(nodeEntity.getNodeName());
        result.setNodeType(nodeEntity.getNodeType());

        TaskNodeExecRequestEntity requestEntity = taskNodeExecRequestRepository
                .findCurrentEntityByNodeInstId(nodeEntity.getId());

        if (requestEntity == null) {
            return result;
        }

        result.setRequestId(requestEntity.getRequestId());

        List<TaskNodeExecParamEntity> requestParamEntities = taskNodeExecParamRepository.findAllByRequestIdAndParamType(
                requestEntity.getRequestId(), TaskNodeExecParamEntity.PARAM_TYPE_REQUEST);

        List<TaskNodeExecParamEntity> responseParamEntities = taskNodeExecParamRepository
                .findAllByRequestIdAndParamType(requestEntity.getRequestId(),
                        TaskNodeExecParamEntity.PARAM_TYPE_RESPONSE);

        List<RequestObjectDto> requestObjects = calculateRequestObjectDtos(requestParamEntities, responseParamEntities);

        requestObjects.forEach(result::addRequestObjects);

        return result;
    }

    private List<RequestObjectDto> calculateRequestObjectDtos(List<TaskNodeExecParamEntity> requestParamEntities,
            List<TaskNodeExecParamEntity> responseParamEntities) {
        List<RequestObjectDto> requestObjects = new ArrayList<>();
        
        if(requestParamEntities == null){
            return requestObjects;
        }
        
        Map<String, Map<String,String>> respParamsByObjectId = new HashMap<String,Map<String,String>>();
        if(responseParamEntities != null){
            for(TaskNodeExecParamEntity respParamEntity : responseParamEntities){
                Map<String, String> respParamsMap = respParamsByObjectId.get(respParamEntity.getObjectId());
                if(respParamsMap == null){
                    respParamsMap = new HashMap<String,String>();
                    respParamsByObjectId.put(respParamEntity.getObjectId(), respParamsMap);
                }
                
                respParamsMap.put(respParamEntity.getParamName(), respParamEntity.getParamDataValue());
            }
        }
        
        Map<String,RequestObjectDto> objs = new HashMap<>();
        for(TaskNodeExecParamEntity rp : requestParamEntities){
            RequestObjectDto ro = objs.get(rp.getObjectId());
            if(ro == null){
                ro = new RequestObjectDto();
                objs.put(rp.getObjectId(), ro);
            }
            
            ro.addInput(rp.getParamName(), rp.getParamDataValue());
        }
        
        for(String objectId : objs.keySet()){
            RequestObjectDto obj = objs.get(objectId);
            Map<String, String> respParamsMap = respParamsByObjectId.get(objectId);
            if(respParamsMap != null){
                respParamsMap.forEach((k,v) -> {
                    obj.addOutput(k, v);
                });
            }
            
            requestObjects.add(obj);
        }

        return requestObjects;
    }


    public List<TaskNodeDefObjectBindInfoDto> getProcessInstanceExecBindings(Integer procInstId) {
        Optional<ProcInstInfoEntity> procInstEntityOpt = procInstInfoRepository.findById(procInstId);
        if (!procInstEntityOpt.isPresent()) {
            throw new WecubeCoreException(String.format("Such entity with id [%s] does not exist.", procInstId));
        }

        ProcInstInfoEntity procInstEntity = procInstEntityOpt.get();

        List<ProcExecBindingEntity> bindEntities = procExecBindingRepository
                .findAllTaskNodeBindingsByProcInstId(procInstEntity.getId());

        List<TaskNodeDefObjectBindInfoDto> result = new ArrayList<>();

        if (bindEntities == null || bindEntities.isEmpty()) {
            return result;
        }

        List<TaskNodeInstInfoEntity> nodeInstEntities = taskNodeInstInfoRepository
                .findAllByProcInstId(procInstEntity.getId());

        bindEntities.forEach(n -> {
            TaskNodeDefObjectBindInfoDto d = new TaskNodeDefObjectBindInfoDto();

            d.setEntityDataId(n.getEntityDataId());
            d.setEntityTypeId(n.getEntityTypeId());
            d.setNodeDefId(n.getNodeDefId());

            TaskNodeInstInfoEntity nodeInstEntity = findTaskNodeInstInfoEntityByTaskNodeDefId(nodeInstEntities,
                    n.getNodeDefId());

            if (nodeInstEntity != null) {
                d.setOrderedNo(nodeInstEntity.getOrderedNo());
            } else {
                d.setOrderedNo("");
            }

            result.add(d);
        });

        return result;
    }

    public void proceedProcessInstance(ProceedProcInstRequestDto request) {
        if (request == null) {
            log.error("request is null");
            throw new WecubeCoreException("Request is null.");
        }

        Optional<ProcInstInfoEntity> procInstOpt = procInstInfoRepository.findById(request.getProcInstId());

        if (!procInstOpt.isPresent()) {
            log.error("such process instance does not exist,id={}", request.getProcInstId());
            throw new WecubeCoreException("Such process instance does not exist.");
        }

        ProcInstInfoEntity procInst = procInstOpt.get();
        refreshProcessInstanceStatus(procInst);

        Optional<TaskNodeInstInfoEntity> nodeInstOpt = taskNodeInstInfoRepository.findById(request.getNodeInstId());
        if (!nodeInstOpt.isPresent()) {
            log.error("such task node instance does not exist,process id :{}, task node id:{}", request.getProcInstId(),
                    request.getNodeInstId());
            throw new WecubeCoreException("Such task node instance does not exist.");
        }

        TaskNodeInstInfoEntity nodeInst = nodeInstOpt.get();

        if (!procInst.getId().equals(nodeInst.getProcInstId())) {
            log.error("Illegal task node id:{}", nodeInst.getProcInstId());
            throw new WecubeCoreException("Illegal task node id");
        }

        if (!ProcInstInfoEntity.IN_PROGRESS_STATUS.equals(procInst.getStatus())) {
            log.error("cannot proceed with such process instance status:{}", procInst.getStatus());
            throw new WecubeCoreException("Cannot proceed with such process instance status");
        }

        if (TaskNodeInstInfoEntity.NOT_STARTED_STATUS.equals(nodeInst.getStatus())
                || TaskNodeInstInfoEntity.IN_PROGRESS_STATUS.equals(nodeInst.getStatus())
                || TaskNodeInstInfoEntity.COMPLETED_STATUS.equals(nodeInst.getStatus())) {
            log.error("cannot proceed with such task node instance status:{}", procInst.getStatus());
            throw new WecubeCoreException("Cannot proceed with such task node instance status");
        }

        doProceedProcessInstance(request, procInst, nodeInst);

        String nodeStatus = workflowEngineService.getTaskNodeStatus(procInst.getProcInstKernelId(),
                nodeInst.getNodeId());
        if (StringUtils.isNotBlank(nodeStatus)) {
            if (!nodeStatus.equals(nodeInst.getStatus())) {
                nodeInst.setUpdatedTime(new Date());
                nodeInst.setStatus(nodeStatus);
                taskNodeInstInfoRepository.save(nodeInst);
            }
        }
    }

    protected void refreshProcessInstanceStatus(ProcInstInfoEntity procInstEntity) {
        List<TaskNodeInstInfoEntity> nodeInstEntities = taskNodeInstInfoRepository
                .findAllByProcInstId(procInstEntity.getId());
        String kernelProcInstId = procInstEntity.getProcInstKernelId();
        for (TaskNodeInstInfoEntity nie : nodeInstEntities) {
            String nodeId = nie.getNodeId();
            String nodeStatus = workflowEngineService.getTaskNodeStatus(kernelProcInstId, nodeId);
            if (StringUtils.isBlank(nodeStatus)) {
                continue;
            }

            if (!nodeStatus.equals(nie.getStatus())) {
                nie.setStatus(nodeStatus);
                nie.setUpdatedTime(new Date());
                taskNodeInstInfoRepository.save(nie);
            }
        }
    }

    protected void doProceedProcessInstance(ProceedProcInstRequestDto request, ProcInstInfoEntity procInst,
            TaskNodeInstInfoEntity nodeInst) {

        if (log.isInfoEnabled()) {
            log.info("about to proceed proceess instance {} task node {} with action {}", procInst.getId(),
                    nodeInst.getId(), request.getAct());
        }
        workflowEngineService.proceedProcessInstance(procInst.getProcInstKernelId(), nodeInst.getNodeId(),
                request.getAct());
    }

    public List<ProcInstInfoDto> getProcessInstances() {
        List<ProcInstInfoDto> result = new ArrayList<>();
        List<ProcInstInfoEntity> procInstInfoEntities = procInstInfoRepository.findAll();

        for (ProcInstInfoEntity e : procInstInfoEntities) {
            ProcInstInfoDto d = new ProcInstInfoDto();
            d.setCreatedTime(formatDate(e.getCreatedTime()));
            d.setId(e.getId());
            d.setOperator(e.getOperator());
            d.setProcDefId(e.getProcDefId());
            d.setProcInstKey(e.getProcInstKey());
            d.setStatus(e.getStatus());
            d.setProcInstName(e.getProcDefName());
            d.setProcInstKey(e.getProcInstKey());

            ProcExecBindingEntity be = procExecBindingRepository.findProcInstBindings(e.getId());
            if (be != null) {
                d.setEntityDataId(be.getEntityDataId());
                d.setEntityTypeId(be.getEntityTypeId());
            }

            result.add(d);
        }

        return result;
    }

    public ProcInstOutlineDto getProcessInstanceOutline(Integer id) {
        return null;
    }

    public ProcInstInfoDto getProcessInstanceById(Integer id) {

        Optional<ProcInstInfoEntity> procInstEntityOpt = procInstInfoRepository.findById(id);
        if (!procInstEntityOpt.isPresent()) {
            throw new WecubeCoreException(String.format("Such entity with id [%s] does not exist.", id));
        }

        ProcInstInfoEntity procInstEntity = procInstEntityOpt.get();

        String procInstanceKernelId = procInstEntity.getProcInstKernelId();

        if (StringUtils.isBlank(procInstanceKernelId)) {
            throw new WecubeCoreException("Unknow kernel process instance.");
        }

        if (!ProcInstInfoEntity.COMPLETED_STATUS.equals(procInstEntity.getStatus())) {
            // TODO refresh status

            ProcInstOutline procInstOutline = workflowEngineService.getProcInstOutline(procInstanceKernelId);
            if (procInstEntity.getStatus().equals(procInstOutline.getStatus())) {
                procInstEntity.setStatus(procInstOutline.getStatus());
                procInstInfoRepository.save(procInstEntity);
            }

            List<TaskNodeInstInfoEntity> nodeInstEntities = taskNodeInstInfoRepository
                    .findAllByProcInstId(procInstEntity.getId());
            for (TaskNodeInstInfoEntity nodeInstEntity : nodeInstEntities) {
                ProcFlowNodeInst pfni = procInstOutline.findProcFlowNodeInstByNodeId(nodeInstEntity.getNodeId());
                if (pfni != null && (pfni.getStatus() != null)
                        && (!pfni.getStatus().equals(nodeInstEntity.getStatus()))) {
                    nodeInstEntity.setStatus(pfni.getStatus());
                    taskNodeInstInfoRepository.save(nodeInstEntity);
                }
            }
        }

        ProcExecBindingEntity procInstBindEntity = procExecBindingRepository
                .findProcInstBindings(procInstEntity.getId());

        String entityTypeId = null;
        String entityDataId = null;

        if (procInstBindEntity != null) {
            entityTypeId = procInstBindEntity.getEntityTypeId();
            entityDataId = procInstBindEntity.getEntityDataId();
        }

        ProcInstInfoDto result = new ProcInstInfoDto();
        result.setId(procInstEntity.getId());
        result.setOperator(procInstEntity.getOperator());
        result.setProcDefId(procInstEntity.getProcDefId());
        result.setProcInstKey(procInstEntity.getProcInstKey());
        result.setProcInstName(procInstEntity.getProcDefName());
        result.setEntityTypeId(entityTypeId);
        result.setEntityDataId(entityDataId);
        result.setStatus(procInstEntity.getStatus());
        result.setCreatedTime(formatDate(procInstEntity.getCreatedTime()));

        List<TaskNodeInstInfoEntity> nodeEntities = taskNodeInstInfoRepository
                .findAllByProcInstId(procInstEntity.getId());

        List<TaskNodeDefInfoEntity> nodeDefEntities = taskNodeDefInfoRepository
                .findAllByProcDefId(procInstEntity.getProcDefId());

        for (TaskNodeInstInfoEntity n : nodeEntities) {
            TaskNodeDefInfoEntity nodeDef = findTaskNodeDefInfoEntityByNodeDefId(nodeDefEntities, n.getNodeDefId());
            TaskNodeInstDto nd = new TaskNodeInstDto();
            nd.setId(n.getId());
            nd.setNodeDefId(n.getNodeDefId());
            nd.setNodeId(n.getNodeId());
            nd.setNodeName(n.getNodeName());
            nd.setNodeType(n.getNodeType());
            nd.setOrderedNo(n.getOrderedNo());

            if (nodeDef != null) {
                nd.setPreviousNodeIds(unmarshalNodeIds(nodeDef.getPreviousNodeIds()));
                nd.setSucceedingNodeIds(unmarshalNodeIds(nodeDef.getSucceedingNodeIds()));
                nd.setOrderedNo(nodeDef.getOrderedNo());
                nd.setRoutineExpression(nodeDef.getRoutineExpression());
            }
            nd.setProcDefId(n.getProcDefId());
            nd.setProcDefKey(n.getProcDefKey());
            nd.setProcInstId(n.getProcInstId());
            nd.setProcInstKey(n.getProcInstKey());
            nd.setStatus(n.getStatus());

            result.addTaskNodeInstances(nd);
        }

        return result;
    }

    private TaskNodeDefInfoEntity findTaskNodeDefInfoEntityByNodeDefId(List<TaskNodeDefInfoEntity> nodeDefEntities,
            String nodeDefId) {
        for (TaskNodeDefInfoEntity nodeDef : nodeDefEntities) {
            if (nodeDefId.equals(nodeDef.getId())) {
                return nodeDef;
            }
        }

        return null;
    }

    public ProcInstInfoDto createProcessInstance(StartProcInstRequestDto requestDto) {
        if (StringUtils.isBlank(requestDto.getProcDefId())) {
            if (log.isDebugEnabled()) {
                log.debug("Process definition ID is blank.");
            }
            throw new WecubeCoreException("Process definition ID is blank.");
        }

        String rootEntityTypeId = requestDto.getEntityTypeId();
        String rootEntityDataId = requestDto.getEntityDataId();

        String procDefId = requestDto.getProcDefId();
        Optional<ProcDefInfoEntity> procDefInfoEntityOpt = processDefInfoRepository.findById(procDefId);

        if (!procDefInfoEntityOpt.isPresent()) {
            throw new WecubeCoreException(String.format("Invalid process definition ID:%s", procDefId));
        }

        ProcDefInfoEntity procDefInfoEntity = procDefInfoEntityOpt.get();
        if (!ProcDefInfoEntity.DEPLOYED_STATUS.equals(procDefInfoEntity.getStatus())) {
            log.error("expected status {} but {} for procDefId {}", ProcDefInfoEntity.DEPLOYED_STATUS,
                    procDefInfoEntity.getStatus(), procDefId);
            throw new WecubeCoreException(String.format("Invalid process definition ID:%s", procDefId));
        }

        if (StringUtils.isBlank(procDefInfoEntity.getProcDefKernelId())) {
            log.error("cannot know process definition id for {}", procDefId);
            throw new WecubeCoreException(String.format("Invalid process definition ID:%s", procDefId));
        }

        String procInstKey = LocalIdGenerator.generateId();

        ProcInstInfoEntity procInstInfoEntity = new ProcInstInfoEntity();
        procInstInfoEntity.setStatus(ProcInstInfoEntity.NOT_STARTED_STATUS);
        procInstInfoEntity.setOperator("admin");
        procInstInfoEntity.setProcDefId(procDefId);
        procInstInfoEntity.setProcDefKey(procDefInfoEntity.getProcDefKey());
        procInstInfoEntity.setProcDefName(procDefInfoEntity.getProcDefName());
        procInstInfoEntity.setProcInstKey(procInstKey);

        procInstInfoRepository.save(procInstInfoEntity);

        ProcExecBindingEntity procInstBindEntity = new ProcExecBindingEntity();
        procInstBindEntity.setBindType(ProcExecBindingEntity.BIND_TYPE_PROC_INSTANCE);
        procInstBindEntity.setEntityTypeId(rootEntityTypeId);
        procInstBindEntity.setEntityDataId(rootEntityDataId);
        procInstBindEntity.setProcDefId(procDefId);
        procInstBindEntity.setProcInstId(procInstInfoEntity.getId());
        procExecBindingRepository.save(procInstBindEntity);

        List<TaskNodeDefInfoEntity> taskNodeDefInfoEntities = taskNodeDefInfoRepository.findAllByProcDefId(procDefId);

        for (TaskNodeDefInfoEntity taskNodeDefInfoEntity : taskNodeDefInfoEntities) {
            TaskNodeInstInfoEntity taskNodeInstInfoEntity = new TaskNodeInstInfoEntity();
            taskNodeInstInfoEntity.setStatus(TaskNodeInstInfoEntity.NOT_STARTED_STATUS);
            taskNodeInstInfoEntity.setNodeDefId(taskNodeDefInfoEntity.getId());
            taskNodeInstInfoEntity.setNodeId(taskNodeDefInfoEntity.getNodeId());
            taskNodeInstInfoEntity.setNodeName(taskNodeDefInfoEntity.getNodeName());
            taskNodeInstInfoEntity.setOperator("admin");
            taskNodeInstInfoEntity.setProcDefId(taskNodeDefInfoEntity.getProcDefId());
            taskNodeInstInfoEntity.setProcDefKey(taskNodeDefInfoEntity.getProcDefKey());
            taskNodeInstInfoEntity.setProcInstId(procInstInfoEntity.getId());
            taskNodeInstInfoEntity.setProcInstKey(procInstInfoEntity.getProcInstKey());
            taskNodeInstInfoEntity.setNodeType(taskNodeDefInfoEntity.getNodeType());

            taskNodeInstInfoRepository.save(taskNodeInstInfoEntity);

            List<TaskNodeDefObjectBindInfoDto> bindInfoDtos = pickUpTaskNodeDefObjectBindInfoDtos(requestDto,
                    taskNodeDefInfoEntity.getId());

            for (TaskNodeDefObjectBindInfoDto bindInfoDto : bindInfoDtos) {
                ProcExecBindingEntity nodeBindEntity = new ProcExecBindingEntity();
                nodeBindEntity.setBindType(ProcExecBindingEntity.BIND_TYPE_TASK_NODE_INSTANCE);
                nodeBindEntity.setProcInstId(procInstInfoEntity.getId());
                nodeBindEntity.setProcDefId(procDefId);
                nodeBindEntity.setNodeDefId(bindInfoDto.getNodeDefId());
                nodeBindEntity.setTaskNodeInstId(taskNodeInstInfoEntity.getId());
                nodeBindEntity.setEntityTypeId(bindInfoDto.getEntityTypeId());
                nodeBindEntity.setEntityDataId(bindInfoDto.getEntityDataId());

                procExecBindingRepository.save(nodeBindEntity);
            }
        }

        ProcInstInfoDto result = doCreateProcessInstance(procInstInfoEntity, procDefInfoEntity.getProcDefKernelId(),
                procInstKey);

        return result;
    }

    protected ProcInstInfoDto doCreateProcessInstance(ProcInstInfoEntity procInstInfoEntity, String processDefinitionId,
            String procInstKey) {
        ProcessInstance processInstance = workflowEngineService.startProcessInstance(processDefinitionId, procInstKey);

        // TODO handle failure
        Optional<ProcInstInfoEntity> existProcInstInfoEntityOpt = procInstInfoRepository
                .findById(procInstInfoEntity.getId());

        if (!existProcInstInfoEntityOpt.isPresent()) {
            log.error("such record does not exist,id={},procInstKey={}", procInstInfoEntity.getId(), procInstKey);
            throw new WecubeCoreException("Errors while starting process instance.");
        }

        ProcInstInfoEntity procEntity = existProcInstInfoEntityOpt.get();

        Date now = new Date();

        procEntity.setUpdatedTime(now);
        procEntity.setProcInstKernelId(processInstance.getId());
        procEntity.setStatus(ProcInstInfoEntity.IN_PROGRESS_STATUS);

        procInstInfoRepository.save(procEntity);

        String entityTypeId = null;
        String entityDataId = null;

        ProcExecBindingEntity procInstBindEntity = procExecBindingRepository
                .findProcInstBindings(procInstInfoEntity.getId());

        if (procInstBindEntity != null) {
            entityTypeId = procInstBindEntity.getEntityTypeId();
            entityDataId = procInstBindEntity.getEntityDataId();
        }

        ProcInstInfoDto result = new ProcInstInfoDto();
        result.setId(procEntity.getId());
        result.setOperator(procEntity.getOperator());
        result.setProcDefId(procEntity.getProcDefId());
        result.setProcInstKey(procEntity.getProcDefKey());
        result.setStatus(procEntity.getStatus());
        result.setEntityTypeId(entityTypeId);
        result.setEntityDataId(entityDataId);

        List<TaskNodeInstInfoEntity> nodeInstEntities = taskNodeInstInfoRepository
                .findAllByProcInstId(procEntity.getId());

        for (TaskNodeInstInfoEntity n : nodeInstEntities) {
            if ("startEvent".equals(n.getNodeType())) {
                n.setUpdatedBy("sys");
                n.setUpdatedTime(now);
                n.setStatus(TaskNodeInstInfoEntity.COMPLETED_STATUS);
                taskNodeInstInfoRepository.save(n);
            }
        }

        List<TaskNodeDefInfoEntity> nodeDefEntities = taskNodeDefInfoRepository
                .findAllByProcDefId(procEntity.getProcDefId());

        for (TaskNodeDefInfoEntity nodeDefEntity : nodeDefEntities) {
            TaskNodeInstInfoEntity nodeInstEntity = findTaskNodeInstInfoEntityByTaskNodeDefId(nodeInstEntities,
                    nodeDefEntity.getId());
            TaskNodeInstDto nd = new TaskNodeInstDto();

            if (nodeInstEntity != null) {
                nd.setId(nodeInstEntity.getId());
                nd.setProcInstId(nodeInstEntity.getProcInstId());
                nd.setProcInstKey(nodeInstEntity.getProcInstKey());
                nd.setStatus(nodeInstEntity.getStatus());
            }
            nd.setNodeDefId(nodeDefEntity.getId());
            nd.setNodeId(nodeDefEntity.getNodeId());
            nd.setNodeName(nodeDefEntity.getNodeName());
            nd.setNodeType(nodeDefEntity.getNodeType());
            nd.setOrderedNo(nodeDefEntity.getOrderedNo());

            nd.setPreviousNodeIds(unmarshalNodeIds(nodeDefEntity.getPreviousNodeIds()));
            nd.setSucceedingNodeIds(unmarshalNodeIds(nodeDefEntity.getSucceedingNodeIds()));
            nd.setProcDefId(nodeDefEntity.getProcDefId());
            nd.setProcDefKey(nodeDefEntity.getProcDefKey());

            result.addTaskNodeInstances(nd);
        }

        return result;
    }

    private TaskNodeInstInfoEntity findTaskNodeInstInfoEntityByTaskNodeDefId(
            List<TaskNodeInstInfoEntity> nodeInstEntities, String nodeDefId) {
        if (nodeInstEntities == null || nodeInstEntities.isEmpty()) {
            return null;
        }

        for (TaskNodeInstInfoEntity inst : nodeInstEntities) {
            if (inst.getNodeDefId().equals(nodeDefId)) {
                return inst;
            }
        }

        return null;
    }

    private List<TaskNodeDefObjectBindInfoDto> pickUpTaskNodeDefObjectBindInfoDtos(StartProcInstRequestDto requestDto,
            String nodeDefId) {
        List<TaskNodeDefObjectBindInfoDto> result = new ArrayList<>();
        if (requestDto.getTaskNodeBinds() == null) {
            return result;
        }

        for (TaskNodeDefObjectBindInfoDto biDto : requestDto.getTaskNodeBinds()) {
            if (nodeDefId.equals(biDto.getNodeDefId())) {
                result.add(biDto);
            }
        }

        return result;
    }

}
