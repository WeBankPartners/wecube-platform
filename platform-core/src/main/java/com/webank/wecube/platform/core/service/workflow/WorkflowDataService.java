package com.webank.wecube.platform.core.service.workflow;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webank.wecube.platform.core.commons.AuthenticationContextHolder;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.plugin.PluginConfigInterface;
import com.webank.wecube.platform.core.domain.plugin.PluginConfigInterfaceParameter;
import com.webank.wecube.platform.core.dto.workflow.FlowNodeDefDto;
import com.webank.wecube.platform.core.dto.workflow.GraphNodeDto;
import com.webank.wecube.platform.core.dto.workflow.InterfaceParameterDto;
import com.webank.wecube.platform.core.dto.workflow.ProcDefOutlineDto;
import com.webank.wecube.platform.core.dto.workflow.ProcessDataPreviewDto;
import com.webank.wecube.platform.core.dto.workflow.RequestObjectDto;
import com.webank.wecube.platform.core.dto.workflow.TaskNodeDefObjectBindInfoDto;
import com.webank.wecube.platform.core.dto.workflow.TaskNodeExecContextDto;
import com.webank.wecube.platform.core.entity.workflow.ProcExecBindingTmpEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeDefInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeExecParamEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeExecRequestEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeInstInfoEntity;
import com.webank.wecube.platform.core.jpa.workflow.ProcExecBindingTmpRepository;
import com.webank.wecube.platform.core.jpa.workflow.TaskNodeDefInfoRepository;
import com.webank.wecube.platform.core.jpa.workflow.TaskNodeExecParamRepository;
import com.webank.wecube.platform.core.jpa.workflow.TaskNodeExecRequestRepository;
import com.webank.wecube.platform.core.jpa.workflow.TaskNodeInstInfoRepository;
import com.webank.wecube.platform.core.service.dme.EntityOperationRootCondition;
import com.webank.wecube.platform.core.service.dme.EntityTreeNodesOverview;
import com.webank.wecube.platform.core.service.dme.StandardEntityOperationService;
import com.webank.wecube.platform.core.service.dme.TreeNode;
import com.webank.wecube.platform.core.service.plugin.PluginConfigService;

@Service
public class WorkflowDataService {
	private static final Logger log = LoggerFactory.getLogger(WorkflowDataService.class);

	@Autowired
	private WorkflowProcDefService workflowProcDefService;

	@Autowired
	private TaskNodeDefInfoRepository taskNodeDefInfoRepository;

	@Autowired
	private TaskNodeInstInfoRepository taskNodeInstInfoRepository;

	@Autowired
	private StandardEntityOperationService standardEntityOperationService;

	@Autowired
	private PluginConfigService pluginConfigService;

	@Autowired
	protected TaskNodeExecParamRepository taskNodeExecParamRepository;

	@Autowired
	protected TaskNodeExecRequestRepository taskNodeExecRequestRepository;

	@Autowired
	protected ProcExecBindingTmpRepository procExecBindingTmpRepository;

	public void updateProcessInstanceExecBindingsOfSession(String nodeDefId, String processSessionId,
			List<TaskNodeDefObjectBindInfoDto> bindings) {
		if (bindings == null || bindings.isEmpty()) {
			return;
		}

		List<ProcExecBindingTmpEntity> bindingEntities = procExecBindingTmpRepository.getAllByNodeAndSession(nodeDefId,
				processSessionId);

		if (bindingEntities == null || bindingEntities.isEmpty()) {
			return;
		}

		for (TaskNodeDefObjectBindInfoDto dto : bindings) {
			ProcExecBindingTmpEntity existEntity = findProcExecBindingTmpEntityWithNodeAndEntity(dto.getNodeDefId(),
					dto.getEntityTypeId(), dto.getEntityDataId(), bindingEntities);
			if (existEntity == null) {
				log.warn("cannot find such binds for {} {} {}", dto.getNodeDefId(), dto.getEntityTypeId(),
						dto.getEntityDataId());
				continue;
			}

			existEntity.setBound(dto.getBound());
			existEntity.setUpdatedBy(AuthenticationContextHolder.getCurrentUsername());
			existEntity.setUpdatedTime(new Date());

			procExecBindingTmpRepository.save(existEntity);
		}
	}

