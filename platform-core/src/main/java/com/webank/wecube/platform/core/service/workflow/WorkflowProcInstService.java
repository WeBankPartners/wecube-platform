package com.webank.wecube.platform.core.service.workflow;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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
import com.webank.wecube.platform.core.entity.workflow.TaskNodeDefInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeInstInfoEntity;
import com.webank.wecube.platform.core.jpa.workflow.GraphNodeRepository;
import com.webank.wecube.platform.core.jpa.workflow.ProcDefInfoRepository;
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

	@Autowired
	private UserManagementServiceImpl userManagementService;

	@Autowired
	private ProcRoleBindingRepository procRoleBindingRepository;

	@Autowired
	private ProcExecBindingTmpRepository procExecBindingTmpRepository;

	@Autowired
	protected GraphNodeRepository graphNodeRepository;

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
		List<String> roleIdList = this.userManagementService
				.getRoleIdsByUsername(AuthenticationContextHolder.getCurrentUsername());
		if (roleIdList.size() == 0) {
			return result;
		}

		List<String> procDefIds = procRoleBindingRepository.findDistinctProcIdByRoleIdsAndPermissionIsUse(roleIdList);
		if (procDefIds.size() == 0) {
			return result;
		}

		List<ProcInstInfoEntity> allProcInstEntities = new ArrayList<>();
		for (String procDefId : procDefIds) {
			List<ProcInstInfoEntity> procInstEntities = procInstInfoRepository.findAllByProcDefId(procDefId);
			if (procInstEntities != null) {
				allProcInstEntities.addAll(procInstEntities);
			}
		}

		if (allProcInstEntities.isEmpty()) {
			return result;
		}

		for (ProcInstInfoEntity e : allProcInstEntities) {
			ProcInstInfoDto d = new ProcInstInfoDto();
			d.setCreatedTime(formatDate(e.getCreatedTime()));
			d.setId(e.getId());
			d.setOperator(e.getOperator());
			d.setProcDefId(e.getProcDefId());
			d.setProcInstKey(e.getProcInstKey());
			d.setStatus(e.getStatus());
			d.setProcInstName(e.getProcDefName());
			d.setProcInstKey(e.getProcInstKey());

			ProcExecBindingEntity rootBindingEntity = procExecBindingRepository.findProcInstBindings(e.getId());
			if (rootBindingEntity != null) {
				d.setEntityDataId(rootBindingEntity.getEntityDataId());
				d.setEntityTypeId(rootBindingEntity.getEntityTypeId());
				d.setEntityDisplayName(
						rootBindingEntity.getEntityDataName() == null ? rootBindingEntity.getEntityDataId()
								: rootBindingEntity.getEntityDataName());
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
	
	private String reduceTaskNodeName(TaskNodeInstInfoEntity nodeInstEntity) {
		if(!StringUtils.isBlank(nodeInstEntity.getNodeName())) {
			return nodeInstEntity.getNodeName();
		}
		
		if("startEvent".equals(nodeInstEntity.getNodeType())) {
			return "S";
		}
		
		if("endEvent".equals(nodeInstEntity.getNodeType())) {
			return "E";
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

	public ProcInstInfoDto createProcessInstance(StartProcInstRequestDto requestDto) {
		if (StringUtils.isBlank(requestDto.getProcDefId())) {
			if (log.isDebugEnabled()) {
				log.debug("Process definition ID is blank.");
			}
			throw new WecubeCoreException("Process definition ID is blank.");
		}

		String rootEntityTypeId = requestDto.getEntityTypeId();
		String rootEntityDataId = requestDto.getEntityDataId();
		String rootEntityDataName = requestDto.getEntityDisplayName();

		if (StringUtils.isBlank(rootEntityDataName)) {
			rootEntityDataName = tryCalEntityDataName(requestDto);
		}

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
		procInstInfoEntity.setOperator(AuthenticationContextHolder.getCurrentUsername());
		procInstInfoEntity.setProcDefId(procDefId);
		procInstInfoEntity.setProcDefKey(procDefInfoEntity.getProcDefKey());
		procInstInfoEntity.setProcDefName(procDefInfoEntity.getProcDefName());
		procInstInfoEntity.setProcInstKey(procInstKey);

		procInstInfoRepository.save(procInstInfoEntity);

		ProcExecBindingEntity procInstBindEntity = new ProcExecBindingEntity();
		procInstBindEntity.setBindType(ProcExecBindingEntity.BIND_TYPE_PROC_INSTANCE);
		procInstBindEntity.setEntityTypeId(rootEntityTypeId);
		procInstBindEntity.setEntityDataId(rootEntityDataId);
		procInstBindEntity.setEntityDataName(rootEntityDataName);
		procInstBindEntity.setProcDefId(procDefId);
		procInstBindEntity.setProcInstId(procInstInfoEntity.getId());
		procExecBindingRepository.save(procInstBindEntity);

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

		for (TaskNodeDefObjectBindInfoDto bindInfoDto : bindInfoDtos) {
			ProcExecBindingEntity nodeBindEntity = new ProcExecBindingEntity();
			nodeBindEntity.setBindType(ProcExecBindingEntity.BIND_TYPE_TASK_NODE_INSTANCE);
			nodeBindEntity.setProcInstId(procInstInfoEntity.getId());
			nodeBindEntity.setProcDefId(procDefId);
			nodeBindEntity.setNodeDefId(bindInfoDto.getNodeDefId());
			nodeBindEntity.setTaskNodeInstId(taskNodeInstInfoEntity.getId());
			nodeBindEntity.setEntityTypeId(bindInfoDto.getEntityTypeId());
			nodeBindEntity.setEntityDataId(bindInfoDto.getEntityDataId());
			nodeBindEntity.setEntityDataName(bindInfoDto.getEntityDisplayName());

			procExecBindingRepository.save(nodeBindEntity);
		}
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

		taskNodeInstInfoRepository.save(taskNodeInstInfoEntity);

		return taskNodeInstInfoEntity;
	}

	private String tryCalEntityDataName(StartProcInstRequestDto requestDto) {
		String entityDataName = null;
		if(!StringUtils.isBlank(requestDto.getProcessSessionId())) {
			List<ProcExecBindingTmpEntity> tmpBindings = procExecBindingTmpRepository.findAllRootBindingsBySession(requestDto.getProcessSessionId());
			if(tmpBindings != null && tmpBindings.size() > 0) {
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

			graphNodeRepository.save(gNode);
		}
	}

	protected ProcInstInfoDto doCreateProcessInstance(ProcInstInfoEntity procInstInfoEntity, String processDefinitionId,
			String procInstKey) {
		ProcessInstance processInstance = workflowEngineService.startProcessInstance(processDefinitionId, procInstKey);

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
				n.setUpdatedBy(AuthenticationContextHolder.getCurrentUsername());
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
}
