package com.webank.wecube.platform.core.service.workflow;

import java.util.*;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.webank.wecube.platform.core.dto.workflow.*;
import com.webank.wecube.platform.core.entity.workflow.*;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.commons.AuthenticationContextHolder;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.jpa.workflow.GraphNodeRepository;
import com.webank.wecube.platform.core.jpa.workflow.ProcDefInfoMapper;
import com.webank.wecube.platform.core.jpa.workflow.ProcExecBindingRepository;
import com.webank.wecube.platform.core.jpa.workflow.ProcExecBindingTmpRepository;
import com.webank.wecube.platform.core.jpa.workflow.ProcInstInfoRepository;
import com.webank.wecube.platform.core.jpa.workflow.ProcRoleBindingRepository;
import com.webank.wecube.platform.core.jpa.workflow.TaskNodeDefInfoRepository;
import com.webank.wecube.platform.core.jpa.workflow.TaskNodeExecParamRepository;
import com.webank.wecube.platform.core.jpa.workflow.TaskNodeExecRequestRepository;
import com.webank.wecube.platform.core.jpa.workflow.TaskNodeInstInfoRepository;
import com.webank.wecube.platform.core.service.user.UserManagementServiceImpl;
import com.webank.wecube.platform.workflow.commons.LocalIdGenerator;
import com.webank.wecube.platform.workflow.model.ProcFlowNodeInst;
import com.webank.wecube.platform.workflow.model.ProcInstOutline;

@Service
public class WorkflowProcInstService extends AbstractWorkflowService {
    private static final Logger log = LoggerFactory.getLogger(WorkflowProcInstService.class);

    @Autowired
    private ProcDefInfoMapper processDefInfoRepository;

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

    @Autowired
    private UserManagementServiceImpl userManagementService;

    @Autowired
    private ProcRoleBindingRepository procRoleBindingRepository;

    @Autowired
    private ProcExecBindingTmpRepository procExecBindingTmpRepository;

    @Autowired
    protected GraphNodeRepository graphNodeRepository;

    @Autowired
    private EntityManager entityManager;

    public List<TaskNodeDefObjectBindInfoDto> getProcessInstanceExecBindings(Integer procInstId) {
        Optional<ProcInstInfoEntity> procInstEntityOpt = procInstInfoRepository.findById(procInstId);
        if (!procInstEntityOpt.isPresent()) {
            throw new WecubeCoreException("3135", String.format("Such entity with id [%s] does not exist.", procInstId),
                    procInstId);
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
            log.warn("request is null");
            throw new WecubeCoreException("3136", "Request is null.");
        }

        Optional<ProcInstInfoEntity> procInstOpt = procInstInfoRepository.findById(request.getProcInstId());

        if (!procInstOpt.isPresent()) {
            log.warn("such process instance does not exist,id={}", request.getProcInstId());
            throw new WecubeCoreException("3137", "Such process instance does not exist.");
        }

        ProcInstInfoEntity procInst = procInstOpt.get();
        refreshProcessInstanceStatus(procInst);

        Optional<TaskNodeInstInfoEntity> nodeInstOpt = taskNodeInstInfoRepository.findById(request.getNodeInstId());
        if (!nodeInstOpt.isPresent()) {
            log.warn("such task node instance does not exist,process id :{}, task node id:{}", request.getProcInstId(),
                    request.getNodeInstId());
            throw new WecubeCoreException("3138", "Such task node instance does not exist.");
        }

        TaskNodeInstInfoEntity nodeInst = nodeInstOpt.get();

        if (!procInst.getId().equals(nodeInst.getProcInstId())) {
            log.warn("Illegal task node id:{}", nodeInst.getProcInstId());
            throw new WecubeCoreException("3139", "Illegal task node id");
        }

        if (!ProcInstInfoEntity.IN_PROGRESS_STATUS.equals(procInst.getStatus())) {
            log.warn("cannot proceed with such process instance status:{}", procInst.getStatus());
            throw new WecubeCoreException("3140", "Cannot proceed with such process instance status");
        }

        if (TaskNodeInstInfoEntity.NOT_STARTED_STATUS.equals(nodeInst.getStatus())
                || TaskNodeInstInfoEntity.IN_PROGRESS_STATUS.equals(nodeInst.getStatus())
                || TaskNodeInstInfoEntity.COMPLETED_STATUS.equals(nodeInst.getStatus())) {
            log.warn("cannot proceed with such task node instance status:{}", procInst.getStatus());
            throw new WecubeCoreException("3141", "Cannot proceed with such task node instance status");
        }

        doProceedProcessInstance(request, procInst, nodeInst);

        String nodeStatus = workflowEngineService.getTaskNodeStatus(procInst.getProcInstKernelId(),
                nodeInst.getNodeId());
        if (StringUtils.isNotBlank(nodeStatus)) {
            if (!nodeStatus.equals(nodeInst.getStatus())) {
                nodeInst.setUpdatedTime(new Date());
                nodeInst.setStatus(nodeStatus);
                taskNodeInstInfoRepository.saveAndFlush(nodeInst);
            }
        }
    }