	private ProcExecBindingTmpEntity findProcExecBindingTmpEntityWithNodeAndEntity(String nodeDefId,
			String entityTypeId, String entityDataId, List<ProcExecBindingTmpEntity> bindingEntities) {
		for (ProcExecBindingTmpEntity entity : bindingEntities) {
			if (nodeDefId.equals(entity.getNodeDefId()) && entityTypeId.equals(entity.getEntityTypeId())
					&& entityDataId.equals(entity.getEntityDataId())) {
				return entity;
			}
		}

		return null;
	}

	public List<TaskNodeDefObjectBindInfoDto> getProcessInstanceExecBindingsOfSession(String processSessionId) {
		List<ProcExecBindingTmpEntity> bindingEntities = procExecBindingTmpRepository.getAllBySession(processSessionId);

		List<TaskNodeDefObjectBindInfoDto> result = new ArrayList<>();
		if (bindingEntities == null) {
			return result;
		}

		bindingEntities.forEach(entity -> {
			TaskNodeDefObjectBindInfoDto dto = new TaskNodeDefObjectBindInfoDto();
			dto.setBound(entity.getBound());
			dto.setEntityDataId(entity.getEntityDataId());
			dto.setEntityTypeId(entity.getEntityTypeId());
			dto.setNodeDefId(entity.getNodeDefId());
			dto.setOrderedNo(entity.getOrderedNo());

			result.add(dto);
		});

		return result;
	}

	public List<TaskNodeDefObjectBindInfoDto> getProcessInstanceExecBindingsOfSessionAndNode(String nodeDefId,
			String processSessionId) {
		List<ProcExecBindingTmpEntity> bindingEntities = procExecBindingTmpRepository.getAllByNodeAndSession(nodeDefId,
				processSessionId);

		List<TaskNodeDefObjectBindInfoDto> result = new ArrayList<>();
		if (bindingEntities == null) {
			return result;
		}

		bindingEntities.forEach(entity -> {
			TaskNodeDefObjectBindInfoDto dto = new TaskNodeDefObjectBindInfoDto();
			dto.setBound(entity.getBound());
			dto.setEntityDataId(entity.getEntityDataId());
			dto.setEntityTypeId(entity.getEntityTypeId());
			dto.setNodeDefId(entity.getNodeDefId());
			dto.setOrderedNo(entity.getOrderedNo());

			result.add(dto);
		});

		return result;
	}

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
		if (StringUtils.isNotBlank(nodeEntity.getErrorMessage())) {
			result.setErrorMessage(nodeEntity.getErrorMessage());
		}

		TaskNodeExecRequestEntity requestEntity = taskNodeExecRequestRepository
				.findCurrentEntityByNodeInstId(nodeEntity.getId());

		if (requestEntity == null) {
			return result;
		}

		result.setRequestId(requestEntity.getRequestId());
		result.setErrorCode(requestEntity.getErrorCode());
		if (StringUtils.isNotBlank(result.getErrorMessage())) {
			result.setErrorMessage(result.getErrorMessage() + "|" + requestEntity.getErrorMessage());
		} else {
			result.setErrorMessage(requestEntity.getErrorMessage());
		}

		List<TaskNodeExecParamEntity> requestParamEntities = taskNodeExecParamRepository.findAllByRequestIdAndParamType(
				requestEntity.getRequestId(), TaskNodeExecParamEntity.PARAM_TYPE_REQUEST);

		List<TaskNodeExecParamEntity> responseParamEntities = taskNodeExecParamRepository
				.findAllByRequestIdAndParamType(requestEntity.getRequestId(),
						TaskNodeExecParamEntity.PARAM_TYPE_RESPONSE);

		List<RequestObjectDto> requestObjects = calculateRequestObjectDtos(requestParamEntities, responseParamEntities);

