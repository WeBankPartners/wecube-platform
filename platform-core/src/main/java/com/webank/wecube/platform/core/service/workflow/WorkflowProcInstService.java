package com.webank.wecube.platform.core.service.workflow;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.commons.AuthenticationContextHolder;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.workflow.ProcInstInfoDto;
import com.webank.wecube.platform.core.dto.workflow.ProcInstOutlineDto;
import com.webank.wecube.platform.core.dto.workflow.ProceedProcInstRequestDto;
import com.webank.wecube.platform.core.dto.workflow.StartProcInstRequestDto;
import com.webank.wecube.platform.core.dto.workflow.TaskNodeDefObjectBindInfoDto;
import com.webank.wecube.platform.core.dto.workflow.TaskNodeInstDto;
import com.webank.wecube.platform.core.entity.workflow.GraphNodeEntity;
import com.webank.wecube.platform.core.entity.workflow.ProcDefInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.ProcExecBindingEntity;
import com.webank.wecube.platform.core.entity.workflow.ProcExecBindingTmpEntity;
import com.webank.wecube.platform.core.entity.workflow.ProcInstInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.ProcInstInfoQueryEntity;
import com.webank.wecube.platform.core.entity.workflow.ProcRoleBindingEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeDefInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeInstInfoEntity;
import com.webank.wecube.platform.core.repository.workflow.GraphNodeMapper;
import com.webank.wecube.platform.core.repository.workflow.ProcDefInfoMapper;
import com.webank.wecube.platform.core.repository.workflow.ProcExecBindingMapper;
import com.webank.wecube.platform.core.repository.workflow.ProcExecBindingTmpMapper;
import com.webank.wecube.platform.core.repository.workflow.ProcInstInfoMapper;
import com.webank.wecube.platform.core.repository.workflow.ProcRoleBindingMapper;
import com.webank.wecube.platform.core.repository.workflow.TaskNodeDefInfoMapper;
import com.webank.wecube.platform.core.repository.workflow.TaskNodeExecParamMapper;
import com.webank.wecube.platform.core.repository.workflow.TaskNodeExecRequestMapper;
import com.webank.wecube.platform.core.repository.workflow.TaskNodeInstInfoMapper;
import com.webank.wecube.platform.workflow.commons.LocalIdGenerator;
import com.webank.wecube.platform.workflow.model.ProcFlowNodeInst;
import com.webank.wecube.platform.workflow.model.ProcInstOutline;

@Service
public class WorkflowProcInstService extends AbstractWorkflowService {
    private static final Logger log = LoggerFactory.getLogger(WorkflowProcInstService.class);

    @Autowired
    private ProcDefInfoMapper processDefInfoRepository;

    @Autowired
    private ProcInstInfoMapper procInstInfoRepository;

    @Autowired
    private TaskNodeDefInfoMapper taskNodeDefInfoRepository;

    @Autowired
    private TaskNodeInstInfoMapper taskNodeInstInfoRepository;

    @Autowired
    private ProcExecBindingMapper procExecBindingRepository;

    @Autowired
    private WorkflowEngineService workflowEngineService;

    @Autowired
    protected TaskNodeExecParamMapper taskNodeExecParamRepository;

    @Autowired
    protected TaskNodeExecRequestMapper taskNodeExecRequestRepository;

    @Autowired
    private ProcRoleBindingMapper procRoleBindingRepository;

    @Autowired
    private ProcExecBindingTmpMapper procExecBindingTmpRepository;

    @Autowired
    protected GraphNodeMapper graphNodeRepository;