    protected void refreshProcessInstanceStatus(ProcInstInfoEntity procInstEntity) {
        List<TaskNodeInstInfoEntity> nodeInstEntities = taskNodeInstInfoRepository
                .findAllByProcInstId(procInstEntity.getId());
        String kernelProcInstId = procInstEntity.getProcInstKernelId();
        Date currTime = new Date();
        for (TaskNodeInstInfoEntity nie : nodeInstEntities) {
            String nodeId = nie.getNodeId();
            String nodeStatus = workflowEngineService.getTaskNodeStatus(kernelProcInstId, nodeId);
            if (StringUtils.isBlank(nodeStatus)) {
                continue;
            }

            if (!nodeStatus.equals(nie.getStatus())) {
                nie.setStatus(nodeStatus);
                nie.setUpdatedTime(currTime);
                taskNodeInstInfoRepository.saveAndFlush(nie);
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
        List<ProcInstInfoDto> results = new ArrayList<>();

        Set<String> logUserRoleNames = AuthenticationContextHolder.getCurrentUserRoles();
        if (logUserRoleNames == null || logUserRoleNames.isEmpty()) {
            return results;
        }

        List<String> roleNames = new ArrayList<String>();
        for (String roleName : logUserRoleNames) {
            roleNames.add(roleName);
        }

        List<?> procInstInfoQueryEntities = queryProcInstInfoByRoleNames(roleNames);

        if (procInstInfoQueryEntities == null || procInstInfoQueryEntities.isEmpty()) {
            return results;
        }

        for (Object obj : procInstInfoQueryEntities) {
            ProcInstInfoQueryEntity e = (ProcInstInfoQueryEntity) obj;
            ProcInstInfoDto d = new ProcInstInfoDto();
            d.setCreatedTime(formatDate(e.getCreatedTime()));
            d.setId(e.getId());
            d.setOperator(e.getOperator());
            d.setProcDefId(e.getProcDefId());
            d.setProcInstKey(e.getProcInstKey());
            d.setStatus(e.getStatus());
            d.setProcInstName(e.getProcDefName());
            d.setProcInstKey(e.getProcInstKey());

            d.setEntityDataId(e.getEntityDataId());
            d.setEntityTypeId(e.getEntityTypeId());
            d.setEntityDisplayName(e.getEntityDataName() == null ? e.getEntityDataId() : e.getEntityDataName());

            results.add(d);
        }

        return results;
    }

    public ProcInstOutlineDto getProcessInstanceOutline(Integer id) {
        return null;
    }

    public ProcInstInfoDto getProcessInstanceById(Integer id) {
        Optional<ProcInstInfoEntity> procInstEntityOpt = procInstInfoRepository.findById(id);
        if (!procInstEntityOpt.isPresent()) {
            throw new WecubeCoreException("3142", String.format("Such entity with id [%s] does not exist.", id), id);
        }

        ProcInstInfoEntity procInstEntity = procInstEntityOpt.get();

        this.checkCurrentUserRole(procInstEntity.getProcDefId());

        ProcInstInfoDto result = new ProcInstInfoDto();

        String procInstanceKernelId = procInstEntity.getProcInstKernelId();

        if (StringUtils.isBlank(procInstanceKernelId)) {
            throw new WecubeCoreException("3143", "Unknow kernel process instance.");
        }

        ProcInstOutline procInstOutline = workflowEngineService.getProcInstOutline(procInstanceKernelId);
        if (procInstEntity.getStatus().equals(procInstOutline.getStatus())) {
            procInstEntity.setStatus(procInstOutline.getStatus());
            procInstInfoRepository.saveAndFlush(procInstEntity);
        }

        List<TaskNodeInstInfoEntity> nodeInstEntities = taskNodeInstInfoRepository
                .findAllByProcInstId(procInstEntity.getId());
        for (TaskNodeInstInfoEntity nodeInstEntity : nodeInstEntities) {
            ProcFlowNodeInst pfni = procInstOutline.findProcFlowNodeInstByNodeId(nodeInstEntity.getNodeId());
            if (pfni != null && (pfni.getStatus() != null) && (!pfni.getStatus().equals(nodeInstEntity.getStatus()))) {
                nodeInstEntity.setStatus(pfni.getStatus());
                nodeInstEntity.setUpdatedTime(new Date());
                taskNodeInstInfoRepository.saveAndFlush(nodeInstEntity);
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
            nd.setNodeName(reduceTaskNodeName(n));
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

    public void checkCurrentUserRole(String procDefId) {
        List<String> roleIdList = this.userManagementService
                .getRoleIdsByUsername(AuthenticationContextHolder.getCurrentUsername());
        if (roleIdList.size() == 0) {
            throw new WecubeCoreException("3144", "No access to this resource.");
        }

        List<String> procDefIds = procRoleBindingRepository.findDistinctProcIdByRoleIdsAndPermissionIsUse(roleIdList);
        if (procDefIds.size() == 0) {
            throw new WecubeCoreException("3145", "No access to this resource.");
        }

        if (!procDefIds.contains(procDefId)) {
            throw new WecubeCoreException("3146", "No access to this resource.");
        }

    }

    private String reduceTaskNodeName(TaskNodeInstInfoEntity nodeInstEntity) {
        if (!StringUtils.isBlank(nodeInstEntity.getNodeName())) {
            return nodeInstEntity.getNodeName();
        }

        if ("startEvent".equals(nodeInstEntity.getNodeType())) {
            return "S";
        }

        if ("endEvent".equals(nodeInstEntity.getNodeType())) {
            return "E";
        }

        if ("exclusiveGateway".equals(nodeInstEntity.getNodeType())) {
            return "X";
        }

        return "";
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

    public ProcInstInfoDto createProcessInstanceAndRole(StartProcInstRequestDto requestDto) {
        if (StringUtils.isBlank(requestDto.getProcDefId())) {
            throw new WecubeCoreException("3147", "Process definition ID is blank.");
        }
        this.checkCurrentUserRole(requestDto.getProcDefId());
        return this.createProcessInstance(requestDto);
    }

    public ProcInstInfoDto createProcessInstance(StartProcInstRequestDto requestDto) {
        if (StringUtils.isBlank(requestDto.getProcDefId())) {
            if (log.isDebugEnabled()) {
                log.debug("Process definition ID is blank.");
            }
            throw new WecubeCoreException("3148", "Process definition ID is blank.");
        }

        String rootEntityTypeId = requestDto.getEntityTypeId();
        String rootEntityDataId = requestDto.getEntityDataId();
        String rootEntityDataName = requestDto.getEntityDisplayName();

        if (StringUtils.isBlank(rootEntityDataName)) {
            rootEntityDataName = tryCalEntityDataName(requestDto);
        }

        String procDefId = requestDto.getProcDefId();
        ProcDefInfoEntity procDefInfoEntity = processDefInfoRepository.selectByPrimaryKey(procDefId);

        if (procDefInfoEntity == null) {
            throw new WecubeCoreException("3149", String.format("Invalid process definition ID:%s", procDefId));
        }

        if (!ProcDefInfoEntity.DEPLOYED_STATUS.equals(procDefInfoEntity.getStatus())) {
            log.warn("expected status {} but {} for procDefId {}", ProcDefInfoEntity.DEPLOYED_STATUS,
                    procDefInfoEntity.getStatus(), procDefId);
            throw new WecubeCoreException("3150", String.format("Invalid process definition ID:%s", procDefId));
        }

        if (StringUtils.isBlank(procDefInfoEntity.getProcDefKernelId())) {
            log.warn("cannot know process definition id for {}", procDefId);
            throw new WecubeCoreException("3151", String.format("Invalid process definition ID:%s", procDefId));
        }

        String procInstKey = LocalIdGenerator.generateId();

        ProcInstInfoEntity procInstInfoEntity = new ProcInstInfoEntity();
        procInstInfoEntity.setStatus(ProcInstInfoEntity.NOT_STARTED_STATUS);
        procInstInfoEntity.setOperator(AuthenticationContextHolder.getCurrentUsername());
        procInstInfoEntity.setProcDefId(procDefId);
        procInstInfoEntity.setProcDefKey(procDefInfoEntity.getProcDefKey());
        procInstInfoEntity.setProcDefName(procDefInfoEntity.getProcDefName());
        procInstInfoEntity.setProcInstKey(procInstKey);

        procInstInfoRepository.saveAndFlush(procInstInfoEntity);

        ProcExecBindingEntity procInstBindEntity = new ProcExecBindingEntity();
        procInstBindEntity.setBindType(ProcExecBindingEntity.BIND_TYPE_PROC_INSTANCE);
        procInstBindEntity.setEntityTypeId(rootEntityTypeId);
        procInstBindEntity.setEntityDataId(rootEntityDataId);
        procInstBindEntity.setEntityDataName(rootEntityDataName);
        procInstBindEntity.setProcDefId(procDefId);
        procInstBindEntity.setProcInstId(procInstInfoEntity.getId());
        procExecBindingRepository.saveAndFlush(procInstBindEntity);

        List<TaskNodeDefInfoEntity> taskNodeDefInfoEntities = taskNodeDefInfoRepository.findAllByProcDefId(procDefId);

        for (TaskNodeDefInfoEntity taskNodeDefInfoEntity : taskNodeDefInfoEntities) {
            processSingleTaskNodeDefInfoEntityWhenCreate(taskNodeDefInfoEntity, procInstInfoEntity, requestDto,
                    procDefId);
        }

        ProcInstInfoDto result = doCreateProcessInstance(procInstInfoEntity, procDefInfoEntity.getProcDefKernelId(),
                procInstKey);

        postHandleGraphNodes(requestDto, result);
        return result;
    }

    private void processSingleTaskNodeDefInfoEntityWhenCreate(TaskNodeDefInfoEntity taskNodeDefInfoEntity,
            ProcInstInfoEntity procInstInfoEntity, StartProcInstRequestDto requestDto, String procDefId) {
        TaskNodeInstInfoEntity taskNodeInstInfoEntity = createTaskNodeInstInfoEntity(taskNodeDefInfoEntity,
                procInstInfoEntity);

        List<TaskNodeDefObjectBindInfoDto> bindInfoDtos = pickUpTaskNodeDefObjectBindInfoDtos(requestDto,
                taskNodeDefInfoEntity.getId());

        List<TaskNodeDefObjectBindInfoDto> savedBindInfoDtos = new ArrayList<>();

        for (TaskNodeDefObjectBindInfoDto bindInfoDto : bindInfoDtos) {
            if (containsBindInfos(savedBindInfoDtos, bindInfoDto)) {
                continue;
            }
            ProcExecBindingEntity nodeBindEntity = new ProcExecBindingEntity();
            nodeBindEntity.setBindType(ProcExecBindingEntity.BIND_TYPE_TASK_NODE_INSTANCE);
            nodeBindEntity.setProcInstId(procInstInfoEntity.getId());
            nodeBindEntity.setProcDefId(procDefId);
            nodeBindEntity.setNodeDefId(bindInfoDto.getNodeDefId());
            nodeBindEntity.setTaskNodeInstId(taskNodeInstInfoEntity.getId());
            nodeBindEntity.setEntityTypeId(bindInfoDto.getEntityTypeId());
            nodeBindEntity.setEntityDataId(bindInfoDto.getEntityDataId());
            nodeBindEntity.setEntityDataName(bindInfoDto.getEntityDisplayName());

            procExecBindingRepository.saveAndFlush(nodeBindEntity);

            savedBindInfoDtos.add(bindInfoDto);
        }
    }

    private boolean containsBindInfos(List<TaskNodeDefObjectBindInfoDto> savedBindInfoDtos,
            TaskNodeDefObjectBindInfoDto bindInfo) {
        for (TaskNodeDefObjectBindInfoDto tb : savedBindInfoDtos) {
            if (tb.getNodeDefId().equals(bindInfo.getNodeDefId())
                    && tb.getEntityTypeId().equals(bindInfo.getEntityTypeId())
                    && tb.getEntityDataId().equals(bindInfo.getEntityDataId())) {
                return true;
            }
        }

        return false;
    }

    private TaskNodeInstInfoEntity createTaskNodeInstInfoEntity(TaskNodeDefInfoEntity taskNodeDefInfoEntity,
            ProcInstInfoEntity procInstInfoEntity) {
        TaskNodeInstInfoEntity taskNodeInstInfoEntity = new TaskNodeInstInfoEntity();
        taskNodeInstInfoEntity.setStatus(TaskNodeInstInfoEntity.NOT_STARTED_STATUS);
        taskNodeInstInfoEntity.setNodeDefId(taskNodeDefInfoEntity.getId());
        taskNodeInstInfoEntity.setNodeId(taskNodeDefInfoEntity.getNodeId());
        taskNodeInstInfoEntity.setNodeName(taskNodeDefInfoEntity.getNodeName());
        taskNodeInstInfoEntity.setOperator(AuthenticationContextHolder.getCurrentUsername());
        taskNodeInstInfoEntity.setProcDefId(taskNodeDefInfoEntity.getProcDefId());
        taskNodeInstInfoEntity.setProcDefKey(taskNodeDefInfoEntity.getProcDefKey());
        taskNodeInstInfoEntity.setProcInstId(procInstInfoEntity.getId());
        taskNodeInstInfoEntity.setProcInstKey(procInstInfoEntity.getProcInstKey());
        taskNodeInstInfoEntity.setNodeType(taskNodeDefInfoEntity.getNodeType());
        taskNodeInstInfoEntity.setOrderedNo(taskNodeDefInfoEntity.getOrderedNo());

        taskNodeInstInfoRepository.saveAndFlush(taskNodeInstInfoEntity);

        return taskNodeInstInfoEntity;
    }

    private String tryCalEntityDataName(StartProcInstRequestDto requestDto) {
        String entityDataName = null;
        if (!StringUtils.isBlank(requestDto.getProcessSessionId())) {
            List<ProcExecBindingTmpEntity> tmpBindings = procExecBindingTmpRepository
                    .findAllRootBindingsBySession(requestDto.getProcessSessionId());
            if (tmpBindings != null && tmpBindings.size() > 0) {
                entityDataName = tmpBindings.get(0).getEntityDataName();
            }
        }
        return entityDataName;
    }

    private void postHandleGraphNodes(StartProcInstRequestDto requestDto, ProcInstInfoDto result) {
        if (StringUtils.isBlank(requestDto.getProcessSessionId())) {
            return;
        }

        List<GraphNodeEntity> gNodes = graphNodeRepository.findAllByProcessSessionId(requestDto.getProcessSessionId());
        if (gNodes == null || gNodes.isEmpty()) {
            return;
        }

        for (GraphNodeEntity gNode : gNodes) {
            gNode.setUpdatedTime(new Date());
            gNode.setProcInstId(result.getId());

            graphNodeRepository.saveAndFlush(gNode);
        }
    }

    protected ProcInstInfoDto doCreateProcessInstance(ProcInstInfoEntity procInstInfoEntity, String processDefinitionId,
            String procInstKey) {
        ProcessInstance processInstance = workflowEngineService.startProcessInstance(processDefinitionId, procInstKey);

        Optional<ProcInstInfoEntity> existProcInstInfoEntityOpt = procInstInfoRepository
                .findById(procInstInfoEntity.getId());

        if (!existProcInstInfoEntityOpt.isPresent()) {
            log.warn("such record does not exist,id={},procInstKey={}", procInstInfoEntity.getId(), procInstKey);
            throw new WecubeCoreException("3152", "Errors while starting process instance.");
        }

        ProcInstInfoEntity procEntity = existProcInstInfoEntityOpt.get();

        Date now = new Date();

        procEntity.setUpdatedTime(now);
        procEntity.setProcInstKernelId(processInstance.getId());
        procEntity.setStatus(ProcInstInfoEntity.IN_PROGRESS_STATUS);

        procInstInfoRepository.saveAndFlush(procEntity);

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
                n.setUpdatedBy(AuthenticationContextHolder.getCurrentUsername());
                n.setUpdatedTime(now);
                n.setStatus(TaskNodeInstInfoEntity.COMPLETED_STATUS);
                taskNodeInstInfoRepository.saveAndFlush(n);
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
        if (StringUtils.isBlank(requestDto.getProcessSessionId())) {
            return pickUpTaskNodeDefObjectBindInfoDtosFromInput(requestDto, nodeDefId);
        } else {
            return pickUpTaskNodeDefObjectBindInfoDtosFromSession(requestDto, nodeDefId);
        }

    }

    private List<TaskNodeDefObjectBindInfoDto> pickUpTaskNodeDefObjectBindInfoDtosFromSession(
            StartProcInstRequestDto requestDto, String nodeDefId) {

        List<ProcExecBindingTmpEntity> sessionBindings = this.procExecBindingTmpRepository
                .findAllNodeBindingsByNodeAndSession(nodeDefId, requestDto.getProcessSessionId());

        List<TaskNodeDefObjectBindInfoDto> result = new ArrayList<>();
        if (sessionBindings == null || sessionBindings.isEmpty()) {
            return result;
        }

        for (ProcExecBindingTmpEntity entity : sessionBindings) {
            if (ProcExecBindingTmpEntity.BOUND.equalsIgnoreCase(entity.getBound())) {
                TaskNodeDefObjectBindInfoDto dto = new TaskNodeDefObjectBindInfoDto();
                dto.setBound(entity.getBound());
                dto.setEntityDataId(entity.getEntityDataId());
                dto.setEntityTypeId(entity.getEntityTypeId());
                dto.setNodeDefId(entity.getNodeDefId());
                dto.setOrderedNo(entity.getOrderedNo());

                result.add(dto);
            }
        }

        return result;
    }

    private List<TaskNodeDefObjectBindInfoDto> pickUpTaskNodeDefObjectBindInfoDtosFromInput(
            StartProcInstRequestDto requestDto, String nodeDefId) {
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

    private List<?> queryProcInstInfoByRoleNames(List<String> roleNames) {
        String sql = "select distinct t1.id,t1.created_time,t1.oper,t1.status,t1.proc_inst_key,t1.proc_def_name,t1.proc_def_id,t2.entity_data_id,t2.entity_type_id,t2.entity_data_name "
                + " from core_ru_proc_inst_info t1  "
                + " left join core_ru_proc_exec_binding t2 on t1.id = t2.proc_inst_id and t2.bind_type = 'process' "
                + " join core_ru_proc_role_binding t3 on t1.proc_def_id = t3.proc_id "
                + " and t3.role_name in (:roleNames) and t3.permission = 'USE' "
                + " order by t1.created_time desc limit 500";
        Query query = entityManager.createNativeQuery(sql, ProcInstInfoQueryEntity.class).setParameter("roleNames",
                roleNames);
        return query.getResultList();
    }
}