		requestObjects.forEach(result::addRequestObjects);

		return result;
	}

	public List<InterfaceParameterDto> getTaskNodeParameters(String procDefId, String nodeDefId) {
		List<InterfaceParameterDto> result = new ArrayList<>();
		Optional<TaskNodeDefInfoEntity> entityOptional = taskNodeDefInfoRepository.findById(nodeDefId);
		if (!entityOptional.isPresent()) {
			return result;
		}

		TaskNodeDefInfoEntity e = entityOptional.get();
		String serviceId = e.getServiceId();

		if (StringUtils.isBlank(serviceId)) {
			log.debug("service id is present for {}", nodeDefId);
			return result;
		}

		PluginConfigInterface pci = pluginConfigService.getPluginConfigInterfaceByServiceName(serviceId);
		Set<PluginConfigInterfaceParameter> inputParameters = pci.getInputParameters();
		Set<PluginConfigInterfaceParameter> outputParameters = pci.getOutputParameters();

		inputParameters.forEach(p -> {
			result.add(buildInterfaceParameterDto(p));
		});

		outputParameters.forEach(p -> {
			result.add(buildInterfaceParameterDto(p));
		});

		return result;
	}

	@Transactional
	public ProcessDataPreviewDto generateProcessDataPreview(String procDefId, String dataId) {
		if (StringUtils.isBlank(procDefId) || StringUtils.isBlank(dataId)) {
			throw new WecubeCoreException("Process definition ID or entity ID is not provided.");
		}

		ProcDefOutlineDto procDefOutline = workflowProcDefService.getProcessDefinitionOutline(procDefId);

		if (procDefOutline == null) {
			log.debug("process definition with id {} does not exist.", procDefId);
			throw new WecubeCoreException(String.format("Such process definition {%s} does not exist.", procDefId));
		}

		return doFetchProcessPreviewData(procDefOutline, dataId);

	}

	protected ProcessDataPreviewDto doFetchProcessPreviewData(ProcDefOutlineDto outline, String dataId) {
		ProcessDataPreviewDto result = new ProcessDataPreviewDto();

		List<GraphNodeDto> hierarchicalEntityNodes = new ArrayList<>();
		String processSessionId = UUID.randomUUID().toString();
		ProcExecBindingTmpEntity procInstBindingTmpEntity = new ProcExecBindingTmpEntity();
		procInstBindingTmpEntity.setBindType(ProcExecBindingTmpEntity.BIND_TYPE_PROC_INSTANCE);
		procInstBindingTmpEntity.setBound(ProcExecBindingTmpEntity.BOUND);
		procInstBindingTmpEntity.setProcSessionId(processSessionId);
		procInstBindingTmpEntity.setProcDefId(outline.getProcDefId());
		procInstBindingTmpEntity.setEntityDataId(dataId);
		procInstBindingTmpEntity.setEntityTypeId(outline.getRootEntity());
		procInstBindingTmpEntity.setCreatedBy(AuthenticationContextHolder.getCurrentUsername());

		procExecBindingTmpRepository.save(procInstBindingTmpEntity);

		for (FlowNodeDefDto f : outline.getFlowNodes()) {
			String nodeType = f.getNodeType();

			if (!"subProcess".equals(nodeType)) {
				continue;
			}

			String routineExpr = calculateDataModelExpression(f);

			if (StringUtils.isBlank(routineExpr)) {
				log.info("the routine expression is blank for {} {}", f.getNodeDefId(), f.getNodeName());
				continue;
			}

			log.info("About to fetch data for node {} {}", f.getNodeDefId(), f.getNodeName());

			log.info("About to fetch data with expression {} and data id {}", routineExpr, dataId);
			EntityOperationRootCondition condition = new EntityOperationRootCondition(routineExpr, dataId);
			List<TreeNode> nodes = null;
			try {
				EntityTreeNodesOverview overview = standardEntityOperationService.generateEntityLinkOverview(condition);
				nodes = overview.getHierarchicalEntityNodes();

				handleLeafNodeEntityNodes(f, overview.getLeafNodeEntityNodes(), processSessionId);
			} catch (Exception e) {
				log.error("errors while fetching data with expr {} and data id {}", routineExpr, dataId, e);
				throw new WecubeCoreException(e.getMessage());
			}

			if (nodes == null || nodes.isEmpty()) {
				log.warn("None data returned for {} and {}", routineExpr, dataId);
				continue;
			}

			log.info("total {} records returned for {} and {}", nodes.size(), routineExpr, dataId);

			for (TreeNode tn : nodes) {
				String treeNodeId = buildId(tn);
				GraphNodeDto currNode = findGraphNodeDtoById(hierarchicalEntityNodes, treeNodeId);
				if (currNode == null) {
					currNode = new GraphNodeDto();
					currNode.setDataId(tn.getRootId().toString());
					currNode.setPackageName(tn.getPackageName());
					currNode.setEntityName(tn.getEntityName());
					currNode.setDisplayName(tn.getDisplayName() == null ? null : tn.getDisplayName().toString());

					addToResult(hierarchicalEntityNodes, currNode);
				}

				TreeNode parentTreeNode = tn.getParent();
				if (parentTreeNode != null) {
					String parentTreeNodeId = buildId(parentTreeNode);
					currNode.addPreviousIds(parentTreeNodeId);
				}

				List<TreeNode> childrenTreeNodes = tn.getChildren();
				if (childrenTreeNodes != null) {
					for (TreeNode ctn : childrenTreeNodes) {
						String ctnId = buildId(ctn);
						currNode.addSucceedingIds(ctnId);
					}
				}
			}
		}

		result.addAllEntityTreeNodes(hierarchicalEntityNodes);
		result.setProcessSessionId(processSessionId);

		return result;

	}

	private void handleLeafNodeEntityNodes(FlowNodeDefDto f, List<TreeNode> leafNodeEntityNodes,
			String processSessionId) {
		if (leafNodeEntityNodes == null) {
			return;
		}

		if (log.isInfoEnabled()) {
			log.info("total {} nodes returned as default bindings for {} {} {}", leafNodeEntityNodes.size(),
					f.getNodeDefId(), f.getNodeId(), f.getNodeName());
		}

		for (TreeNode tn : leafNodeEntityNodes) {
			ProcExecBindingTmpEntity taskNodeBinding = new ProcExecBindingTmpEntity();
			taskNodeBinding.setBindType(ProcExecBindingTmpEntity.BIND_TYPE_TASK_NODE_INSTANCE);
			taskNodeBinding.setBound(ProcExecBindingTmpEntity.BOUND);
			taskNodeBinding.setProcSessionId(processSessionId);
			taskNodeBinding.setProcDefId(f.getProcDefId());
			taskNodeBinding.setEntityDataId(String.valueOf(tn.getRootId()));
			taskNodeBinding.setEntityTypeId(String.format("%s:%s", tn.getPackageName(), tn.getEntityName()));
			taskNodeBinding.setNodeDefId(f.getNodeDefId());
			taskNodeBinding.setOrderedNo(f.getOrderedNo());
			taskNodeBinding.setCreatedBy(AuthenticationContextHolder.getCurrentUsername());

			procExecBindingTmpRepository.save(taskNodeBinding);
		}

		return;

	}

	private String calculateDataModelExpression(FlowNodeDefDto f) {
		if (StringUtils.isBlank(f.getRoutineExpression())) {
			return null;
		}

		String expr = f.getRoutineExpression();

		if (StringUtils.isBlank(f.getServiceId())) {
			return expr;
		}

		PluginConfigInterface inter = pluginConfigService.getPluginConfigInterfaceByServiceName(f.getServiceId());
		if (inter == null) {
			return expr;
		}

		if (StringUtils.isBlank(inter.getFilterRule())) {
			return expr;
		}

		return expr + inter.getFilterRule();
	}

	private void addToResult(List<GraphNodeDto> result, GraphNodeDto... nodes) {
		for (GraphNodeDto n : nodes) {
			if (result.contains(n)) {
				continue;
			}

			GraphNodeDto exist = findGraphNodeDtoById(result, n.getId());
			if (exist == null) {
				result.add(n);
			}
		}
	}

	private GraphNodeDto findGraphNodeDtoById(List<GraphNodeDto> result, String id) {
		for (GraphNodeDto n : result) {
			if (n.getId().equals(id)) {
				return n;
			}
		}

		return null;
	}

	private String buildId(TreeNode n) {
		return String.format("%s:%s:%s", n.getPackageName(), n.getEntityName(), n.getRootId());
	}

	private boolean isSensitiveData(TaskNodeExecParamEntity respParamEntity) {
		if (respParamEntity == null) {
			return false;
		}

		if (respParamEntity.getSensitive() == null) {
			return false;
		}

		if (Boolean.TRUE.equals(respParamEntity.getSensitive())) {
			return true;

		}

		return false;

	}

	private Map<String, Map<String, String>> calculateRespParamsByObjectId(
			List<TaskNodeExecParamEntity> requestParamEntities, List<TaskNodeExecParamEntity> responseParamEntities) {
		Map<String, Map<String, String>> respParamsByObjectId = new HashMap<String, Map<String, String>>();
		if (responseParamEntities != null) {
			for (TaskNodeExecParamEntity respParamEntity : responseParamEntities) {
				Map<String, String> respParamsMap = respParamsByObjectId.get(respParamEntity.getObjectId());
				if (respParamsMap == null) {
					respParamsMap = new HashMap<String, String>();
					respParamsByObjectId.put(respParamEntity.getObjectId(), respParamsMap);
				}
				if (isSensitiveData(respParamEntity)) {
					respParamsMap.put(respParamEntity.getParamName(), "***MASK***");
				} else {
					respParamsMap.put(respParamEntity.getParamName(), respParamEntity.getParamDataValue());
				}

			}
		}

		return respParamsByObjectId;
	}

	private Map<String, RequestObjectDto> calculateRequestObjects(List<TaskNodeExecParamEntity> requestParamEntities,
			List<TaskNodeExecParamEntity> responseParamEntities) {
		Map<String, RequestObjectDto> objs = new HashMap<>();
		for (TaskNodeExecParamEntity rp : requestParamEntities) {
			RequestObjectDto ro = objs.get(rp.getObjectId());
			if (ro == null) {
				ro = new RequestObjectDto();
				objs.put(rp.getObjectId(), ro);
			}

			if (isSensitiveData(rp)) {
				ro.addInput(rp.getParamName(), "***MASK***");
			} else {
				ro.addInput(rp.getParamName(), rp.getParamDataValue());
			}
		}

		return objs;
	}

	private List<RequestObjectDto> calculateRequestObjectDtos(List<TaskNodeExecParamEntity> requestParamEntities,
			List<TaskNodeExecParamEntity> responseParamEntities) {
		List<RequestObjectDto> requestObjects = new ArrayList<>();

		if (requestParamEntities == null) {
			return requestObjects;
		}

		Map<String, Map<String, String>> respParamsByObjectId = calculateRespParamsByObjectId(requestParamEntities,
				responseParamEntities);

		Map<String, RequestObjectDto> objs = calculateRequestObjects(requestParamEntities, responseParamEntities);

		for (String objectId : objs.keySet()) {
			RequestObjectDto obj = objs.get(objectId);
			Map<String, String> respParamsMap = respParamsByObjectId.get(objectId);
			if (respParamsMap != null) {
				respParamsMap.forEach((k, v) -> {
					obj.addOutput(k, v);
				});
			}

			requestObjects.add(obj);
		}

		return requestObjects;
	}

	private InterfaceParameterDto buildInterfaceParameterDto(PluginConfigInterfaceParameter p) {
		InterfaceParameterDto d = new InterfaceParameterDto();
		d.setType(p.getType());
		d.setName(p.getName());
		d.setDataType(p.getDataType());

		return d;
	}

}
