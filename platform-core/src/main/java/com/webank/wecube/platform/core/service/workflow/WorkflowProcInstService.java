package com.webank.wecube.platform.core.service.workflow;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
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
    private ProcDefInfoMapper processDefInfoMapper;

    @Autowired
    private ProcInstInfoMapper procInstInfoMapper;

    @Autowired
    private TaskNodeDefInfoMapper taskNodeDefInfoMapper;

    @Autowired
    private TaskNodeInstInfoMapper taskNodeInstInfoMapper;

    @Autowired
    private ProcExecBindingMapper procExecBindingMapper;

    @Autowired
    private WorkflowEngineService workflowEngineService;

    @Autowired
    protected TaskNodeExecParamMapper taskNodeExecParamMapper;

    @Autowired
    protected TaskNodeExecRequestMapper taskNodeExecRequestMapper;

    @Autowired
    private ProcRoleBindingMapper procRoleBindingMapper;

    @Autowired
    private ProcExecBindingTmpMapper procExecBindingTmpMapper;

    @Autowired
    protected GraphNodeMapper graphNodeMapper;

    /**
     * 
     * @param procInstId
     */
    public void createProcessInstanceTermination(int procInstId) {
        ProcInstInfoEntity procInstEntity = procInstInfoMapper.selectByPrimaryKey(procInstId);
        if (procInstEntity == null) {
            throw new WecubeCoreException("3135", String.format("Such entity with id [%s] does not exist.", procInstId),
                    procInstId);
        }

        if (log.isInfoEnabled()) {
            log.info("About to terminate process instance, procInstId={},procInstKernelId={}", procInstId,
                    procInstEntity.getProcInstKernelId());
        }

        if (isProcessInstanceFinalStatus(procInstEntity)) {
            String errMsg = String.format(
                    "Current process has already been completed with status:%s,and can not proceed termination.",
                    procInstEntity.getStatus());
            throw new WecubeCoreException(errMsg);
        }

        checkCurrentUserRole(procInstEntity.getProcDefId());

        procInstEntity.setStatus(ProcInstInfoEntity.INTERNALLY_TERMINATED_STATUS);
        procInstEntity.setUpdatedBy(AuthenticationContextHolder.getCurrentUsername());
        procInstEntity.setUpdatedTime(new Date());
        procInstInfoMapper.updateByPrimaryKeySelective(procInstEntity);
        workflowEngineService.deleteProcessInstance(procInstEntity.getProcInstKernelId());
    }

    /**
     * 
     * @param procInstId
     * @return
     */
    public List<TaskNodeDefObjectBindInfoDto> getProcessInstanceExecBindings(Integer procInstId) {
        ProcInstInfoEntity procInstEntity = procInstInfoMapper.selectByPrimaryKey(procInstId);
        if (procInstEntity == null) {
            throw new WecubeCoreException("3135", String.format("Such entity with id [%s] does not exist.", procInstId),
                    procInstId);
        }

        List<ProcExecBindingEntity> bindEntities = procExecBindingMapper
                .selectAllTaskNodeBindingsByProcInstId(procInstEntity.getId());

        List<TaskNodeDefObjectBindInfoDto> result = new ArrayList<>();

        if (bindEntities == null || bindEntities.isEmpty()) {
            return result;
        }

        List<TaskNodeInstInfoEntity> nodeInstEntities = taskNodeInstInfoMapper
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

        ProcInstInfoEntity procInst = procInstInfoMapper.selectByPrimaryKey(request.getProcInstId());

        if (procInst == null) {
            log.warn("such process instance does not exist,id={}", request.getProcInstId());
            throw new WecubeCoreException("3137", "Such process instance does not exist.");
        }

        refreshProcessInstanceStatus(procInst);

        TaskNodeInstInfoEntity nodeInst = taskNodeInstInfoMapper.selectByPrimaryKey(request.getNodeInstId());
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
                taskNodeInstInfoMapper.updateByPrimaryKeySelective(nodeInst);
            }
        }
    }

    /**
     * 
     * @return
     */
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

    /**
     * 
     * @param id
     * @return
     */
    public ProcInstInfoDto getProcessInstanceById(Integer id) {
        ProcInstInfoEntity procInstEntity = procInstInfoMapper.selectByPrimaryKey(id);
        if (procInstEntity == null) {
            throw new WecubeCoreException("3142", String.format("Such entity with id [%s] does not exist.", id), id);
        }

        this.checkCurrentUserRole(procInstEntity.getProcDefId());

        ProcInstInfoDto procInstInfoResultDto = new ProcInstInfoDto();

        String procInstanceKernelId = procInstEntity.getProcInstKernelId();

        if (StringUtils.isBlank(procInstanceKernelId)) {
            throw new WecubeCoreException("3143", "Unknow kernel process instance.");
        }

        ProcInstOutline procInstOutline = workflowEngineService.getProcInstOutline(procInstanceKernelId);
        if (!procInstEntity.getStatus().equals(procInstOutline.getStatus()) && (procInstOutline.getStatus() != null)
                && !ProcInstInfoEntity.INTERNALLY_TERMINATED_STATUS.equalsIgnoreCase(procInstEntity.getStatus())) {
            procInstEntity.setStatus(procInstOutline.getStatus());
            procInstEntity.setUpdatedTime(new Date());
            procInstEntity.setUpdatedBy(AuthenticationContextHolder.getCurrentUsername());
            procInstInfoMapper.updateByPrimaryKeySelective(procInstEntity);
        }

        if (!ProcInstInfoEntity.INTERNALLY_TERMINATED_STATUS.equalsIgnoreCase(procInstEntity.getStatus())) {

            List<TaskNodeInstInfoEntity> nodeInstEntities = taskNodeInstInfoMapper
                    .selectAllByProcInstId(procInstEntity.getId());
            for (TaskNodeInstInfoEntity nodeInstEntity : nodeInstEntities) {
                ProcFlowNodeInst pfni = procInstOutline.findProcFlowNodeInstByNodeId(nodeInstEntity.getNodeId());
                if (pfni != null && (pfni.getStatus() != null)
                        && (!pfni.getStatus().equals(nodeInstEntity.getStatus()))) {
                    if (!TaskNodeInstInfoEntity.RISKY_STATUS.equalsIgnoreCase(nodeInstEntity.getStatus())) {
                        nodeInstEntity.setStatus(pfni.getStatus());
                        nodeInstEntity.setUpdatedTime(new Date());
                        nodeInstEntity.setUpdatedBy(AuthenticationContextHolder.getCurrentUsername());
                        taskNodeInstInfoMapper.updateByPrimaryKeySelective(nodeInstEntity);
                    }
                }
            }

        }

        ProcExecBindingEntity procInstBindEntity = procExecBindingMapper.selectProcInstBindings(procInstEntity.getId());

        String entityTypeId = null;
        String entityDataId = null;

        if (procInstBindEntity != null) {
            entityTypeId = procInstBindEntity.getEntityTypeId();
            entityDataId = procInstBindEntity.getEntityDataId();
        }

        procInstInfoResultDto.setId(procInstEntity.getId());
        procInstInfoResultDto.setOperator(procInstEntity.getOper());
        procInstInfoResultDto.setProcDefId(procInstEntity.getProcDefId());
        procInstInfoResultDto.setProcInstKey(procInstEntity.getProcInstKey());
        procInstInfoResultDto.setProcInstName(procInstEntity.getProcDefName());
        procInstInfoResultDto.setEntityTypeId(entityTypeId);
        procInstInfoResultDto.setEntityDataId(entityDataId);
        procInstInfoResultDto.setStatus(procInstEntity.getStatus());
        procInstInfoResultDto.setCreatedTime(formatDate(procInstEntity.getCreatedTime()));

        List<TaskNodeInstInfoEntity> nodeEntities = taskNodeInstInfoMapper
                .selectAllByProcInstId(procInstEntity.getId());

        List<TaskNodeDefInfoEntity> nodeDefEntities = taskNodeDefInfoMapper
                .selectAllByProcDefId(procInstEntity.getProcDefId());

        for (TaskNodeInstInfoEntity n : nodeEntities) {
            TaskNodeDefInfoEntity nodeDef = findTaskNodeDefInfoEntityByNodeDefId(nodeDefEntities, n.getNodeDefId());
            TaskNodeInstDto nd = new TaskNodeInstDto();
            nd.setId(n.getId());
            nd.setNodeDefId(n.getNodeDefId());
            nd.setNodeId(n.getNodeId());
            nd.setNodeName(deduceTaskNodeName(n));
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

            procInstInfoResultDto.addTaskNodeInstances(nd);
        }

        return procInstInfoResultDto;
    }

    /**
     * 
     * @param requestDto
     * @return
     */
    public ProcInstInfoDto createProcessInstanceWithPermissionValidation(StartProcInstRequestDto requestDto) {
        if (StringUtils.isBlank(requestDto.getProcDefId())) {
            throw new WecubeCoreException("3147", "Process definition ID is blank.");
        }
        this.checkCurrentUserRole(requestDto.getProcDefId());
        ProcInstInfoDto procInstInfoResultDto = this.createProcessInstance(requestDto);
        return procInstInfoResultDto;
    }

    /**
     * 
     * @param requestDto
     * @return
     */
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
        ProcDefInfoEntity procDefInfoEntity = processDefInfoMapper.selectByPrimaryKey(procDefId);

        if (procDefInfoEntity == null) {
            throw new WecubeCoreException("3149", String.format("Invalid process definition ID:%s", procDefId),
                    procDefId);
        }

        if (!ProcDefInfoEntity.DEPLOYED_STATUS.equals(procDefInfoEntity.getStatus())) {
            log.warn("expected status {} but {} for procDefId {}", ProcDefInfoEntity.DEPLOYED_STATUS,
                    procDefInfoEntity.getStatus(), procDefId);
            throw new WecubeCoreException("3150", String.format("Invalid process definition ID:%s", procDefId),
                    procDefId);
        }

        if (StringUtils.isBlank(procDefInfoEntity.getProcDefKernelId())) {
            log.warn("cannot know process definition id for {}", procDefId);
            throw new WecubeCoreException("3151", String.format("Invalid process definition ID:%s", procDefId),
                    procDefId);
        }

        String procInstKey = LocalIdGenerator.generateId();

        ProcInstInfoEntity procInstInfoEntity = tryBuildProcInstInfoEntity(procDefInfoEntity, procInstKey);

        tryBuildProcInstProcExecBinding(rootEntityTypeId, rootEntityDataId, rootEntityDataName, procDefInfoEntity,
                procInstInfoEntity);

        // procInstInfoEntity.setProcInstBindEntity(procInstBindEntity);

        tryBuildTaskNodeInstances(procDefInfoEntity, procInstInfoEntity, requestDto);

        //
        tryVerifyExcludeModeBindings(procDefInfoEntity, procInstInfoEntity);

        tryStoreEnrichedProcInstInfoEntity(procInstInfoEntity);

        ProcInstInfoDto result = doCreateProcessInstance(procInstInfoEntity, procDefInfoEntity.getProcDefKernelId(),
                procInstKey);
        result.setProcDefKey(procDefInfoEntity.getProcDefKey());

        postHandleGraphNodes(requestDto, result);
        return result;
    }

    private void tryStoreEnrichedProcInstInfoEntity(ProcInstInfoEntity procInstInfoEntity) {
        if (procInstInfoEntity == null) {
            return;
        }

        procInstInfoMapper.insert(procInstInfoEntity);

        ProcExecBindingEntity procInstBindEntity = procInstInfoEntity.getProcInstBindEntity();
        if (procInstBindEntity != null) {
            procInstBindEntity.setProcInstId(procInstInfoEntity.getId());
            procExecBindingMapper.insert(procInstBindEntity);
        }

        List<TaskNodeInstInfoEntity> nodeInstInfos = procInstInfoEntity.getNodeInstInfos();
        if (nodeInstInfos == null || nodeInstInfos.isEmpty()) {
            return;
        }

        for (TaskNodeInstInfoEntity taskNodeInstInfoEntity : nodeInstInfos) {
            taskNodeInstInfoEntity.setProcInstId(procInstInfoEntity.getId());
            taskNodeInstInfoMapper.insert(taskNodeInstInfoEntity);

            tryStoreProcExecBindingEntities(taskNodeInstInfoEntity.getNodeBindEntities(), taskNodeInstInfoEntity,
                    procInstInfoEntity);
        }

    }

    private void tryStoreProcExecBindingEntities(List<ProcExecBindingEntity> nodeBindEntities,
            TaskNodeInstInfoEntity taskNodeInstInfoEntity, ProcInstInfoEntity procInstInfoEntity) {
        if (nodeBindEntities == null || nodeBindEntities.isEmpty()) {
            return;
        }

        for (ProcExecBindingEntity nodeBind : nodeBindEntities) {
            nodeBind.setTaskNodeInstId(taskNodeInstInfoEntity.getId());
            nodeBind.setProcInstId(procInstInfoEntity.getId());
            procExecBindingMapper.insert(nodeBind);
        }
    }

    private void tryVerifyExcludeModeBindings(ProcDefInfoEntity procDefInfoEntity,
            ProcInstInfoEntity procInstInfoEntity) {
        String excludeMode = procDefInfoEntity.getExcludeMode();
        if (ProcDefInfoEntity.EXCLUDE_MODE_YES.equalsIgnoreCase(excludeMode)) {
            tryVerifyIfAnyRunningProcInstBound(procDefInfoEntity, procInstInfoEntity);
        } else {
            tryVerifyIfAnyExclusiveRunningProcInstBound(procDefInfoEntity, procInstInfoEntity);
        }
    }

    private void tryVerifyIfAnyExclusiveRunningProcInstBound(ProcDefInfoEntity procDefInfoEntity,
            ProcInstInfoEntity procInstInfoEntity) {
        Set<WfBindEntityDataInfo> selfBoundEntityDataInfos = collectAllBoundEntityDataInfos(procInstInfoEntity);
        if (selfBoundEntityDataInfos == null || selfBoundEntityDataInfos.isEmpty()) {
            return;
        }

        Set<Integer> boundExclusiveProcInstIds = new HashSet<>();

        for (WfBindEntityDataInfo bindEntityDataInfo : selfBoundEntityDataInfos) {
            if (StringUtils.isBlank(bindEntityDataInfo.getEntityDataId())) {
                continue;
            }

            int exclusiveProcInstCount = procExecBindingMapper
                    .countAllExclusiveBoundRunningProcInstances(bindEntityDataInfo.getEntityDataId());

            if (exclusiveProcInstCount <= 0) {
                continue;
            }

            List<Integer> boundProcInstIdsOfSingleEntity = procExecBindingMapper
                    .selectAllExclusiveBoundRunningProcInstances(bindEntityDataInfo.getEntityDataId());
            if (boundProcInstIdsOfSingleEntity == null || boundProcInstIdsOfSingleEntity.isEmpty()) {
                continue;
            }

            boundExclusiveProcInstIds.addAll(boundProcInstIdsOfSingleEntity);
        }

        if (boundExclusiveProcInstIds.isEmpty()) {
            return;
        }

        String runningMsg = assembleExclusiveExceptionMsg(boundExclusiveProcInstIds);

        String errMsg = String.format(
                "Data binding conflicts exist.Current process is shared mode,but the following exclusive processes are still running: %s",
                runningMsg);
        throw new WecubeCoreException(errMsg);

    }

    private void tryVerifyIfAnyRunningProcInstBound(ProcDefInfoEntity procDefInfoEntity,
            ProcInstInfoEntity procInstInfoEntity) {
        Set<WfBindEntityDataInfo> selfBoundEntityDataInfos = collectAllBoundEntityDataInfos(procInstInfoEntity);
        if (selfBoundEntityDataInfos == null || selfBoundEntityDataInfos.isEmpty()) {
            return;
        }

        Set<Integer> boundProcInstIds = new HashSet<>();

        for (WfBindEntityDataInfo bindEntityDataInfo : selfBoundEntityDataInfos) {
            if (StringUtils.isBlank(bindEntityDataInfo.getEntityDataId())) {
                continue;
            }
            int boundCount = procExecBindingMapper
                    .countAllBoundRunningProcInstances(bindEntityDataInfo.getEntityDataId());
            if (boundCount <= 0) {
                continue;
            }

            List<Integer> boundProcInstIdsOfSingleEntity = procExecBindingMapper
                    .selectAllBoundRunningProcInstances(bindEntityDataInfo.getEntityDataId());
            if (boundProcInstIdsOfSingleEntity == null || boundProcInstIdsOfSingleEntity.isEmpty()) {
                continue;
            }

            boundProcInstIds.addAll(boundProcInstIdsOfSingleEntity);
        }

        if (boundProcInstIds.isEmpty()) {
            return;
        }

        String runningMsg = assembleExclusiveExceptionMsg(boundProcInstIds);

        String errMsg = String.format(
                "Data binding conflicts exist.Current process is exclusive, but the following processes are still in progress: %s",
                runningMsg);
        throw new WecubeCoreException(errMsg);
    }

    private String assembleExclusiveExceptionMsg(Set<Integer> boundExclusiveProcInstIds) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        int size = 0;
        for (Integer id : boundExclusiveProcInstIds) {
            if (size >= 10) {
                break;
            }

            ProcInstInfoQueryEntity entity = procInstInfoMapper.selectQueryEntityByPrimaryKey(id);
            if (entity == null) {
                continue;
            }

            if (size != 0) {
                sb.append(",");
            }
            String entityDataName = entity.getEntityDataName();
            if (StringUtils.isBlank(entityDataName)) {
                entityDataName = entity.getEntityDataId();
            }
            sb.append(entity.getProcDefName()).append(":").append(entityDataName);
            sb.append(":").append(entity.getOperator()).append(":").append(formatDate(entity.getCreatedTime()));

            size++;
        }

        sb.append("]");

        return sb.toString();
    }

    private Set<WfBindEntityDataInfo> collectAllBoundEntityDataInfos(ProcInstInfoEntity procInstInfoEntity) {
        Set<WfBindEntityDataInfo> entityDataInfos = new HashSet<>();
        List<TaskNodeInstInfoEntity> nodeInstInfos = procInstInfoEntity.getNodeInstInfos();
        if (nodeInstInfos == null || nodeInstInfos.isEmpty()) {
            return entityDataInfos;
        }

        for (TaskNodeInstInfoEntity nodeInstInfo : nodeInstInfos) {
            List<ProcExecBindingEntity> nodeBindEntities = nodeInstInfo.getNodeBindEntities();
            if (nodeBindEntities == null || nodeBindEntities.isEmpty()) {
                continue;
            }

            for (ProcExecBindingEntity nodeBindEntity : nodeBindEntities) {
                WfBindEntityDataInfo bindDataInfo = new WfBindEntityDataInfo(nodeBindEntity.getEntityTypeId(),
                        nodeBindEntity.getEntityDataId());
                entityDataInfos.add(bindDataInfo);
            }
        }

        return entityDataInfos;
    }

    private void tryBuildTaskNodeInstances(ProcDefInfoEntity procDefInfoEntity, ProcInstInfoEntity procInstInfoEntity,
            StartProcInstRequestDto requestDto) {
        List<TaskNodeDefInfoEntity> taskNodeDefInfoEntities = taskNodeDefInfoMapper
                .selectAllByProcDefId(procDefInfoEntity.getId());

        if (taskNodeDefInfoEntities == null || taskNodeDefInfoEntities.isEmpty()) {
            log.info("There is not task node definitions found for process definition {}", procDefInfoEntity.getId());
            return;
        }

        for (TaskNodeDefInfoEntity taskNodeDefInfoEntity : taskNodeDefInfoEntities) {
            processSingleTaskNodeDefInfoEntityWhenCreate(taskNodeDefInfoEntity, procInstInfoEntity, requestDto,
                    procDefInfoEntity.getId());
        }
    }

    private ProcExecBindingEntity tryBuildProcInstProcExecBinding(String rootEntityTypeId, String rootEntityDataId,
            String rootEntityDataName, ProcDefInfoEntity procDefInfoEntity, ProcInstInfoEntity procInstInfoEntity) {
        ProcExecBindingEntity procInstBindEntity = new ProcExecBindingEntity();
        procInstBindEntity.setBindType(ProcExecBindingEntity.BIND_TYPE_PROC_INSTANCE);

        String butifiedRootEntityTypeId = butifyEntityTypeId(rootEntityTypeId);
        procInstBindEntity.setEntityTypeId(butifiedRootEntityTypeId);
        procInstBindEntity.setEntityDataId(rootEntityDataId);
        procInstBindEntity.setEntityDataName(rootEntityDataName);
        procInstBindEntity.setProcDefId(procDefInfoEntity.getId());
        // procInstBindEntity.setProcInstId(procInstInfoEntity.getId());
        procInstBindEntity.setCreatedBy(AuthenticationContextHolder.getCurrentUsername());
        procInstBindEntity.setCreatedTime(new Date());

        procInstInfoEntity.setProcInstBindEntity(procInstBindEntity);
        // procExecBindingRepository.insert(procInstBindEntity);

        return procInstBindEntity;
    }

    private String butifyEntityTypeId(String entityTypeId) {
        if (StringUtils.isBlank(entityTypeId)) {
            return null;
        }

        int idx = entityTypeId.indexOf("{");
        if (idx <= 0) {
            return entityTypeId;
        }

        return entityTypeId.substring(0, idx);
    }

    private ProcInstInfoEntity tryBuildProcInstInfoEntity(ProcDefInfoEntity procDefInfoEntity, String procInstKey) {
        ProcInstInfoEntity procInstInfoEntity = new ProcInstInfoEntity();
        procInstInfoEntity.setStatus(ProcInstInfoEntity.NOT_STARTED_STATUS);
        procInstInfoEntity.setOper(AuthenticationContextHolder.getCurrentUsername());
        procInstInfoEntity.setProcDefId(procDefInfoEntity.getId());
        procInstInfoEntity.setProcDefKey(procDefInfoEntity.getProcDefKey());
        procInstInfoEntity.setProcDefName(procDefInfoEntity.getProcDefName());
        procInstInfoEntity.setProcInstKey(procInstKey);
        procInstInfoEntity.setCreatedBy(AuthenticationContextHolder.getCurrentUsername());
        procInstInfoEntity.setCreatedTime(new Date());
        procInstInfoEntity.setRev(0);

        // procInstInfoRepository.insert(procInstInfoEntity);

        return procInstInfoEntity;
    }

    private void processSingleTaskNodeDefInfoEntityWhenCreate(TaskNodeDefInfoEntity taskNodeDefInfoEntity,
            ProcInstInfoEntity procInstInfoEntity, StartProcInstRequestDto requestDto, String procDefId) {
        TaskNodeInstInfoEntity taskNodeInstInfoEntity = tryBuildTaskNodeInstInfoEntity(taskNodeDefInfoEntity,
                procInstInfoEntity);

        procInstInfoEntity.addNodeInstInfo(taskNodeInstInfoEntity);

        List<TaskNodeDefObjectBindInfoDto> bindInfoDtos = pickUpTaskNodeDefObjectBindInfoDtos(requestDto,
                taskNodeDefInfoEntity.getId());

        List<TaskNodeDefObjectBindInfoDto> savedBindInfoDtos = new ArrayList<>();

        for (TaskNodeDefObjectBindInfoDto bindInfoDto : bindInfoDtos) {
            if (containsBindInfos(savedBindInfoDtos, bindInfoDto)) {
                continue;
            }
            ProcExecBindingEntity nodeBindEntity = new ProcExecBindingEntity();
            nodeBindEntity.setBindType(ProcExecBindingEntity.BIND_TYPE_TASK_NODE_INSTANCE);
            nodeBindEntity.setBindFlag(ProcExecBindingEntity.BIND_FLAG_YES);
            nodeBindEntity.setProcInstId(procInstInfoEntity.getId());
            nodeBindEntity.setProcDefId(procDefId);
            nodeBindEntity.setNodeDefId(bindInfoDto.getNodeDefId());
            nodeBindEntity.setTaskNodeInstId(taskNodeInstInfoEntity.getId());
            nodeBindEntity.setEntityTypeId(bindInfoDto.getEntityTypeId());
            nodeBindEntity.setEntityDataId(bindInfoDto.getEntityDataId());
            nodeBindEntity.setFullEntityDataId(bindInfoDto.getFullEntityDataId());
            nodeBindEntity.setEntityDataName(bindInfoDto.getEntityDisplayName());
            nodeBindEntity.setCreatedBy(AuthenticationContextHolder.getCurrentUsername());
            nodeBindEntity.setCreatedTime(new Date());

            // procExecBindingRepository.insert(nodeBindEntity);
            taskNodeInstInfoEntity.addNodeBindEntity(nodeBindEntity);

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

    private TaskNodeInstInfoEntity tryBuildTaskNodeInstInfoEntity(TaskNodeDefInfoEntity taskNodeDefInfoEntity,
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

        // taskNodeInstInfoRepository.insert(taskNodeInstInfoEntity);

        return taskNodeInstInfoEntity;
    }

    private String tryCalEntityDataName(StartProcInstRequestDto requestDto) {
        String entityDataName = null;
        if (!StringUtils.isBlank(requestDto.getProcessSessionId())) {
            List<ProcExecBindingTmpEntity> tmpBindings = procExecBindingTmpMapper
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

        List<GraphNodeEntity> gNodes = graphNodeMapper.selectAllByProcessSessionId(requestDto.getProcessSessionId());
        if (gNodes == null || gNodes.isEmpty()) {
            return;
        }

        for (GraphNodeEntity gNode : gNodes) {
            gNode.setUpdatedTime(new Date());
            gNode.setUpdatedBy(AuthenticationContextHolder.getCurrentUsername());
            gNode.setProcInstId(result.getId());

            graphNodeMapper.updateByPrimaryKeySelective(gNode);
        }
    }

    protected ProcInstInfoDto doCreateProcessInstance(ProcInstInfoEntity procInstInfoEntity, String processDefinitionId,
            String procInstKey) {
        ProcessInstance processInstance = workflowEngineService.startProcessInstance(processDefinitionId, procInstKey);

        ProcInstInfoEntity procEntity = procInstInfoMapper.selectByPrimaryKey(procInstInfoEntity.getId());

        if (procEntity == null) {
            log.warn("such record does not exist,id={},procInstKey={}", procInstInfoEntity.getId(), procInstKey);
            throw new WecubeCoreException("3152", "Errors while starting process instance.");
        }

        Date currTime = new Date();

        procEntity.setUpdatedTime(currTime);
        procEntity.setUpdatedBy(AuthenticationContextHolder.getCurrentUsername());
        procEntity.setProcInstKernelId(processInstance.getId());
        procEntity.setStatus(ProcInstInfoEntity.IN_PROGRESS_STATUS);

        procInstInfoMapper.updateByPrimaryKeySelective(procEntity);

        String entityTypeId = null;
        String entityDataId = null;

        ProcExecBindingEntity procInstBindEntity = procExecBindingMapper
                .selectProcInstBindings(procInstInfoEntity.getId());

        if (procInstBindEntity != null) {
            entityTypeId = procInstBindEntity.getEntityTypeId();
            entityDataId = procInstBindEntity.getEntityDataId();
        }

        ProcInstInfoDto procInstInfoResultDto = new ProcInstInfoDto();
        procInstInfoResultDto.setId(procEntity.getId());
        procInstInfoResultDto.setOperator(procEntity.getOper());
        procInstInfoResultDto.setProcDefId(procEntity.getProcDefId());
        procInstInfoResultDto.setProcInstKey(procEntity.getProcDefKey());
        procInstInfoResultDto.setStatus(procEntity.getStatus());
        procInstInfoResultDto.setEntityTypeId(entityTypeId);
        procInstInfoResultDto.setEntityDataId(entityDataId);

        List<TaskNodeInstInfoEntity> nodeInstEntities = taskNodeInstInfoMapper
                .selectAllByProcInstId(procEntity.getId());

        for (TaskNodeInstInfoEntity n : nodeInstEntities) {
            if (NODE_START_EVENT.equals(n.getNodeType())) {
                n.setUpdatedBy(AuthenticationContextHolder.getCurrentUsername());
                n.setUpdatedTime(currTime);
                n.setStatus(TaskNodeInstInfoEntity.COMPLETED_STATUS);
                taskNodeInstInfoMapper.updateByPrimaryKeySelective(n);
            }
        }

        List<TaskNodeDefInfoEntity> nodeDefEntities = taskNodeDefInfoMapper
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

            procInstInfoResultDto.addTaskNodeInstances(nd);
        }

        return procInstInfoResultDto;
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

        List<ProcExecBindingTmpEntity> sessionBindings = this.procExecBindingTmpMapper
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
                dto.setEntityDisplayName(entity.getEntityDataName());
                dto.setNodeDefId(entity.getNodeDefId());
                dto.setOrderedNo(entity.getOrderedNo());
                //#2169
                dto.setFullEntityDataId(entity.getFullEntityDataId());

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
        List<ProcInstInfoQueryEntity> insts = procInstInfoMapper.selectAllByProcInstInfoByRoleNames(roleNames);
        return insts;
    }

    protected void refreshProcessInstanceStatus(ProcInstInfoEntity procInstEntity) {
        if (ProcInstInfoEntity.INTERNALLY_TERMINATED_STATUS.equalsIgnoreCase(procInstEntity.getStatus())) {
            return;
        }

        List<TaskNodeInstInfoEntity> nodeInstEntities = taskNodeInstInfoMapper
                .selectAllByProcInstId(procInstEntity.getId());
        String kernelProcInstId = procInstEntity.getProcInstKernelId();
        Date currTime = new Date();
        for (TaskNodeInstInfoEntity nie : nodeInstEntities) {
            String nodeId = nie.getNodeId();
            String nodeStatus = workflowEngineService.getTaskNodeStatus(kernelProcInstId, nodeId);
            if (StringUtils.isBlank(nodeStatus)) {
                continue;
            }

            if (TaskNodeInstInfoEntity.RISKY_STATUS.equalsIgnoreCase(nie.getStatus())) {
                continue;
            }

            if (!nodeStatus.equals(nie.getStatus())) {
                nie.setStatus(nodeStatus);
                nie.setUpdatedTime(currTime);
                nie.setUpdatedBy(AuthenticationContextHolder.getCurrentUsername());
                taskNodeInstInfoMapper.updateByPrimaryKeySelective(nie);
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

    private TaskNodeDefInfoEntity findTaskNodeDefInfoEntityByNodeDefId(List<TaskNodeDefInfoEntity> nodeDefEntities,
            String nodeDefId) {
        for (TaskNodeDefInfoEntity nodeDef : nodeDefEntities) {
            if (nodeDefId.equals(nodeDef.getId())) {
                return nodeDef;
            }
        }

        return null;
    }

    private void checkCurrentUserRole(String procDefId) {
        // List<String> roleIdList = this.userManagementService
        // .getRoleIdsByUsername(AuthenticationContextHolder.getCurrentUsername());

        Set<String> currRoleNames = AuthenticationContextHolder.getCurrentUserRoles();
        if (currRoleNames == null || currRoleNames.isEmpty()) {
            throw new WecubeCoreException("3144", "No access to this resource due to current user did not log in.");
        }

        List<ProcRoleBindingEntity> procRoleBindingEntities = procRoleBindingMapper
                .selectDistinctProcIdByRolesAndPermissionIsUse(currRoleNames);
        if (procRoleBindingEntities == null || procRoleBindingEntities.isEmpty()) {
            throw new WecubeCoreException("3145", "No access to this resource due to permission not configured.");
        }

        for (ProcRoleBindingEntity e : procRoleBindingEntities) {
            if (procDefId.equals(e.getProcId())) {
                return;
            }
        }

        throw new WecubeCoreException("3146", "No access to this resource due to none permission configuration found.");

    }

}