    /**
     * 
     * @param procInstId
     */
    public void createProcessInstanceTermination(int procInstId) {
        ProcInstInfoEntity procInstEntity = procInstInfoRepository.selectByPrimaryKey(procInstId);
        if (procInstEntity == null) {
            throw new WecubeCoreException("3135", String.format("Such entity with id [%s] does not exist.", procInstId),
                    procInstId);
        }

        if (log.isInfoEnabled()) {
            log.info("About to terminate process instance, procInstId={},procInstKernelId={}", procInstId,
                    procInstEntity.getProcInstKernelId());
        }
        
        procInstEntity.setStatus(ProcInstInfoEntity.INTERNALLY_TERMINATED_STATUS);
        procInstEntity.setUpdatedBy(AuthenticationContextHolder.getCurrentUsername());
        procInstEntity.setUpdatedTime(new Date());
        procInstInfoRepository.updateByPrimaryKeySelective(procInstEntity);
        workflowEngineService.deleteProcessInstance(procInstEntity.getProcInstKernelId());
    }

    /**
     * 
     * @param procInstId
     * @return
     */
    public List<TaskNodeDefObjectBindInfoDto> getProcessInstanceExecBindings(Integer procInstId) {
        ProcInstInfoEntity procInstEntity = procInstInfoRepository.selectByPrimaryKey(procInstId);
        if (procInstEntity == null) {
            throw new WecubeCoreException("3135", String.format("Such entity with id [%s] does not exist.", procInstId),
                    procInstId);
        }

        List<ProcExecBindingEntity> bindEntities = procExecBindingRepository
                .selectAllTaskNodeBindingsByProcInstId(procInstEntity.getId());

        List<TaskNodeDefObjectBindInfoDto> result = new ArrayList<>();

        if (bindEntities == null || bindEntities.isEmpty()) {
            return result;
        }

        List<TaskNodeInstInfoEntity> nodeInstEntities = taskNodeInstInfoRepository
                .selectAllByProcInstId(procInstEntity.getId());

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

    /**
     * 
     * @param request
     */
    public void proceedProcessInstance(ProceedProcInstRequestDto request) {
        if (request == null) {
            log.warn("request is null");
            throw new WecubeCoreException("3136", "Request is null.");
        }

        ProcInstInfoEntity procInst = procInstInfoRepository.selectByPrimaryKey(request.getProcInstId());

        if (procInst == null) {
            log.warn("such process instance does not exist,id={}", request.getProcInstId());
            throw new WecubeCoreException("3137", "Such process instance does not exist.");
        }

        refreshProcessInstanceStatus(procInst);

        TaskNodeInstInfoEntity nodeInst = taskNodeInstInfoRepository.selectByPrimaryKey(request.getNodeInstId());
        if (nodeInst == null) {
            log.warn("such task node instance does not exist,process id :{}, task node id:{}", request.getProcInstId(),
                    request.getNodeInstId());
            throw new WecubeCoreException("3138", "Such task node instance does not exist.");
        }

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
                nodeInst.setUpdatedBy(AuthenticationContextHolder.getCurrentUsername());
                nodeInst.setStatus(nodeStatus);
                taskNodeInstInfoRepository.updateByPrimaryKeySelective(nodeInst);
            }
        }
    }

    protected void refreshProcessInstanceStatus(ProcInstInfoEntity procInstEntity) {
        List<TaskNodeInstInfoEntity> nodeInstEntities = taskNodeInstInfoRepository
                .selectAllByProcInstId(procInstEntity.getId());
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
                nie.setUpdatedBy(AuthenticationContextHolder.getCurrentUsername());
                taskNodeInstInfoRepository.updateByPrimaryKeySelective(nie);
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

        List<ProcInstInfoQueryEntity> procInstInfoQueryEntities = queryProcInstInfoByRoleNames(roleNames);

        if (procInstInfoQueryEntities == null || procInstInfoQueryEntities.isEmpty()) {
            return results;
        }

        for (ProcInstInfoQueryEntity e : procInstInfoQueryEntities) {
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
        ProcInstInfoEntity procInstEntity = procInstInfoRepository.selectByPrimaryKey(id);
        if (procInstEntity == null) {
            throw new WecubeCoreException("3142", String.format("Such entity with id [%s] does not exist.", id), id);
        }

        this.checkCurrentUserRole(procInstEntity.getProcDefId());

        ProcInstInfoDto result = new ProcInstInfoDto();

        String procInstanceKernelId = procInstEntity.getProcInstKernelId();

        if (StringUtils.isBlank(procInstanceKernelId)) {
            throw new WecubeCoreException("3143", "Unknow kernel process instance.");
        }

        ProcInstOutline procInstOutline = workflowEngineService.getProcInstOutline(procInstanceKernelId);
        if (procInstEntity.getStatus().equals(procInstOutline.getStatus())) {
            procInstEntity.setStatus(procInstOutline.getStatus());
            procInstEntity.setUpdatedTime(new Date());
            procInstEntity.setUpdatedBy(AuthenticationContextHolder.getCurrentUsername());
            procInstInfoRepository.updateByPrimaryKeySelective(procInstEntity);
        }

        List<TaskNodeInstInfoEntity> nodeInstEntities = taskNodeInstInfoRepository
                .selectAllByProcInstId(procInstEntity.getId());
        for (TaskNodeInstInfoEntity nodeInstEntity : nodeInstEntities) {
            ProcFlowNodeInst pfni = procInstOutline.findProcFlowNodeInstByNodeId(nodeInstEntity.getNodeId());
            if (pfni != null && (pfni.getStatus() != null) && (!pfni.getStatus().equals(nodeInstEntity.getStatus()))) {
                nodeInstEntity.setStatus(pfni.getStatus());
                nodeInstEntity.setUpdatedTime(new Date());
                nodeInstEntity.setUpdatedBy(AuthenticationContextHolder.getCurrentUsername());
                taskNodeInstInfoRepository.updateByPrimaryKeySelective(nodeInstEntity);
            }
        }

        ProcExecBindingEntity procInstBindEntity = procExecBindingRepository
                .selectProcInstBindings(procInstEntity.getId());

        String entityTypeId = null;
        String entityDataId = null;

        if (procInstBindEntity != null) {
            entityTypeId = procInstBindEntity.getEntityTypeId();
            entityDataId = procInstBindEntity.getEntityDataId();
        }

        result.setId(procInstEntity.getId());
        result.setOperator(procInstEntity.getOper());
        result.setProcDefId(procInstEntity.getProcDefId());
        result.setProcInstKey(procInstEntity.getProcInstKey());
        result.setProcInstName(procInstEntity.getProcDefName());
        result.setEntityTypeId(entityTypeId);
        result.setEntityDataId(entityDataId);
        result.setStatus(procInstEntity.getStatus());
        result.setCreatedTime(formatDate(procInstEntity.getCreatedTime()));

        List<TaskNodeInstInfoEntity> nodeEntities = taskNodeInstInfoRepository
                .selectAllByProcInstId(procInstEntity.getId());

        List<TaskNodeDefInfoEntity> nodeDefEntities = taskNodeDefInfoRepository
                .selectAllByProcDefId(procInstEntity.getProcDefId());

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
                nd.setPreviousNodeIds(unmarshalNodeIds(nodeDef.getPrevNodeIds()));
                nd.setSucceedingNodeIds(unmarshalNodeIds(nodeDef.getSucceedNodeIds()));
                nd.setOrderedNo(nodeDef.getOrderedNo());
                nd.setRoutineExpression(nodeDef.getRoutineExp());
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
//        List<String> roleIdList = this.userManagementService
//                .getRoleIdsByUsername(AuthenticationContextHolder.getCurrentUsername());
        
        Set<String> currRoleNames = AuthenticationContextHolder.getCurrentUserRoles();
        if (currRoleNames == null || currRoleNames.isEmpty()) {
            throw new WecubeCoreException("3144", "No access to this resource due to current user did not log in.");
        }

        List<ProcRoleBindingEntity> procRoleBindingEntities = procRoleBindingRepository.selectDistinctProcIdByRolesAndPermissionIsUse(currRoleNames);
        if (procRoleBindingEntities == null || procRoleBindingEntities.isEmpty()) {
            throw new WecubeCoreException("3145", "No access to this resource due to permission not configured.");
        }
        
        for(ProcRoleBindingEntity e : procRoleBindingEntities){
            if(procDefId.equals(e.getProcId())){
                return;
            }
        }

        throw new WecubeCoreException("3146", "No access to this resource due to none permission configuration found.");

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
        procInstInfoEntity.setOper(AuthenticationContextHolder.getCurrentUsername());
        procInstInfoEntity.setProcDefId(procDefId);
        procInstInfoEntity.setProcDefKey(procDefInfoEntity.getProcDefKey());
        procInstInfoEntity.setProcDefName(procDefInfoEntity.getProcDefName());
        procInstInfoEntity.setProcInstKey(procInstKey);
        procInstInfoEntity.setCreatedBy(AuthenticationContextHolder.getCurrentUsername());
        procInstInfoEntity.setCreatedTime(new Date());

        procInstInfoRepository.insert(procInstInfoEntity);

        ProcExecBindingEntity procInstBindEntity = new ProcExecBindingEntity();
        procInstBindEntity.setBindType(ProcExecBindingEntity.BIND_TYPE_PROC_INSTANCE);
        procInstBindEntity.setEntityTypeId(rootEntityTypeId);
        procInstBindEntity.setEntityDataId(rootEntityDataId);
        procInstBindEntity.setEntityDataName(rootEntityDataName);
        procInstBindEntity.setProcDefId(procDefId);
        procInstBindEntity.setProcInstId(procInstInfoEntity.getId());
        procInstBindEntity.setCreatedBy(AuthenticationContextHolder.getCurrentUsername());
        procInstBindEntity.setCreatedTime(new Date());
        procExecBindingRepository.insert(procInstBindEntity);

        List<TaskNodeDefInfoEntity> taskNodeDefInfoEntities = taskNodeDefInfoRepository.selectAllByProcDefId(procDefId);

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
            nodeBindEntity.setCreatedBy(AuthenticationContextHolder.getCurrentUsername());
            nodeBindEntity.setCreatedTime(new Date());

            procExecBindingRepository.insert(nodeBindEntity);

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
        taskNodeInstInfoEntity.setOper(AuthenticationContextHolder.getCurrentUsername());
        taskNodeInstInfoEntity.setProcDefId(taskNodeDefInfoEntity.getProcDefId());
        taskNodeInstInfoEntity.setProcDefKey(taskNodeDefInfoEntity.getProcDefKey());
        taskNodeInstInfoEntity.setProcInstId(procInstInfoEntity.getId());
        taskNodeInstInfoEntity.setProcInstKey(procInstInfoEntity.getProcInstKey());
        taskNodeInstInfoEntity.setNodeType(taskNodeDefInfoEntity.getNodeType());
        taskNodeInstInfoEntity.setOrderedNo(taskNodeDefInfoEntity.getOrderedNo());
        taskNodeInstInfoEntity.setCreatedBy(AuthenticationContextHolder.getCurrentUsername());
        taskNodeInstInfoEntity.setCreatedTime(new Date());

        taskNodeInstInfoRepository.insert(taskNodeInstInfoEntity);

        return taskNodeInstInfoEntity;
    }

    private String tryCalEntityDataName(StartProcInstRequestDto requestDto) {
        String entityDataName = null;
        if (!StringUtils.isBlank(requestDto.getProcessSessionId())) {
            List<ProcExecBindingTmpEntity> tmpBindings = procExecBindingTmpRepository
                    .selectAllRootBindingsBySession(requestDto.getProcessSessionId());
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

        List<GraphNodeEntity> gNodes = graphNodeRepository.selectAllByProcessSessionId(requestDto.getProcessSessionId());
        if (gNodes == null || gNodes.isEmpty()) {
            return;
        }

        for (GraphNodeEntity gNode : gNodes) {
            gNode.setUpdatedTime(new Date());
            gNode.setUpdatedBy(AuthenticationContextHolder.getCurrentUsername());
            gNode.setProcInstId(result.getId());

            graphNodeRepository.updateByPrimaryKeySelective(gNode);
        }
    }

    protected ProcInstInfoDto doCreateProcessInstance(ProcInstInfoEntity procInstInfoEntity, String processDefinitionId,
            String procInstKey) {
        ProcessInstance processInstance = workflowEngineService.startProcessInstance(processDefinitionId, procInstKey);

        ProcInstInfoEntity procEntity = procInstInfoRepository.selectByPrimaryKey(procInstInfoEntity.getId());

        if (procEntity == null) {
            log.warn("such record does not exist,id={},procInstKey={}", procInstInfoEntity.getId(), procInstKey);
            throw new WecubeCoreException("3152", "Errors while starting process instance.");
        }

        Date now = new Date();

        procEntity.setUpdatedTime(now);
        procEntity.setUpdatedBy(AuthenticationContextHolder.getCurrentUsername());
        procEntity.setProcInstKernelId(processInstance.getId());
        procEntity.setStatus(ProcInstInfoEntity.IN_PROGRESS_STATUS);

        procInstInfoRepository.updateByPrimaryKeySelective(procEntity);

        String entityTypeId = null;
        String entityDataId = null;

        ProcExecBindingEntity procInstBindEntity = procExecBindingRepository
                .selectProcInstBindings(procInstInfoEntity.getId());

        if (procInstBindEntity != null) {
            entityTypeId = procInstBindEntity.getEntityTypeId();
            entityDataId = procInstBindEntity.getEntityDataId();
        }

        ProcInstInfoDto result = new ProcInstInfoDto();
        result.setId(procEntity.getId());
        result.setOperator(procEntity.getOper());
        result.setProcDefId(procEntity.getProcDefId());
        result.setProcInstKey(procEntity.getProcDefKey());
        result.setStatus(procEntity.getStatus());
        result.setEntityTypeId(entityTypeId);
        result.setEntityDataId(entityDataId);

        List<TaskNodeInstInfoEntity> nodeInstEntities = taskNodeInstInfoRepository
                .selectAllByProcInstId(procEntity.getId());

        for (TaskNodeInstInfoEntity n : nodeInstEntities) {
            if ("startEvent".equals(n.getNodeType())) {
                n.setUpdatedBy(AuthenticationContextHolder.getCurrentUsername());
                n.setUpdatedTime(now);
                n.setStatus(TaskNodeInstInfoEntity.COMPLETED_STATUS);
                taskNodeInstInfoRepository.updateByPrimaryKeySelective(n);
            }
        }

        List<TaskNodeDefInfoEntity> nodeDefEntities = taskNodeDefInfoRepository
                .selectAllByProcDefId(procEntity.getProcDefId());

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

            nd.setPreviousNodeIds(unmarshalNodeIds(nodeDefEntity.getPrevNodeIds()));
            nd.setSucceedingNodeIds(unmarshalNodeIds(nodeDefEntity.getSucceedNodeIds()));
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
                .selectAllNodeBindingsByNodeAndSession(nodeDefId, requestDto.getProcessSessionId());

        List<TaskNodeDefObjectBindInfoDto> result = new ArrayList<>();
        if (sessionBindings == null || sessionBindings.isEmpty()) {
            return result;
        }

        for (ProcExecBindingTmpEntity entity : sessionBindings) {
            if (ProcExecBindingTmpEntity.BOUND.equalsIgnoreCase(entity.getIsBound())) {
                TaskNodeDefObjectBindInfoDto dto = new TaskNodeDefObjectBindInfoDto();
                dto.setBound(entity.getIsBound());
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

    private List<ProcInstInfoQueryEntity> queryProcInstInfoByRoleNames(List<String> roleNames) {
        List<ProcInstInfoQueryEntity> insts = procInstInfoRepository.selectAllByProcInstInfoByRoleNames(roleNames);
        return insts;
    }
}
